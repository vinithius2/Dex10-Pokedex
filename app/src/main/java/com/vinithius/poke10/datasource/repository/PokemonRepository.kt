package com.vinithius.poke10.datasource.repository

import android.content.Context
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.vinithius.poke10.BuildConfig
import com.vinithius.poke10.datasource.response.*
//import com.vinithius.poke10.extension.getIsFavorite
import retrofit2.HttpException


class PokemonRepository(private val remoteDataSource: PokemonRemoteDataSource) {

    suspend fun getPokemonList(): List<Pokemon>? {
        return try {
            remoteDataSource.getPokemonList()
        } catch (e: HttpException) {
            Log.e("Pokemon list", e.toString())
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
