package com.vinithius.dex10.datasource.response

data class EvolutionDetails(
    val gender: Int?,
    val held_item: Default?,
    val item: Default?,
    val known_move: Default?,
    val known_move_type: Default?,
    val location: Default?,
    val min_affection: Int?,
    val min_beauty: Int?,
    val min_happiness: Int?,
    val min_level: Int?,
    val needs_overworld_rain: Boolean?,
    val party_species: Default?,
    val party_type: Default?,
    val relative_physical_stats: Int?,
    val time_of_day: String?,
    val trigger: Default,
    val turn_upside_down: Boolean?
)
