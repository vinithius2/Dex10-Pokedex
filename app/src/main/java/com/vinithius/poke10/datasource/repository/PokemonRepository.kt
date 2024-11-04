package com.vinithius.poke10.datasource.repository

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import com.vinithius.poke10.datasource.database.PokemonCard
import com.vinithius.poke10.datasource.database.PokemonDao
import com.vinithius.poke10.datasource.response.Characteristic
import com.vinithius.poke10.datasource.response.Damage
import com.vinithius.poke10.datasource.response.EvolutionChain
import com.vinithius.poke10.datasource.response.Location
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.datasource.response.PokemonDataWrapper
import com.vinithius.poke10.datasource.response.Specie
import com.vinithius.poke10.ui.MainActivity.Companion.FAVORITES
import retrofit2.HttpException


class PokemonRepository(private val remoteDataSource: PokemonRemoteDataSource, private val localDataSource: PokemonDao) {

    // Local

    suspend fun getAllPokemonCard() : List<PokemonCard> {
        return localDataSource.getAllPokemon()
    }

    suspend fun insertPokemonCard(
        pokemon: Pokemon
    ) {
        pokemon.id
        pokemon.name
        pokemon.types
        pokemon.evolution
        pokemon.
    }
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type_id", index = true) val typeId: Int?,
    @ColumnInfo(name = "evolution_id", index = true) val evolutionId: Int?,
    @ColumnInfo(name = "image_path") val imagePath: String?




    // Remote

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

    fun getFavorite(name: String, context: Context) : Boolean {
        val sharedPreferences = context.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(name, false)
    }
}
