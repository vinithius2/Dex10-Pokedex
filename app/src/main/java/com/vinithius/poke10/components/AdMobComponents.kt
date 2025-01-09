package com.vinithius.poke10.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

private val adUnitIdTeste = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun AdmobBanner(viewModel: PokemonViewModel = getViewModel()) {
    val adUnitId by viewModel.adUnitId.observeAsState()

    adUnitId?.takeIf { it.isNotEmpty() }?.let { validAdUnitId ->
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = validAdUnitId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}