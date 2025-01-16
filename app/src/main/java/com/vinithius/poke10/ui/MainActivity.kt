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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.vinithius.poke10.R
import com.vinithius.poke10.extension.getColorByString
import com.vinithius.poke10.extension.getToolBarColorByString
import com.vinithius.poke10.ui.screens.PokemonDetailScreen
import com.vinithius.poke10.ui.screens.PokemonListScreen
import com.vinithius.poke10.ui.theme.ThemePoke10
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import com.vinithius.poke10.ui.viewmodel.RequestStateDetail
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemePoke10 {
                MainScreen()
            }
        }
        MobileAds.initialize(this@MainActivity) {
            // Do nothing
        }
    }

    companion object {
        const val FAVORITES = "FAVORITES"
        const val MAX_POKEMONS = "MAX_POKEMONS"
    }
}

@Composable
private fun GetAdUnitId(viewModel: PokemonViewModel = getViewModel()) {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    remoteConfig.fetchAndActivate()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Ads
                val adUnitIdList = remoteConfig.getString("adUnitId_list")
                val adUnitIdDetails = remoteConfig.getString("adUnitId_details")
                viewModel.setAdUnitIdList(adUnitIdList)
                viewModel.setAdUnitIdDetails(adUnitIdDetails)
                // Social media
                val facebookUrl = remoteConfig.getString("facebook_url")
                val instagranUrl = remoteConfig.getString("instagran_url")
                val redditUrl = remoteConfig.getString("reddit_url")
                viewModel.setFacebookUrl(facebookUrl)
                viewModel.setInstagranUrl(instagranUrl)
                viewModel.setRedditUrl(redditUrl)
            }
        }
}

@Composable
fun MainScreen(
    viewModel: PokemonViewModel = getViewModel()
) {
    GetAdUnitId()
    SetupSystemUI(viewModel)
    val navController = rememberNavController()
    Scaffold(topBar = { GetTopBar(viewModel, navController) }) { innerPadding ->
        GetNavHost(
            innerPadding,
            navController
        )
    }
}

@Composable
fun SetupSystemUI(viewModel: PokemonViewModel) {
    val systemUiController = rememberSystemUiController()
    val color by viewModel.pokemonColor.observeAsState()
    val statusBarColor = color?.getColorByString() ?: MaterialTheme.colorScheme.tertiary
    systemUiController.setStatusBarColor(
        color = statusBarColor,
        darkIcons = statusBarColor.luminance() > 0.5
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GetTopBar(
    viewModel: PokemonViewModel,
    navController: NavHostController?
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val isDetailsScreen by viewModel.isDetailScreen.observeAsState()
    val pokemonItems by viewModel.pokemonList.observeAsState(emptyList())
    val pokemonListBackup by viewModel.pokemonListBackup.observeAsState(emptyList())
    val color by viewModel.pokemonColor.observeAsState()

    if (isDetailsScreen != null) {
        isDetailsScreen?.takeIf { it }?.run {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.setDetailsScreen(false)
                        navController?.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = color?.getToolBarColorByString()
                        ?: MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    scrolledContainerColor = color?.getToolBarColorByString()
                        ?: MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    AppMenuPageDetail(
                        context,
                        viewModel
                    )
                }
            )
        } ?: run {
            // Page List Pokemon
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
                    if (pokemonListBackup.isNotEmpty()) {
                        if (isSearchActive.not()) {
                            IconButton(onClick = { isSearchActive = isSearchActive.not() }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                        }
                        AppMenuPageList(
                            context,
                            viewModel
                        )
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun GetTopBatPreviewList(
    viewModel: PokemonViewModel = getViewModel()
) {
    viewModel.setDetailsScreen(false)
    GetTopBar(
        viewModel,
        null
    )
}

@Preview
@Composable
private fun GetTopBatPreviewDetail(
    viewModel: PokemonViewModel = getViewModel()
) {
    viewModel.setDetailsScreen(true)
    GetTopBar(
        viewModel,
        null
    )
}

@Composable
private fun AppMenuPageList(
    context: Context,
    viewModel: PokemonViewModel
) {
    val favoriteFilter by viewModel.isFavoriteFilter.observeAsState(false)
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = {
        viewModel.getPokemonFavoriteList(favoriteFilter.not())
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
        viewModel = viewModel,
        onDismissRequest = { expanded = false },
        onShareAppClick = {
            shareApp(context)
        },
        onRateAppClick = {
            requestInAppReview(context)
        },
        onSuggestionsClick = {
            suggestionsClick(context)
        },
        onDonateClick = {
            donateClick(context)
        },
        onInstagranClick = { url ->
            instagranClick(url, context)
        },
        onFacebookClick = { url ->
            facebookClick(url, context)
        },
        onRedditClick = { url ->
            redditClick(url, context)
        }
    )
}

@Composable
private fun AppMenuPageDetail(
    context: Context,
    viewModel: PokemonViewModel
) {
    val requestState by viewModel.stateDetail.observeAsState(RequestStateDetail.Loading)
    val isDetailFavorite by viewModel.isDetailFavorite.observeAsState(false)
    val idPokemon by viewModel.idPokemon.observeAsState()
    when (requestState) {
        is RequestStateDetail.Loading -> {
            Box(
                modifier = Modifier
                    .size(70.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                )
            }
        }

        is RequestStateDetail.Success -> {
            IconButton(onClick = {
                idPokemon?.let { viewModel.setFavorite(it) }
            }) {
                Icon(
                    imageVector = if (isDetailFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.favorite),
                    tint = Color.White
                )
            }
        }

        is RequestStateDetail.Error -> {
            // Do nothing
        }
    }
}

@Composable
private fun DropDownMenuRight(
    expanded: Boolean,
    viewModel: PokemonViewModel,
    onDismissRequest: () -> Unit,
    onShareAppClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onSuggestionsClick: () -> Unit,
    onDonateClick: () -> Unit,
    onInstagranClick: (url: String) -> Unit,
    onFacebookClick: (url: String) -> Unit,
    onRedditClick: (url: String) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
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
        HorizontalDivider()
        // Feedback and Support
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
        HorizontalDivider()
        // Social media
        Text(
            text = stringResource(id = R.string.social_media),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        with(viewModel) {
            val facebookUrl by facebookUrl.observeAsState()
            val instagranUrl by instagranUrl.observeAsState()
            val redditUrl by redditUrl.observeAsState()
            if (instagranUrl.isNullOrEmpty().not()) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.instagran)) },
                    onClick = { instagranUrl?.let { onInstagranClick(it) } }
                )
            }
            if (facebookUrl.isNullOrEmpty().not()) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.facebook)) },
                    onClick = { facebookUrl?.let { onFacebookClick(it) } }
                )
            }
            if (redditUrl.isNullOrEmpty().not()) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.reddit)) },
                    onClick = { redditUrl?.let { onRedditClick(it) } }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GetNavHost(
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "pokemonList",
            Modifier.padding(innerPadding)
        ) {
            composable("pokemonList") {
                //
                PokemonListScreen(
                    navController,
                    this
                )
            }
            composable(
                route = "pokemonDetail/{pokemonId}/{pokemonName}/{pokemonColor}",
                arguments = listOf(
                    navArgument("pokemonId") { type = NavType.StringType },
                    navArgument("pokemonName") { type = NavType.StringType },
                    navArgument("pokemonColor") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val pokemonId = backStackEntry.arguments?.getString("pokemonId")?.toIntOrNull()
                val pokemonName = backStackEntry.arguments?.getString("pokemonName")
                val pokemonColor = backStackEntry.arguments?.getString("pokemonColor")

                if (pokemonId != null && pokemonName != null && pokemonColor != null) {
                    PokemonDetailScreen(
                        navController,
                        pokemonId,
                        pokemonName,
                        pokemonColor,
                        this
                    )
                }
            }

        }
    }
}

fun shareApp(context: Context) {
    val appPackageName = context.packageName
    val appPlayStoreLink = "https://play.google.com/store/apps/details?id=$appPackageName"
    val shareMessage = String.format(
        context.getString(R.string.share_app_message),
        appPlayStoreLink
    )
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareMessage)
        type = "text/plain"
    }
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.share_app_via)
        )
    )
}

fun requestInAppReview(context: Context) {
    val reviewManager = ReviewManagerFactory.create(context.applicationContext)
    reviewManager.requestReviewFlow().addOnCompleteListener {
        if (it.isSuccessful) {
            reviewManager.launchReviewFlow(context as Activity, it.result)
        }
    }
}

fun suggestionsClick(context: Context) {
    val url = "https://forms.gle/6Rbsv7voquN3AjbT8"
    getIntentToUrl(url, context)
}

fun instagranClick(url: String, context: Context) {
    getIntentToUrl(url, context)
}

fun facebookClick(url: String, context: Context) {
    getIntentToUrl(url, context)
}

fun redditClick(url: String, context: Context) {
    getIntentToUrl(url, context)
}

fun getIntentToUrl(url: String, context: Context) {
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
    context.startActivity(intent)
}

fun donateClick(context: Context) {
    val idButton = "48SNSQLTQ87HS"
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.paypal.com/donate/?hosted_button_id=$idButton")
        )
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ThemePoke10 {
        GetTopBar(
            getViewModel(),
            null,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenDarkPreview() {
    ThemePoke10(darkTheme = true) {
        GetTopBar(
            getViewModel(),
            null,
        )
    }
}
