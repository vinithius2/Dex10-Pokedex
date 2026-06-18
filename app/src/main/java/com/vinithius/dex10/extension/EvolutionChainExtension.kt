package com.vinithius.dex10.extension

import com.vinithius.dex10.datasource.response.Chain
import com.vinithius.dex10.datasource.response.EvolutionChain
import com.vinithius.dex10.datasource.response.EvolutionDetails

// ── UI model ────────────────────────────────────────────────────────────────

data class EvoEntry(
    val name: String,
    val url: String,
    val triggerText: String?
)

data class EvoStage(val entries: List<EvoEntry>)

data class EvoDisplayEntry(
    val id: Int,
    val name: String,
    val triggerText: String?
)

// ── Stage-based tree (BFS) ───────────────────────────────────────────────────

/**
 * Converts the recursive API chain into a flat list of stages via BFS.
 * Stage 0 = base Pokémon, Stage 1 = first evolution(s), etc.
 * Multiple entries in a stage = branching evolutions (e.g. all Eevee forms).
 */
fun EvolutionChain.toEvoStages(): List<EvoStage> {
    val stages = mutableListOf<EvoStage>()
    val root = chain.species ?: return emptyList()
    stages.add(EvoStage(listOf(EvoEntry(root.name ?: return emptyList(), root.url ?: "", null))))

    var currentLevel = chain.evolves_to.orEmpty()
    while (currentLevel.isNotEmpty()) {
        val entries = currentLevel.mapNotNull { node ->
            val name = node.species?.name ?: return@mapNotNull null
            EvoEntry(
                name = name,
                url = node.species.url ?: "",
                triggerText = node.evolution_details?.firstOrNull()?.toTriggerText()
            )
        }
        if (entries.isNotEmpty()) stages.add(EvoStage(entries))
        currentLevel = currentLevel.flatMap { it.evolves_to.orEmpty() }
    }
    return stages
}

private fun EvolutionDetails.toTriggerText(): String? {
    return when (trigger.name) {
        "level-up" -> when {
            min_level != null -> "Lv. $min_level"
            min_happiness != null -> when (time_of_day) {
                "day" -> "Happiness\n(Day)"
                "night" -> "Happiness\n(Night)"
                else -> "Happiness"
            }
            min_affection != null -> "Affection"
            min_beauty != null -> "Max Beauty"
            time_of_day == "day" -> "Level up\n(Day)"
            time_of_day == "night" -> "Level up\n(Night)"
            needs_overworld_rain == true -> "Level up\n(Rain)"
            known_move != null -> "Learn\n${known_move.name?.formatItemName()}"
            known_move_type != null -> "Learn ${known_move_type.name?.formatItemName()} move"
            location != null -> "Level up\n(${location.name?.formatItemName()})"
            relative_physical_stats != null -> when (relative_physical_stats) {
                1 -> "Atk > Def"
                -1 -> "Def > Atk"
                0 -> "Atk = Def"
                else -> null
            }
            else -> null
        }
        "trade" -> held_item?.name?.let { "Trade\n(${it.formatItemName()})" } ?: "Trade"
        "use-item" -> item?.name?.formatItemName()
        "shed" -> "Lv. 20\n(empty slot)"
        "three-critical-hits" -> "3 critical hits"
        "take-damage" -> "Take damage"
        "agile-style-move" -> "Agile style"
        "strong-style-move" -> "Strong style"
        "recoil-damage" -> "Recoil damage"
        else -> trigger.name?.replace('-', ' ')?.replaceFirstChar { it.uppercase() }
    }
}

private fun String.formatItemName(): String =
    split('-').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

// ── Legacy flat list (kept for any remaining call sites) ────────────────────

fun EvolutionChain.getListEvolutions(): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    chain.species?.let {
        list.add(Pair(it.name!!, it.url!!))
        collectEvolvesTo(chain, list)
    }
    return list.toList()
}

private fun collectEvolvesTo(chain: Chain, list: MutableList<Pair<String, String>>) {
    chain.evolves_to?.forEach { node ->
        node.species?.let { list.add(Pair(it.name!!, it.url!!)) }
        collectEvolvesTo(node, list)
    }
}
