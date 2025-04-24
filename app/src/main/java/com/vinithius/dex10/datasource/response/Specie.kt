package com.vinithius.dex10.datasource.response

data class Specie(
    val base_happiness: Int?,
    val capture_rate: Int?,
    val color: Default?,
    val egg_groups: List<Default>?,
    val evolution_chain: DefaultUrl?,
    val evolves_from_species: Default?,
    val flavor_text_entries: List<FlavorText>?,
    val forms_switchable: Boolean?,
    val gender_rate: Int?,
    val generation: Default?,
    val growth_rate: Default?,
    val habitat: Default?,
    val has_gender_differences: Boolean?,
    val hatch_counter: Int?,
    val id: Int?,
    val is_baby: Boolean?,
    val is_legendary: Boolean?,
    val is_mythical: Boolean?,
    val name: String?,
    val order: Int?,
    val pal_park_encounters: List<PalParkEncounters>?,
    val pokedex_numbers: List<PokedexNumbers>?,
    val shape: Default?,
    val varieties: List<Varieties>?
)
