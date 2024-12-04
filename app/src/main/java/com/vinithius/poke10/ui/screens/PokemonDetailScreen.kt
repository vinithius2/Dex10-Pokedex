package com.vinithius.poke10.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

private val imagePath =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/3.gif"
private val dotGif = ".gif"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PokemonDetailScreen(
    navController: NavController?,
    pokemonId: Int,
    pokemonName: String,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    viewModel: PokemonViewModel = getViewModel()
) {
    val painter = viewModel.getSharedImage(pokemonId.toString())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = pokemonName,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(180.dp)
                    .sharedElement(
                        state = rememberSharedContentState(key = "$pokemonId"),
                        animatedVisibilityScope = animatedVisibilityScope!!,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 1000)
                        }
                    )
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun PokemonDetailScreenPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            PokemonDetailScreen(
                null,
                1,
                "Bulbasauro",
                null,
            )
        }
    }
}