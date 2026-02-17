package com.vinithius.dex10.datasource.database

object EntityMock {
    fun createTeam(id: Int, name: String): TeamEntity {
        return TeamEntity(
            id = id,
            name = name,
            format = "6v6",
            notes = null,
            createdAt = System.currentTimeMillis()
        )
    }
}
