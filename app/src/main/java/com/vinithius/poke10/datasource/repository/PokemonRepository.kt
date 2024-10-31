package com.vinithius.poke10.datasource.repository

import android.util.Log
import com.vinithius.poke10.BuildConfig
import com.vinithius.poke10.datasource.response.Characteristic
import com.vinithius.poke10.datasource.response.Damage
import com.vinithius.poke10.datasource.response.EvolutionChain
import com.vinithius.poke10.datasource.response.Location
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.datasource.response.PokemonDataWrapper
import com.vinithius.poke10.datasource.response.Specie
import retrofit2.HttpException


class PokemonRepository(private val remoteDataSource: PokemonRemoteDataSource) {

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

    /*
    suspend fun setFavorite(pokemon: Pokemon, context: Context?) {
        context?.let {
            try {
                val favorite = pokemon.name.getIsFavorite(context)
                if (favorite) {
                    remoteDataSource.setFavorite(urlFavorite, pokemon)
                } else {
                    remoteDataSource.deleteFavorite(urlFavorite, pokemon)
                }
            } catch (e: HttpException) {
                Log.e("Favorite", e.toString())
            }
        }
    }
    */
    companion object {
        const val urlFavorite: String = "https://webhook.site/${BuildConfig.WEBHOOK_KEY}"
    }

}
