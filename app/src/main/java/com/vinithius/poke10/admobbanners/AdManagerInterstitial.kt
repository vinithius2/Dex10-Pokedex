package com.vinithius.poke10.admobbanners

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
//import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vinithius.poke10.BuildConfig
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

class AdManagerInterstitial(
    private val context: Context
) {

    private var interstitialAd: InterstitialAd? = null
    private val adUnitId = "ca-app-pub-1482093243889958/8361382344" // Substitua pelo seu ID

    fun loadAd(onAdLoaded: (() -> Unit)? = null) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d("InterstitialAdManager", "Ad carregado com sucesso")
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
                //FirebaseCrashlytics.getInstance().recordException(adError.toException())
                Log.e(
                    "InterstitialAdManager",
                    "Falha ao carregar o anúncio: ${adError.message}")
            }
        })
    }

    fun showAd(activity: Activity) {
        interstitialAd?.show(activity) ?: run {
            Log.d(
                "InterstitialAdManager",
                "O anúncio intersticial não está pronto para ser exibido"
            )
        }
    }
}
