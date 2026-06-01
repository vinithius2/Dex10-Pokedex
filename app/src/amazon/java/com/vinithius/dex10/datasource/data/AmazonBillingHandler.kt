package com.vinithius.dex10.datasource.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.vinithius.dex10.analytics.AnalyticsManager

/**
 * Amazon In-App Purchasing v3 handler.
 * Requires the Amazon Appstore SDK JAR placed in app/libs/amazon/
 * Download from: https://developer.amazon.com/apps-and-games/sdk
 */
class AmazonBillingHandler(private val context: Context) : BillingHandler, PurchasingListener {

    private val tag = "AmazonBillingHandler"
    private var listener: BillingListener? = null

    override fun setup(activity: Activity, listener: BillingListener) {
        this.listener = listener
        PurchasingService.registerObserver(this)
        PurchasingService.getUserData()
    }

    override fun queryExistingPurchases() {
        PurchasingService.getPurchaseUpdates(false)
    }

    override fun launchPurchaseFlow(activity: Activity) {
        AnalyticsManager.logPurchaseAttempt(PremiumManager.SKU_PREMIUM)
        PurchasingService.purchase(PremiumManager.SKU_PREMIUM)
    }

    override fun launchDonationFlow(activity: Activity) {
        AnalyticsManager.logPurchaseAttempt(PremiumManager.SKU_COFFEE)
        PurchasingService.purchase(PremiumManager.SKU_COFFEE)
    }

    override fun onUserDataResponse(response: UserDataResponse) {
        if (response.requestStatus == UserDataResponse.RequestStatus.SUCCESSFUL) {
            Log.d(tag, "User data loaded: ${response.userData.userId}")
            PurchasingService.getPurchaseUpdates(false)
            PurchasingService.getProductData(setOf(PremiumManager.SKU_PREMIUM, PremiumManager.SKU_COFFEE))
        } else {
            Log.e(tag, "User data failed: ${response.requestStatus}")
        }
    }

    override fun onProductDataResponse(response: ProductDataResponse) {
        if (response.requestStatus == ProductDataResponse.RequestStatus.SUCCESSFUL) {
            val premiumPrice = response.productData[PremiumManager.SKU_PREMIUM]?.price
            val coffeePrice = response.productData[PremiumManager.SKU_COFFEE]?.price
            listener?.onPricesLoaded(premiumPrice, coffeePrice)
        } else {
            Log.e(tag, "Product data failed: ${response.requestStatus}")
        }
    }

    override fun onPurchaseResponse(response: PurchaseResponse) {
        when (response.requestStatus) {
            PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                val receipt = response.receipt
                if (!receipt.isCanceled) {
                    when (receipt.sku) {
                        PremiumManager.SKU_PREMIUM -> {
                            listener?.onPremiumPurchased()
                            PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                        }
                        PremiumManager.SKU_COFFEE -> {
                            listener?.onDonationConsumed()
                            PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                        }
                    }
                }
            }
            PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                if (response.receipt?.sku == PremiumManager.SKU_PREMIUM) {
                    listener?.onPremiumStatusChecked(true)
                }
            }
            else -> {
                val reason = response.requestStatus.name
                listener?.onPurchaseFailed(reason)
                AnalyticsManager.logPurchaseFail(reason)
            }
        }
    }

    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
        if (response.requestStatus == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            val hasPremium = response.receipts.any {
                it.sku == PremiumManager.SKU_PREMIUM && !it.isCanceled
            }
            listener?.onPremiumStatusChecked(hasPremium)
            if (response.hasMore()) PurchasingService.getPurchaseUpdates(false)
        } else {
            Log.e(tag, "Purchase updates failed: ${response.requestStatus}")
        }
    }

    override fun destroy() {
        // Amazon SDK does not require explicit cleanup
    }
}
