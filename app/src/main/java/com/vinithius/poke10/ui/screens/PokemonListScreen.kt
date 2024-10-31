package com.vinithius.poke10.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonViewModel = getViewModel()
) {
    // Chama a função para obter a lista de pokémons
    LaunchedEffect(Unit) {
        viewModel.getPokemonList()  // Chama a função para buscar os Pokémons
    }

    val pokemonItems = viewModel.pokemonList.observeAsState(emptyList())
    val context = LocalContext.current
    LazyColumn {
        items(pokemonItems.value) { pokemon ->
            PokemonListItem(
                pokemon = pokemon,
                onClickDetail = { id ->
                    viewModel.setIdPokemon(id)
                    navController.navigate("pokemonDetail/$id")
                },
                onClickFavorite = { pokemonClicked ->
                    viewModel.setFavorite(pokemonClicked, context)
                }
            )
        }
    }
}

@Composable
fun PokemonListItem(
    pokemon: Pokemon,
    onClickDetail: ((Int) -> Unit)?,
    onClickFavorite: ((Pokemon) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                pokemon.id?.let {
                    if (onClickDetail != null) {
                        onClickDetail(it)
                    }
                }
            },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = pokemon.name, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                if (onClickFavorite != null) {
                    onClickFavorite(pokemon)
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonListScreenPreview() {
    val test = Pokemon(
        0,
        "Pikachu",
        "https://pokeapi.co/api/v2/pokemon/25/",
        0,
        0,
        0,
        null,
        null,
        null,
        null,
        emptyList(),
        null,
        null,
        null,
        listOf()
    )
    PokemonListItem(test, null, null)
    // Preview não mostra dados reais, pois depende do ViewModel e da coleta de Paging.
}
