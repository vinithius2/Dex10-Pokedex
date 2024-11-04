package com.vinithius.poke10.datasource.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "EVOLUTION")
data class Evolution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "stage") val stage: String
)

@Entity(tableName = "TYPE")
data class Type(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "type_name") val typeName: String
)

@Entity(
    tableName = "POKEMON",
    foreignKeys = [
        ForeignKey(
            entity = Type::class,
            parentColumns = ["id"],
            childColumns = ["type_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Evolution::class,
            parentColumns = ["id"],
            childColumns = ["evolution_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class PokemonCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type_id", index = true) val typeId: Int?,
    @ColumnInfo(name = "evolution_id", index = true) val evolutionId: Int?,
    @ColumnInfo(name = "image_path") val imagePath: String?
)
