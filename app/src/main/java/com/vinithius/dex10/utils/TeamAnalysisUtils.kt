package com.vinithius.dex10.utils

import com.vinithius.dex10.datasource.database.PokemonWithDetails
import com.vinithius.dex10.datasource.response.Type

object TeamAnalysisUtils {

    // 0 = Normal Effect (1x)
    // 1 = Super Effective (2x)
    // -1 = Not Very Effective (0.5x)
    // -2 = Immune (0x)

    // Simplified Type Chart mapping (Attacker -> Defender)
    // This is a comprehensive matrix for Gen 6+
    private val typeChart = mapOf(
        "normal" to mapOf("rock" to -1, "ghost" to -2, "steel" to -1),
        "fire" to mapOf("fire" to -1, "water" to -1, "grass" to 1, "ice" to 1, "bug" to 1, "rock" to -1, "dragon" to -1, "steel" to 1),
        "water" to mapOf("fire" to 1, "water" to -1, "grass" to -1, "ground" to 1, "rock" to 1, "dragon" to -1),
        "electric" to mapOf("water" to 1, "electric" to -1, "grass" to -1, "ground" to -2, "flying" to 1, "dragon" to -1),
        "grass" to mapOf("fire" to -1, "water" to 1, "grass" to -1, "poison" to -1, "ground" to 1, "flying" to -1, "bug" to -1, "rock" to 1, "dragon" to -1, "steel" to -1),
        "ice" to mapOf("fire" to -1, "water" to -1, "grass" to 1, "ice" to -1, "ground" to 1, "flying" to 1, "dragon" to 1, "steel" to -1),
        "fighting" to mapOf("normal" to 1, "ice" to 1, "poison" to -1, "flying" to -1, "psychic" to -1, "bug" to -1, "rock" to 1, "ghost" to -2, "dark" to 1, "steel" to 1, "fairy" to -1),
        "poison" to mapOf("grass" to 1, "poison" to -1, "ground" to -1, "rock" to -1, "ghost" to -1, "steel" to -2, "fairy" to 1),
        "ground" to mapOf("fire" to 1, "electric" to 1, "grass" to -1, "poison" to 1, "flying" to -2, "bug" to -1, "rock" to 1, "steel" to 1),
        "flying" to mapOf("electric" to -1, "grass" to 1, "fighting" to 1, "bug" to 1, "rock" to -1, "steel" to -1),
        "psychic" to mapOf("fighting" to 1, "poison" to 1, "psychic" to -1, "dark" to -2, "steel" to -1),
        "bug" to mapOf("fire" to -1, "grass" to 1, "fighting" to -1, "poison" to -1, "flying" to -1, "psychic" to 1, "ghost" to -1, "dark" to 1, "steel" to -1, "fairy" to -1),
        "rock" to mapOf("fire" to 1, "ice" to 1, "fighting" to -1, "ground" to -1, "flying" to 1, "bug" to 1, "steel" to -1),
        "ghost" to mapOf("normal" to -2, "psychic" to 1, "ghost" to 1, "dark" to -1),
        "dragon" to mapOf("dragon" to 1, "steel" to -1, "fairy" to -2),
        "dark" to mapOf("fighting" to -1, "psychic" to 1, "ghost" to 1, "dark" to -1, "fairy" to -1),
        "steel" to mapOf("fire" to -1, "water" to -1, "electric" to -1, "ice" to 1, "rock" to 1, "steel" to -1, "fairy" to 1),
        "fairy" to mapOf("fire" to -1, "fighting" to 1, "poison" to -1, "dragon" to 1, "dark" to 1, "steel" to -1)
    )

    private val allTypes = listOf(
        "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground",
        "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
    )

    data class TeamCoverage(
        val weaknesses: Map<String, Int>, // Type -> Count of team members weak to it
        val resistances: Map<String, Int>, // Type -> Count of team members resistant to it
        val immunities: Map<String, Int>   // Type -> Count of team members immune to it
    )

    fun analyzeCoverage(team: List<PokemonWithDetails>): TeamCoverage {
        val weaknesses = mutableMapOf<String, Int>()
        val resistances = mutableMapOf<String, Int>()
        val immunities = mutableMapOf<String, Int>()

        allTypes.forEach { attackingType ->
            var weakCount = 0
            var resistCount = 0
            var immuneCount = 0

            team.forEach { member ->
                val effectiveness = calculateEffectiveness(attackingType, member.types.map { it.typeName })
                when {
                    effectiveness > 1.0 -> weakCount++
                    effectiveness < 1.0 && effectiveness > 0.0 -> resistCount++
                    effectiveness == 0.0 -> immuneCount++
                }
            }
            if (weakCount > 0) weaknesses[attackingType] = weakCount
            if (resistCount > 0) resistances[attackingType] = resistCount
            if (immuneCount > 0) immunities[attackingType] = immuneCount
        }

        return TeamCoverage(weaknesses, resistances, immunities)
    }

    private fun calculateEffectiveness(attackingType: String, defendingTypes: List<String>): Double {
        var multiplier = 1.0
        val attackerMap = typeChart[attackingType.lowercase()] ?: return 1.0

        defendingTypes.forEach { type ->
            val defenseType = type.lowercase()
            val effect = attackerMap[defenseType] ?: 0 // Default 0 means 1x (normal) in simplified map? No.
            // Wait, my map uses 1, -1, -2.
            // 0 in map means "Normal" (not present in map)
            
            // Correct logic:
            // if type is NOT in map, it's 1x.
            // if type IS in map:
            // 1 -> 2x
            // -1 -> 0.5x
            // -2 -> 0x
            
            multiplier *= when (effect) {
                1 -> 2.0
                -1 -> 0.5
                -2 -> 0.0
                else -> 1.0
            }
        }
        return multiplier
    }
}
