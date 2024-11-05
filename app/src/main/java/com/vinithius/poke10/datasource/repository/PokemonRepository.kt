package com.vinithius.poke10.datasource.repository

import android.content.Context
import android.util.Log
import com.vinithius.poke10.datasource.database.PokemonAbility
import com.vinithius.poke10.datasource.database.PokemonDao
import com.vinithius.poke10.datasource.database.PokemonEntity
import com.vinithius.poke10.datasource.database.PokemonStat
import com.vinithius.poke10.datasource.database.PokemonType
import com.vinithius.poke10.datasource.mapper.toAbilityEntities
import com.vinithius.poke10.datasource.mapper.toEntity
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
import retrofit2.HttpException


class PokemonRepository(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonDao
) {

    // LOCAL - DATABSE

    suspend fun getAllPokemonEntities(): List<PokemonEntity> {
        return localDataSource.getAllPokemonsEntities()
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
            localDataSource.insertPokemonType(PokemonType(pokemonId = pokemonId.toInt(), typeId = typeId.toInt()))
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
            localDataSource.insertPokemonStat(PokemonStat(pokemonId = pokemonId.toInt(), statId = statId.toInt()))
        }
    }

    // REMOTE - POKE API

    suspend fun getPokemonList(): PokemonDataWrapper? {
        return try {
            remoteDataSource.getPokemonList(1302)
        } catch (e: HttpException) {
            Log.e("Pokemon dataWrapper", e.toString())
            null
        }
    }

    suspend fun getPokemonDetail(id: Int): Pokemon? {
        return try {
            remoteDataSource.getPokemonDetail(id)
        } catch (e: HttpException) {
            Log.e("Pokemon (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonEncounters(id: Int): List<Location>? {
        return try {
            remoteDataSource.getPokemonEncounters(id)
        } catch (e: HttpException) {
            Log.e("Encounters (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonEvolution(id: Int): EvolutionChain? {
        return try {
            remoteDataSource.getPokemonEvolution(id)
        } catch (e: HttpException) {
            Log.e("EvolutionChain (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonCharacteristic(id: Int): Characteristic? {
        return try {
            remoteDataSource.getPokemonCharacteristic(id)
        } catch (e: HttpException) {
            Log.e("Characteristic (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonSpecies(id: Int): Specie? {
        return try {
            remoteDataSource.getPokemonSpecies(id)
        } catch (e: HttpException) {
            Log.e("Specie (ID: $id) ", e.toString())
            null
        }
    }

    suspend fun getPokemonDamageRelations(type: String): Damage? {
        return try {
            remoteDataSource.getPokemonDamageRelations(type)
        } catch (e: HttpException) {
            Log.e("Damage (Type: $type) ", e.toString())
            null
        }
    }


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

    fun getFavorite(name: String, context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(name, false)
    }
}
