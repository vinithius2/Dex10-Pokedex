package com.vinithius.dex10.datasource.response

import com.google.gson.annotations.SerializedName

data class Sprites(
    var back_default: String?,
    var back_female: String?,
    var back_shiny: String?,
    var back_shiny_female: String?,
    var front_default: String?,
    var front_female: String?,
    var front_shiny: String?,
    var front_shiny_female: String?,
    var other: Other?,
)

data class BaseSprite(
    var back_default: String?,
    var back_female: String?,
    var back_shiny: String?,
    var back_shiny_female: String?,
    var front_default: String?,
    var front_female: String?,
    var front_shiny: String?,
    var front_shiny_female: String?,
)

data class Other(
    var dream_world: BaseSprite?,
    var home: BaseSprite?,
    @SerializedName("official-artwork")
    var official_artwork: BaseSprite?,
    var showdown: BaseSprite?,
)
