package com.vinithius.poke10x.datasource.mapper

import com.vinithius.poke10x.datasource.database.Ability
import com.vinithius.poke10x.datasource.database.PokemonEntity
import com.vinithius.poke10x.datasource.database.Stat
import com.vinithius.poke10x.datasource.database.StatType
import com.vinithius.poke10x.datasource.response.Abilities
import com.vinithius.poke10x.datasource.response.Chain
import com.vinithius.poke10x.datasource.response.Characteristic
import com.vinithius.poke10x.datasource.response.Damage
import com.vinithius.poke10x.datasource.response.DamageRelations
import com.vinithius.poke10x.datasource.response.Default
import com.vinithius.poke10x.datasource.response.Description
import com.vinithius.poke10x.datasource.response.EvolutionChain
import com.vinithius.poke10x.datasource.response.Location
import com.vinithius.poke10x.datasource.response.Pokemon
import com.vinithius.poke10x.datasource.response.Specie
import com.vinithius.poke10x.datasource.response.Sprites
import com.vinithius.poke10x.datasource.response.Stat as StatResponse
import com.vinithius.poke10x.datasource.response.Type as TypeResponse
import com.vinithius.poke10x.datasource.database.Type as TypeEntity

fun Pokemon.toEntity(): PokemonEntity {
    return PokemonEntity(
        id = id ?: 0,
        name = name,
        imagePath = sprites?.front_default,
        color = color ?: "unknow",
        habitat = habitat ?: "unknow",
    )
}

fun Pokemon.toStatEntities(): List<Stat>? {
    return stats?.map { stat ->
        Stat(
            name = StatType.valueOf(stat.stat.name?.uppercase()?.replace("-", "_")!!),
            baseStat = stat.base_stat,
            effort = stat.effort
        )
    }
}

fun Pokemon.toAbilityEntities(): List<Ability>? {
    return abilities?.map { ability ->
        Ability(
            name = ability.ability.name ?: String(),
            isHidden = ability.is_hidden,
            slot = ability.slot
        )
    }
}

fun Pokemon.toTypeEntities(): List<TypeEntity>? {
    return types?.map { type ->
        TypeEntity(typeName = type.type.name ?: String())
    }
}

fun HashMap<*, *>.toPokemon(): Pokemon {
    val stats = (this["stats"] as? List<HashMap<*, *>>)?.map {
        StatResponse(
            stat = Default(it["name"] as? String ?: String(), null),
            base_stat = (it["base_stat"] as? Long)?.toInt() ?: 0,
            effort = (it["effort"] as? Long)?.toInt() ?: 0
        )
    } ?: emptyList()
    val types = (this["types"] as? List<HashMap<*, *>>)?.map {
        TypeResponse(
            slot = null,
            type = Default(it["type_name"] as? String ?: String(), null)
        )
    } ?: emptyList()
    val abilities = (this["abilities"] as? List<HashMap<*, *>>)?.map {
        Abilities(
            is_hidden = it["is_hidden"] as? Boolean ?: false,
            slot = (it["slot"] as? Long)?.toInt() ?: 0,
            ability = Default(it["name"] as? String ?: String(), null)
        )
    } ?: emptyList()

    val encounters = (this["encounters"] as? List<String>)?.map {
        Location(location_area = Default(it, null), version_details = null)
    } ?: emptyList()

    val evolution = EvolutionChain(
        chain = Chain(
            is_baby = false,
            species = Default(this["evolution"] as? String ?: "", null),
            evolution_details = null,
            evolves_to = null
        ),
        baby_trigger_item = null,
        id = 0
    )

    val characteristic = Characteristic(
        descriptions = listOf(
            Description(
                description = this["characteristic"] as? String ?: "",
                language = Default("", null) // Adicionar traduções depois
            )
        ),
        gene_modulo = 0,
        highest_stat = Default("", null),
        id = 0,
        possible_values = emptyList()
    )
    val specie = Specie(
        base_happiness = null,
        capture_rate = null,
        color = null,
        egg_groups = null,
        evolution_chain = null,
        evolves_from_species = null,
        flavor_text_entries = null,
        forms_switchable = null,
        gender_rate = null,
        generation = null,
        growth_rate = null,
        habitat = null,
        has_gender_differences = null,
        hatch_counter = null,
        id = null,
        is_baby = null,
        is_legendary = null,
        is_mythical = null,
        name = this["specie"] as String,
        order = null,
        pal_park_encounters = null,
        pokedex_numbers = null,
        shape = null,
        varieties = null
    )
    val sprites = Sprites(
        front_default = this["sprites"] as String?,
        back_default = null,
        back_female = null,
        back_shiny = null,
        back_shiny_female = null,
        front_female = null,
        front_shiny = null,
        front_shiny_female = null,
        other = null,
    )
    val damage = listOf(
        Damage(
            type = null,
            damage_relations = DamageRelations(
                effective_damage_from = ((this["damage"] as HashMap<*, *>)["double_damage_from"] as List<String>).map {
                    Default(
                        it,
                        null
                    )
                },
                effective_damage_to = (this["damage"] as HashMap<*, *>)["double_damage_to"]?.run {
                    (this as List<String>).map {
                        Default(
                            it,
                            null
                        )
                    }
                },
                ineffective_damage_from = emptyList(),
                ineffective_damage_to = emptyList(),
                no_damage_from = emptyList(),
                no_damage_to = emptyList()
            )
        )
    )

    return Pokemon(
        id = this["id"].toString().toInt(),
        name = this["name"].toString(),
        url = null,
        height = this["height"].toString().toInt(),
        weight = this["weight"].toString().toInt(),
        base_experience = this["base_experience"].toString().toInt(),
        stats = stats,
        types = types,
        abilities = abilities,
        sprites = sprites,
        encounters = encounters,
        evolution = evolution,
        characteristic = characteristic,
        specie = specie,
        damage = damage,
        favorite = this["favorite"].toString().toBoolean(),
        color = this["color"].toString(),
        habitat = this["habitat"].toString(),
    )
}

fun Damage.toType(): TypeResponse {
    val name = this.type?.name ?: String()
    val url = this.type?.url ?: String()
    return TypeResponse(slot = null, type = Default(name, url))
}

fun List<Damage>.fromDamageToListType(): List<TypeResponse> {
    return this.map { it.toType() }
}

fun Default.toType(): TypeResponse {
    val name = this.name ?: String()
    val url = this.url ?: String()
    return TypeResponse(slot = null, type = Default(name, url))
}

fun List<Default>.fromDefaultToListType(): List<TypeResponse> {
    return this.map { it.toType() }
}