package com.vinithius.dex10.datasource.repository

import com.vinithius.dex10.datasource.database.TeamDao
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers

class TeamRepository(private val teamDao: TeamDao) : ITeamRepository {
    override suspend fun getAllTeams(): List<TeamWithMembers> {
        return teamDao.getAllTeamsWithMembers()
    }

    override suspend fun getTeam(id: Int): TeamWithMembers? {
        return teamDao.getTeamWithMembers(id)
    }

    override suspend fun createTeam(name: String): Long {
        return teamDao.insertTeam(TeamEntity(name = name))
    }

    override suspend fun updateTeam(team: TeamEntity) {
        teamDao.updateTeam(team)
    }

    override suspend fun deleteTeam(team: TeamEntity) {
        teamDao.deleteTeam(team)
    }

    override suspend fun addMember(member: TeamMemberEntity) {
        teamDao.insertMember(member)
    }

    override suspend fun updateMember(member: TeamMemberEntity) {
        teamDao.updateMember(member)
    }

    override suspend fun removeMember(member: TeamMemberEntity) {
        teamDao.deleteMember(member)
    }

    override suspend fun getTeamCount(): Int {
        return teamDao.getTeamCount()
    }

    override suspend fun getMemberCount(teamId: Int): Int {
        return teamDao.getMemberCount(teamId)
    }
}
