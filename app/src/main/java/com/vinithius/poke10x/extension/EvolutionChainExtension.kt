package com.vinithius.poke10x.extension

import com.vinithius.poke10x.datasource.response.Chain
import com.vinithius.poke10x.datasource.response.EvolutionChain

/**
 * Get species pokemon and return evolutions from getEvolvesTo().
 */
fun EvolutionChain.getListEvolutions(): List<Pair<String, String>> {
    var listEvolutions = mutableListOf<Pair<String, String>>()
    this.chain.species?.let {
        listEvolutions.add(Pair(it.name!!, it.url!!))
        listEvolutions = getEvolvesTo(this.chain, listEvolutions)
    }
    return listEvolutions.toList()
}

/**
 * Organize evolutions pokemon from species.
 */
private fun getEvolvesTo(
    chain: Chain,
    listEvolutions: MutableList<Pair<String, String>>
): MutableList<Pair<String, String>> {
    if (chain.evolves_to?.size != 0) {
        chain.evolves_to?.forEach { evolve ->
            evolve.species?.let { specie ->
                listEvolutions.add(Pair(specie.name!!, specie.url!!))
                evolve.evolves_to?.forEach { evolvesTo ->
                    getEvolvesTo(evolvesTo, listEvolutions)
                }
            }
        }
    } else {
        chain.species?.let { specie -> listEvolutions.add(Pair(specie.name!!, specie.url!!)) }
    }
    return listEvolutions
}
