package com.vinithius.dex10.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.datasource.repository.IPokemonRepository
import com.vinithius.dex10.datasource.repository.ITeamRepository
import com.vinithius.dex10.utils.TeamAnalysisUtils
import com.vinithius.dex10.utils.TeamValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModel(
    private val teamRepository: ITeamRepository,
    private val pokemonRepository: IPokemonRepository,
    val premiumManager: PremiumManager
) : ViewModel() {

    // --- CONTROLE DE ESTADO ---

    // ID do time atual (Gatilho para o Flow)
    private val _currentTeamId = MutableStateFlow<Int?>(null)

    // Lista de times (Blindada contra crashes)
    val teams: StateFlow<List<TeamWithMembers>> = teamRepository.getAllTeams()
        .catch { e ->
            Log.e("TeamViewModel", "Error loading teams list", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Time Selecionado (Reativo: Muda automaticamente quando o ID muda)
    val selectedTeam: StateFlow<TeamWithMembers?> = _currentTeamId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else teamRepository.getTeam(id)
        }
        .onEach { team ->
            // Dispara análise em paralelo (IO) para NÃO bloquear a UI
            if (team != null) {
                // Launch em separado garante que o fluxo do banco continue livre
                viewModelScope.launch(Dispatchers.IO) {
                    performAnalysis(team)
                }
            } else {
                // Limpa análise se o time for nulo
                _teamAnalysis.value = null
                _teamIssues.value = emptyList()
            }
        }
        .catch { e ->
            Log.e("TeamViewModel", "Error in selectedTeam flow", e)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _teamAnalysis = MutableStateFlow<TeamAnalysisUtils.TeamCoverage?>(null)
    val teamAnalysis: StateFlow<TeamAnalysisUtils.TeamCoverage?> = _teamAnalysis.asStateFlow()

    private val _pokemonDetails = MutableStateFlow<Map<Int, com.vinithius.dex10.datasource.database.PokemonWithDetails>>(emptyMap())
    val pokemonDetails: StateFlow<Map<Int, com.vinithius.dex10.datasource.database.PokemonWithDetails>> = _pokemonDetails.asStateFlow()

    private val _teamIssues = MutableStateFlow<List<TeamValidator.ValidationIssue>>(emptyList())
    val teamIssues: StateFlow<List<TeamValidator.ValidationIssue>> = _teamIssues.asStateFlow()

    private val _showUpsell = MutableStateFlow(false)
    val showUpsell: StateFlow<Boolean> = _showUpsell.asStateFlow()

    // --- AÇÕES ---

    // Chamado pela UI ao entrar na tela
    fun selectTeam(teamId: Int) {
        _currentTeamId.value = teamId
    }

    // Lógica pesada isolada em IO
    private suspend fun performAnalysis(team: TeamWithMembers) {
        try {
            val pokemonIds = team.members.map { it.pokemonId }

            // Busca detalhes (protegido contra falhas individuais)
            val membersDetails = pokemonIds.mapNotNull { id ->
                try {
                    pokemonRepository.getPokemonWithDetailsById(id)
                } catch (e: Exception) {
                    Log.e("TeamViewModel", "Failed to fetch details for $id", e)
                    null
                }
            }

            _pokemonDetails.value = membersDetails.associateBy { it.pokemon.id }

            // Cálculos
            val analysis = TeamAnalysisUtils.analyzeCoverage(membersDetails)
            _teamAnalysis.value = analysis

            val issues = TeamValidator.validateTeam(team)
            _teamIssues.value = issues

        } catch (e: Exception) {
            Log.e("TeamViewModel", "Global Analysis failed", e)
        }
    }

    // --- CRUD (Sempre em Dispatchers.IO) ---

    fun createTeam(name: String, onFinished: (Long?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Checagem segura de Premium
                val isPremium = premiumManager.isPremium.value
                val count = teamRepository.getTeamCount()

                if (!isPremium && count >= PremiumManager.FREE_TEAM_LIMIT) {
                    _showUpsell.value = true
                    withContext(Dispatchers.Main) { onFinished(null) }
                    return@launch
                }

                val newId = teamRepository.createTeam(name)
                withContext(Dispatchers.Main) { onFinished(newId) }
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error creating team", e)
                withContext(Dispatchers.Main) { onFinished(null) }
            }
        }
    }

    init {
        Log.d("TeamViewModel", "Initialized: $this")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TeamViewModel", "onCleared: $this")
    }

    fun deleteTeam(team: TeamEntity) {
        Log.d("TeamViewModel", "deleteTeam called for ${team.id}. Scope active: ${viewModelScope.isActive}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("TeamViewModel", "Requesting deletion for team ${team.id}...")
                withContext(kotlinx.coroutines.NonCancellable) {
                    teamRepository.deleteTeam(team)
                }
                Log.d("TeamViewModel", "Deletion request completed for team ${team.id}")
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error deleting team ${team.id}", e)
            }
        }
    }

    fun addMember(teamId: Int, pokemonId: Int, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                teamRepository.addMember(TeamMemberEntity(teamId = teamId, pokemonId = pokemonId, position = position))
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error adding member", e)
            }
        }
    }

    fun removeMember(member: TeamMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                teamRepository.removeMember(member)
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error removing member", e)
            }
        }
    }

    fun updateMember(member: TeamMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                teamRepository.updateMember(member)
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error updating member", e)
            }
        }
    }

    fun renameTeam(team: TeamEntity, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try { teamRepository.updateTeam(team.copy(name = newName)) } catch (e: Exception) {}
        }
    }

    fun updateTeamFormat(team: TeamEntity, format: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try { teamRepository.updateTeam(team.copy(format = format)) } catch (e: Exception) {}
        }
    }

    fun updateTeamNotes(team: TeamEntity, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try { teamRepository.updateTeam(team.copy(notes = notes)) } catch (e: Exception) {}
        }
    }

    fun triggerUpsell() { _showUpsell.value = true }
    fun dismissUpsell() { _showUpsell.value = false }
}