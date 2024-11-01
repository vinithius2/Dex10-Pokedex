package com.vinithius.poke10.ui

import android.content.Context
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemePoke10 {
                MainScreen()
            }
        }
    }

    /**
     * Add the boolean values of the favorite.
     */
    private fun setFavorite(name: String, value: Boolean) {
        val sharedPreferences = this.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(name, value)
            apply()
        }
    }

    /**
     * Get favorite preferences and add into global parameter favorite.
     */
    private fun getFavorite(name: String): Boolean {
        val sharedPreferences = this.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(name, false)
    }

    companion object {
        const val FAVORITES = "FAVORITES"
    }
}

@Composable
fun MainScreen() {
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
                        /* Ação de filtro de favoritos */
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
            composable("pokemonDetail") { PokemonDetailScreen(navController) }
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
