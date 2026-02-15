package com.vinithius.dex10.datasource.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "team_member",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PokemonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pokemon_id"]
        )
    ],
    indices = [Index("team_id"), Index("pokemon_id")]
)
data class TeamMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "team_id") val teamId: Int,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "position") val position: Int, // 0-5
    @ColumnInfo(name = "nickname") val nickname: String? = null,
    @ColumnInfo(name = "ability") val ability: String? = null,
    @ColumnInfo(name = "nature") val nature: String? = null,
    @ColumnInfo(name = "item") val item: String? = null,
    @ColumnInfo(name = "move1") val move1: String? = null,
    @ColumnInfo(name = "move2") val move2: String? = null,
    @ColumnInfo(name = "move3") val move3: String? = null,
    @ColumnInfo(name = "move4") val move4: String? = null,
    @ColumnInfo(name = "ivs") val ivs: String? = null, // JSON: {"hp":31,"atk":31,"def":31,"spa":31,"spd":31,"spe":31}
    @ColumnInfo(name = "evs") val evs: String? = null  // JSON: {"hp":252,"atk":0,"def":0,"spa":252,"spd":4,"spe":0}
)
