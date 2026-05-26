package com.vinithius.dex10.datasource.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import com.vinithius.dex10.analytics.AnalyticsManager
import java.lang.ref.WeakReference

class HuaweiBillingHandler(private val context: Context) : BillingHandler {

    private val tag = "HuaweiBillingHandler"
    private var listener: BillingListener? = null
    private var activityRef: WeakReference<Activity>? = null
    private var purchaseLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var pendingSku: String? = null

    override fun setup(activity: Activity, listener: BillingListener) {
        this.listener = listener
        this.activityRef = WeakReference(activity)

        if (activity is ComponentActivity) {
            purchaseLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                handlePurchaseResult(result.resultCode, result.data)
            }
        }

        Iap.getIapClient(activity).isEnvReady
            .addOnSuccessListener {
                Log.d(tag, "HMS IAP environment ready")
                queryExistingPurchases()
                loadProductDetails(activity)
            }
            .addOnFailureListener { e ->
                Log.e(tag, "HMS IAP not available: ${e.message}")
            }
    }

    override fun queryExistingPurchases() {
        val activity = activityRef?.get() ?: return
        val req = OwnedPurchasesReq().apply {
            priceType = IapClient.PRICE_TYPE_IN_APP_NONCONSUMABLE
        }
        Iap.getIapClient(activity).obtainOwnedPurchases(req)
            .addOnSuccessListener { result ->
                val hasPremium = result.inAppPurchaseDataList?.any { dataStr ->
                    runCatching {
                        val data = InAppPurchaseData(dataStr)
                        data.productId == PremiumManager.SKU_PREMIUM &&
                                data.purchaseState == InAppPurchaseData.PurchaseState.PURCHASED
                    }.getOrDefault(false)
                } ?: false
                listener?.onPremiumStatusChecked(hasPremium)
            }
            .addOnFailureListener { e ->
                Log.e(tag, "obtainOwnedPurchases failed: ${e.message}")
            }
    }

    private fun loadProductDetails(activity: Activity) {
        val nonConsumableReq = ProductInfoReq().apply {
            priceType = IapClient.PRICE_TYPE_IN_APP_NONCONSUMABLE
            productList = listOf(PremiumManager.SKU_PREMIUM)
        }
        val consumableReq = ProductInfoReq().apply {
            priceType = IapClient.PRICE_TYPE_IN_APP_CONSUMABLE
            productList = listOf(PremiumManager.SKU_COFFEE)
        }
        var premiumPrice: String? = null
        var coffeePrice: String? = null

        Iap.getIapClient(activity).obtainProductInfo(nonConsumableReq)
            .addOnSuccessListener { result ->
                premiumPrice = result.productInfoList?.firstOrNull()?.price
                Iap.getIapClient(activity).obtainProductInfo(consumableReq)
                    .addOnSuccessListener { r ->
                        coffeePrice = r.productInfoList?.firstOrNull()?.price
                        listener?.onPricesLoaded(premiumPrice, coffeePrice)
                    }
            }
            .addOnFailureListener { e -> Log.e(tag, "obtainProductInfo failed: ${e.message}") }
    }

    override fun launchPurchaseFlow(activity: Activity) {
        launchHuaweiPurchase(activity, PremiumManager.SKU_PREMIUM, IapClient.PRICE_TYPE_IN_APP_NONCONSUMABLE)
    }

    override fun launchDonationFlow(activity: Activity) {
        launchHuaweiPurchase(activity, PremiumManager.SKU_COFFEE, IapClient.PRICE_TYPE_IN_APP_CONSUMABLE)
    }

    private fun launchHuaweiPurchase(activity: Activity, sku: String, priceType: Int) {
        val req = PurchaseIntentReq().apply {
            productId = sku
            this.priceType = priceType
        }
        AnalyticsManager.logPurchaseAttempt(sku)
        pendingSku = sku
        Iap.getIapClient(activity).createPurchaseIntent(req)
            .addOnSuccessListener { result ->
                val status = result.status
                if (status.hasResolution()) {
                    purchaseLauncher?.launch(
                        IntentSenderRequest.Builder(status.resolution).build()
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "createPurchaseIntent failed: ${e.message}")
                listener?.onPurchaseFailed(e.message ?: "unknown")
            }
    }

    private fun handlePurchaseResult(resultCode: Int, data: android.content.Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            listener?.onPurchaseFailed("user_canceled")
            return
        }
        val purchaseResultInfo = IapClientHelper.parseSubPurchaseResultInfoFromIntent(data)
        if (purchaseResultInfo.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
            val purchaseData = runCatching {
                InAppPurchaseData(purchaseResultInfo.inAppPurchaseData)
            }.getOrNull()
            when (purchaseData?.productId) {
                PremiumManager.SKU_PREMIUM -> listener?.onPremiumPurchased()
                PremiumManager.SKU_COFFEE -> consumeCoffee(purchaseResultInfo.inAppPurchaseData)
            }
        } else {
            val reason = "return_code_${purchaseResultInfo.returnCode}"
            listener?.onPurchaseFailed(reason)
            AnalyticsManager.logPurchaseFail(reason)
        }
    }

    private fun consumeCoffee(purchaseDataStr: String) {
        val activity = activityRef?.get() ?: return
        val token = runCatching {
            InAppPurchaseData(purchaseDataStr).purchaseToken
        }.getOrNull() ?: return

        val req = ConsumeOwnedPurchaseReq().apply { purchaseToken = token }
        Iap.getIapClient(activity).consumeOwnedPurchase(req)
            .addOnSuccessListener { listener?.onDonationConsumed() }
            .addOnFailureListener { e -> Log.e(tag, "consumeOwnedPurchase failed: ${e.message}") }
    }

    override fun destroy() {
        activityRef?.clear()
        purchaseLauncher = null
    }
}
