package com.vinithius.poke10x.datasource.response

data class Chain(
    val is_baby: Boolean,
    val species: Default?,
    val evolution_details: List<EvolutionDetails>?,
    val evolves_to: List<Chain>?
)
