package com.vinithius.poke10.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vinithius.poke10.ui.screens.PokemonDetailScreen
import com.vinithius.poke10.ui.screens.PokemonListScreen
import com.vinithius.poke10.ui.theme.ThemePoke10
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemePoke10 {
                MainScreen()
            }
        }
    }

    companion object {
        const val FAVORITES = "FAVORITES"
    }
}

@Composable
fun MainScreen(viewModel: PokemonViewModel = getViewModel()) {
    var favoriteFilter = false
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Pokémon App", color = Color.White)
                },
                backgroundColor = MaterialTheme.colors.primary,
                actions = {
                    IconButton(onClick = {
                        favoriteFilter = favoriteFilter.not()
                        viewModel.getPokemonFavoriteList(favoriteFilter)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorito",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "pokemonList",
            Modifier.padding(innerPadding)
        ) {
            composable("pokemonList") { PokemonListScreen(navController) }
            // Definindo a rota pokemonDetail com parâmetro
            composable("pokemonDetail/{pokemonId}") { backStackEntry ->
                val pokemonId = backStackEntry.arguments?.getString("pokemonId")?.toInt()
                if (pokemonId != null) {
                    PokemonDetailScreen(navController, pokemonId)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ThemePoke10 {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenDarkPreview() {
    ThemePoke10(darkTheme = true) {
        MainScreen()
    }
}
