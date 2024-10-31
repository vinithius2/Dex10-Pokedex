package com.vinithius.poke10.datasource.repository

import com.vinithius.poke10.datasource.response.*
import retrofit2.http.*

interface PokemonRemoteDataSource {

    @GET("pokemon/")
    suspend fun getPokemonList(@Query("limit") limit: Int): PokemonDataWrapper
    //suspend fun getPokemonList(@Query("offset") offset: Int): PokemonDataWrapper

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

    @POST
    suspend fun setFavorite(
        @Url url: String,
        @Body pokemon: Pokemon
    )

    @HTTP(
        method = "DELETE",
        hasBody = true
    )
    suspend fun deleteFavorite(@Url url: String, @Body pokemon: Pokemon)

}
