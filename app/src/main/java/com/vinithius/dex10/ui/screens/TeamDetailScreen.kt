package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.data.AppPreferences
import com.vinithius.dex10.ui.viewmodel.TeamViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.get
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.components.TypeItem
import com.vinithius.dex10.extension.getDrawableHabitat

@Composable
fun TeamDetailScreen(
    navController: NavController,
    teamId: Int,
    viewModel: TeamViewModel = getViewModel()
) {
    LaunchedEffect(teamId) {
        viewModel.selectTeam(teamId)
    }

    val team by viewModel.selectedTeam.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Strategy")

    var showPicker by remember { mutableStateOf<Int?>(null) } // Store position being picked
    var editingMember by remember { mutableStateOf<com.vinithius.dex10.datasource.database.TeamMemberEntity?>(null) }

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
                viewModel.removeMember(member)
                editingMember = null
            }
        )
    }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEach { title ->
                    Tab(
                        selected = selectedTab == tabs.indexOf(title),
                        onClick = { selectedTab = tabs.indexOf(title) },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> team?.let { 
                    TeamOverviewTab(it, viewModel, navController, 
                        onEditMember = { member -> editingMember = member },
                        onAddMember = { position -> showPicker = position }
                    ) 
                }
                1 -> team?.let { TeamStrategyTab(it, viewModel) }
            }
        }
    }
}

@Composable
fun TeamOverviewTab(
    teamWithMembers: com.vinithius.dex10.datasource.database.TeamWithMembers,
    viewModel: TeamViewModel,
    navController: NavController,
    onEditMember: (com.vinithius.dex10.datasource.database.TeamMemberEntity) -> Unit,
    onAddMember: (Int) -> Unit
) {
    val team = teamWithMembers.team
    
    Column {
        // Team Header Info
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
                Icon(Icons.Default.Edit, contentDescription = "Edit Format")
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(6) { index ->
                val member = teamWithMembers.members.find { it.position == index }
                if (member != null) {
                    TeamMemberCard(member, viewModel) {
                        onEditMember(member)
                    }
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
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

@Composable
fun TeamStrategyTab(
    teamWithMembers: com.vinithius.dex10.datasource.database.TeamWithMembers,
    viewModel: TeamViewModel
) {
    val team = teamWithMembers.team
    val pokemonDetails by viewModel.pokemonDetails.collectAsState()
    val analysis by viewModel.teamAnalysis.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Notes Section
        Text(
            text = stringResource(R.string.team_notes_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        var notesText by remember(team.notes) { mutableStateOf(team.notes ?: "") }
        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.team_notes_placeholder)) },
            trailingIcon = {
                if (notesText != (team.notes ?: "")) {
                    IconButton(onClick = { viewModel.updateTeamNotes(team, notesText) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save Notes", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Strategy Tools Header
        Text(
            text = stringResource(R.string.strategy_tools),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Average Stats Chart (Custom Compose Implementation)
        Text(
            text = stringResource(R.string.avg_stats_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        AvgStatsChart(teamWithMembers, pokemonDetails)

        Spacer(modifier = Modifier.height(24.dp))

        // Existing Type coverage
        Text(
            text = stringResource(R.string.type_coverage_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        analysis?.let { coverage ->
            CoverageSection("Defensive Weaknesses", coverage.weaknesses, Color(0xFFE57373))
            Spacer(modifier = Modifier.height(16.dp))
            CoverageSection("Defensive Resistances", coverage.resistances, Color(0xFF81C784))
        }
    }
}

@Composable
fun AvgStatsChart(
    team: com.vinithius.dex10.datasource.database.TeamWithMembers,
    details: Map<Int, com.vinithius.dex10.datasource.database.PokemonWithDetails>
) {
    if (team.members.isEmpty()) return

    val stats = listOf("HP", "ATK", "DEF", "SATK", "SDEF", "SPE")
    val avgStats = mutableMapOf<String, Float>()
    
    stats.forEach { statName ->
        val total = team.members.mapNotNull { member ->
            val pokemon = details[member.pokemonId]
            pokemon?.stats?.find { it.name.value.uppercase().contains(statName) }?.baseStat
        }.sum()
        avgStats[statName] = total.toFloat() / team.members.size
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        avgStats.forEach { (name, value) ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    modifier = Modifier.width(40.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                ) {
                    val progress = (value / 150f).coerceIn(0f, 1f) // Max 150 for avg
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                when {
                                    value > 100 -> Color(0xFF4CAF50)
                                    value > 70 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                    )
                }
                
                Text(
                    text = value.toInt().toString(),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoverageSection(title: String, types: Map<String, Int>, color: Color) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = color)
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { (type, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.vinithius.dex10.components.TypeItem(type)
                    Text(
                        text = " : $count",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
fun TeamMemberCard(
    member: com.vinithius.dex10.datasource.database.TeamMemberEntity,
    viewModel: TeamViewModel,
    onClick: () -> Unit
) {
    val pokemonDetails by viewModel.pokemonDetails.collectAsState()
    val details = pokemonDetails[member.pokemonId]
    val pokemonName = details?.pokemon?.name?.replaceFirstChar { it.uppercase() } ?: "Loading..."
    val habitat = details?.pokemon?.habitat ?: "unknown"
    
    val appPreferences: AppPreferences = get()
    val lowQuality by appPreferences.lowQualityImages.collectAsState(initial = false)
    
    val imageUrl = if (lowQuality) {
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${member.pokemonId}.png"
    } else {
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${member.pokemonId}.png"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Habitat Background
            Image(
                painter = painterResource(id = habitat.getDrawableHabitat()),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.4f)
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    // Removed Pokeball background as requested
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = pokemonName,
                        modifier = Modifier.size(70.dp)
                    )
                }
                
                Text(
                    text = pokemonName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f)
                    ),
                    maxLines = 1,
                    color = Color.White
                )
                
                if (!member.nickname.isNullOrBlank()) {
                    Text(
                        text = "\"${member.nickname}\"",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Types and Details Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Small Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        details?.stats?.take(6)?.forEach { stat ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stat.name.value.take(3).uppercase().replace("SPE", "SPD"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = stat.baseStat.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Moves: ${member.move1 ?: "-"}, ${member.move2 ?: "-"}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Item: ${member.item ?: "None"}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySlotCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.height(200.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = "Add Pokemon", modifier = Modifier.size(48.dp))
        }
    }
}

