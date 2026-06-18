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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.R
import com.vinithius.dex10.components.TypeItem
import com.vinithius.dex10.extension.LoadGifWithCoil
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.rememberPokemonViewModel

/**
 * Bottom sheet that lists every Pokémon belonging to a given [typeName].
 * Inspired by [PokemonPickerSheet] (Team Builder), reusing the same grid layout.
 * Each entry is clickable and navigates to that Pokémon's detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypePokemonSheet(
    typeName: String,
    onDismiss: () -> Unit,
    onPokemonSelected: (Int) -> Unit,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allPokemon by viewModel.pokemonListBackup.observeAsState(emptyList())
    val pokemonList = remember(typeName, allPokemon) {
        allPokemon.filter { pwd ->
            pwd.types.any { it.typeName.equals(typeName, ignoreCase = true) }
        }
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
            TypeItem(typeName)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.pokemon_of_type_fmt,
                    typeName.replaceFirstChar { it.uppercase() }
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (pokemonList.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_pokemon_of_type),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
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
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(4.dp)
                                ) {
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
}

