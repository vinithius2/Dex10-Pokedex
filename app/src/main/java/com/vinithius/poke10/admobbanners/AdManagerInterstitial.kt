package com.vinithius.poke10.admobbanners

//import com.google.firebase.crashlytics.FirebaseCrashlytics
import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManagerInterstitial(
    private val context: Context
) {

    private var interstitialAd: InterstitialAd? = null
    lateinit var adUnitId : String

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
                    "Falha ao carregar o anúncio: ${adError.message}"
                )
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
