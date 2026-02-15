package com.vinithius.dex10.datasource.model

import org.json.JSONObject

/**
 * Individual Values - Pokemon genetics (0-31)
 */
data class IVs(
    val hp: Int = 31,
    val atk: Int = 31,
    val def: Int = 31,
    val spa: Int = 31,  // Special Attack
    val spd: Int = 31,  // Special Defense
    val spe: Int = 31   // Speed
) {
    init {
        require(hp in 0..31) { "HP IV must be between 0 and 31" }
        require(atk in 0..31) { "ATK IV must be between 0 and 31" }
        require(def in 0..31) { "DEF IV must be between 0 and 31" }
        require(spa in 0..31) { "SPA IV must be between 0 and 31" }
        require(spd in 0..31) { "SPD IV must be between 0 and 31" }
        require(spe in 0..31) { "SPE IV must be between 0 and 31" }
    }

    fun toJson(): String {
        return JSONObject().apply {
            put("hp", hp)
            put("atk", atk)
            put("def", def)
            put("spa", spa)
            put("spd", spd)
            put("spe", spe)
        }.toString()
    }

    companion object {
        fun fromJson(json: String?): IVs {
            if (json.isNullOrEmpty()) return IVs() // Default 31/31/31/31/31/31
            return try {
                val obj = JSONObject(json)
                IVs(
                    hp = obj.optInt("hp", 31),
                    atk = obj.optInt("atk", 31),
                    def = obj.optInt("def", 31),
                    spa = obj.optInt("spa", 31),
                    spd = obj.optInt("spd", 31),
                    spe = obj.optInt("spe", 31)
                )
            } catch (e: Exception) {
                IVs() // Default on error
            }
        }
    }
}

/**
 * Effort Values - Pokemon training (0-252 per stat, max 510 total)
 */
data class EVs(
    val hp: Int = 0,
    val atk: Int = 0,
    val def: Int = 0,
    val spa: Int = 0,
    val spd: Int = 0,
    val spe: Int = 0
) {
    val total: Int = hp + atk + def + spa + spd + spe

    init {
        require(hp in 0..252) { "HP EV must be between 0 and 252" }
        require(atk in 0..252) { "ATK EV must be between 0 and 252" }
        require(def in 0..252) { "DEF EV must be between 0 and 252" }
        require(spa in 0..252) { "SPA EV must be between 0 and 252" }
        require(spd in 0..252) { "SPD EV must be between 0 and 252" }
        require(spe in 0..252) { "SPE EV must be between 0 and 252" }
        require(total <= 510) { "Total EVs must not exceed 510 (current: $total)" }
    }

    fun toJson(): String {
        return JSONObject().apply {
            put("hp", hp)
            put("atk", atk)
            put("def", def)
            put("spa", spa)
            put("spd", spd)
            put("spe", spe)
        }.toString()
    }

    companion object {
        fun fromJson(json: String?): EVs {
            if (json.isNullOrEmpty()) return EVs() // Default 0/0/0/0/0/0
            return try {
                val obj = JSONObject(json)
                EVs(
                    hp = obj.optInt("hp", 0),
                    atk = obj.optInt("atk", 0),
                    def = obj.optInt("def", 0),
                    spa = obj.optInt("spa", 0),
                    spd = obj.optInt("spd", 0),
                    spe = obj.optInt("spe", 0)
                )
            } catch (e: Exception) {
                EVs() // Default on error
            }
        }

        // Common EV spreads
        val SWEEPER_PHYSICAL = EVs(hp = 4, atk = 252, def = 0, spa = 0, spd = 0, spe = 252)
        val SWEEPER_SPECIAL = EVs(hp = 4, atk = 0, def = 0, spa = 252, spd = 0, spe = 252)
        val WALL_PHYSICAL = EVs(hp = 252, atk = 0, def = 252, spa = 0, spd = 4, spe = 0)
        val WALL_SPECIAL = EVs(hp = 252, atk = 0, def = 4, spa = 0, spd = 252, spe = 0)
        val BULKY_ATTACKER = EVs(hp = 252, atk = 252, def = 4, spa = 0, spd = 0, spe = 0)
    }
}
