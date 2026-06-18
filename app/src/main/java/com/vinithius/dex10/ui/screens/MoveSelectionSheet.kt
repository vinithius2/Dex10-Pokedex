package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.vinithius.dex10.R
import com.vinithius.dex10.components.TypeItem
import com.vinithius.dex10.datasource.response.MoveDetailsResponse
import com.vinithius.dex10.datasource.response.MoveResponse
import com.vinithius.dex10.ui.components.MoveCategoryIcon
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel

import com.vinithius.dex10.ui.viewmodel.rememberPokemonViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSelectionSheet(
    pokemonId: Int,
    onMoveSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    var moves by remember { mutableStateOf<List<MoveResponse>?>(null) }
    var moveDetails by remember { mutableStateOf<Map<String, MoveDetailsResponse>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val allLabel = stringResource(R.string.all)
    var selectedGroup by remember { mutableStateOf(allLabel) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(pokemonId) {
        com.vinithius.dex10.analytics.AnalyticsManager.logScreenView("move_selection", "MoveSelectionSheet")
        isLoading = true
        moves = viewModel.getMovesForPokemon(pokemonId)
        isLoading = false

        val names = moves?.mapNotNull { it.move.name } ?: return@LaunchedEffect
        scope.launch {
            val accumulated = mutableMapOf<String, MoveDetailsResponse>()
            coroutineScope {
                names.chunked(20).forEach { batch ->
                    batch.map { moveName ->
                        async { moveName to viewModel.getMoveDetails(moveName) }
                    }.awaitAll().forEach { (name, detail) ->
                        if (detail != null) accumulated[name] = detail
                    }
                    // Update UI after each batch — cached moves appear instantly,
                    // uncached ones fill in progressively as API responds.
                    moveDetails = accumulated.toMap()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                stringResource(R.string.choose_move),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_moves)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
            
            // Filter chips for grouping
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.by_level),
                    stringResource(R.string.by_tm),
                    stringResource(R.string.by_egg),
                    stringResource(R.string.by_tutor)
                ).forEach { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Group moves
                val groupedMoves = moves?.groupBy { moveEntry ->
                    val learnMethod = moveEntry.version_group_details.firstOrNull()?.move_learn_method?.name ?: "unknown"
                    when {
                        learnMethod.contains("level") -> stringResource(R.string.by_level)
                        learnMethod.contains("machine") || learnMethod.contains("tm") -> stringResource(R.string.by_tm)
                        learnMethod.contains("egg") -> stringResource(R.string.by_egg)
                        learnMethod.contains("tutor") -> stringResource(R.string.by_tutor)
                        else -> stringResource(R.string.other)
                    }
                } ?: emptyMap()
                
                // Filter by search and selected group
                val displayGroups = if (selectedGroup == stringResource(R.string.all)) {
                    groupedMoves
                } else {
                    groupedMoves.filter { it.key == selectedGroup }
                }

                if (displayGroups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.no_moves_found),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.search_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        displayGroups.forEach { (groupName, groupMoves) ->
                            item {
                                Text(
                                    groupName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            
                            val filteredGroupMoves = groupMoves.filter {
                                it.move.name?.contains(searchQuery, ignoreCase = true) == true
                            }
                            
                            items(filteredGroupMoves) { moveEntry ->
                                val moveName = moveEntry.move.name ?: "Unknown"
                                val detail = moveDetails[moveName]
                                val levelLearned = moveEntry.version_group_details.firstOrNull()?.level_learned_at ?: 0
                                
                                ProfessionalMoveItem(
                                    moveName = moveName,
                                    moveDetail = detail,
                                    levelLearned = levelLearned,
                                    groupName = groupName,
                                    onClick = { onMoveSelected(moveName) }
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
fun ProfessionalMoveItem(
    moveName: String,
    moveDetail: MoveDetailsResponse?,
    levelLearned: Int,
    groupName: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Name and level/TM info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (groupName == "By TM" && levelLearned > 0) {
                        Text(
                            text = "TM${String.format("%03d", levelLearned)} ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (groupName == "By Level" && levelLearned > 0) {
                        Text(
                            text = "Lv.$levelLearned ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = moveName.replace("-", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Move effect explanation — addresses user feedback about missing
                // descriptions of moves when building a team.
                moveDetail?.shortEffect?.takeIf { it.isNotBlank() }?.let { effect ->
                    Text(
                        text = effect,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                    )
                }
            }
            
            // Middle: Power and PP
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (moveDetail == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Power
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = moveDetail?.power?.toString() ?: "-",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                // PP
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = moveDetail?.pp?.toString() ?: "-",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp
                    )
                }
                }
            }
            
            // Right side: Type and Category icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                // Type icon
                if (moveDetail != null) {
                    TypeItem(typeName = moveDetail.type.name ?: "normal")
                }
                
                // Category icon
                if (moveDetail != null) {
                    MoveCategoryIcon(
                        category = moveDetail.damage_class.name ?: "status"
                    )
                }
            }
        }
    }
}
