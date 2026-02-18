package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.vinithius.dex10.R
import com.vinithius.dex10.ui.components.AppButton
import com.vinithius.dex10.ui.components.ButtonVariant
import com.vinithius.dex10.ui.components.IVEVEditor
import com.vinithius.dex10.ui.components.NatureSelector
import com.vinithius.dex10.datasource.database.TeamMemberEntity
import com.vinithius.dex10.datasource.model.EVs
import com.vinithius.dex10.datasource.model.IVs
import com.vinithius.dex10.datasource.model.Nature
import com.vinithius.dex10.datasource.model.StatType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberSheet(
    member: TeamMemberEntity,
    pokemonDetails: com.vinithius.dex10.datasource.database.PokemonWithDetails?,
    onDismiss: () -> Unit,
    onSave: (TeamMemberEntity) -> Unit,
    onRemove: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    var nickname by remember { mutableStateOf(member.nickname ?: "") }
    var item by remember { mutableStateOf(member.item ?: "") }
    var ability by remember { mutableStateOf(member.ability ?: "") }
    var move1 by remember { mutableStateOf(member.move1 ?: "") }
    var move2 by remember { mutableStateOf(member.move2 ?: "") }
    var move3 by remember { mutableStateOf(member.move3 ?: "") }
    var move4 by remember { mutableStateOf(member.move4 ?: "") }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    // IV/EV/Nature state
    var ivs by remember { mutableStateOf(IVs.fromJson(member.ivs)) }
    var evs by remember { mutableStateOf(EVs.fromJson(member.evs)) }
    var selectedNature by remember { 
        mutableStateOf<Nature?>(
            member.nature?.let { natureName ->
                Nature.values().find { it.displayName.equals(natureName, ignoreCase = true) }
            }
        )
    }

    var activeMoveSlot by remember { mutableStateOf<Int?>(null) }
    var showStatsEditor by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Get base stats from pokemon details
    val baseStats = remember(pokemonDetails) {
        pokemonDetails?.stats?.associate { stat ->
            // Map database StatType enum to our model StatType enum
            val statType = when (stat.name) {
                com.vinithius.dex10.datasource.database.StatType.HP -> com.vinithius.dex10.datasource.model.StatType.HP
                com.vinithius.dex10.datasource.database.StatType.ATTACK -> com.vinithius.dex10.datasource.model.StatType.ATK
                com.vinithius.dex10.datasource.database.StatType.DEFENSE -> com.vinithius.dex10.datasource.model.StatType.DEF
                com.vinithius.dex10.datasource.database.StatType.SPECIAL_ATTACK -> com.vinithius.dex10.datasource.model.StatType.SPA
                com.vinithius.dex10.datasource.database.StatType.SPECIAL_DEFENSE -> com.vinithius.dex10.datasource.model.StatType.SPD
                com.vinithius.dex10.datasource.database.StatType.SPEED -> com.vinithius.dex10.datasource.model.StatType.SPE
            }
            statType to stat.baseStat
        } ?: emptyMap()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.edit_pokemon_details), 
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(stringResource(R.string.nickname_label)) },
                placeholder = { Text(pokemonDetails?.pokemon?.name ?: stringResource(R.string.nickname_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Ability Selection
            val abilities = pokemonDetails?.abilities?.map { it.name } ?: emptyList()
            var abilityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = abilityExpanded,
                onExpandedChange = { abilityExpanded = !abilityExpanded }
            ) {
                OutlinedTextField(
                    value = ability,
                    onValueChange = { ability = it },
                    label = { Text(stringResource(R.string.ability_label)) },
                    readOnly = abilities.isNotEmpty(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abilityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                if (abilities.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = abilityExpanded,
                        onDismissRequest = { abilityExpanded = false }
                    ) {
                        abilities.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    ability = selection
                                    abilityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Professional Nature Selection
            NatureSelector(
                selectedNature = selectedNature,
                onNatureSelected = { selectedNature = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = item,
                onValueChange = { item = it },
                label = { Text(stringResource(R.string.held_item_label)) },
                placeholder = { Text(stringResource(R.string.held_item_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // IV/EV Stats Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.stats_title), 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Info,
                                contentDescription = stringResource(R.string.info_title),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (showStatsEditor) {
                        IVEVEditor(
                            ivs = ivs,
                            evs = evs,
                            nature = selectedNature,
                            baseStats = baseStats,
                            onIVsChanged = { ivs = it },
                            onEVsChanged = { evs = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showStatsEditor = false }) {
                            Text(stringResource(R.string.hide_stats_editor))
                        }
                    } else {
                        // Summary view
                        Text(
                            "IVs: ${ivs.hp}/${ivs.atk}/${ivs.def}/${ivs.spa}/${ivs.spd}/${ivs.spe}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "EVs: ${evs.hp}/${evs.atk}/${evs.def}/${evs.spa}/${evs.spd}/${evs.spe} (${evs.total}/510)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showStatsEditor = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.edit_stats))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.moveset_title), 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val move1Label = stringResource(R.string.move_slot_label, 1)
            val move2Label = stringResource(R.string.move_slot_label, 2)
            val move3Label = stringResource(R.string.move_slot_label, 3)
            val move4Label = stringResource(R.string.move_slot_label, 4)
            
            val moveFields = listOf(
                Triple(move1, move1Label, 1),
                Triple(move2, move2Label, 2),
                Triple(move3, move3Label, 3),
                Triple(move4, move4Label, 4)
            )

            moveFields.forEachIndexed { index, (moveValue, label, slot) ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().padding(top = if (index > 0) 8.dp else 0.dp)) {
                    OutlinedTextField(
                        value = moveValue ?: "",
                        onValueChange = { },
                        label = { Text(label) },
                        placeholder = { Text(stringResource(R.string.tap_to_select)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false, // Disable to prevent focus, we handle click on Box
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { activeMoveSlot = slot }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AppButton(
                text = stringResource(R.string.save_changes),
                onClick = {
                    val updatedMember = member.copy(
                        nickname = nickname,
                        item = item,
                        ability = ability,
                        move1 = move1,
                        move2 = move2,
                        move3 = move3,
                        move4 = move4,
                        ivs = ivs.toJson(),
                        evs = evs.toJson(),
                        nature = selectedNature?.name
                    )
                    com.vinithius.dex10.analytics.AnalyticsManager.logEvent("save_member", "pokemon", pokemonDetails?.pokemon?.name ?: "unknown")
                    onSave(updatedMember)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        activeMoveSlot?.let { slot ->
            MoveSelectionSheet(
                pokemonId = member.pokemonId,
                onMoveSelected = { moveName ->
                    val name = moveName.replace("-", " ").replaceFirstChar { it.uppercase() }
                    when (slot) {
                        1 -> move1 = name
                        2 -> move2 = name
                        3 -> move3 = name
                        4 -> move4 = name
                    }
                    activeMoveSlot = null
                },
                onDismiss = { activeMoveSlot = null }
            )
        }
        }

        if (showInfoDialog) {
            TeamBuilderInfoDialog(onDismiss = { showInfoDialog = false })
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.remove_confirm_title)) },
                text = { Text(stringResource(R.string.remove_confirm_msg, member.nickname ?: pokemonDetails?.pokemon?.name ?: "this Pokemon")) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            com.vinithius.dex10.analytics.AnalyticsManager.logEvent("remove_member", "pokemon", pokemonDetails?.pokemon?.name ?: "unknown")
                            onRemove()
                            showDeleteConfirm = false
                        }
                    ) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

@Composable
fun TeamBuilderInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.info_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.info_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                InfoSection(
                    title = stringResource(R.string.info_base_stats_title),
                    desc = stringResource(R.string.info_base_stats_desc)
                )
                InfoSection(
                    title = stringResource(R.string.info_ivs_title),
                    desc = stringResource(R.string.info_ivs_desc)
                )
                InfoSection(
                    title = stringResource(R.string.info_evs_title),
                    desc = stringResource(R.string.info_evs_desc)
                )
                InfoSection(
                    title = stringResource(R.string.info_nature_title),
                    desc = stringResource(R.string.info_nature_desc)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.got_it))
            }
        }
    )
}

@Composable
fun InfoSection(title: String, desc: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
