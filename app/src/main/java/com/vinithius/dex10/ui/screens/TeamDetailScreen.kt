package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.data.AppPreferences
import com.vinithius.dex10.datasource.database.PokemonWithDetails
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.extension.getDrawableHabitat
import com.vinithius.dex10.ui.viewmodel.TeamViewModel
import com.vinithius.dex10.utils.TeamValidator
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun TeamDetailScreen(
    navController: NavController,
    teamId: Int,
    viewModel: TeamViewModel = getViewModel()
) {
    // 1. Apenas atualiza o ID. O ViewModel cuida do fluxo.
    LaunchedEffect(teamId) {
        viewModel.selectTeam(teamId)
    }

    // 2. Coleta de Estado
    val teamWithMembers by viewModel.selectedTeam.collectAsState()
    val teamIssues by viewModel.teamIssues.collectAsState()

    // 3. UI Local
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(R.string.overview), stringResource(R.string.strategy))

    var showPicker by remember { mutableStateOf<Int?>(null) }
    var editingMember by remember { mutableStateOf<TeamMemberEntity?>(null) }
    var teamToDelete by remember { mutableStateOf<TeamEntity?>(null) }
    var memberToRemove by remember { mutableStateOf<TeamMemberEntity?>(null) }

    // --- SHEETS & DIALOGS ---

    showPicker?.let { position ->
        PokemonPickerSheet(
            onDismiss = { showPicker = null },
            onPokemonSelected = { pokemonId ->
                viewModel.addMember(teamId, pokemonId, position)
                showPicker = null
            }
        )
    }

    editingMember?.let { member ->
        val pokemonDetailsMap by viewModel.pokemonDetails.collectAsState()
        val memberDetails = pokemonDetailsMap[member.pokemonId]

        EditMemberSheet(
            member = member,
            pokemonDetails = memberDetails,
            onDismiss = { editingMember = null },
            onSave = { updatedMember ->
                viewModel.updateMember(updatedMember)
                editingMember = null
            },
            onRemove = {
                memberToRemove = member
                editingMember = null
            }
        )
    }

    teamToDelete?.let { teamEntity ->
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text(stringResource(R.string.delete_team)) },
            text = { Text(stringResource(R.string.delete_team_confirm, teamEntity.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Navegação e Ação Explícitas
                        viewModel.deleteTeam(teamEntity)
                        teamToDelete = null
                        navController.popBackStack()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { teamToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    memberToRemove?.let { member ->
        val pokemonDetailsMap by viewModel.pokemonDetails.collectAsState()
        val name = pokemonDetailsMap[member.pokemonId]?.pokemon?.name?.replaceFirstChar { it.uppercase() } ?: ""

        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text(stringResource(R.string.remove_confirm_title)) },
            text = { Text(stringResource(R.string.remove_confirm_msg, name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeMember(member)
                        memberToRemove = null
                    }
                ) {
                    Text(stringResource(R.string.remove_from_team), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // --- SCAFFOLD ---

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tratamento de Loading para evitar tela branca ou crash
            if (teamWithMembers == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val currentTeam = teamWithMembers!!

                when (selectedTab) {
                    0 -> TeamOverviewTab(
                        teamWithMembers = currentTeam,
                        viewModel = viewModel,
                        onEditMember = { member -> editingMember = member },
                        onAddMember = { position -> showPicker = position },
                        onDeleteTeam = { teamToDelete = currentTeam.team },
                        onRemoveMember = { memberToRemove = it },
                        issues = teamIssues
                    )
                    1 -> TeamStrategyTab(
                        teamWithMembers = currentTeam,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// --- SUB-COMPONENTES (MANTIDOS E INTEGRADOS) ---

@Composable
fun TeamOverviewTab(
    teamWithMembers: TeamWithMembers,
    viewModel: TeamViewModel,
    onEditMember: (TeamMemberEntity) -> Unit,
    onAddMember: (Int) -> Unit,
    onDeleteTeam: () -> Unit,
    onRemoveMember: (TeamMemberEntity) -> Unit,
    issues: List<TeamValidator.ValidationIssue>
) {
    val team = teamWithMembers.team

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.format_label) + " ${team.format ?: "6v6"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            var showFormatDialog by remember { mutableStateOf(false) }
            IconButton(onClick = { showFormatDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_format))
            }

            IconButton(onClick = onDeleteTeam) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_team),
                    tint = MaterialTheme.colorScheme.error
                )
            }

            if (showFormatDialog) {
                FormatSelectionDialog(
                    currentFormat = team.format ?: "6v6",
                    onDismiss = { showFormatDialog = false },
                    onFormatSelected = {
                        viewModel.updateTeamFormat(team, it)
                        showFormatDialog = false
                    }
                )
            }
        }

        if (issues.isNotEmpty()) {
            ValidationAlertsSection(issues)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(6) { index ->
                val member = teamWithMembers.members.find { it.position == index }
                val hasIssue = issues.any { issue ->
                    when(issue) {
                        is TeamValidator.ValidationIssue.DuplicateSpecies -> issue.positions.contains(index)
                        is TeamValidator.ValidationIssue.DuplicateItem -> issue.positions.contains(index)
                        else -> false
                    }
                }

                if (member != null) {
                    TeamMemberCard(
                        member = member,
                        viewModel = viewModel,
                        hasError = hasIssue,
                        onClick = { onEditMember(member) },
                        onRemove = { onRemoveMember(member) }
                    )
                } else {
                    EmptySlotCard {
                        onAddMember(index)
                    }
                }
            }
        }
    }
}

@Composable
fun FormatSelectionDialog(
    currentFormat: String,
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit
) {
    val formats = listOf(
        "1v1" to stringResource(R.string.format_1v1),
        "2v2" to stringResource(R.string.format_2v2),
        "3v3" to stringResource(R.string.format_3v3),
        "6v6" to stringResource(R.string.format_6v6)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.format_label)) },
        text = {
            Column {
                formats.forEach { (id, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFormatSelected(id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentFormat == id, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
fun ValidationAlertsSection(issues: List<TeamValidator.ValidationIssue>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.team_invalid_title), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            issues.forEach { issue ->
                val message = when (issue) {
                    is TeamValidator.ValidationIssue.DuplicateSpecies -> stringResource(R.string.species_clause_warning)
                    is TeamValidator.ValidationIssue.DuplicateItem -> stringResource(R.string.item_clause_warning)
                    else -> "Validation issue detected"
                }
                Text(text = "• $message", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 4.dp, start = 8.dp))
            }
        }
    }
}

@Composable
fun TeamStrategyTab(
    teamWithMembers: TeamWithMembers,
    viewModel: TeamViewModel
) {
    val team = teamWithMembers.team
    val pokemonDetails by viewModel.pokemonDetails.collectAsState()
    val analysis by viewModel.teamAnalysis.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text(text = stringResource(R.string.team_notes_label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        var notesText by remember(team.notes) { mutableStateOf(team.notes ?: "") }
        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.team_notes_placeholder)) },
            trailingIcon = {
                if (notesText != (team.notes ?: "")) {
                    IconButton(onClick = { viewModel.updateTeamNotes(team, notesText) }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_notes), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.strategy_tools), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.avg_stats_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        AvgStatsChart(teamWithMembers, pokemonDetails)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.type_coverage_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        analysis?.let { coverage ->
            CoverageSection(stringResource(R.string.defensive_weaknesses), coverage.weaknesses, Color(0xFFE57373))
            Spacer(modifier = Modifier.height(16.dp))
            CoverageSection(stringResource(R.string.defensive_resistances), coverage.resistances, Color(0xFF81C784))
        }
    }
}

@Composable
fun AvgStatsChart(team: TeamWithMembers, details: Map<Int, PokemonWithDetails>) {
    if (team.members.isEmpty()) return
    val stats = listOf("HP", "ATK", "DEF", "SATK", "SDEF", "SPE")
    val avgStats = mutableMapOf<String, Float>()
    stats.forEach { statName ->
        val total = team.members.mapNotNull { member ->
            val pokemon = details[member.pokemonId]
            pokemon?.stats?.find { it.name.value.uppercase().contains(statName) }?.baseStat
        }.sum()
        avgStats[statName] = if (team.members.isNotEmpty()) total.toFloat() / team.members.size else 0f
    }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp)) {
        avgStats.forEach { (name, value) ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, modifier = Modifier.width(40.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant)) {
                    val progress = (value / 150f).coerceIn(0f, 1f)
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(when { value > 100 -> Color(0xFF4CAF50); value > 70 -> Color(0xFFFFC107); else -> Color(0xFFF44336) }))
                }
                Text(text = value.toInt().toString(), modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoverageSection(title: String, types: Map<String, Int>, color: Color) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = color)
        FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { (type, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.vinithius.dex10.components.TypeItem(type)
                    Text(text = " : $count", modifier = Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
fun TeamMemberCard(member: TeamMemberEntity, viewModel: TeamViewModel, hasError: Boolean = false, onClick: () -> Unit, onRemove: () -> Unit) {
    val pokemonDetails by viewModel.pokemonDetails.collectAsState()
    val details = pokemonDetails[member.pokemonId]
    val pokemonName = details?.pokemon?.name?.replaceFirstChar { it.uppercase() } ?: stringResource(R.string.loading_dots)
    val habitat = details?.pokemon?.habitat ?: "unknown"
    val appPreferences: AppPreferences = get()
    val lowQuality by appPreferences.lowQualityImages.collectAsState(initial = false)
    val imageUrl = if (lowQuality) "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${member.pokemonId}.png" else "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${member.pokemonId}.png"

    Card(modifier = Modifier.fillMaxWidth().height(220.dp).clickable { onClick() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), shape = RoundedCornerShape(16.dp), border = if (hasError) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = habitat.getDrawableHabitat()), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().alpha(0.4f))
            IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_from_team), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    Image(painter = rememberAsyncImagePainter(ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build()), contentDescription = pokemonName, modifier = Modifier.size(70.dp))
                }
                Text(text = pokemonName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f)), maxLines = 1, color = Color.White)
                if (!member.nickname.isNullOrBlank()) Text(text = "\"${member.nickname}\"", style = MaterialTheme.typography.bodySmall, maxLines = 1, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        details?.stats?.take(6)?.forEach { stat ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = stat.name.value.take(3).uppercase().replace("SPE", "SPD"), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.White.copy(alpha = 0.7f))
                                Text(text = stat.baseStat.toString(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${stringResource(R.string.moves)}: ${member.move1 ?: "-"}, ${member.move2 ?: "-"}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White.copy(alpha = 0.9f))
                    Text(text = "${stringResource(R.string.item)}: ${member.item ?: stringResource(R.string.none)}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}

@Composable
fun EmptySlotCard(onClick: () -> Unit) {
    Card(modifier = Modifier.height(200.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_pokemon), modifier = Modifier.size(48.dp))
        }
    }
}