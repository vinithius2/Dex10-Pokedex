package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.database.TeamEntity
import com.vinithius.dex10.datasource.database.TeamWithMembers
import com.vinithius.dex10.extension.LoadGifWithCoil
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.TeamViewModel
import com.vinithius.dex10.ui.components.UpsellBottomSheet
import com.vinithius.dex10.ui.components.AppButton
import com.vinithius.dex10.ui.components.ButtonVariant
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
            onRestoreClick = {
                teamViewModel.dismissUpsell()
                teamViewModel.premiumManager.restorePurchases()
            }
        )
    }

    if (showCreateDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                teamViewModel.createTeam(name) { newId ->
                    showCreateDialog = false
                    navController.navigate("teamDetail/$newId")
                }
            }
        )
    }

    if (teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text("Delete Team") },
            text = { Text("Are you sure you want to delete the team \"${teamToDelete?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        android.util.Log.d("TeamListScreen", "Delete clicked for team: ${teamToDelete?.id}")
                        teamToDelete?.let { teamViewModel.deleteTeam(it) }
                        teamToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        android.util.Log.d("TeamListScreen", "Delete dismissed")
                        teamToDelete = null 
                    }
                ) {
                    Text("Cancel")
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
                Icon(Icons.Default.Add, contentDescription = "Create Team")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (teams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No teams yet. Create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = teams, key = { it.team.id }) { team ->
                        TeamItem(
                            team = team,
                            onClick = { navController.navigate("teamDetail/${team.team.id}") },
                            onDelete = { teamToDelete = team.team },
                            pokemonViewModel = pokemonViewModel
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
    pokemonViewModel: PokemonViewModel
) {
    // Swipe to dismiss or just a delete button?
    // Let's use a Card with a delete button for simplicity and safety.
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.team.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onDelete(team.team) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // Display up to 6 mini sprites
                for (i in 0 until 6) {
                    val member = team.members.find { it.position == i }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (member != null) {
                            member.pokemonId.LoadGifWithCoil(
                                viewModel = pokemonViewModel
                            )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Team") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Team Name") },
                singleLine = true
            )
        },
        confirmButton = {
            AppButton(
                text = "Create",
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank()
            )
        },
        dismissButton = {
            AppButton(
                text = "Cancel",
                onClick = onDismiss,
                variant = ButtonVariant.Text
            )
        }
    )
}
