package com.vinithius.dex10.ui

import com.vinithius.dex10.datasource.data.AlertMessage
import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.RectangleShape

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vinithius.dex10.datasource.data.AppPreferences
import com.vinithius.dex10.ui.screens.SettingsScreen
import com.vinithius.dex10.ui.components.UpsellBottomSheet
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
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
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.extension.getColorByString
import com.vinithius.dex10.extension.getToolBarColorByString
import com.vinithius.dex10.extension.getVersionName

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
        val splashScreen = installSplashScreen() // Keep this splashScreen variable
        val appPrefs: AppPreferences by inject(AppPreferences::class.java)
        val premiumManager: PremiumManager by inject(PremiumManager::class.java)
        premiumManager.setupBillingClient(this)

        setContent {
            val darkModeOverride by appPrefs.darkMode.collectAsState()
            ThemeDex10(darkModeOverride = darkModeOverride) {
                MainScreen(
                    activity = this@MainActivity,
                    appPreferences = appPrefs,
                    premiumManager = premiumManager
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
            },
            context = this
        )

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
        onError: (Exception) -> Unit,
        context: Context
    ) {
        val deviceLanguage = Locale.getDefault().language

        val supportedLanguages = context.resources.getStringArray(
            R.array.supported_languages
        ).toSet()

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

        // Flavours
        const val GOOGLE = "google"
        const val AMAZON = "amazon"
        const val HAUWEI = "hauwei"

        // Keys Remote Config
        const val RC_BANNER = "adUnitId_banner"
        const val IS_REWARD = "isRewarded"
        const val RC_BANNER_INTERSTITIAL = "adUnitId_choiceOfTheDay_interstitial"
        const val RC_BANNER_REWARDED = "adUnitId_choiceOfTheDay_rewarded"
        const val RC_BANNER_ADVANCED_NATIVE = "adUnitId_advancedNative"
        const val FACEBOOK = "facebook_url"
        const val INSTAGRAN = "instagran_url"
        const val REDDIT = "reddit_url"
        const val HAS_DONATE = "has_donate"
        const val REVIEW_GOOGLE = "review_google_url"
        const val REVIEW_AMAZON = "review_amazon_url"
        const val REVIEW_HAUWEI = "review_hauwei_url"
        const val GOOGLE_FORM = "google_form"
        const val ALERT_MESSAGE = "alert_message"
        const val PRIVACY_POLICY = "privacy_policy"
        const val AMOUNT_OF_ADS = "amountOfAds"
        const val ITEM_RANGE_FOR_ADS = "itemRangeForAds"
        const val ITEM_RANGE_FOR_ADS_TABLET = "itemRangeForAdsTablet"
    }
}

@Composable
private fun GetAdUnitId(
    context: Context,
    viewModel: PokemonViewModel = getViewModel()
) {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    remoteConfig.fetchAndActivate()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Ads
                val adUnitIdList = remoteConfig.getString(MainActivity.RC_BANNER)
                val isRewarded = remoteConfig.getBoolean(MainActivity.IS_REWARD)
                val adUnitIdChoiceOfTheDayInterstitial =
                    remoteConfig.getString(MainActivity.RC_BANNER_INTERSTITIAL)
                val adUnitIdChoiceOfTheDayRewarded =
                    remoteConfig.getString(MainActivity.RC_BANNER_REWARDED)
                val adAdvancedNative =
                    remoteConfig.getString(MainActivity.RC_BANNER_ADVANCED_NATIVE)
                // Ads count
                val itemRangeForAds = remoteConfig.getDouble(MainActivity.AMOUNT_OF_ADS)
                val itemRangeForAdsTablet = remoteConfig.getDouble(MainActivity.ITEM_RANGE_FOR_ADS)
                val amountOfAds = remoteConfig.getDouble(MainActivity.ITEM_RANGE_FOR_ADS_TABLET)
                // Viewmodel set
                viewModel.setAdUnitIdList(adUnitIdList)
                viewModel.setIsRewarded(isRewarded)
                viewModel.setAdUnitIdChoiceOfTheDayInterstitial(adUnitIdChoiceOfTheDayInterstitial)
                viewModel.setAdUnitIdChoiceOfTheDayRewarded(adUnitIdChoiceOfTheDayRewarded)
                viewModel.setAdUnitIdAdAdvancedNative(adAdvancedNative)

                viewModel.setItemRangeForAds(itemRangeForAds.toInt())
                viewModel.setItemRangeForAdsTablet(itemRangeForAdsTablet.toInt())
                viewModel.setAmountOfAds(amountOfAds.toInt())
                // Social media
                val facebookUrl = remoteConfig.getString(MainActivity.FACEBOOK)
                val instagranUrl = remoteConfig.getString(MainActivity.INSTAGRAN)
                val redditUrl = remoteConfig.getString(MainActivity.REDDIT)
                val hasDonate = remoteConfig.getBoolean(MainActivity.HAS_DONATE)
                val privacyPolicy = remoteConfig.getString(MainActivity.PRIVACY_POLICY)
                // Review
                val reviewUrl = when (BuildConfig.FLAVOR) {
                    MainActivity.GOOGLE -> remoteConfig.getString(MainActivity.REVIEW_GOOGLE)
                    MainActivity.AMAZON -> remoteConfig.getString(MainActivity.REVIEW_AMAZON)
                    MainActivity.HAUWEI -> remoteConfig.getString(MainActivity.REVIEW_HAUWEI)
                    else -> remoteConfig.getString(MainActivity.REVIEW_GOOGLE)
                }

                val googleForm = remoteConfig.getString(MainActivity.GOOGLE_FORM)

                // Viewmodel set
                viewModel.setHasDonate(hasDonate)
                viewModel.setReviewUrl(reviewUrl)
                viewModel.setFacebookUrl(facebookUrl)
                viewModel.setInstagranUrl(instagranUrl)
                viewModel.setRedditUrl(redditUrl)
                viewModel.setGoogleForm(googleForm)
                viewModel.setPrivacyPolicy(privacyPolicy)

                // ALERT MESSAGE
                try {
                    val alertMessageJson = remoteConfig.getString(MainActivity.ALERT_MESSAGE)
                    val alertMessageData =
                        Gson().fromJson(alertMessageJson, AlertMessage::class.java)
                    val languageCode = Locale.getDefault().language
                    val localized = alertMessageData.getLocalizedContent(languageCode, context)
                    val localizedAlert = AlertMessage(
                        show = alertMessageData.show,
                        versionCode = alertMessageData.versionCode,
                        urlAction = alertMessageData.urlAction,
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
    viewModel: PokemonViewModel = getViewModel(),
    appPreferences: AppPreferences? = null,
    premiumManager: PremiumManager? = null
) {
    val navController = rememberNavController()
    val deeplinkRoute by viewModel.deeplinkNavigation.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Deeplink
    LaunchedEffect(deeplinkRoute) {
        deeplinkRoute?.let { route ->
            navController.navigate(route)
            viewModel.clearDeeplinkNavigation()
        }
    }
    val context = LocalContext.current
    GetAdUnitId(context)
    SetInterstitialOrRewardedAdManager(activity, navController)
    SetupSystemUI(viewModel)
    
    // Upsell Sheet Logic
    val showUpsell by (premiumManager?.showUpsell ?: MutableStateFlow(false)).collectAsState()
    if (showUpsell) {
        UpsellBottomSheet(
            onDismiss = { premiumManager?.dismissUpsell() },
            onPurchaseClick = {
                premiumManager?.dismissUpsell()
                premiumManager?.launchPurchaseFlow(activity)
            },
            onRestoreClick = {
                premiumManager?.dismissUpsell()
                premiumManager?.restorePurchases()
            }
        )
    }

    // Determine if we're on the detail screen (drawer should be disabled)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val isDetailScreen = currentRoute?.startsWith("pokemonDetail") == true
    val isSettingsScreen = currentRoute == "settings"

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isDetailScreen && !isSettingsScreen,
        drawerContent = {
            ModalDrawerSheet(drawerShape = RectangleShape) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DrawerContent(
                        viewModel = viewModel,
                        navController = navController,
                        onCloseDrawer = { scope.launch { drawerState.close() } }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isSettingsScreen) {
                    SettingsTopBar(onBack = { navController.popBackStack() })
                } else {
                    GetTopBar(
                        viewModel = viewModel,
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            },
            bottomBar = {
                if (!isSettingsScreen) {
                    AdmobBanner()
                }
            }
        ) { innerPadding ->
            GetNavHost(
                innerPadding,
                navController,
                appPreferences,
                premiumManager
            )
        }
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
    ThemeDex10 { }
    val statusBarColor =
        color?.getColorByString(isSystemInDarkTheme()) ?: MaterialTheme.colorScheme.tertiary
    systemUiController.setStatusBarColor(
        color = statusBarColor,
        darkIcons = statusBarColor.luminance() > 0.5
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings),
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GetTopBar(
    viewModel: PokemonViewModel,
    navController: NavHostController?,
    onOpenDrawer: (() -> Unit)? = null
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
    val isTeamScreen = currentRoute?.startsWith("team") == true

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
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
            },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = color?.getToolBarColorByString(isSystemInDarkTheme())
                    ?: MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                scrolledContainerColor = color?.getToolBarColorByString(isSystemInDarkTheme())
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
    } ?: isTeamScreen.takeIf { it }?.run {
        TopAppBar(
            title = { 
                Text(
                    text = if (currentRoute?.startsWith("teamDetail") == true) "Team Details" else "My Teams",
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
            },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            ),
            actions = { }
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
            navigationIcon = {
                IconButton(onClick = {
                    onOpenDrawer?.invoke()
                    activity?.trackButtonClick("Open drawer menu")
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_menu),
                        tint = Color.White
                    )
                }
            },
            actions = {
                if (pokemonListBackup.isNotEmpty()) {
                    if (isSearchActive.not()) {
                        IconButton(onClick = {
                            activity?.trackButtonClick("Click search filter")
                            isSearchActive = isSearchActive.not()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search),
                                tint = Color.White
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
private fun DrawerContent(
    viewModel: PokemonViewModel,
    navController: NavHostController,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val reviewUrl by viewModel.reviewUrl.observeAsState()
    val googleForm by viewModel.googleForm.observeAsState()
    val hasDonate by viewModel.hasDonate.observeAsState(true)
    val facebookUrl by viewModel.facebookUrl.observeAsState()
    val instagranUrl by viewModel.instagranUrl.observeAsState()
    val redditUrl by viewModel.redditUrl.observeAsState()
    val privacyPolicy by viewModel.privacyPolicy.observeAsState()
    val version = context.getVersionName()

    Column {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.ico_start_toolbar_dex_10),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${stringResource(id = R.string.version)} ${version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        // Premium Section
        val isPremium by viewModel.premiumManager.isPremium.collectAsState()
        if (!isPremium) {
            DrawerItem(
                icon = Icons.Default.Star, // Or a diamond/crown icon if available
                label = stringResource(id = R.string.go_premium),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: Go Premium")
                    viewModel.premiumManager.triggerUpsell()
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Team Builder
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("My Teams") },
            selected = false,
            onClick = {
                onCloseDrawer()
                activity?.trackButtonClick("Drawer: My Teams")
                navController.navigate("teamList")
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // Settings
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(id = R.string.settings)) },
            selected = false,
            onClick = {
                onCloseDrawer()
                activity?.trackButtonClick("Drawer: Settings")
                navController.navigate("settings")
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Section: Interaction
        DrawerSectionHeader(stringResource(id = R.string.interaction))

        DrawerItem(
            icon = Icons.Default.Share,
            label = stringResource(id = R.string.share_app),
            onClick = {
                onCloseDrawer()
                activity?.trackButtonClick("Drawer: share app")
                shareApp(reviewUrl.toString(), context)
            }
        )

        DrawerItem(
            icon = Icons.Default.Star,
            label = stringResource(id = R.string.rate_app),
            onClick = {
                onCloseDrawer()
                activity?.trackButtonClick("Drawer: rate app")
                getIntentToUrl(reviewUrl.toString(), context)
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))



        // Section: Feedback and Support
        DrawerSectionHeader(stringResource(id = R.string.feedback_and_support))

        googleForm?.takeIf { it.isNotEmpty() }?.let {
            DrawerItem(
                icon = Icons.Default.ThumbUp,
                label = stringResource(id = R.string.suggestions_or_bugs),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: google form")
                    getIntentToUrl(it, context)
                }
            )
        }

        if (hasDonate) {
            DrawerItem(
                icon = Icons.Default.Favorite,
                label = stringResource(id = R.string.donate_to_developer),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: donate")
                    // Do nothing yet
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Section: Social Media
        DrawerSectionHeader(stringResource(id = R.string.social_media))

        instagranUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            DrawerItem(
                label = stringResource(id = R.string.instagran),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: instagram")
                    getIntentToUrl(url, context)
                }
            )
        }

        facebookUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            DrawerItem(
                label = stringResource(id = R.string.facebook),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: facebook")
                    getIntentToUrl(url, context)
                }
            )
        }

        redditUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            DrawerItem(
                label = stringResource(id = R.string.reddit),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: reddit")
                    getIntentToUrl(url, context)
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Section: Info
        privacyPolicy?.takeIf { it.isNotEmpty() }?.let { url ->
            DrawerSectionHeader(stringResource(id = R.string.info))
            DrawerItem(
                icon = Icons.Default.Info,
                label = stringResource(id = R.string.privacy_policy),
                onClick = {
                    onCloseDrawer()
                    activity?.trackButtonClick("Drawer: Privacy Policy")
                    getIntentToUrl(url, context)
                }
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
    )
}

@Composable
private fun DrawerItem(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    NavigationDrawerItem(
        icon = icon?.let { { Icon(it, contentDescription = null) } },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GetNavHost(
    innerPadding: PaddingValues,
    navController: NavHostController,
    appPreferences: AppPreferences? = null,
    premiumManager: PremiumManager? = null
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
            composable("settings") {
                appPreferences?.let { prefs ->
                    SettingsScreen(
                        appPreferences = prefs,
                        premiumManager = premiumManager,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("teamList") {
                com.vinithius.dex10.ui.screens.TeamListScreen(navController)
            }
            composable(
                route = "teamDetail/{teamId}",
                arguments = listOf(navArgument("teamId") { type = NavType.IntType })
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getInt("teamId") ?: 0
                com.vinithius.dex10.ui.screens.TeamDetailScreen(navController, teamId)
            }
        }
    }
}

fun shareApp(reviewUrl: String, context: Context) {
    val shareMessage = String.format(
        context.getString(R.string.share_app_message),
        reviewUrl
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

fun getIntentToUrl(url: String, context: Context) {
    url.takeIf { it.isNotEmpty() }?.run {
        val intent =
            Intent(
                Intent.ACTION_VIEW,
                url.toUri()
            )
        context.startActivity(intent)
    }
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
