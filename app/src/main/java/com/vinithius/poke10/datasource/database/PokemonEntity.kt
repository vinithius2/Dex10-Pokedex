package com.vinithius.poke10.datasource.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StatType(val value: String) {
    HP("hp"),
    ATTACK("attack"),
    DEFENSE("defense"),
    SPECIAL_ATTACK("special-attack"),
    SPECIAL_DEFENSE("special-defense"),
    SPEED("speed")
}

@Entity(tableName = "stat")
data class Stat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: StatType, // ENUM para representar os nomes dos stats
    @ColumnInfo(name = "base_stat") val baseStat: Int,
    @ColumnInfo(name = "effort") val effort: Int
)

@Entity(tableName = "ability")
data class Ability(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "is_hidden") val isHidden: Boolean,
    @ColumnInfo(name = "slot") val slot: Int
)

@Entity(tableName = "type")
data class Type(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "type_name") val typeName: String
)

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "image_path") val imagePath: String?
)

// Relações --->

// Tabela para detalhar a relação entre Pokémon e o seu tipo
@Entity(tableName = "pokemon_type")
data class PokemonType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "type_id") val typeId: Int
)

// Tabela para detalhar a relação entre Pokémon e suas estatísticas
@Entity(tableName = "pokemon_stat")
data class PokemonStat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "stat_id") val statId: Int
)

// Tabela para detalhar a relação entre Pokémon e suas habilidades
@Entity(tableName = "pokemon_ability")
data class PokemonAbility(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "ability_id") val abilityId: Int
)
