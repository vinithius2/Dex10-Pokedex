package com.vinithius.dex10.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.vinithius.dex10.R
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
    
    // Theme Theme colors
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryRed = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // BEGIN: ToggleTabs (Premium Segmented Style)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = cardColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // IVs Tab
                TabItem(
                    text = stringResource(R.string.ivs),
                    isSelected = showIVEditor,
                    onClick = { showIVEditor = true },
                    modifier = Modifier.weight(1f),
                    selectedColor = primaryRed,
                    unselectedContentColor = onSurfaceVariant
                )
                // EVs Tab
                TabItem(
                    text = stringResource(R.string.evs),
                    isSelected = !showIVEditor,
                    onClick = { showIVEditor = false },
                    modifier = Modifier.weight(1f),
                    selectedColor = primaryRed,
                    unselectedContentColor = onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (showIVEditor) {
            IVEditor(ivs = ivs, onChange = onIVsChanged)
        } else {
            EVEditor(evs = evs, onChange = onEVsChanged)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats preview (Summary Card)
        StatsPreview(
            baseStats = baseStats,
            ivs = ivs,
            evs = evs,
            nature = nature
        )
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color,
    unselectedContentColor: Color
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) selectedColor else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else unselectedContentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun IVEditor(
    ivs: IVs,
    onChange: (IVs) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.iv_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        
        StatSlider(
            label = stringResource(R.string.stat_hp_full),
            value = ivs.hp,
            onValueChange = { onChange(ivs.copy(hp = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = stringResource(R.string.stat_atk_full),
            value = ivs.atk,
            onValueChange = { onChange(ivs.copy(atk = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = stringResource(R.string.stat_def_full),
            value = ivs.def,
            onValueChange = { onChange(ivs.copy(def = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = stringResource(R.string.stat_spa_full),
            value = ivs.spa,
            onValueChange = { onChange(ivs.copy(spa = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = stringResource(R.string.stat_spd_full),
            value = ivs.spd,
            onValueChange = { onChange(ivs.copy(spd = it)) },
            range = 0f..31f
        )
        StatSlider(
            label = stringResource(R.string.stat_spe_full),
            value = ivs.spe,
            onValueChange = { onChange(ivs.copy(spe = it)) },
            range = 0f..31f
        )
        
        // Quick Set Buttons (Specific to IVs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { onChange(IVs()) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.max_all),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick = { onChange(IVs(0, 0, 0, 0, 0, 0)) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.min_all),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
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
                stringResource(R.string.ev_title),
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
            label = stringResource(R.string.stat_hp_full),
            value = evs.hp,
            onValueChange = { onChange(evs.withStat("hp", it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = stringResource(R.string.stat_atk_full),
            value = evs.atk,
            onValueChange = { onChange(evs.withStat("atk", it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = stringResource(R.string.stat_def_full),
            value = evs.def,
            onValueChange = { onChange(evs.withStat("def", it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = stringResource(R.string.stat_spa_full),
            value = evs.spa,
            onValueChange = { onChange(evs.withStat("spa", it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = stringResource(R.string.stat_spd_full),
            value = evs.spd,
            onValueChange = { onChange(evs.withStat("spd", it)) },
            range = 0f..252f,
            steps = 251
        )
        StatSlider(
            label = stringResource(R.string.stat_spe_full),
            value = evs.spe,
            onValueChange = { onChange(evs.withStat("spe", it)) },
            range = 0f..252f,
            steps = 251
        )
        
        // Common EV spreads
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.common_spreads),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EVSpreadButton(text = stringResource(R.string.physical), onClick = { onChange(EVs.SWEEPER_PHYSICAL) })
            EVSpreadButton(text = stringResource(R.string.special), onClick = { onChange(EVs.SWEEPER_SPECIAL) })
            EVSpreadButton(text = stringResource(R.string.wall), onClick = { onChange(EVs.WALL_PHYSICAL) })
        }
    }
}

@Composable
fun EVSpreadButton(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier.clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 30
) {
    // Colors from the premium design
    // Theme colors from tokens
    val primaryColor = MaterialTheme.colorScheme.primary
    val inputBackground = MaterialTheme.colorScheme.surfaceVariant
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(64.dp)
        )
        
        // Custom Slider
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = steps,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = trackColor,
                inactiveTrackColor = trackColor,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            thumb = {
                // Custom thin rectangular thumb like in the design
                Surface(
                    modifier = Modifier
                        .size(width = 8.dp, height = 20.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = primaryColor,
                    shadowElevation = 4.dp
                ) {}
            }
        )
        
        // Numeric Input Box
        Surface(
            modifier = Modifier
                .width(52.dp)
                .height(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = inputBackground
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    // Glass-card style container
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Semi-transparent for glass effect
        border = androidx.compose.foundation.BorderStroke(1.dp, onSurface.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.final_stats_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid layout for stats (2 columns)
            val stats = StatType.values().toList()
            val chunkedStats = stats.chunked(2)

            chunkedStats.forEachIndexed { rowIndex, rowStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowStats.forEachIndexed { colIndex, statType ->
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
                            (2 * base + iv + ev / 4) + 110
                        } else {
                            ((2 * base + iv + ev / 4) + 5) * natureModifier
                        }.toInt()

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    end = if (colIndex == 0) 16.dp else 0.dp,
                                    start = if (colIndex == 1) 16.dp else 0.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statType.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariant
                            )
                            Text(
                                text = finalStat.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    natureModifier > 1.0 -> Color(0xFF2ECC71) // Green for boosted
                                    natureModifier < 1.0 -> Color(0xFFE74C3C) // Red for decreased
                                    else -> onSurface
                                }
                            )
                        }
                        
                        // Add vertical divider if it's the first column
                        if (colIndex == 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(onSurface.copy(alpha = 0.1f))
                            )
                        }
                    }
                }
                if (rowIndex < chunkedStats.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
