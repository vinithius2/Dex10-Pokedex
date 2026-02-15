package com.vinithius.dex10.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinithius.dex10.datasource.model.EVs
import com.vinithius.dex10.datasource.model.IVs
import com.vinithius.dex10.datasource.model.Nature
import com.vinithius.dex10.datasource.model.StatType

/**
 * IV/EV Editor Component for Professional Team Builder
 */
@Composable
fun IVEVEditor(
    ivs: IVs,
    evs: EVs,
    nature: Nature?,
    baseStats: Map<StatType, Int>,
    onIVsChanged: (IVs) -> Unit,
    onEVsChanged: (EVs) -> Unit,
    modifier: Modifier = Modifier
) {
    var showIVEditor by remember { mutableStateOf(true) }
    
    Column(modifier = modifier) {
        // Toggle between IV and EV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showIVEditor = true },
                modifier = Modifier.weight(1f),
                colors = if (showIVEditor) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("IVs")
            }
            Button(
                onClick = { showIVEditor = false },
                modifier = Modifier.weight(1f),
                colors = if (!showIVEditor) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("EVs")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (showIVEditor) {
            IVEditor(ivs = ivs, onChange = onIVsChanged)
        } else {
            EVEditor(evs = evs, onChange = onEVsChanged)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats preview
        StatsPreview(
            baseStats = baseStats,
            ivs = ivs,
            evs = evs,
            nature = nature
        )
    }
}

@Composable
fun IVEditor(
    ivs: IVs,
    onChange: (IVs) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Individual Values (0-31)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        
        StatSlider(
            label = "HP",
            value = ivs.hp,
            onValueChange = { onChange(ivs.copy(hp = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = "Attack",
            value = ivs.atk,
            onValueChange = { onChange(ivs.copy(atk = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = "Defense",
            value = ivs.def,
            onValueChange = { onChange(ivs.copy(def = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = "Sp. Atk",
            value = ivs.spa,
            onValueChange = { onChange(ivs.copy(spa = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = "Sp. Def",
            value = ivs.spd,
            onValueChange = { onChange(ivs.copy(spd = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = "Speed",
            value = ivs.spe,
            onValueChange = { onChange(ivs.copy(spe = it)) },
            range = 0f..31f
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { onChange(IVs()) }) {
                Text("Max All (31)")
            }
            TextButton(onClick = { onChange(IVs(0, 0, 0, 0, 0, 0)) }) {
                Text("Min All (0)")
            }
        }
    }
}

@Composable
fun EVEditor(
    evs: EVs,
    onChange: (EVs) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Effort Values (0-252)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${evs.total}/510",
                style = MaterialTheme.typography.labelLarge,
                color = if (evs.total > 510) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        StatSlider(
            label = "HP",
            value = evs.hp,
            onValueChange = { onChange(evs.copy(hp = it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = "Attack",
            value = evs.atk,
            onValueChange = { onChange(evs.copy(atk = it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = "Defense",
            value = evs.def,
            onValueChange = { onChange(evs.copy(def = it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = "Sp. Atk",
            value = evs.spa,
            onValueChange = { onChange(evs.copy(spa = it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = "Sp. Def",
            value = evs.spd,
            onValueChange = { onChange(evs.copy(spd = it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = "Speed",
            value = evs.spe,
            onValueChange = { onChange(evs.copy(spe = it)) },
            range = 0f..252f,
            steps = 251
        )
        
        // Common EV spreads
        Text("Common Spreads:", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(onClick = { onChange(EVs.SWEEPER_PHYSICAL) }) {
                Text("Physical", fontSize = 10.sp)
            }
            TextButton(onClick = { onChange(EVs.SWEEPER_SPECIAL) }) {
                Text("Special", fontSize = 10.sp)
            }
            TextButton(onClick = { onChange(EVs.WALL_PHYSICAL) }) {
                Text("Wall", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun StatSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 30
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier
                .width(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatsPreview(
    baseStats: Map<StatType, Int>,
    ivs: IVs,
    evs: EVs,
    nature: Nature?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Final Stats (Lv.100)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            StatType.values().forEach { statType ->
                val base = baseStats[statType] ?: 0
                val iv = when (statType) {
                    StatType.HP -> ivs.hp
                    StatType.ATK -> ivs.atk
                    StatType.DEF -> ivs.def
                    StatType.SPA -> ivs.spa
                    StatType.SPD -> ivs.spd
                    StatType.SPE -> ivs.spe
                }
                val ev = when (statType) {
                    StatType.HP -> evs.hp
                    StatType.ATK -> evs.atk
                    StatType.DEF -> evs.def
                    StatType.SPA -> evs.spa
                    StatType.SPD -> evs.spd
                    StatType.SPE -> evs.spe
                }
                
                val natureModifier = nature?.getModifier(statType) ?: 1.0
                
                val finalStat = if (statType == StatType.HP) {
                    // HP formula: (2 * Base + IV + EV/4) + 110
                    (2 * base + iv + ev / 4) + 110
                } else {
                    // Other stats: ((2 * Base + IV + EV/4) + 5) * Nature
                    ((2 * base + iv + ev / 4) + 5) * natureModifier
                }.toInt()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(statType.displayName, style = MaterialTheme.typography.bodySmall)
                    Text(
                        finalStat.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            natureModifier > 1.0 -> Color(0xFF2ECC71) // Green for boosted
                            natureModifier < 1.0 -> Color(0xFFE74C3C) // Red for decreased
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}
