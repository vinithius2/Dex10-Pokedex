package com.vinithius.dex10.datasource.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "format") val format: String? = "6v6",
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

data class TeamWithMembers(
    @androidx.room.Embedded val team: TeamEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "team_id"
    )
    val members: List<TeamMemberEntity>
)




