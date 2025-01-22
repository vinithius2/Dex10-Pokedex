package com.vinithius.poke10.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vinithius.poke10.BuildConfig
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

// Teste Ad Unit ID
private val adUnitIdTeste = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun AdmobBanner(typeScreen: Int = 1) {
    val adUnitId = getTypeAdUnitScreen(typeScreen)
    adUnitId?.takeIf { it.isNotEmpty() }?.let { validAdUnitId ->
        AndroidView(
            modifier = Modifier
                .fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    // Define um tamanho adaptativo
                    val displayMetrics = context.resources.displayMetrics
                    val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
                    this.adUnitId = validAdUnitId
                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: IllegalStateException) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        Log.e("AdmobBanner", "Failed to load ad: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
private fun getTypeAdUnitScreen(
    typeScreen: Int,
    viewModel: PokemonViewModel = getViewModel()
): String? {
    return if (BuildConfig.DEBUG) {
        adUnitIdTeste
    } else {
        when (typeScreen) {
            // Screen List
            1 -> viewModel.adUnitIdList.observeAsState().value
            // Detail List
            2 -> viewModel.adUnitIdDetails.observeAsState().value
            else -> null
        }
    }
}
