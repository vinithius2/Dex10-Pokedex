package com.vinithius.dex10.datasource.repository

import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import kotlinx.coroutines.flow.Flow

interface ITeamRepository {
    fun getAllTeams(): Flow<List<TeamWithMembers>>
    fun getTeam(id: Int): Flow<TeamWithMembers?>
    suspend fun createTeam(name: String): Long
    suspend fun updateTeam(team: TeamEntity)
    suspend fun deleteTeam(team: TeamEntity)
    suspend fun addMember(member: TeamMemberEntity)
    suspend fun updateMember(member: TeamMemberEntity)
    suspend fun removeMember(member: TeamMemberEntity)
    suspend fun getTeamCount(): Int
    suspend fun getMemberCount(teamId: Int): Int
}
