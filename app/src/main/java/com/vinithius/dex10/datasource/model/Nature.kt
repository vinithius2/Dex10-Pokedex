package com.vinithius.dex10.datasource.model

/**
 * Pokémon natures and their stat modifiers
 * Based on competitive Pokémon mechanics
 */
enum class Nature(
    val displayName: String,
    val increasedStat: StatType?,
    val decreasedStat: StatType?
) {
    // Neutral natures (no stat changes)
    HARDY("Hardy", null, null),
    DOCILE("Docile", null, null),
    SERIOUS("Serious", null, null),
    BASHFUL("Bashful", null, null),
    QUIRKY("Quirky", null, null),
    
    // Attack+ natures
    LONELY("Lonely", StatType.ATK, StatType.DEF),
    BRAVE("Brave", StatType.ATK, StatType.SPE),
    ADAMANT("Adamant", StatType.ATK, StatType.SPA),
    NAUGHTY("Naughty", StatType.ATK, StatType.SPD),
    
    // Defense+ natures
    BOLD("Bold", StatType.DEF, StatType.ATK),
    RELAXED("Relaxed", StatType.DEF, StatType.SPE),
    IMPISH("Impish", StatType.DEF, StatType.SPA),
    LAX("Lax", StatType.DEF, StatType.SPD),
    
    // Special Attack+ natures
    MODEST("Modest", StatType.SPA, StatType.ATK),
    MILD("Mild", StatType.SPA, StatType.DEF),
    QUIET("Quiet", StatType.SPA, StatType.SPE),
    RASH("Rash", StatType.SPA, StatType.SPD),
    
    // Special Defense+ natures
    CALM("Calm", StatType.SPD, StatType.ATK),
    GENTLE("Gentle", StatType.SPD, StatType.DEF),
    SASSY("Sassy", StatType.SPD, StatType.SPE),
    CAREFUL("Careful", StatType.SPD, StatType.SPA),
    
    // Speed+ natures
    TIMID("Timid", StatType.SPE, StatType.ATK),
    HASTY("Hasty", StatType.SPE, StatType.DEF),
    JOLLY("Jolly", StatType.SPE, StatType.SPA),
    NAIVE("Naive", StatType.SPE, StatType.SPD);
    
    fun getModifier(stat: StatType): Double {
        return when {
            stat == increasedStat -> 1.1
            stat == decreasedStat -> 0.9
            else -> 1.0
        }
    }
    
    fun getDescription(): String {
        return when {
            increasedStat == null && decreasedStat == null -> "No stat changes"
            else -> "+${increasedStat?.displayName}, -${decreasedStat?.displayName}"
        }
    }
    
    companion object {
        /**
         * Get recommended natures for common roles
         */
        fun getRecommendedForRole(role: String): List<Nature> {
            return when (role.lowercase()) {
                "sweeper", "attacker" -> listOf(JOLLY, ADAMANT, TIMID, MODEST)
                "tank", "wall" -> listOf(BOLD, IMPISH, CALM, CAREFUL)
                "support" -> listOf(BOLD, CALM, TIMID)
                else -> values().filter { it.increasedStat != null }.take(5)
            }
        }
    }
}

enum class StatType(val displayName: String, val shortName: String) {
    HP("HP", "HP"),
    ATK("Attack", "Atk"),
    DEF("Defense", "Def"),
    SPA("Sp. Attack", "SpA"),
    SPD("Sp. Defense", "SpD"),
    SPE("Speed", "Spe")
}
