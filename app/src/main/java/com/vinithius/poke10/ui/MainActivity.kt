package com.vinithius.poke10.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
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
                            contentDescription = stringResource(id = R.string.search_icon),
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
                                contentDescription = stringResource(id = R.string.clear_search),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.pokemon_logo_small),
                    contentDescription = stringResource(id = R.string.app_name),
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
            contentDescription = stringResource(id = R.string.favorite),
            tint = Color.White
        )
    }
    IconButton(onClick = { expanded = true }) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = stringResource(id = R.string.more_options)
        )
    }
    DropDownMenuRight(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onChangelogClick = { },
        onNightModeToggle = { },
        isNightMode = false,
        onShareAppClick = { },
        onInviteFriendsClick = { },
        onRateAppClick = {
            requestInAppReview(context)
        },
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
            text = stringResource(id = R.string.settings),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenuItem(
            text = {
                Text(
                    "${stringResource(id = R.string.night_mode)} ${
                        if (isNightMode) stringResource(
                            id = R.string.enabled
                        ) else stringResource(id = R.string.disabled)
                    }"
                )
            },
            onClick = { onNightModeToggle(!isNightMode) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.changelog_whats_new)) },
            onClick = onChangelogClick
        )
        Divider()

        // Categoria: Interação com o App
        Text(
            text = stringResource(id = R.string.interaction),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.share_app)) },
            onClick = onShareAppClick
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.rate_app)) },
            onClick = onRateAppClick
        )
        Divider()

        // Categoria: Feedback e Apoio
        Text(
            text = stringResource(id = R.string.feedback_and_support),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.suggestions_or_bugs)) },
            onClick = onSuggestionsClick
        )

        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.donate_to_developer)) },
            onClick = onDonateClick
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GetNavHost(innerPadding: PaddingValues) {
    SharedTransitionLayout {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "pokemonList",
            Modifier.padding(innerPadding)
        ) {
            composable("pokemonList") {
                PokemonListScreen(
                    navController,
                    this
                )
            }
            composable(
                route = "pokemonDetail/{pokemonId}/{pokemonName}",
                arguments = listOf(
                    navArgument("pokemonId") { type = NavType.StringType },
                    navArgument("pokemonName") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val pokemonId = backStackEntry.arguments?.getString("pokemonId")?.toIntOrNull()
                val pokemonName = backStackEntry.arguments?.getString("pokemonName")

                if (pokemonId != null && pokemonName != null) {
                    PokemonDetailScreen(
                        navController,
                        pokemonId,
                        pokemonName,
                        this
                    )
                }
            }

        }
    }
}

fun requestInAppReview(context: Context) {
    val reviewManager: ReviewManager = ReviewManagerFactory.create(context)
    val requestFlow = reviewManager.requestReviewFlow()
    requestFlow.addOnCompleteListener { requestTask ->
        if (requestTask.isSuccessful) {
            val reviewInfo = requestTask.result
            val flow = reviewManager.launchReviewFlow(context as Activity, reviewInfo)
            flow.addOnCompleteListener { _ ->
                // Do nothing
            }
        } else {
            val exception = requestTask.exception
            exception?.printStackTrace()
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
