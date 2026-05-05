package com.vinithius.dex10.datasource.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vinithius.dex10.datasource.database.PokemonAbility
import com.vinithius.dex10.datasource.database.PokemonDao
import com.vinithius.dex10.datasource.database.PokemonEntity
import com.vinithius.dex10.datasource.database.PokemonStat
import com.vinithius.dex10.datasource.database.PokemonType
import com.vinithius.dex10.datasource.database.PokemonWithDetails
import com.vinithius.dex10.datasource.database.Type as TypeEntity
import com.vinithius.dex10.datasource.mapper.toAbilityEntities
import com.vinithius.dex10.datasource.mapper.toEntity
import com.vinithius.dex10.datasource.mapper.toPokemon
import com.vinithius.dex10.datasource.mapper.toStatEntities
import com.vinithius.dex10.datasource.mapper.toTypeEntities
import com.vinithius.dex10.datasource.response.Characteristic
import com.vinithius.dex10.datasource.response.Damage
import com.vinithius.dex10.datasource.response.EvolutionChain
import com.vinithius.dex10.datasource.response.JikanCharacterData
import com.vinithius.dex10.datasource.response.Location
import com.vinithius.dex10.datasource.response.Pokemon
import com.vinithius.dex10.datasource.response.PokemonDataWrapper
import com.vinithius.dex10.datasource.response.Specie
import com.vinithius.dex10.datasource.response.TcgCard
import com.vinithius.dex10.ui.MainActivity.Companion.FAVORITES
import com.vinithius.dex10.ui.MainActivity.Companion.MAX_POKEMONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.coroutines.resume


class PokemonRepository(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val tcgRemoteDataSource: TcgRemoteDataSource,
    private val jikanRemoteDataSource: JikanRemoteDataSource,
    private val localDataSource: PokemonDao
) : IPokemonRepository {

    override suspend fun getPokemonEntityList(
        context: Context,
        callBackLoadingFirebaseCounter: ((progress: Float) -> Unit),
        callBackLoadingFirebase: (() -> Unit),
        callBackLoadingPostProcess: (() -> Unit),
        callBackLoading: (() -> Unit),
        callBackError: ((e: Exception) -> Unit),
    ): List<PokemonWithDetails>? {
        insertPokemonFromFirebaseToLocal(
            context,
            callBackLoadingFirebase,
            callBackLoadingFirebaseCounter,
            callBackLoadingPostProcess,
            callBackError
        )
        callBackLoading.invoke()
        val pokemonList = localDataSource.getPokemonListWithDetails()
        return pokemonList
    }

    /**
     * Execute once (on first launch or when VALIDATION_VERSION / DATA_VERSION is incremented).
     * When DATA_VERSION increases, forces a full Firebase re-sync + enrichment so that
     * new data fields are populated before the list is shown — retry on next open if it fails.
     */
    private suspend fun insertPokemonFromFirebaseToLocal(
        context: Context,
        callBackLoadingFirebase: (() -> Unit),
        callBackLoadingFirebaseCounter: ((progress: Float) -> Unit),
        callBackLoadingPostProcess: (() -> Unit),
        callBackError: ((e: Exception) -> Unit),
    ) {
        try {
            // DATA_VERSION trigger: reset stored max so the Firebase sync path is forced.
            // Captured up-front so we know whether this is a first install / version bump
            // (and therefore whether the post-process loader should be shown to the user).
            val isFirstRunOrVersionBump = needsDataRefresh(context)
            if (isFirstRunOrVersionBump) {
                setCountMaxPokemon(0, context)
            }

            val countLocal = getCountPokemonEntities()
            var maxPokemonsSize = getCountMaxPokemon(context)
            if (maxPokemonsSize == 0 || countLocal < maxPokemonsSize) {
                callBackLoadingFirebase.invoke()
                val pokemonFirebaseList = getFirebasePokemonList(callBackError)
                maxPokemonsSize = pokemonFirebaseList.size
                setCountMaxPokemon(maxPokemonsSize, context)
                var count = 0
                pokemonFirebaseList.forEach { pokemon ->
                    val isFavorite = getFavorite(pokemon.name, context)
                    pokemon.favorite = isFavorite
                    insertPokemonCard(pokemon)
                    count += 1
                    val progress = count.toFloat() / maxPokemonsSize.toFloat()
                    callBackLoadingFirebaseCounter.invoke(progress)
                    Log.i("Insert pokemon", "${pokemon.id} ${pokemon.name}")
                }
            }

            // Validation + enrichment only run on first install / DATA_VERSION bump.
            // On regular launches the data is already complete (or was best-effort filled
            // on the first run) and we must NOT show the indeterminate loading bar again.
            // Any partially-failed enrichment from the first run is intentionally left
            // until the next DATA_VERSION bump to avoid blocking subsequent app starts.
            if (isFirstRunOrVersionBump) {
                val needsValidation = getValidationVersion(context) < VALIDATION_VERSION
                val needsEnrichment = localDataSource.getPokemonIdsNotEnriched().isNotEmpty()
                if (needsValidation || needsEnrichment) {
                    callBackLoadingPostProcess.invoke()
                }

                validateAndCorrectPokemonData(context)
                enrichPokemonData(context)
                setDataVersionDone(context)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            callBackError.invoke(e)
        }
    }

    private fun getValidationVersion(context: Context): Int {
        val prefs = context.getSharedPreferences(VALIDATION_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(VALIDATION_VERSION_KEY, 0)
    }

    private fun setValidationVersion(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(VALIDATION_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(VALIDATION_VERSION_KEY, version).apply()
    }

    private fun needsDataRefresh(context: Context): Boolean {
        val prefs = context.getSharedPreferences(DATA_VERSION_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(DATA_VERSION_KEY, 0) < DATA_VERSION
    }

    private fun setDataVersionDone(context: Context) {
        val prefs = context.getSharedPreferences(DATA_VERSION_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(DATA_VERSION_KEY, DATA_VERSION).apply()
    }

    /**
     * Cross-checks local DB data against PokeAPI and corrects two known Firebase issues:
     *  1. Types stored as "normal" when the real type(s) differ.
     *  2. Habitat stored as "unknow" / "null" when PokeAPI has a real value.
     *
     * Only runs once per [VALIDATION_VERSION]. Increment [VALIDATION_VERSION] in the
     * companion object to force a re-run on the next app start.
     */
    private suspend fun validateAndCorrectPokemonData(context: Context) {
        if (getValidationVersion(context) >= VALIDATION_VERSION) return
        var corrections = 0

        coroutineScope {
            // --- 1. Fix types ---
            val normalOnlyIds = localDataSource.getPokemonIdsWithOnlyNormalType()
            Log.i("PokemonValidation", "Checking ${normalOnlyIds.size} Pokémon with only 'normal' type")
            val typeResults = normalOnlyIds.map { pokemonId ->
                async(Dispatchers.IO) {
                    try {
                        val apiTypes = remoteDataSource.getPokemonDetail(pokemonId)?.types ?: return@async false
                        val apiTypeNames = apiTypes.mapNotNull { it.type.name?.lowercase() }.filter { it.isNotEmpty() }
                        if (apiTypeNames.isEmpty() || apiTypeNames == listOf("normal")) return@async false

                        val oldTypeIds = localDataSource.getTypesByPokemonId(pokemonId).map { it.typeId }
                        localDataSource.deleteTypesByPokemonId(pokemonId)
                        localDataSource.deleteTypesByIds(oldTypeIds)
                        for (typeResponse in apiTypes) {
                            val typeName = typeResponse.type.name ?: continue
                            val typeId = localDataSource.insertType(TypeEntity(typeName = typeName))
                            localDataSource.insertPokemonType(PokemonType(pokemonId = pokemonId, typeId = typeId.toInt()))
                        }
                        Log.i("PokemonValidation", "Pokémon $pokemonId types corrected → $apiTypeNames")
                        true
                    } catch (e: Exception) {
                        Log.w("PokemonValidation", "Skipping type correction for Pokémon $pokemonId: $e")
                        false
                    }
                }
            }.awaitAll()
            corrections += typeResults.count { it }

            // --- 2. Fix habitats ---
            val unknownHabitatIds = localDataSource.getPokemonIdsWithUnknownHabitat()
            Log.i("PokemonValidation", "Checking ${unknownHabitatIds.size} Pokémon with unknown habitat")
            val habitatResults = unknownHabitatIds.map { pokemonId ->
                async(Dispatchers.IO) {
                    try {
                        val habitatName = remoteDataSource.getPokemonSpecies(pokemonId).habitat?.name
                        if (!habitatName.isNullOrBlank()) {
                            localDataSource.updatePokemonHabitat(pokemonId, habitatName)
                            Log.i("PokemonValidation", "Pokémon $pokemonId habitat corrected → $habitatName")
                            true
                        } else false
                    } catch (e: Exception) {
                        Log.w("PokemonValidation", "Skipping habitat correction for Pokémon $pokemonId: $e")
                        false
                    }
                }
            }.awaitAll()
            corrections += habitatResults.count { it }
        }

        setValidationVersion(context, VALIDATION_VERSION)
        Log.i("PokemonValidation", "Validation finished — $corrections correction(s) applied")
    }

    private fun getEnrichmentVersion(context: Context): Int {
        val prefs = context.getSharedPreferences(ENRICHMENT_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(ENRICHMENT_VERSION_KEY, 0)
    }

    private fun setEnrichmentVersion(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(ENRICHMENT_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(ENRICHMENT_VERSION_KEY, version).apply()
    }

    /**
     * Populates generation, legendary/mythical/baby flags, shape, growth rate and egg groups
     * for Pokémon that are still missing these fields (generation == "").
     *
     * Runs on every launch but does real work only when there are unenriched rows — this
     * handles both the first install after migration and any new Pokémon added to Firebase
     * after the initial enrichment pass.
     *
     * To force a full re-enrichment of already-enriched Pokémon (e.g. when adding a new
     * field), increment [ENRICHMENT_VERSION] and add a migration step that resets
     * `generation = ''` for all rows.
     */
    private suspend fun enrichPokemonData(context: Context) {
        val unenrichedIds = localDataSource.getPokemonIdsNotEnriched()
        if (unenrichedIds.isEmpty()) return
        Log.i("PokemonEnrichment", "Enriching ${unenrichedIds.size} Pokémon")
        var enriched = 0
        coroutineScope {
            unenrichedIds.chunked(ENRICHMENT_BATCH_SIZE).forEach { batch ->
                val results = batch.map { pokemonId ->
                    async(Dispatchers.IO) {
                        try {
                            val specie = remoteDataSource.getPokemonSpecies(pokemonId)
                            val generation = mapGeneration(specie.generation?.name)
                            val isLegendary = specie.is_legendary ?: false
                            val isMythical = specie.is_mythical ?: false
                            val isBaby = specie.is_baby ?: false
                            val shape = specie.shape?.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() } ?: ""
                            val growthRate = specie.growth_rate?.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() } ?: ""
                            val eggGroups = specie.egg_groups
                                ?.mapNotNull { it.name?.replace("-", " ")?.replaceFirstChar { c -> c.uppercase() } }
                                ?.joinToString(",") ?: ""
                            localDataSource.updatePokemonEnrichment(
                                pokemonId, generation, isLegendary, isMythical, isBaby, shape, growthRate, eggGroups
                            )
                            true
                        } catch (e: Exception) {
                            Log.w("PokemonEnrichment", "Skipping Pokémon $pokemonId: $e")
                            false
                        }
                    }
                }.awaitAll()
                enriched += results.count { it }
            }
        }
        setEnrichmentVersion(context, ENRICHMENT_VERSION)
        Log.i("PokemonEnrichment", "Enrichment finished — $enriched Pokémon updated")
    }

    private fun mapGeneration(gen: String?): String = when (gen?.lowercase()) {
        "generation-i"    -> "Gen I - Kanto"
        "generation-ii"   -> "Gen II - Johto"
        "generation-iii"  -> "Gen III - Hoenn"
        "generation-iv"   -> "Gen IV - Sinnoh"
        "generation-v"    -> "Gen V - Unova"
        "generation-vi"   -> "Gen VI - Kalos"
        "generation-vii"  -> "Gen VII - Alola"
        "generation-viii" -> "Gen VIII - Galar"
        "generation-ix"   -> "Gen IX - Paldea"
        else -> gen?.replaceFirstChar { it.uppercase() } ?: ""
    }

    private fun setCountMaxPokemon(count: Int, context: Context?) {
        context?.let { ctx ->
            val sharedPreferences = ctx.getSharedPreferences(MAX_POKEMONS, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt(MAX_POKEMONS, count)
                apply()
            }
        } ?: run {
            Log.e("setCountMaxPokemon", "Context is null")
        }
    }

    private fun getCountMaxPokemon(context: Context?): Int {
        return context?.let { ctx ->
            val sharedPreferences = ctx.getSharedPreferences(MAX_POKEMONS, Context.MODE_PRIVATE)
            sharedPreferences.getInt(MAX_POKEMONS, 0)
        } ?: 0
    }


    // LOCAL - DATABSE

    override suspend fun getAllPokemonEntities(): List<PokemonEntity> {
        return localDataSource.getAllPokemonsEntities()
    }

    suspend fun getCountPokemonEntities(): Int {
        return localDataSource.getCountPokemons()
    }

    override suspend fun getPokemonWithDetailsByName(name: String): PokemonWithDetails? {
        return localDataSource.getPokemonWithDetailsByName(name)
    }

    override suspend fun getPokemonWithDetailsByListName(pokemonNames: List<String>): List<PokemonWithDetails> {
        return localDataSource.getPokemonWithDetailsByListName(pokemonNames)
    }

    override suspend fun getPokemonWithDetailsByIds(pokemonIds: List<Int>): List<PokemonWithDetails> {
        return localDataSource.getPokemonWithDetailsByIds(pokemonIds)
    }

    override suspend fun getPokemonWithDetailsById(id: Int): PokemonWithDetails? {
        return localDataSource.getPokemonWithDetailsById(id)
    }

    override suspend fun getPokemonColorById(id: Int): String? {
        return localDataSource.getPokemonColorById(id)
    }

    suspend fun insertPokemonCard(pokemon: Pokemon) {
        val pokemonEntity = pokemon.toEntity()
        val statEntityList = pokemon.toStatEntities()
        val typeEntityList = pokemon.toTypeEntities()
        val abilityEntityList = pokemon.toAbilityEntities()
        // Inserir Pokémon
        val pokemonId = localDataSource.insertPokemon(pokemonEntity)
        // Adicionando tipos (se existirem)
        typeEntityList?.forEach { type ->
            val typeId = localDataSource.insertType(type)
            localDataSource.insertPokemonType(
                PokemonType(
                    pokemonId = pokemonId.toInt(),
                    typeId = typeId.toInt()
                )
            )
        }
        // Adicionando habilidades (se existirem)
        abilityEntityList?.forEach { ability ->
            val abilityId = localDataSource.insertAbility(ability)
            localDataSource.insertPokemonAbility(
                PokemonAbility(
                    pokemonId = pokemonId.toInt(),
                    abilityId = abilityId.toInt()
                )
            )
        }
        // Adicionando stats (se existirem)
        statEntityList?.forEach { stat ->
            val statId = localDataSource.insertStat(stat)
            localDataSource.insertPokemonStat(
                PokemonStat(
                    pokemonId = pokemonId.toInt(),
                    statId = statId.toInt()
                )
            )
        }
    }

    // REMOTE - POKE API

    private suspend fun getPokemonList(limit: Int = 1302): PokemonDataWrapper? {
        return try {
            remoteDataSource.getPokemonList(limit)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Pokemon dataWrapper", e.toString())
            null
        }
    }

    override suspend fun getPokemonDetail(id: Int): Pokemon? {
        return try {
            remoteDataSource.getPokemonDetail(id)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Pokemon (ID: $id) ", e.toString())
            null
        }
    }

    override suspend fun getPokemonEncounters(id: Int): List<Location>? {
        return try {
            remoteDataSource.getPokemonEncounters(id)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Encounters (ID: $id) ", e.toString())
            null
        }
    }

    override suspend fun getPokemonEvolution(id: Int): EvolutionChain? {
        return try {
            remoteDataSource.getPokemonEvolution(id)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("EvolutionChain (ID: $id) ", e.toString())
            null
        }
    }

    override suspend fun getPokemonCharacteristic(id: Int): Characteristic? {
        return try {
            remoteDataSource.getPokemonCharacteristic(id)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Characteristic (ID: $id) ", e.toString())
            null
        }
    }

    override suspend fun getPokemonSpecies(id: Int): Specie? {
        return try {
            remoteDataSource.getPokemonSpecies(id)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Specie (ID: $id) ", e.toString())
            null
        }
    }

    override suspend fun getPokemonDamageRelations(type: String): Damage? {
        return try {
            remoteDataSource.getPokemonDamageRelations(type)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Damage (Type: $type) ", e.toString())
            null
        }
    }

    override suspend fun setFavorite(pokemon: PokemonEntity): Boolean {
        val countUpdate = localDataSource.updatePokemonIsFavorite(pokemon)
        return countUpdate > 0
    }

    private fun getFavorite(name: String, context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(name, false)
    }

    // REMOTE - Firebase
    private suspend fun getFirebasePokemonList(
        callBackError: ((e: Exception) -> Unit),
    ): List<Pokemon> = withContext(Dispatchers.IO) {
        val pokemonList = mutableListOf<Pokemon>()
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("pokemons")
        try {
            suspendCancellableCoroutine { continuation ->
                val listener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (pokemonSnapshot in dataSnapshot.children) {
                            val pokemonData = (pokemonSnapshot.value as HashMap<*, *>).toPokemon()
                            pokemonList.add(pokemonData)
                            Log.i("Firebase pokemon", pokemonData.toString())
                        }
                        continuation.resume(pokemonList)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        callBackError.invoke(databaseError.toException())
                        Log.w(
                            "FirebasePokemon",
                            "Erro ao ler os dados",
                            databaseError.toException()
                        )
                        continuation.resumeWith(Result.failure(databaseError.toException()))
                    }
                }
                database.addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    database.removeEventListener(listener)
                }
            }
        } catch (e: FirebaseNetworkException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        }
    }

   override suspend fun getMovesForPokemon(pokemonId: Int): List<com.vinithius.dex10.datasource.response.MoveResponse>? {
        return try {
            remoteDataSource.getPokemonDetail(pokemonId)?.moves
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error fetching moves for pokemon $pokemonId", e)
            null
        }
    }

    override suspend fun getMoveDetails(moveName: String): com.vinithius.dex10.datasource.response.MoveDetailsResponse? {
        localDataSource.getMoveDetail(moveName)?.let { cached ->
            return com.vinithius.dex10.datasource.response.MoveDetailsResponse(
                id = 0,
                name = cached.name,
                power = cached.power,
                accuracy = cached.accuracy,
                pp = cached.pp,
                type = com.vinithius.dex10.datasource.response.Default(name = cached.typeName, url = null),
                damage_class = com.vinithius.dex10.datasource.response.Default(name = cached.damageClass, url = null),
                priority = cached.priority
            )
        }
        return try {
            remoteDataSource.getMoveDetail(moveName)?.also { r ->
                localDataSource.upsertMoveDetail(
                    com.vinithius.dex10.datasource.database.MoveDetailEntity(
                        name = r.name,
                        power = r.power,
                        accuracy = r.accuracy,
                        pp = r.pp,
                        typeName = r.type.name,
                        damageClass = r.damage_class.name,
                        priority = r.priority
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error fetching move details for $moveName", e)
            null
        }
    }

    override suspend fun getTcgCards(name: String): List<com.vinithius.dex10.datasource.response.TcgCard>? {
        return try {
            val searchName = formatPokemonNameForSearch(name)
            Log.d("PokemonRepository", "Fetching TCGdex cards for: $searchName")
            val results = tcgRemoteDataSource.getTcgCards(searchName)

            // Filter ensures the card belongs to exactly this Pokémon.
            // Uses startsWith + word-boundary so "Mr. Mime" never matches "Mr. Mime Jr."
            // and "Nidoran♀" never matches "Nidoran♂".
            results.filter { card -> isTcgCardForPokemon(card.name, searchName) }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error fetching TCG cards for $name", e)
            null
        }
    }

    override suspend fun getAnimeInfo(name: String): com.vinithius.dex10.datasource.response.JikanAnimeInfo? {
        return try {
            val searchName = formatPokemonNameForSearch(name)
            Log.d("PokemonRepository", "Fetching Anime info for: $searchName")
            val charSearch = jikanRemoteDataSource.searchCharacter(searchName, limit = 10)

            val candidates = charSearch.data
            if (candidates.isEmpty()) return null

            // Prefer Pokemon-franchise candidates, but fallback to best name match for broader coverage.
            val pokemonCandidates = candidates.filter(::isPokemonFranchiseCharacter)
            val pool = if (pokemonCandidates.isNotEmpty()) pokemonCandidates else candidates
            val character = pool.maxByOrNull { scoreCharacterMatch(it, searchName) } ?: return null

            val voices = jikanRemoteDataSource.getCharacterVoices(character.mal_id)
            val japaneseVoice = voices.data.firstOrNull {
                it.language.trim().equals("Japanese", ignoreCase = true)
            }
            val englishVoice = voices.data.firstOrNull {
                it.language.trim().equals("English", ignoreCase = true)
            }
            val selectedVoice = japaneseVoice ?: englishVoice ?: voices.data.firstOrNull()
            if (selectedVoice == null) {
                Log.w("PokemonRepository", "No voice actor found for: $searchName")
            }

            com.vinithius.dex10.datasource.response.JikanAnimeInfo(
                characterName = character.name,
                characterImageUrl = character.images?.jpg?.image_url,
                voiceActorName = selectedVoice?.person?.name,
                voiceActorImageUrl = selectedVoice?.person?.images?.jpg?.image_url
            )
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error fetching anime info for $name", e)
            null
        }
    }

    override suspend fun getLocalPokemonList(): List<PokemonWithDetails>? =
        localDataSource.getPokemonListWithDetails()

    override suspend fun runEnrichmentIfNeeded(context: Context) {
        enrichPokemonData(context)
    }

    private fun isPokemonFranchiseCharacter(character: JikanCharacterData): Boolean {
        return character.anime.orEmpty().any { animeEntry ->
            val animeName = animeEntry.anime?.name.orEmpty().lowercase()
            animeName.contains("pokemon") || animeName.contains("pokémon")
        }
    }

    private fun scoreCharacterMatch(character: JikanCharacterData, searchName: String): Int {
        val normalizedSearch = normalizeForMatch(searchName)
        val normalizedCharacterName = normalizeForMatch(character.name)

        var score = 0
        if (normalizedCharacterName == normalizedSearch) {
            score += 100
        } else if (normalizedCharacterName.startsWith(normalizedSearch)) {
            score += 60
        } else if (normalizedSearch.isNotEmpty() && normalizedCharacterName.contains(normalizedSearch)) {
            score += 40
        }

        if (isPokemonFranchiseCharacter(character)) {
            score += 1000
        }

        if (!character.anime.isNullOrEmpty()) {
            score += 10
        }

        return score
    }

    private fun normalizeForMatch(value: String?): String {
        return value
            .orEmpty()
            .lowercase()
            .replace("pokémon", "pokemon")
            .replace(Regex("[^a-z0-9]"), "")
    }

    companion object {

        // Increment to force a new validation pass on the next app start
        private const val VALIDATION_VERSION = 1
        private const val VALIDATION_PREFS = "pokemon_validation_prefs"
        private const val VALIDATION_VERSION_KEY = "validation_version"

        // Increment to force a new enrichment pass (generation, legendary, shape, etc.)
        private const val ENRICHMENT_VERSION = 1
        private const val ENRICHMENT_PREFS = "pokemon_enrichment_prefs"
        private const val ENRICHMENT_VERSION_KEY = "enrichment_version"
        private const val ENRICHMENT_BATCH_SIZE = 20

        // Increment when new data fields or Firebase content require a full re-sync + enrichment.
        // Until the loading screen completes successfully, this stays < DATA_VERSION and the
        // sync is retried on every app open.
        private const val DATA_VERSION = 1
        private const val DATA_VERSION_PREFS = "pokemon_data_version_prefs"
        private const val DATA_VERSION_KEY = "data_version"

        /**
         * Maps PokeAPI slugs to the exact name used in the TCG.
         * Add entries here whenever a new Pokémon with a non-standard name is released.
         */
        internal val POKEMON_NAME_OVERRIDES: Map<String, String> = mapOf(
            // Punctuation / apostrophes
            "ho-oh"        to "Ho-Oh",
            "mr-mime"      to "Mr. Mime",
            "mr-rime"      to "Mr. Rime",
            "mime-jr"      to "Mime Jr.",
            "porygon-z"    to "Porygon-Z",
            "type-null"    to "Type: Null",
            "jangmo-o"     to "Jangmo-o",
            "hakamo-o"     to "Hakamo-o",
            "kommo-o"      to "Kommo-o",
            "sirfetchd"    to "Sirfetch'd",
            "farfetchd"    to "Farfetch'd",
            // Gender symbols
            "nidoran-f"    to "Nidoran♀",
            "nidoran-m"    to "Nidoran♂",
            // Accented characters
            "flabebe"      to "Flabébé",
            // Indeedee gender forms share the same TCG base name
            "indeedee-f"   to "Indeedee",
            "indeedee-m"   to "Indeedee",
            // Meowstic gender forms
            "meowstic-f"   to "Meowstic",
            "meowstic-m"   to "Meowstic",
            // Basculin forms
            "basculin-blue-striped" to "Basculin",
            "basculin-white-striped" to "Basculin",
            // Lycanroc forms that have distinct TCG cards keep the base name
            "lycanroc-midday"   to "Lycanroc",
            "lycanroc-midnight" to "Lycanroc",
            "lycanroc-dusk"     to "Lycanroc",
            // Oricorio forms
            "oricorio-pom-pom" to "Oricorio",
            "oricorio-pau"     to "Oricorio",
            "oricorio-sensu"   to "Oricorio",
            // Wormadam forms
            "wormadam-sandy"   to "Wormadam",
            "wormadam-trash"   to "Wormadam",
            // Toxtricity forms
            "toxtricity-low-key" to "Toxtricity",
            // Urshifu forms
            "urshifu-single-strike" to "Urshifu",
            "urshifu-rapid-strike"  to "Urshifu",
            // Calyrex riders
            "calyrex-ice"    to "Calyrex",
            "calyrex-shadow" to "Calyrex",
            // Enamorus forms
            "enamorus-therian" to "Enamorus",
            // Paradox / DLC multi-word names handled by prefix logic above,
            // but listed here for safety
            "chien-pao"  to "Chien-Pao",
            "chi-yu"     to "Chi-Yu",
            "ting-lu"    to "Ting-Lu",
            "wo-chien"   to "Wo-Chien",
        )

        /**
         * Formats Pokémon names for external services like TCGdex and Jikan.
         * PokeAPI names (ho-oh, mr-mime) are transformed to their official formatting
         * (Ho-Oh, Mr. Mime) to increase search precision.
         *
         * Priority: exact-match table → gender/form suffix strip → multi-word prefixes → first-word.
         */
        fun formatPokemonNameForSearch(name: String): String {
            val lowercaseName = name.lowercase()

            // Exact overrides — names that cannot be derived algorithmically
            POKEMON_NAME_OVERRIDES[lowercaseName]?.let { return it }

            // Gender variants: strip the suffix and keep canonical base name
            if (lowercaseName.endsWith("-f") || lowercaseName.endsWith("-m")) {
                val base = lowercaseName.dropLast(2)
                POKEMON_NAME_OVERRIDES[base]?.let { return it }
            }

            // Two-word compound names: the prefix IS part of the canonical name
            val multiWordPrefixes = listOf(
                "tapu-", "iron-", "scream-", "brute-", "flutter-", "slither-",
                "sandy-", "roaring-", "walking-", "gouging-", "raging-",
                "great-", "wo-", "chien-", "chi-", "ting-"
            )
            if (multiWordPrefixes.any { lowercaseName.startsWith(it) }) {
                // Use lowercaseName so input like "IRON-BUNDLE" still produces "Iron Bundle"
                return lowercaseName.split("-").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            }

            // Default: take only the base name before any form/variant hyphen
            // Use lowercaseName so input like "PIKACHU" produces "Pikachu" not "PIKACHU"
            return lowercaseName.split("-").first().replaceFirstChar { it.uppercase() }
        }

        // Regex matching the card-type suffixes used in TCG card names.
        // These are the only valid tokens that can follow a Pokémon name + space.
        // Example: "Pikachu V", "Charizard ex", "Mewtwo VMAX", "Darkrai BREAK"
        private val TCG_SUFFIX_REGEX = Regex(
            """^(v|vmax|vstar|gx|ex|break|lv\.x|tag team|prism star|\d)""",
            RegexOption.IGNORE_CASE
        )

        /**
         * Returns true if [cardName] belongs to [pokemonName].
         *
         * A card matches when its name:
         *  - equals the Pokémon name exactly, OR
         *  - starts with "PokémonName-" (covers Charizard-EX style), OR
         *  - starts with "PokémonName " AND what follows is a known TCG suffix
         *    (V, VMAX, VSTAR, GX, EX, ex, BREAK …) — this deliberately rejects
         *    "Mr. Mime Jr." when searching for "Mr. Mime".
         */
        fun isTcgCardForPokemon(cardName: String, pokemonName: String): Boolean {
            val card = cardName.lowercase().trim()
            val target = pokemonName.lowercase()
            if (card == target) return true
            if (card.startsWith("$target-")) return true
            if (card.startsWith("$target ")) {
                val suffix = card.removePrefix("$target ")
                return TCG_SUFFIX_REGEX.containsMatchIn(suffix)
            }
            return false
        }
    }
}
