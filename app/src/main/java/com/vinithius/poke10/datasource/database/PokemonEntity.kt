package com.vinithius.poke10.datasource.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class StatType(val value: String) {
    HP("hp"),
    ATTACK("attack"),
    DEFENSE("defense"),
    SPECIAL_ATTACK("special-attack"),
    SPECIAL_DEFENSE("special-defense"),
    SPEED("speed")
}

@Entity(
    tableName = "stat",
    indices = [Index(value = ["name"])] // Para melhorar buscas por nome, caso necessário
)
data class Stat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: StatType, // ENUM para representar os nomes dos stats
    @ColumnInfo(name = "base_stat") val baseStat: Int,
    @ColumnInfo(name = "effort") val effort: Int
)

@Entity(
    tableName = "ability",
    indices = [Index(value = ["name"])] // Para melhorar buscas por nome, caso necessário
)
data class Ability(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "is_hidden") val isHidden: Boolean,
    @ColumnInfo(name = "slot") val slot: Int
)

@Entity(
    tableName = "type",
    indices = [Index(value = ["type_name"])] // Para melhorar buscas por tipo
)
data class Type(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "type_name") val typeName: String
)

@Entity(
    tableName = "pokemon",
    indices = [Index(value = ["name"])] // Para melhorar buscas por nome de Pokémon
)
data class PokemonEntity(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "habitat") val habitat: String,
    @ColumnInfo(name = "favorite") var favorite: Boolean = false,
    @ColumnInfo(name = "image_path") val imagePath: String?
)

// Relações --->

@Entity(
    tableName = "pokemon_type",
    indices = [
        Index(value = ["pokemon_id"]),
        Index(value = ["type_id"])
    ]
)
data class PokemonType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "type_id") val typeId: Int
)

@Entity(
    tableName = "pokemon_stat",
    indices = [
        Index(value = ["pokemon_id"]),
        Index(value = ["stat_id"])
    ]
)
data class PokemonStat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "stat_id") val statId: Int
)

@Entity(
    tableName = "pokemon_ability",
    indices = [
        Index(value = ["pokemon_id"]),
        Index(value = ["ability_id"])
    ]
)
data class PokemonAbility(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "ability_id") val abilityId: Int
)

data class PokemonWithDetails(
    @Embedded val pokemon: PokemonEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = Type::class,
        associateBy = Junction(
            value = PokemonType::class,
            parentColumn = "pokemon_id",
            entityColumn = "type_id"
        )
    )
    val types: List<Type>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = Ability::class,
        associateBy = Junction(
            value = PokemonAbility::class,
            parentColumn = "pokemon_id",
            entityColumn = "ability_id"
        )
    )
    val abilities: List<Ability>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = Stat::class,
        associateBy = Junction(
            value = PokemonStat::class,
            parentColumn = "pokemon_id",
            entityColumn = "stat_id"
        )
    )
    val stats: List<Stat>
)

