package com.vinithius.poke10x.datasource.response

data class VersionDetails(
    val encounter_details: List<EncounterDetails>,
    val max_chance: Int,
    val version: Default
)
