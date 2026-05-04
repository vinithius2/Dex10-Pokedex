package com.vinithius.dex10.datasource.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "move_detail")
data class MoveDetailEntity(
    @PrimaryKey val name: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val typeName: String?,
    val damageClass: String?,
    val priority: Int
)
