package com.vinithius.dex10.admobbanners

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vinithius.dex10.BuildConfig
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.rememberPokemonViewModel
import org.koin.androidx.compose.get

// Teste Ad Unit ID
private val adUnitIdTeste = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun AdmobBanner(
    premiumManager: PremiumManager = get()
) {
    val isPremium by premiumManager.isPremium.collectAsState()

    if (isPremium) {
        return
    }

    com.vinithius.dex10.analytics.AnalyticsManager.logEvent(
        "ad_impression_attempt",
        "screen",
        "banner"
    )

    val adUnitId = getTypeAdUnitScreen()
    adUnitId?.takeIf { it.isNotEmpty() }?.let { validAdUnitId ->
        AndroidView(
            modifier = Modifier
                .fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    // Define um tamanho adaptativo
                    val displayMetrics = context.resources.displayMetrics
                    val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context,
                            adWidth
                        )
                    )
                    this.adUnitId = validAdUnitId
                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: IllegalStateException) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }
        )
    }
}

@Composable
private fun getTypeAdUnitScreen(
    viewModel: PokemonViewModel = rememberPokemonViewModel()
): String? {
    return if (BuildConfig.DEBUG) {
        adUnitIdTeste
    } else {
        viewModel.adUnitIdBanner.observeAsState().value
    }
}
