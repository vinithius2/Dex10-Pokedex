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
fun AdmobBanner(viewModel: PokemonViewModel = getViewModel()) {
    val adUnitId =
        if (BuildConfig.DEBUG) adUnitIdTeste else viewModel.adUnitId.observeAsState().value
    adUnitId?.takeIf { it.isNotEmpty() }?.let { validAdUnitId ->
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
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
