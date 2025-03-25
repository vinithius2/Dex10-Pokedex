package com.vinithius.poke10.admobbanners

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManagerRewarded(
    private val context: Context
) {

    private var rewardedAd: RewardedAd? = null
    lateinit var adUnitId: String

    fun loadAd(onAdLoaded: (() -> Unit)? = null) {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Log.d("RewardedAdManager", "Anúncio premiado carregado com sucesso")
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                Log.e("RewardedAdManager", "Falha ao carregar o anúncio premiado: ${adError.message}")
            }
        })
    }

    fun showAd(activity: Activity, onUserEarnedReward: ((RewardItem) -> Unit)? = null) {
        rewardedAd?.show(activity) { rewardItem ->
            Log.d("RewardedAdManager", "Usuário ganhou: ${rewardItem.amount} ${rewardItem.type}")
            onUserEarnedReward?.invoke(rewardItem)
        } ?: run {
            Log.d("RewardedAdManager", "O anúncio premiado não está pronto para ser exibido")
        }
    }
}
