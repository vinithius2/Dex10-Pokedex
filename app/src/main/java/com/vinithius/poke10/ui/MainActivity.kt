package com.vinithius.poke10.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.vinithius.poke10.R
import com.vinithius.poke10.ui.screens.PokemonDetailScreen
import com.vinithius.poke10.ui.screens.PokemonListScreen
import com.vinithius.poke10.ui.theme.ThemePoke10
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemePoke10 {
                MainScreen()
            }
        }
        showAds()
    }

    private fun showAds() {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@MainActivity) {}
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
    val statusBarColor = MaterialTheme.colorScheme.tertiary
    systemUiController.setStatusBarColor(
        color = statusBarColor,
        darkIcons = MaterialTheme.colorScheme.tertiary.luminance() > 0.5
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GetTopBar(
    viewModel: PokemonViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.getPokemonSearch(searchQuery)
                    },
                    placeholder = {
                        Text(
                            text = "${stringResource(R.string.search)}...",
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp, top = 4.dp)
                        .clip(RoundedCornerShape(40.dp)),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchQuery = String()
                            viewModel.getPokemonSearch(searchQuery)
                            isSearchActive = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedIndicatorColor = Color.Transparent, // Remove a linha quando não está focado
                        focusedIndicatorColor = Color.Transparent,  // Remove a linha quando está focado
                        disabledIndicatorColor = Color.Transparent  // Remove a linha quando desativado
                    ),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.pokemon_logo_small),
                    contentDescription = "Poke10",
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            if (isSearchActive.not()) {
                IconButton(onClick = { isSearchActive = isSearchActive.not() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
            AppMenu(context, viewModel)
        }
    )
}

@Composable
private fun AppMenu(
    context: Context,
    viewModel: PokemonViewModel
) {
    var favoriteFilter by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = {
        favoriteFilter = favoriteFilter.not()
        viewModel.getPokemonFavoriteList(favoriteFilter)
    }) {
        Icon(
            imageVector = if (favoriteFilter) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorito",
            tint = Color.White
        )
    }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropDownMenuRight(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onChangelogClick = { },
        onNightModeToggle = { },
        isNightMode = false,
        onShareAppClick = { },
        onInviteFriendsClick = { },
        onRateAppClick = { },
        onSuggestionsClick = {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://forms.gle/6Rbsv7voquN3AjbT8")
                )
            context.startActivity(intent)
        },
        onDonateClick = { }
    )
}

@Composable
private fun DropDownMenuRight(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onChangelogClick: () -> Unit,
    onNightModeToggle: (Boolean) -> Unit,
    isNightMode: Boolean,
    onShareAppClick: () -> Unit,
    onInviteFriendsClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onSuggestionsClick: () -> Unit,
    onDonateClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        // Categoria: Configurações
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenuItem(
            text = { Text("Modo Noturno: ${if (isNightMode) "Ativado" else "Desativado"}") },
            onClick = { onNightModeToggle(!isNightMode) }
        )
        DropdownMenuItem(
            text = { Text("Changelog (Novidades)") },
            onClick = onChangelogClick
        )
        Divider()

        // Categoria: Interação com o App
        Text(
            text = "Interação",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenuItem(
            text = { Text("Compartilhar App") },
            onClick = onShareAppClick
        )
        DropdownMenuItem(
            text = { Text("Avaliar App") },
            onClick = onRateAppClick
        )
        Divider()

        // Categoria: Feedback e Apoio
        Text(
            text = "Feedback e Apoio",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenuItem(
            text = { Text("Sugestões ou Bugs") },
            onClick = onSuggestionsClick
        )
        DropdownMenuItem(
            text = { Text("Doar para o Desenvolvedor ❤️") },
            onClick = onDonateClick
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
        GetTopBar(
            getViewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenDarkPreview() {
    ThemePoke10(darkTheme = true) {
        GetTopBar(
            getViewModel()
        )
    }
}
