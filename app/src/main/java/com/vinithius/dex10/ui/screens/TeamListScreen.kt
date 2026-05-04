package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.R
import androidx.navigation.NavController
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.extension.LoadGifWithCoil
import com.vinithius.dex10.ui.components.AppButton
import com.vinithius.dex10.ui.components.ButtonVariant
import com.vinithius.dex10.ui.components.UpsellBottomSheet
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.TeamViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun TeamListScreen(
    navController: NavController,
    teamViewModel: TeamViewModel = getViewModel(),
    pokemonViewModel: PokemonViewModel = getViewModel() // Needed for LoadGifWithCoil extension usually
) {
    val teams by teamViewModel.teams.collectAsState()
    val showUpsell by teamViewModel.showUpsell.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var teamToDelete by remember { mutableStateOf<TeamEntity?>(null) }
    val isPremium by teamViewModel.premiumManager.isPremium.collectAsState(initial = false)

    val context = androidx.compose.ui.platform.LocalContext.current
    if (showUpsell) {
        UpsellBottomSheet(
            onDismiss = { teamViewModel.dismissUpsell() },
            onPurchaseClick = {
                teamViewModel.dismissUpsell()
                (context as? android.app.Activity)?.let { activity ->
                    teamViewModel.premiumManager.launchPurchaseFlow(activity)
                }
            },
        )
    }

    if (showCreateDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                teamViewModel.createTeam(name) { newId ->
                    showCreateDialog = false
                    newId?.let { id ->
                        navController.navigate("teamDetail/$id")
                    }
                }
            }
        )
    }

    if (teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text(stringResource(R.string.delete_team)) },
            text = { Text(stringResource(R.string.delete_team_confirm, teamToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        android.util.Log.d(
                            "TeamListScreen",
                            "Delete clicked for team: ${teamToDelete?.id}"
                        )
                        teamToDelete?.let { teamViewModel.deleteTeam(it) }
                        teamToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        android.util.Log.d("TeamListScreen", "Delete dismissed")
                        teamToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val currentTeamCount = teams.size
                    if (isPremium || currentTeamCount < com.vinithius.dex10.datasource.data.PremiumManager.FREE_TEAM_LIMIT) {
                        showCreateDialog = true
                    } else {
                        teamViewModel.triggerUpsell()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_team))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(top = 0.dp, start = 0.dp, end = 0.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isPremium) {
                    item {
                        com.vinithius.dex10.ui.components.PremiumPromoBanner(
                            onUpgradeClick = { teamViewModel.triggerUpsell() }
                        )
                    }
                }
                if (teams.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_teams_yet),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(items = teams, key = { it.team.id }) { team ->
                        TeamItem(
                            team = team,
                            onClick = { navController.navigate("teamDetail/${team.team.id}") },
                            onDelete = { teamToDelete = team.team },
                            pokemonViewModel = pokemonViewModel,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamItem(
    team: TeamWithMembers,
    onClick: () -> Unit,
    onDelete: (TeamEntity) -> Unit,
    pokemonViewModel: PokemonViewModel,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(team.team.createdAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.team.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = team.team.format ?: "6v6",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${team.members.size}/6",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDelete(team.team) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display up to 6 mini sprites with premium containers
                    for (i in 0 until 6) {
                        val member = team.members.find { it.position == i }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (member != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (member != null) {
                                member.pokemonId.LoadGifWithCoil(
                                    viewModel = pokemonViewModel,
                                    modifier = Modifier.padding(4.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTeamDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_team)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.team_name)) },
                singleLine = true,
                enabled = !isCreating
            )
        },
        confirmButton = {
            AppButton(
                text = stringResource(if (isCreating) R.string.loading_dots else R.string.create),
                onClick = { 
                    if (text.isNotBlank() && !isCreating) {
                        isCreating = true
                        onConfirm(text) 
                    }
                },
                enabled = text.isNotBlank() && !isCreating
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                variant = ButtonVariant.Text,
                enabled = !isCreating
            )
        }
    )
}
