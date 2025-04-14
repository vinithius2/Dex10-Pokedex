package com.vinithius.poke10x.datasource.response

data class Characteristic (
    val descriptions: List<Description>,
    val gene_modulo: Int,
    val highest_stat: Default,
    val id: Int,
    val possible_values: List<Int>
)
