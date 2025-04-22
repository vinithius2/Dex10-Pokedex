package com.vinithius.dex10.ui

import AlertMessage
import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.vinithius.dex10.BuildConfig
import com.vinithius.dex10.R
import com.vinithius.dex10.admobbanners.AdManagerInterstitial
import com.vinithius.dex10.admobbanners.AdManagerRewarded
import com.vinithius.dex10.admobbanners.AdmobBanner
import com.vinithius.dex10.extension.getColorByString
import com.vinithius.dex10.extension.getToolBarColorByString
import com.vinithius.dex10.ui.screens.PokemonDetailScreen
import com.vinithius.dex10.ui.screens.PokemonListScreen
import com.vinithius.dex10.ui.theme.ThemeDex10
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.RequestStateDetail
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class MainActivity : ComponentActivity() {

    private lateinit var analytics: FirebaseAnalytics
    private val viewModel: PokemonViewModel by viewModel()
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("pokemon_prefs", MODE_PRIVATE)

        val splashScreen = installSplashScreen()

        setContent {
            ThemeDex10 {
                MainScreen(
                    activity = this@MainActivity
                )
            }
        }
        analytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this@MainActivity)

        requestNotificationPermission()
        pushNotification()
        downloadTranslationModelIfSupported(
            onDownloaded = {
                Log.d("MLKit", "Translation template now available or successfully downloaded.")
            },
            onError = {
                Log.e("MLKit", "Error downloading translation template", it)
            }
        )


        // Processar deeplink recebido ao iniciar a Activity
        handleDeeplink(intent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Processar deeplink recebido enquanto o app já está aberto
        handleDeeplink(intent)
    }

    // Metodo para processar deeplinks
    private fun handleDeeplink(intent: Intent?) {
        intent?.data?.let { uri ->
            when (uri.host) {
                "details" -> {
                    val id = uri.getQueryParameter("id")
                    if (id != null) {
                        // Passar a rota de navegação para o ViewModel
                        viewModel.setDeeplinkNavigation("pokemonDetail/$id")
                    } else {
                        Log.e("Deeplink", "ID do Pokémon não encontrado no deeplink")
                    }
                }

                else -> {
                    Log.w("Deeplink", "Host desconhecido no deeplink: ${uri.host}")
                }
            }
        }
    }

    /**
     * Realiza o download do modelo de tradução do inglês para o idioma do dispositivo,
     * se este for um dos suportados: Português, Espanhol, Francês ou Hindi.
     *
     * @param onDownloaded Callback chamado quando o modelo estiver disponível.
     * @param onError Callback chamado se houver erro no download.
     */
    fun downloadTranslationModelIfSupported(
        onDownloaded: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val deviceLanguage = Locale.getDefault().language
        val supportedLanguages = setOf("pt", "es", "fr", "hi")

        if (supportedLanguages.contains(deviceLanguage)) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(deviceLanguage)
                .build()

            val translator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder()
                .build()

            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    onDownloaded()
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        } else {
            // Ignora se o idioma não for suportado
            onDownloaded()
        }
    }

    companion object {
        const val FAVORITES = "FAVORITES"
        const val MAX_POKEMONS = "MAX_POKEMONS"
    }
}

@Composable
private fun GetAdUnitId(
    activity: MainActivity,
    viewModel: PokemonViewModel = getViewModel()
) {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    remoteConfig.fetchAndActivate()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Ads
                val adUnitIdList = remoteConfig.getString("adUnitId_banner")
                val isRewarded = remoteConfig.getBoolean("isRewarded")
                val adUnitIdChoiceOfTheDayInterstitial =
                    remoteConfig.getString("adUnitId_choiceOfTheDay_interstitial")
                val adUnitIdChoiceOfTheDayRewarded =
                    remoteConfig.getString("adUnitId_choiceOfTheDay_rewarded")
                // Viewmodel set
                viewModel.setAdUnitIdList(adUnitIdList)
                viewModel.setIsRewarded(isRewarded)
                viewModel.setAdUnitIdChoiceOfTheDayInterstitial(adUnitIdChoiceOfTheDayInterstitial)
                viewModel.setAdUnitIdChoiceOfTheDayRewarded(adUnitIdChoiceOfTheDayRewarded)
                // Social media
                val facebookUrl = remoteConfig.getString("facebook_url")
                val instagranUrl = remoteConfig.getString("instagran_url")
                val redditUrl = remoteConfig.getString("reddit_url")
                val googleForm = remoteConfig.getString("google_form")
                val paypalId = remoteConfig.getString("paypal_id")
                // Viewmodel set
                viewModel.setFacebookUrl(facebookUrl)
                viewModel.setInstagranUrl(instagranUrl)
                viewModel.setRedditUrl(redditUrl)
                viewModel.setGoogleForm(googleForm)
                viewModel.setPaypalId(paypalId)
                // ALERT MESSAGE
                try {
                    val alertMessageJson = remoteConfig.getString("alert_message")
                    val alertMessageData =
                        Gson().fromJson(alertMessageJson, AlertMessage::class.java)
                    val languageCode = Locale.getDefault().language
                    val localized = alertMessageData.getLocalizedContent(languageCode)
                    val localizedAlert = AlertMessage(
                        show = alertMessageData.show,
                        version_code = alertMessageData.version_code,
                        url_action = alertMessageData.url_action,
                        content = mapOf(languageCode to localized)
                    )
                    viewModel.setTopAlertMessage(localizedAlert)
                } catch (e: Exception) {
                    Log.e("RemoteConfig", "Error converting JSON to AlertMessage", e)
                }
            }
        }
}

@Composable
fun MainScreen(
    activity: MainActivity,
    viewModel: PokemonViewModel = getViewModel()
) {
    val navController = rememberNavController()
    val deeplinkRoute by viewModel.deeplinkNavigation.observeAsState()

    // Deeplink
    LaunchedEffect(deeplinkRoute) {
        deeplinkRoute?.let { route ->
            navController.navigate(route)
            viewModel.clearDeeplinkNavigation()
        }
    }

    GetAdUnitId(activity)
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
        activity.trackButtonClick("Click button detail ID: $id")
        viewModel.setIdPokemon(id)
        viewModel.setChoiceOfTheDay(choiceOfTheDayStatus)
        navController.navigate("pokemonDetail/$id")
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
    val pokemonListBackup by viewModel.pokemonListBackup.observeAsState(emptyList())
    val color by viewModel.pokemonColor.observeAsState()

    val navBackStackEntry = navController?.currentBackStackEntryAsState()?.value
    val currentRoute = navBackStackEntry?.destination?.route
    val isDetailScreen = currentRoute?.startsWith("pokemonDetail") == true

    isDetailScreen.takeIf { it }?.run {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = {
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
                            viewModel.getPokemonSearch(searchQuery, context)
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
                                viewModel.getPokemonSearch(searchQuery, context)
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
                        painter = painterResource(id = R.drawable.ico_start_toolbar_dex_10),
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

@Preview
@Composable
private fun GetTopBatPreviewList(
    viewModel: PokemonViewModel = getViewModel()
) {
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
        viewModel.getPokemonFavoriteList(favoriteFilter.not(), context)
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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("pokemonList") {
                PokemonListScreen(navController, this)
            }
            composable(
                route = "pokemonDetail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val pokemonId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                if (pokemonId != null) {
                    PokemonDetailScreen(
                        navController = navController,
                        pokemonId = pokemonId,
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
    ThemeDex10 {
        GetTopBar(
            getViewModel(),
            null,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenDarkPreview() {
    ThemeDex10(darkTheme = true) {
        GetTopBar(
            getViewModel(),
            null,
        )
    }
}
