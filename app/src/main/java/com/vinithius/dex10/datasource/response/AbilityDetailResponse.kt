package com.vinithius.dex10.datasource.response

/** PokeAPI `ability/{name}` payload (only the fields we use). */
data class AbilityDetailResponse(
    val id: Int?,
    val name: String?,
    val effect_entries: List<AbilityEffectEntry>? = null,
    val flavor_text_entries: List<AbilityFlavorTextEntry>? = null,
)

data class AbilityEffectEntry(
    val effect: String?,
    val short_effect: String?,
    val language: Default?,
)

data class AbilityFlavorTextEntry(
    val flavor_text: String?,
    val language: Default?,
)

