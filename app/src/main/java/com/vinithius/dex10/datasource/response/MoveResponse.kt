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
    val priority: Int
)
