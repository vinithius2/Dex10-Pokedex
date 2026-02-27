package com.vinithius.dex10.datasource.repository

import com.vinithius.dex10.datasource.response.TcgCard
import retrofit2.http.GET
import retrofit2.http.Query

interface TcgRemoteDataSource {

    @GET("cards")
    suspend fun getTcgCards(
        @Query("name") name: String
    ): List<TcgCard>

}
