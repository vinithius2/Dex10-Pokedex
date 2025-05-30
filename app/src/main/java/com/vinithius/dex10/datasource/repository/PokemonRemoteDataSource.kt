package com.vinithius.dex10.datasource.repository

import com.vinithius.dex10.datasource.response.Characteristic
import com.vinithius.dex10.datasource.response.Damage
import com.vinithius.dex10.datasource.response.EvolutionChain
import com.vinithius.dex10.datasource.response.Location
import com.vinithius.dex10.datasource.response.Pokemon
import com.vinithius.dex10.datasource.response.PokemonDataWrapper
import com.vinithius.dex10.datasource.response.Specie
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonRemoteDataSource {

    @GET("pokemon/")
    suspend fun getPokemonList(@Query("limit") limit: Int): PokemonDataWrapper

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): Pokemon

    @GET("evolution-chain/{id}")
    suspend fun getPokemonEvolution(@Path("id") id: Int): EvolutionChain

    @GET("pokemon/{id}/encounters")
    suspend fun getPokemonEncounters(@Path("id") id: Int): List<Location>

    @GET("characteristic/{id}")
    suspend fun getPokemonCharacteristic(@Path("id") id: Int): Characteristic

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): Specie

    @GET("type/{type}")
    suspend fun getPokemonDamageRelations(@Path("type") type: String): Damage

}
