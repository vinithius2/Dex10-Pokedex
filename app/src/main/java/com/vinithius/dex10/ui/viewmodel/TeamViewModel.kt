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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModel(
    private val teamRepository: ITeamRepository,
    private val pokemonRepository: IPokemonRepository,
    val premiumManager: PremiumManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
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
            // Cancels previous analysis before starting a new one
            analysisJob?.cancel()
            if (team != null) {
                analysisJob = viewModelScope.launch(dispatcher) {
                    performAnalysis(team)
                }
            } else {
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

    private var analysisJob: Job? = null

    // --- AÇÕES ---

    // Chamado pela UI ao entrar na tela
    fun selectTeam(teamId: Int) {
        _currentTeamId.value = teamId
    }

    // Lógica pesada isolada em IO
    private suspend fun performAnalysis(team: TeamWithMembers) {
        try {
            val pokemonIds = team.members.map { it.pokemonId }

            val membersDetails = pokemonRepository.getPokemonWithDetailsByIds(pokemonIds)

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

    // --- CRUD (Sempre em dispatcher) ---

    fun createTeam(name: String, onFinished: (Long?) -> Unit) {
        viewModelScope.launch(dispatcher) {
            try {
                // Checagem segura de Premium
                val isPremium = premiumManager.isPremium.value
                val count = teamRepository.getTeamCount()

                if (!isPremium && count >= PremiumManager.FREE_TEAM_LIMIT) {
                    com.vinithius.dex10.analytics.AnalyticsManager.logLimitReached("team_limit")
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
        viewModelScope.launch(dispatcher) {
            try {
                Log.d("TeamViewModel", "Requesting deletion for team ${team.id}...")
                withContext(kotlinx.coroutines.NonCancellable) {
                    teamRepository.deleteTeam(team)
                }
                com.vinithius.dex10.analytics.AnalyticsManager.logDeleteTeam()
                Log.d("TeamViewModel", "Deletion request completed for team ${team.id}")
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error deleting team ${team.id}", e)
            }
        }
    }

    fun addMember(teamId: Int, pokemonId: Int, position: Int) {
        viewModelScope.launch(dispatcher) {
            try {
                teamRepository.addMember(TeamMemberEntity(teamId = teamId, pokemonId = pokemonId, position = position))
            val pokemon = pokemonRepository.getPokemonDetail(pokemonId)
            pokemon?.name?.let { name ->
                    com.vinithius.dex10.analytics.AnalyticsManager.logAddMember(name)
                }
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error adding member", e)
            }
        }
    }

    fun removeMember(member: TeamMemberEntity) {
        viewModelScope.launch(dispatcher) {
            try {
                teamRepository.removeMember(member)
            val pokemon = pokemonRepository.getPokemonDetail(member.pokemonId)
            pokemon?.name?.let { name ->
                    com.vinithius.dex10.analytics.AnalyticsManager.logRemoveMember(name)
                }
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error removing member", e)
            }
        }
    }

    fun updateMember(member: TeamMemberEntity) {
        viewModelScope.launch(dispatcher) {
            try {
                teamRepository.updateMember(member)
            } catch (e: Exception) {
                Log.e("TeamViewModel", "Error updating member", e)
            }
        }
    }

    fun renameTeam(team: TeamEntity, newName: String) {
        viewModelScope.launch(dispatcher) {
            try { teamRepository.updateTeam(team.copy(name = newName)) } catch (e: Exception) {}
        }
    }

    fun updateTeamFormat(team: TeamEntity, format: String) {
        viewModelScope.launch(dispatcher) {
            try { teamRepository.updateTeam(team.copy(format = format)) } catch (e: Exception) {}
        }
    }

    fun updateTeamNotes(team: TeamEntity, notes: String) {
        viewModelScope.launch(dispatcher) {
            try { teamRepository.updateTeam(team.copy(notes = notes)) } catch (e: Exception) {}
        }
    }

    fun triggerUpsell() { _showUpsell.value = true }
    fun dismissUpsell() { _showUpsell.value = false }
}