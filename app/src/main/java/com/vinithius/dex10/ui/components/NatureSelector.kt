package com.vinithius.dex10.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.datasource.model.Nature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatureSelector(
    selectedNature: Nature?,
    onNatureSelected: (Nature) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedNature?.displayName ?: "Select Nature",
            onValueChange = {},
            readOnly = true,
            label = { Text("Nature") },
            trailingIcon = {
                Column(horizontalAlignment = Alignment.End) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    selectedNature?.let {
                        Text(
                            text = it.getDescription(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            Divider()
            
            val filteredNatures = Nature.values().filter {
                it.displayName.contains(searchQuery, ignoreCase = true)
            }
            
            // Group: Neutral natures
            val neutralNatures = filteredNatures.filter { it.increasedStat == null }
            if (neutralNatures.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Neutral",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {},
                    enabled = false
                )
                neutralNatures.forEach { nature ->
                    DropdownMenuItem(
                        text = { 
                            Text(nature.displayName)
                        },
                        onClick = {
                            onNatureSelected(nature)
                            expanded = false
                            searchQuery = ""
                        }
                    )
                }
            }
            
            // Group: Beneficial natures
            val beneficialNatures = filteredNatures.filter { it.increasedStat != null }
            if (beneficialNatures.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Beneficial",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {},
                    enabled = false
                )
                beneficialNatures.forEach { nature ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(nature.displayName, fontWeight = FontWeight.Medium)
                                Text(
                                    nature.getDescription(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onNatureSelected(nature)
                            expanded = false
                            searchQuery = ""
                        }
                    )
                }
            }
        }
    }
}

/**
 * Simpler nature dropdown for compact views
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatureSelectorCompact(
    selectedNature: Nature?,
    onNatureSelected: (Nature) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedNature?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Nature") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            Nature.values().forEach { nature ->
                DropdownMenuItem(
                    text = { Text(nature.displayName) },
                    onClick = {
                        onNatureSelected(nature)
                        expanded = false
                    }
                )
            }
        }
    }
}
