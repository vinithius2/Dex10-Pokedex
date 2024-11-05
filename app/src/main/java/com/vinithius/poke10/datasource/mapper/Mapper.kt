package com.vinithius.poke10.datasource.mapper

import com.vinithius.poke10.datasource.database.Ability
import com.vinithius.poke10.datasource.database.PokemonEntity
import com.vinithius.poke10.datasource.database.Stat
import com.vinithius.poke10.datasource.database.StatType
import com.vinithius.poke10.datasource.database.Type
import com.vinithius.poke10.datasource.response.Pokemon

fun Pokemon.toEntity(): PokemonEntity {
    return PokemonEntity(
        id = id ?: 0,
        name = name,
        imagePath = sprites?.other?.showdown?.front_default
    )
}

fun Pokemon.toStatEntities(): List<Stat>? {
    return stats?.map { stat ->
        Stat(
            name = StatType.valueOf(stat.stat.name.uppercase()),
            baseStat = stat.base_stat,
            effort = stat.effort
        )
    }
}

fun Pokemon.toAbilityEntities(): List<Ability>? {
    return abilities?.map { ability ->
        Ability(
            name = ability.ability.name,
            isHidden = ability.is_hidden,
            slot = ability.slot
        )
    }
}

fun Pokemon.toTypeEntities(): List<Type>? {
    return types?.map { type ->
        Type(typeName = type.type.name)
    }

}