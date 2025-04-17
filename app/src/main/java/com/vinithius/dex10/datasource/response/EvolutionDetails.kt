package com.vinithius.dex10.datasource.response

data class EvolutionDetails(
    val gender: Int?,
    val min_affection: Int?,
    val min_beauty: Int?,
    val min_happiness: Int?,
    val min_level: Int?,
    val needs_overworld_rain: Boolean?,
    val relative_physical_stats: Int?,
    val time_of_day: String?,
    val trigger: Default,
    val turn_upside_down: Boolean?
)
