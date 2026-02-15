package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.extension.LoadGifWithCoil
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPickerSheet(
    onDismiss: () -> Unit,
    onPokemonSelected: (Int) -> Unit,
    viewModel: PokemonViewModel = getViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    // We can use the existing list from ViewModel
    // Note: getPokemonList() might be paginated or full load. 
    // Let's use pokemonListBackup which seems to be the full filtered list in MainActivity usage.
    // Observe the FILTERED list from ViewModel
    val pokemonList by viewModel.pokemonList.observeAsState(emptyList())
    val searchNameFilter by viewModel.searchNameFilter.observeAsState("")

    // Sync local query with ViewModel if needed, but here we want Picker to own its state
    // but trigger the VM filtering.
    
    // Trigger search when local query changes
    LaunchedEffect(searchQuery) {
        viewModel.getPokemonSearch(searchQuery, context)
    }

    // Cleanup search when sheet is dismissed
    LaunchedEffect(Unit) {
        // Optional: clear search on open
        // viewModel.getPokemonSearch("", context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Select Pokemon", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(items = pokemonList, key = { it.pokemon.id }) { pokemonWithTypes ->
                    Card(
                        modifier = Modifier
                            .height(120.dp)
                            .clickable { onPokemonSelected(pokemonWithTypes.pokemon.id) },
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.size(80.dp).padding(4.dp)) {
                                pokemonWithTypes.pokemon.id.LoadGifWithCoil(viewModel)
                            }
                            Text(
                                text = pokemonWithTypes.pokemon.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
