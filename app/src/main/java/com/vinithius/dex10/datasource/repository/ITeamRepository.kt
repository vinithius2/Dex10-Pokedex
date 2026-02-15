package com.vinithius.dex10.datasource.repository

import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers

interface ITeamRepository {
    suspend fun getAllTeams(): List<TeamWithMembers>
    suspend fun getTeam(id: Int): TeamWithMembers?
    suspend fun createTeam(name: String): Long
    suspend fun updateTeam(team: TeamEntity)
    suspend fun deleteTeam(team: TeamEntity)
    suspend fun addMember(member: TeamMemberEntity)
    suspend fun updateMember(member: TeamMemberEntity)
    suspend fun removeMember(member: TeamMemberEntity)
    suspend fun getTeamCount(): Int
    suspend fun getMemberCount(teamId: Int): Int
}
