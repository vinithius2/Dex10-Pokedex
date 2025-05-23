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
import com.vinithius.dex10.datasource.mapper.toAbilityEntities
import com.vinithius.dex10.datasource.mapper.toEntity
import com.vinithius.dex10.datasource.mapper.toPokemon
import com.vinithius.dex10.datasource.mapper.toStatEntities
import com.vinithius.dex10.datasource.mapper.toTypeEntities
import com.vinithius.dex10.datasource.response.Characteristic
import com.vinithius.dex10.datasource.response.Damage
import com.vinithius.dex10.datasource.response.EvolutionChain
import com.vinithius.dex10.datasource.response.Location
import com.vinithius.dex10.datasource.response.Pokemon
import com.vinithius.dex10.datasource.response.PokemonDataWrapper
import com.vinithius.dex10.datasource.response.Specie
import com.vinithius.dex10.ui.MainActivity.Companion.FAVORITES
import com.vinithius.dex10.ui.MainActivity.Companion.MAX_POKEMONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.coroutines.resume


class PokemonRepository(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonDao
) {

    suspend fun getPokemonEntityList(
        context: Context,
        callBackLoadingFirebaseCounter: ((progress: Float) -> Unit),
        callBackLoadingFirebase: (() -> Unit),
        callBackLoading: (() -> Unit),
        callBackError: ((e: Exception) -> Unit),
    ): List<PokemonWithDetails>? {
        insertPokemonFromFirebaseToLocal(
            context,
            callBackLoadingFirebase,
            callBackLoadingFirebaseCounter,
            callBackError
        )
        callBackLoading.invoke()
        val pokemonList = localDataSource.getPokemonListWithDetails()
        return pokemonList
    }

    /**
     * Execute once
     */
    private suspend fun insertPokemonFromFirebaseToLocal(
        context: Context,
        callBackLoadingFirebase: (() -> Unit),
        callBackLoadingFirebaseCounter: ((progress: Float) -> Unit),
        callBackError: ((e: Exception) -> Unit),
    ) {
        try {
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
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            callBackError.invoke(e)
        }
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

    suspend fun getAllPokemonEntities(): List<PokemonEntity> {
        return localDataSource.getAllPokemonsEntities()
    }

    suspend fun getCountPokemonEntities(): Int {
        return localDataSource.getCountPokemons()
    }

    suspend fun getPokemonWithDetailsByName(name: String): PokemonWithDetails? {
        return localDataSource.getPokemonWithDetailsByName(name)
    }

    suspend fun getPokemonWithDetailsByListName(pokemonNames: List<String>): List<PokemonWithDetails> {
        return localDataSource.getPokemonWithDetailsByListName(pokemonNames)
    }

    suspend fun getPokemonWithDetailsById(id: Int): PokemonWithDetails? {
        return localDataSource.getPokemonWithDetailsById(id)
    }

    suspend fun getPokemonColorById(id: Int): String? {
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

    suspend fun getPokemonDetail(id: Int): Pokemon? {
        return try {
            remoteDataSource.getPokemonDetail(id)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Pokemon (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonEncounters(id: Int): List<Location>? {
        return try {
            remoteDataSource.getPokemonEncounters(id)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Encounters (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonEvolution(id: Int): EvolutionChain? {
        return try {
            remoteDataSource.getPokemonEvolution(id)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("EvolutionChain (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonCharacteristic(id: Int): Characteristic? {
        return try {
            remoteDataSource.getPokemonCharacteristic(id)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Characteristic (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonSpecies(id: Int): Specie? {
        return try {
            remoteDataSource.getPokemonSpecies(id)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Specie (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonDamageRelations(type: String): Damage? {
        return try {
            remoteDataSource.getPokemonDamageRelations(type)
        } catch (e: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Damage (Type: $type) ", e.toString())
            null
        }
    }

    suspend fun setFavorite(pokemon: PokemonEntity): Boolean {
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
                database.addValueEventListener(object : ValueEventListener {
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
                })
            }
        } catch (e: FirebaseNetworkException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        }
    }
}
