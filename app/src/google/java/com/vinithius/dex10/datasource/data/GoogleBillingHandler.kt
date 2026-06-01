package com.vinithius.dex10.datasource.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.vinithius.dex10.analytics.AnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleBillingHandler(private val context: Context) : BillingHandler, PurchasesUpdatedListener {

    private val tag = "GoogleBillingHandler"
    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private var coffeeProductDetails: ProductDetails? = null
    private var listener: BillingListener? = null

    override fun setup(activity: Activity, listener: BillingListener) {
        this.listener = listener

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(tag, "Billing client connected")
                    queryExistingPurchases()
                    queryProductDetails()
                } else {
                    Log.e(tag, "Billing setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(tag, "Billing service disconnected")
            }
        })
    }

    override fun queryExistingPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any {
                    it.products.contains(PremiumManager.SKU_PREMIUM) &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                listener?.onPremiumStatusChecked(hasPremium)
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                    .forEach { acknowledgePurchase(it) }
            } else {
                Log.e(tag, "Query purchases failed: ${result.debugMessage}")
            }
        }
    }

    private fun queryProductDetails() {
        val products = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PremiumManager.SKU_PREMIUM)
                .setProductType(BillingClient.ProductType.INAPP).build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PremiumManager.SKU_COFFEE)
                .setProductType(BillingClient.ProductType.INAPP).build()
        )
        billingClient?.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build()
        ) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = details.find { it.productId == PremiumManager.SKU_PREMIUM }
                coffeeProductDetails = details.find { it.productId == PremiumManager.SKU_COFFEE }
                listener?.onPricesLoaded(
                    productDetails?.oneTimePurchaseOfferDetails?.formattedPrice,
                    coffeeProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice
                )
            }
        }
    }

    override fun launchPurchaseFlow(activity: Activity) = launchFlow(activity, productDetails, PremiumManager.SKU_PREMIUM)
    override fun launchDonationFlow(activity: Activity) = launchFlow(activity, coffeeProductDetails, PremiumManager.SKU_COFFEE)

    private fun launchFlow(activity: Activity, details: ProductDetails?, sku: String) {
        if (details == null) {
            AnalyticsManager.logPurchaseFail("${sku}_details_null")
            return
        }
        AnalyticsManager.logPurchaseAttempt(sku)
        billingClient?.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build())
                ).build()
        )
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    when {
                        purchase.products.contains(PremiumManager.SKU_PREMIUM) -> {
                            listener?.onPremiumPurchased()
                            if (!purchase.isAcknowledged) acknowledgePurchase(purchase)
                        }
                        purchase.products.contains(PremiumManager.SKU_COFFEE) -> consumeCoffee(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                listener?.onPurchaseFailed("user_canceled")
            else -> listener?.onPurchaseFailed(result.debugMessage)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        CoroutineScope(Dispatchers.IO).launch {
            billingClient?.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            ) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK)
                    Log.e(tag, "Acknowledge failed: ${result.debugMessage}")
            }
        }
    }

    private fun consumeCoffee(purchase: Purchase) {
        CoroutineScope(Dispatchers.IO).launch {
            billingClient?.consumeAsync(
                ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            ) { result, _ ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK)
                    listener?.onDonationConsumed()
                else
                    Log.e(tag, "Consume failed: ${result.debugMessage}")
            }
        }
    }

    override fun destroy() {
        billingClient?.endConnection()
    }
}
