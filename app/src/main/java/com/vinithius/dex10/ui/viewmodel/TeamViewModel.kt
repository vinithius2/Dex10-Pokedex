package com.vinithius.dex10.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.datasource.repository.IPokemonRepository
import com.vinithius.dex10.datasource.repository.ITeamRepository
import com.vinithius.dex10.utils.TeamAnalysisUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: ITeamRepository,
    private val pokemonRepository: IPokemonRepository,
    val premiumManager: PremiumManager
) : ViewModel() {

    private val _teams = MutableStateFlow<List<TeamWithMembers>>(emptyList())
    val teams: StateFlow<List<TeamWithMembers>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow<TeamWithMembers?>(null)
    val selectedTeam: StateFlow<TeamWithMembers?> = _selectedTeam.asStateFlow()

    private val _teamAnalysis = MutableStateFlow<TeamAnalysisUtils.TeamCoverage?>(null)
    val teamAnalysis: StateFlow<TeamAnalysisUtils.TeamCoverage?> = _teamAnalysis.asStateFlow()

    private val _pokemonDetails = MutableStateFlow<Map<Int, com.vinithius.dex10.datasource.database.PokemonWithDetails>>(emptyMap())
    val pokemonDetails: StateFlow<Map<Int, com.vinithius.dex10.datasource.database.PokemonWithDetails>> = _pokemonDetails.asStateFlow()

    private val _showUpsell = MutableStateFlow(false)
    val showUpsell: StateFlow<Boolean> = _showUpsell.asStateFlow()

    init {
        loadTeams()
    }

    fun loadTeams() {
        viewModelScope.launch {
            _teams.value = teamRepository.getAllTeams()
        }
    }

    fun selectTeam(teamId: Int) {
        viewModelScope.launch {
            val team = teamRepository.getTeam(teamId)
            _selectedTeam.value = team
            team?.let {
                analyzeTeam(it)
            }
        }
    }

    private fun analyzeTeam(team: TeamWithMembers) {
        viewModelScope.launch {
            val pokemonIds = team.members.map { it.pokemonId }
            // Fetch details for all team members to get their Types
            val membersDetails = pokemonIds.mapNotNull { 
                pokemonRepository.getPokemonWithDetailsById(it) 
            }
            _pokemonDetails.value = membersDetails.associateBy { it.pokemon.id }
            _teamAnalysis.value = TeamAnalysisUtils.analyzeCoverage(membersDetails)
        }
    }

    fun createTeam(name: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val isPremium = premiumManager.isPremium.value
            val currentCount = teamRepository.getTeamCount()
            
            if (!isPremium && currentCount >= PremiumManager.FREE_TEAM_LIMIT) {
                _showUpsell.value = true
                return@launch
            }

            val newId = teamRepository.createTeam(name)
            loadTeams()
            onSuccess(newId)
        }
    }
    
    fun triggerUpsell() {
        _showUpsell.value = true
    }

    fun dismissUpsell() {
        _showUpsell.value = false
    }

    fun deleteTeam(team: TeamEntity) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TeamViewModel", "Deleting team: ${team.id} - ${team.name}")
                teamRepository.deleteTeam(team)
                loadTeams()
                if (_selectedTeam.value?.team?.id == team.id) {
                    _selectedTeam.value = null
                    _teamAnalysis.value = null
                }
                android.util.Log.d("TeamViewModel", "Team deleted successfully")
            } catch (e: Exception) {
                android.util.Log.e("TeamViewModel", "Error deleting team: ${team.id}", e)
            }
        }
    }
    
    fun renameTeam(team: TeamEntity, newName: String) {
        viewModelScope.launch {
            teamRepository.updateTeam(team.copy(name = newName))
            loadTeams()
            if (_selectedTeam.value?.team?.id == team.id) {
                selectTeam(team.id)
            }
        }
    }

    fun updateTeamFormat(team: TeamEntity, format: String) {
        viewModelScope.launch {
            teamRepository.updateTeam(team.copy(format = format))
            loadTeams()
            if (_selectedTeam.value?.team?.id == team.id) {
                selectTeam(team.id)
            }
        }
    }

    fun updateTeamNotes(team: TeamEntity, notes: String) {
        viewModelScope.launch {
            teamRepository.updateTeam(team.copy(notes = notes))
            loadTeams()
            if (_selectedTeam.value?.team?.id == team.id) {
                selectTeam(team.id)
            }
        }
    }

    fun addMember(teamId: Int, pokemonId: Int, position: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TeamViewModel", "Adding member: pokemonId=$pokemonId, position=$position to team=$teamId")
                val member = TeamMemberEntity(
                    teamId = teamId, 
                    pokemonId = pokemonId, 
                    position = position
                )
                teamRepository.addMember(member)
                selectTeam(teamId)
                loadTeams() // Refresh list preview
            } catch (e: Exception) {
                android.util.Log.e("TeamViewModel", "Error adding member", e)
            }
        }
    }

    fun removeMember(member: TeamMemberEntity) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TeamViewModel", "Removing member: id=${member.id}, pos=${member.position}, pokemonId=${member.pokemonId}")
                teamRepository.removeMember(member)
                android.util.Log.d("TeamViewModel", "Member removed from repository")
                selectTeam(member.teamId)
                loadTeams()
                android.util.Log.d("TeamViewModel", "Team re-selected and teams loaded")
            } catch (e: Exception) {
                android.util.Log.e("TeamViewModel", "Error removing member", e)
            }
        }
    }

    fun updateMember(member: TeamMemberEntity) {
         viewModelScope.launch {
             teamRepository.updateMember(member)
             selectTeam(member.teamId)
         }
    }
}
