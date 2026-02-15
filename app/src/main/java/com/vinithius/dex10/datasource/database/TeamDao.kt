package com.vinithius.dex10.datasource.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface TeamDao {
    @Insert
    suspend fun insertTeam(team: TeamEntity): Long

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Delete
    suspend fun deleteTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TeamMemberEntity)

    @Update
    suspend fun updateMember(member: TeamMemberEntity)

    @Delete
    suspend fun deleteMember(member: TeamMemberEntity)

    @Query("DELETE FROM team_member WHERE team_id = :teamId")
    suspend fun clearMembers(teamId: Int)

    @Transaction
    @Query("SELECT * FROM team ORDER BY created_at DESC")
    suspend fun getAllTeamsWithMembers(): List<TeamWithMembers>

    @Transaction
    @Query("SELECT * FROM team WHERE id = :id")
    suspend fun getTeamWithMembers(id: Int): TeamWithMembers?

    @Query("SELECT COUNT(*) FROM team")
    suspend fun getTeamCount(): Int

    @Query("SELECT COUNT(*) FROM team_member WHERE team_id = :teamId")
    suspend fun getMemberCount(teamId: Int): Int
}
