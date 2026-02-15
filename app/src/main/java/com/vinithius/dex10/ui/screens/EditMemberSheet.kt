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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                "Edit Pokemon Details", 
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                placeholder = { Text(pokemonDetails?.pokemon?.name ?: "Enter nickname") },
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
                    label = { Text("Ability") },
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
                label = { Text("Held Item") },
                placeholder = { Text("e.g., Life Orb, Leftovers") },
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
                            "Stats (IVs/EVs)", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Info,
                                contentDescription = "Info",
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
                            Text("Hide Stats Editor")
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
                            Text("Edit IVs/EVs")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Moveset", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val moveFields = listOf(
                Triple(move1, "Move 1", 1),
                Triple(move2, "Move 2", 2),
                Triple(move3, "Move 3", 3),
                Triple(move4, "Move 4", 4)
            )

            moveFields.forEachIndexed { index, (moveValue, label, slot) ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().padding(top = if (index > 0) 8.dp else 0.dp)) {
                    OutlinedTextField(
                        value = moveValue ?: "",
                        onValueChange = { },
                        label = { Text(label) },
                        placeholder = { Text("Tap to select") },
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
                text = "Save Changes",
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

            Spacer(modifier = Modifier.height(12.dp))

            AppButton(
                text = "Remove Pokemon from Team",
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Text,
                containerColor = MaterialTheme.colorScheme.error
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
                title = { Text("Remove from Team") },
                text = { Text("Are you sure you want to remove ${member.nickname ?: pokemonDetails?.pokemon?.name ?: "this Pokemon"}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            com.vinithius.dex10.analytics.AnalyticsManager.logEvent("remove_member", "pokemon", pokemonDetails?.pokemon?.name ?: "unknown")
                            onRemove()
                            showDeleteConfirm = false
                        }
                    ) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
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
                text = "Como funciona o Team Builder?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Monte times competitivos como um profissional!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                InfoSection(
                    title = "Base Stats (Status Base)",
                    desc = "Valores fixos de cada espécie de Pokémon (ex: Charizard tem Speed maior que Blastoise). É o ponto de partida."
                )
                InfoSection(
                    title = "IVs (Individual Values)",
                    desc = "O 'DNA' do Pokémon. Varia de 0 a 31 para cada status. Em batalhas competitivas, usa-se 31 (máximo) para garantir o melhor desempenho, exceto em casos específicos (ex: 0 Attack para evitar dano de Foul Play)."
                )
                InfoSection(
                    title = "EVs (Effort Values)",
                    desc = "Pontos ganhos com o treinamento. Você tem 510 pontos totais para distribuir, com máximo de 252 em um único status. Use isso para especializar seu Pokémon (ex: maximizar Speed e Attack)."
                )
                InfoSection(
                    title = "Nature (Natureza)",
                    desc = "A personalidade do Pokémon. Aumenta um status em 10% (+) e diminui outro em 10% (-). Escolha uma Nature que aumente o ponto forte do seu Pokémon e diminua um status que ele não usa."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendi")
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
