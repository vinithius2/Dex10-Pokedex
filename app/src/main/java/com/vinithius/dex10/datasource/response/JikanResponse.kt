package com.vinithius.dex10.datasource.response

data class JikanCharacterSearchResponse(
    val data: List<JikanCharacterData>
)

data class JikanCharacterData(
    val mal_id: Int,
    val name: String,
    val images: JikanImages?
)

data class JikanImages(
    val jpg: JikanJpg?
)

data class JikanJpg(
    val image_url: String?
)

data class JikanVoicesResponse(
    val data: List<JikanVoiceData>
)

data class JikanVoiceData(
    val language: String,
    val person: JikanPerson
)

data class JikanPerson(
    val mal_id: Int,
    val name: String,
    val images: JikanImages?
)

data class JikanAnimeInfo(
    val characterName: String?,
    val characterImageUrl: String?,
    val voiceActorName: String?,
    val voiceActorImageUrl: String?
)
