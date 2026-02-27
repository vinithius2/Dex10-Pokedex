package com.vinithius.dex10.datasource.repository

import com.vinithius.dex10.datasource.response.JikanCharacterSearchResponse
import com.vinithius.dex10.datasource.response.JikanVoicesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanRemoteDataSource {

    @GET("characters")
    suspend fun searchCharacter(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1
    ): JikanCharacterSearchResponse

    @GET("characters/{id}/voices")
    suspend fun getCharacterVoices(
        @Path("id") characterId: Int
    ): JikanVoicesResponse

}
