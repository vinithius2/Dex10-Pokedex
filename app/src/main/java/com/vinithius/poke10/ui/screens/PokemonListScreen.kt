package com.vinithius.poke10.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vinithius.poke10.components.PokeballComponent
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonViewModel = getViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getPokemonList(context)
    }
    val pokemonItems by viewModel.pokemonList.observeAsState(emptyList())
    val isFavoriteFilter by viewModel.isFavoriteFilter.observeAsState(false)

    LazyColumn {
        items(
            items = pokemonItems,
            key = { pokemon -> pokemon.id!! }
        ) { pokemon ->
            var isVisible by remember { mutableStateOf(true) }
            AnimatedVisibility(
                visible = isVisible,
                exit = scaleOut(animationSpec = tween(durationMillis = 300))
            ) {
                PokemonListItem(
                    pokemon = pokemon,
                    onCallBackFinishAnimation = {
                        if (isFavoriteFilter) {
                            isVisible = false
                            viewModel.removeItemIfNotIsFavorite()
                        }
                    },
                    onClickDetail = { id ->
                        viewModel.setIdPokemon(id)
                        navController.navigate("pokemonDetail/$id")
                    },
                    onClickFavorite = { pokemonFavorite ->
                        viewModel.setFavorite(pokemonFavorite, context)
                    }
                )
            }
        }
    }
}

@Composable
fun PokemonListItem(
    pokemon: Pokemon,
    onCallBackFinishAnimation: (() -> Unit)?,
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
        Holder(pokemon, onClickFavorite, onCallBackFinishAnimation)
    }
}

@Composable
fun Holder(
    pokemon: Pokemon,
    onClickFavorite: ((Pokemon) -> Unit)?,
    onCallBackFinishAnimation: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = pokemon.name, modifier = Modifier.weight(1f))
        PokeballComponent(
            favorite = pokemon.favorite,
            onCallBackFinishAnimation = {
                onCallBackFinishAnimation?.invoke()
            }
        ) {
            onClickFavorite?.run {
                onClickFavorite(pokemon)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonListScreenPreview() {
    PokemonListItem(MockuPokemon(), null, null, null)
}

private fun MockuPokemon(): Pokemon {
    return Pokemon(
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
        listOf(),
        false
    )
}
