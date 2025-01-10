package com.vinithius.poke10.datasource.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vinithius.poke10.datasource.database.PokemonAbility
import com.vinithius.poke10.datasource.database.PokemonDao
import com.vinithius.poke10.datasource.database.PokemonEntity
import com.vinithius.poke10.datasource.database.PokemonStat
import com.vinithius.poke10.datasource.database.PokemonType
import com.vinithius.poke10.datasource.database.PokemonWithDetails
import com.vinithius.poke10.datasource.mapper.toAbilityEntities
import com.vinithius.poke10.datasource.mapper.toEntity
import com.vinithius.poke10.datasource.mapper.toPokemon
import com.vinithius.poke10.datasource.mapper.toStatEntities
import com.vinithius.poke10.datasource.mapper.toTypeEntities
import com.vinithius.poke10.datasource.response.Characteristic
import com.vinithius.poke10.datasource.response.Damage
import com.vinithius.poke10.datasource.response.EvolutionChain
import com.vinithius.poke10.datasource.response.Location
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.datasource.response.PokemonDataWrapper
import com.vinithius.poke10.datasource.response.Specie
import com.vinithius.poke10.ui.MainActivity.Companion.FAVORITES
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
            // TODO: Compare the count from firebase and database for get the difference
            if (countLocal == 0) {
                callBackLoadingFirebase.invoke()
                val pokemonFirebaseList = getFirebasePokemonList(callBackError)
                val maxSize = pokemonFirebaseList.size
                var count = 0
                pokemonFirebaseList.forEach { pokemon ->
                    val isFavorite = getFavorite(pokemon.name, context)
                    pokemon.favorite = isFavorite
                    insertPokemonCard(pokemon)
                    count += 1
                    val progress = count.toFloat() / maxSize.toFloat()
                    callBackLoadingFirebaseCounter.invoke(progress)
                    Log.i("Insert pokemon", "${pokemon.id} ${pokemon.name}")
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            callBackError.invoke(e)
        }
    }

    // LOCAL - DATABSE

    suspend fun getAllPokemonEntities(): List<PokemonEntity> {
        return localDataSource.getAllPokemonsEntities()
    }

    suspend fun getCountPokemonEntities(): Int {
        return localDataSource.getCountPokemons()
    }

    suspend fun getPokemonWithDetails(id: Int): PokemonWithDetails? {
        return localDataSource.getPokemonWithDetailsById(id)
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

    /*
        fun setFavorite(name: String, context: Context?): Boolean {
            return try {
                val result = context?.run {
                    val sharedPreferences = getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
                    val isFavorite = sharedPreferences.getBoolean(name, false)
                    with(sharedPreferences.edit()) {
                        putBoolean(name, isFavorite.not())
                        apply()
                    }
                    isFavorite.not()
                }
                result ?: false
            } catch (e: HttpException) {
                Log.e("Favorite", e.toString())
                false
            }
        }
    */

    fun getFavorite(name: String, context: Context): Boolean {
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
                        Log.w("FirebasePokemon", "Erro ao ler os dados", databaseError.toException())
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
