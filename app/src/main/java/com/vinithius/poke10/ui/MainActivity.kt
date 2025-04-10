package com.vinithius.poke10.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.vinithius.poke10.BuildConfig
import com.vinithius.poke10.R
import com.vinithius.poke10.admobbanners.AdManagerInterstitial
import com.vinithius.poke10.admobbanners.AdManagerRewarded
import com.vinithius.poke10.admobbanners.AdmobBanner
import com.vinithius.poke10.extension.getColorByString
import com.vinithius.poke10.extension.getToolBarColorByString
import com.vinithius.poke10.ui.screens.PokemonDetailScreen
import com.vinithius.poke10.ui.screens.PokemonListScreen
import com.vinithius.poke10.ui.theme.ThemePoke10
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import com.vinithius.poke10.ui.viewmodel.RequestStateDetail
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    private lateinit var analytics: FirebaseAnalytics

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemePoke10 {
                MainScreen(
                    activity = this@MainActivity
                )
            }
        }
        analytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this@MainActivity)
        requestNotificationPermission()
        pushNotification()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.i("permission", "Notification permission granted")
            } else {
                Log.i("permission", "Notification permission denied")
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun pushNotification() {
        // Criação do canal de notificação
        getFID()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Default channel for notifications"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getFID() {
        FirebaseInstallations.getInstance().id
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fid = task.result
                    Log.i("FID", fid)
                } else {
                    Log.e("Error FID", task.exception?.message ?: "Error not found")
                }
            }
    }

    fun trackButtonClick(buttonName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName)
        analytics.logEvent("button_click", bundle)
    }

    fun trackScreenView(screenName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
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
                val isRewarded = remoteConfig.getBoolean("isRewarded")
                val adUnitIdChoiceOfTheDayInterstitial =
                    remoteConfig.getString("adUnitId_choiceOfTheDay_interstitial")
                val adUnitIdChoiceOfTheDayRewarded =
                    remoteConfig.getString("adUnitId_choiceOfTheDay_rewarded")

                viewModel.setAdUnitIdList(adUnitIdList)
                viewModel.setAdUnitIdDetails(adUnitIdDetails)
                viewModel.setIsRewarded(isRewarded)
                viewModel.setAdUnitIdChoiceOfTheDayInterstitial(adUnitIdChoiceOfTheDayInterstitial)
                viewModel.setAdUnitIdChoiceOfTheDayRewarded(adUnitIdChoiceOfTheDayRewarded)

                // Social media

                val facebookUrl = remoteConfig.getString("facebook_url")
                val instagranUrl = remoteConfig.getString("instagran_url")
                val redditUrl = remoteConfig.getString("reddit_url")
                val googleForm = remoteConfig.getString("google_form")
                val paypalId = remoteConfig.getString("paypal_id")

                viewModel.setFacebookUrl(facebookUrl)
                viewModel.setInstagranUrl(instagranUrl)
                viewModel.setRedditUrl(redditUrl)
                viewModel.setGoogleForm(googleForm)
                viewModel.setPaypalId(paypalId)
            }
        }
}

@Composable
fun MainScreen(
    activity: MainActivity,
    viewModel: PokemonViewModel = getViewModel()
) {
    val navController = rememberNavController()
    GetAdUnitId()
    SetInterstitialOrRewardedAdManager(activity, navController)
    SetupSystemUI(viewModel)
    Scaffold(
        topBar = {
            GetTopBar(viewModel, navController)
        },
        bottomBar = {
            AdmobBanner()
        }
    ) { innerPadding ->
        GetNavHost(
            innerPadding,
            navController
        )
    }
}

@Composable
fun SetInterstitialOrRewardedAdManager(
    activity: MainActivity,
    navController: NavHostController,
    viewModel: PokemonViewModel = getViewModel()
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pokemon_prefs", Context.MODE_PRIVATE)
    val isRewarded by viewModel.isRewarded.observeAsState(true)

    if (isRewarded) {
        with(sharedPreferences.edit()) {
            putBoolean("is_rewarded", true)
            apply()
        }


        val adManagerRewarded = remember { AdManagerRewarded(context) }
        val adUnitId by viewModel.adUnitIdChoiceOfTheDayRewarded.observeAsState()
        val isShowingRewarded by viewModel.choiceOfTheDayRewardedShow.observeAsState(false)
        val isAdLoadedRewarded by viewModel.isAdLoadedRewarded.observeAsState(false)
        val adDataToDetails by viewModel.adDataToDetails.observeAsState()
        val navigationTriggered = remember { mutableStateOf(false) }

        LaunchedEffect(adUnitId) {
            adUnitId?.let {
                getRewarded(it, adManagerRewarded) {
                    viewModel.setIsAdLoadedRewarded(true)
                }
            }
        }
        LaunchedEffect(isShowingRewarded) {
            if (isShowingRewarded) {
                if (isAdLoadedRewarded) {
                    adManagerRewarded.showAd(activity) {
                        with(sharedPreferences.edit()) {
                            putBoolean("hide_pokemon_of_the_day", false)
                            apply()
                        }
                        viewModel.setHidePokemonOfTheDay(false)
                        gotToDetails(
                            adDataToDetails,
                            activity,
                            navController,
                            viewModel,
                        )
                        viewModel.adUnitIdChoiceOfTheDayRewardedShow(false)
                        viewModel.setIsAdLoadedRewarded(false)
                    }
                } else {
                    gotToDetails(
                        adDataToDetails,
                        activity,
                        navController,
                        viewModel,
                    )
                    viewModel.adUnitIdChoiceOfTheDayRewardedShow(false)
                }
            }
        }


    } else {
        with(sharedPreferences.edit()) {
            putBoolean("is_rewarded", false)
            apply()
        }

        val adManagerInterstitial = remember { AdManagerInterstitial(context) }
        val adUnitId by viewModel.adUnitIdChoiceOfTheDayInterstitial.observeAsState()
        val isShowingInterstitial by viewModel.choiceOfTheDayInterstitialShow.observeAsState(false)
        val isAdLoadedInterstitial by viewModel.isAdLoadedInterstitial.observeAsState(false)

        LaunchedEffect(adUnitId) {
            adUnitId?.let {
                getInterstitial(it, adManagerInterstitial) {
                    viewModel.setIsAdLoadedInterstitial(true)
                }
            }
        }

        LaunchedEffect(isShowingInterstitial, isAdLoadedInterstitial) {
            if (isShowingInterstitial && isAdLoadedInterstitial) {
                adManagerInterstitial.showAd(activity)
                with(sharedPreferences.edit()) {
                    putBoolean("hide_pokemon_of_the_day", false)
                    apply()
                }
                with(viewModel) {
                    viewModel.setHidePokemonOfTheDay(false)
                    adUnitIdChoiceOfTheDayInterstitialShow(false)
                }
            }
        }
    }
}

private fun gotToDetails(
    adDataToDetails: PokemonViewModel.AdData?,
    activity: MainActivity,
    navController: NavHostController,
    viewModel: PokemonViewModel,
) {
    adDataToDetails?.run {
        activity.trackButtonClick("Click button detail: $name")
        viewModel.setIdPokemon(id)
        viewModel.setChoiceOfTheDay(choiceOfTheDayStatus)
        navController.navigate("pokemonDetail/$id/$name/$color")
    }
}

private fun getRewarded(
    adUnitIdChoiceOfTheDayRewarded: String?,
    adManagerRewarded: AdManagerRewarded,
    adUnitIdChoiceOfTheDayTestRewarded: String = "ca-app-pub-3940256099942544/5224354917", // Test
    callbackOnAdLoaded: () -> Unit,
) {
    if (adUnitIdChoiceOfTheDayRewarded.isNullOrEmpty().not()) {
        adManagerRewarded.adUnitId =
            if (BuildConfig.DEBUG) {
                adUnitIdChoiceOfTheDayTestRewarded
            } else {
                adUnitIdChoiceOfTheDayRewarded!!
            }
        adManagerRewarded.loadAd(
            onAdLoaded = {
                callbackOnAdLoaded.invoke()
            }
        )
    }
}

private fun getInterstitial(
    adUnitIdChoiceOfTheDayInterstitial: String?,
    adManagerInterstitial: AdManagerInterstitial,
    adUnitIdChoiceOfTheDayTestInterstitial: String = "ca-app-pub-3940256099942544/1033173712", // Test
    callbackOnAdLoaded: () -> Unit,
) {
    if (adUnitIdChoiceOfTheDayInterstitial.isNullOrEmpty().not()) {
        adManagerInterstitial.adUnitId = if (BuildConfig.DEBUG) {
            adUnitIdChoiceOfTheDayTestInterstitial
        } else {
            adUnitIdChoiceOfTheDayInterstitial!!
        }
        adManagerInterstitial.loadAd(
            onAdLoaded = {
                callbackOnAdLoaded.invoke()
            }
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
    val activity = context as? MainActivity
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val isDetailsScreen by viewModel.isDetailScreen.observeAsState()
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
                        activity?.trackButtonClick("Back from detail to list")
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
                            IconButton(onClick = {
                                activity?.trackButtonClick("Click search filter")
                                isSearchActive = isSearchActive.not()
                            }) {
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
    val activity = context as? MainActivity
    val favoriteFilter by viewModel.isFavoriteFilter.observeAsState(false)
    val googleForm by viewModel.googleForm.observeAsState()
    val paypalId by viewModel.paypalId.observeAsState()
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = {
        activity?.trackButtonClick("Menu toolbar: favorites -> ${favoriteFilter.not()}")
        viewModel.getPokemonFavoriteList(favoriteFilter.not())
    }) {
        Icon(
            imageVector = if (favoriteFilter) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = stringResource(id = R.string.favorite),
            tint = Color.White
        )
    }
    IconButton(onClick = {
        expanded = true
        activity?.trackButtonClick("Menu toolbar: 3 dots")
    }) {
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
            activity?.trackButtonClick("Menu toolbar: share app")
            shareApp(context)
        },
        onRateAppClick = {
            activity?.trackButtonClick("Menu toolbar: rate app")
            requestInAppReview(context)
        },
        onSuggestionsClick = {
            googleForm?.takeIf { it.isNotEmpty() }?.run {
                activity?.trackButtonClick("Menu toolbar: google form")
                suggestionsClick(googleForm!!, context)
            }
        },
        onDonateClick = {
            paypalId?.takeIf { it.isNotEmpty() }?.run {
                activity?.trackButtonClick("Menu toolbar: donate")
                donateClick(paypalId!!, context)
            }
        },
        onInstagranClick = { url ->
            activity?.trackButtonClick("Menu toolbar: instagran")
            instagranClick(url, context)
        },
        onFacebookClick = { url ->
            activity?.trackButtonClick("Menu toolbar: facebook")
            facebookClick(url, context)
        },
        onRedditClick = { url ->
            activity?.trackButtonClick("Menu toolbar: reddit")
            redditClick(url, context)
        }
    )
}

@Composable
private fun AppMenuPageDetail(
    context: Context,
    viewModel: PokemonViewModel
) {
    val activity = context as? MainActivity
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
                idPokemon?.let {
                    activity?.trackButtonClick("Click favorite toolbar detail: ID -> $it")
                    viewModel.setFavorite(it)
                }
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
                    navArgument("pokemonColor") { type = NavType.StringType },
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

fun suggestionsClick(googleForm: String, context: Context) {
    getIntentToUrl(googleForm, context)
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

fun donateClick(paypalId: String, context: Context) {
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.paypal.com/donate/?hosted_button_id=$paypalId")
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
