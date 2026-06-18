package com.vinithius.dex10.datasource.response

data class MoveResponse(
    val move: Default,
    val version_group_details: List<VersionGroupDetail>
)

data class VersionGroupDetail(
    val level_learned_at: Int,
    val move_learn_method: Default,
    val version_group: Default
)

data class MoveDetailsResponse(
    val id: Int,
    val name: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val type: Default,
    val damage_class: Default, // physical, special, status
    val priority: Int,
    val effect_entries: List<MoveEffectEntry>? = null,
    val flavor_text_entries: List<MoveFlavorTextEntry>? = null,
    /** Short, human-readable effect text resolved from the API (English). */
    val shortEffect: String? = null,
)

data class MoveEffectEntry(
    val effect: String?,
    val short_effect: String?,
    val language: Default?,
)

data class MoveFlavorTextEntry(
    val flavor_text: String?,
    val language: Default?,
    val version_group: Default?,
)
