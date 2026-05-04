package com.vinithius.dex10.datasource.response

import com.google.gson.annotations.SerializedName

data class TcgCard(
    val id: String,
    val localId: String,
    val name: String,
    val image: String?
) {
    // TCGdex provides a base image URL. We append the quality we want.
    fun getSmallImage(): String? = image?.let { "$it/low.jpg" }
    fun getLargeImage(): String? = image?.let { "$it/high.jpg" }
}
