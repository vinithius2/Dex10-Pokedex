package com.vinithius.poke10.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vinithius.poke10.R
import com.vinithius.poke10.ui.screens.PokemonDetailScreen
import com.vinithius.poke10.ui.screens.PokemonListScreen
import com.vinithius.poke10.ui.theme.ThemePoke10
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemePoke10 { MainScreen() }
        }
    }

    companion object {
        const val FAVORITES = "FAVORITES"
    }
}

@Composable
fun MainScreen(
    viewModel: PokemonViewModel = getViewModel()
) {
    SetupSystemUI()
    Scaffold(topBar = { GetTopBar(viewModel) }) { innerPadding -> GetNavHost(innerPadding) }
}

@Composable
fun SetupSystemUI() {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colors.primary
    systemUiController.setStatusBarColor(
        color = statusBarColor,
        darkIcons = MaterialTheme.colors.primary.luminance() > 0.5 // Ícones escuros para cores claras
    )
}

@Composable
private fun GetTopBar(
    viewModel: PokemonViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.pokemon_logo_small),
                contentDescription = "Poke10",
                modifier = Modifier.size(40.dp)
            )
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.getSearchPokemon(searchQuery)
                    }, // Adicionar callback aqui
                    placeholder = {
                        Text(
                            text = "${stringResource(R.string.search)}...",
                            color = MaterialTheme.colors.secondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { // Ícone a esquerda do TextField
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MaterialTheme.colors.secondary
                        )
                    },
                    trailingIcon = { // Ícone a direita do TextField
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = String()
                                viewModel.getSearchPokemon(searchQuery)
                                isSearchActive = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colors.secondary
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colors.primary,
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            if (isSearchActive.not()) {
                IconButton(onClick = { isSearchActive = isSearchActive.not() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)

                    )
                }
            }
            GetIconButtons(viewModel)
        }
    )
}

@Composable
private fun GetIconButtons(viewModel: PokemonViewModel) {
    var favoriteFilter by remember { mutableStateOf(false) }
    IconButton(onClick = {
        favoriteFilter = favoriteFilter.not()
        viewModel.getPokemonFavoriteList(favoriteFilter)
    }) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorito",
            tint = if (favoriteFilter) Color.Red else Color.White
        )
    }
}

@Composable
private fun GetNavHost(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "pokemonList",
        Modifier.padding(innerPadding)
    ) {
        composable("pokemonList") { PokemonListScreen(navController) }
        composable("pokemonDetail/{pokemonId}") { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getString("pokemonId")?.toInt()
            if (pokemonId != null) {
                PokemonDetailScreen(navController, pokemonId)
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
