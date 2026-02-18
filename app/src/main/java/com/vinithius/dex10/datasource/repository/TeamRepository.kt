package com.vinithius.dex10.datasource.repository

import android.util.Log
import com.vinithius.dex10.datasource.database.TeamDao
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import kotlinx.coroutines.flow.Flow

class TeamRepository(private val teamDao: TeamDao) : ITeamRepository {

    override fun getAllTeams(): Flow<List<TeamWithMembers>> = teamDao.getAllTeamsWithMembers()
    override fun getTeam(id: Int): Flow<TeamWithMembers?> = teamDao.getTeamWithMembers(id)
    override suspend fun createTeam(name: String): Long = teamDao.insertTeam(TeamEntity(name = name))
    override suspend fun updateTeam(team: TeamEntity) = teamDao.updateTeam(team)

    // --- AQUI ESTÁ A CORREÇÃO ---
    override suspend fun deleteTeam(team: TeamEntity) {
        Log.d("TeamRepository", "Starting deletion transaction for team ${team.id}")
        teamDao.clearMembers(team.id)
        val rowsDeleted = teamDao.deleteTeamById(team.id)
        if (rowsDeleted == 0) {
            Log.e("TeamRepository", "ERRO GRAVE: O time ${team.id} não foi encontrado para deleção!")
        } else {
            Log.d("TeamRepository", "Sucesso: Time ${team.id} e seus membros foram removidos.")
        }
    }
    // ---------------------------

    override suspend fun addMember(member: TeamMemberEntity) = teamDao.insertMember(member)
    override suspend fun updateMember(member: TeamMemberEntity) = teamDao.updateMember(member)
    override suspend fun removeMember(member: TeamMemberEntity) = teamDao.deleteMember(member)
    override suspend fun getTeamCount(): Int = teamDao.getTeamCount()
    override suspend fun getMemberCount(teamId: Int): Int = teamDao.getMemberCount(teamId)
}