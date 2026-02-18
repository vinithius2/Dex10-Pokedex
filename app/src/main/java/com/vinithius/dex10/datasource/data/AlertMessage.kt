package com.vinithius.dex10.datasource.data

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.vinithius.dex10.R

data class AlertMessage(
    val show: Boolean,
    @SerializedName("version_code")
    val versionCode: Long?,
    @SerializedName("url_action")
    val urlAction: String?,
    val content: Map<String, LocalizedContent>
) {
    fun getLocalizedContent(
        languageCode: String?,
        context: Context
    ): LocalizedContent {
        val supportedLanguages = context.resources.getStringArray(
            R.array.supported_languages
        ).toSet()
        val code = languageCode?.takeIf { it in supportedLanguages } ?: "en"
        return content[code] ?: content["en"] ?: LocalizedContent(
            title = String(),
            message = String(),
            titleButton = String()
        )
    }
}

data class LocalizedContent(
    val title: String,
    val message: String,
    @SerializedName("title_button")
    val titleButton: String
)

