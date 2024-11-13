package com.vinithius.poke10.extension

import com.vinithius.poke10.datasource.database.PokemonWithDetails
import com.vinithius.poke10.datasource.response.Abilities
import com.vinithius.poke10.datasource.response.BaseSprite
import com.vinithius.poke10.datasource.response.Default
import com.vinithius.poke10.datasource.response.Other
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.datasource.response.Sprites
import com.vinithius.poke10.datasource.response.Stat
import com.vinithius.poke10.datasource.response.Type

fun Pokemon.updateWithLocalData(pokemonWithDetails: PokemonWithDetails): Pokemon {
    this.id = pokemonWithDetails.pokemon.id
    this.types = pokemonWithDetails.types.map { Type(it.id, Default(it.typeName, String())) }
    this.abilities = pokemonWithDetails.abilities.map { ability ->
        Abilities(
            ability = Default(ability.name, String()),
            is_hidden = ability.isHidden,
            slot = ability.slot
        )
    }
    this.stats = pokemonWithDetails.stats.map { stat ->
        Stat(
            stat = Default(stat.name.value, String()),
            base_stat = stat.baseStat,
            effort = stat.effort
        )
    }
    this.sprites = Sprites(
        front_default = pokemonWithDetails.pokemon.imagePath,
        back_female = null,
        back_shiny = null,
        back_shiny_female = null,
        back_default = null,
        front_female = null,
        front_shiny = null,
        front_shiny_female = null,
        other = null,
    )
    return this
}
