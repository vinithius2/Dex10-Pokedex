package com.vinithius.dex10.datasource.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.*
import java.text.NumberFormat
import java.util.Currency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages premium subscription state using Google Play Billing Library.
 * Uses EncryptedSharedPreferences for secure offline caching of premium status.
 */
class PremiumManager(
    private val context: Context,
    injectedPrefs: android.content.SharedPreferences? = null,
    private val appPreferences: AppPreferences? = null,
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PremiumManager"
        private const val PREFS_NAME = "dex10_premium_prefs"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_DEBUG_PREMIUM = "debug_premium"
        const val SKU_PREMIUM = "dex10_pro_lifetime"
        const val SKU_COFFEE = "donation_coffee_small"
        const val FREE_TEAM_LIMIT = 1
        const val FREE_FAVORITE_LIMIT = 50

        // Preço cheio do produto antes de qualquer promoção configurada no Play Console.
        // Atualizar este valor caso o preço base do produto seja alterado.
        // Exemplo: R$49,99 → 49_990_000L  (1 unidade monetária = 1.000.000 micros)
        private const val BASE_PRICE_AMOUNT_MICROS = 49_990_000L
    }

    // private val encryptedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // private val encryptedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val encryptedPrefs = if (injectedPrefs != null) {
        injectedPrefs
    } else {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _isPremium = MutableStateFlow(
        if (com.vinithius.dex10.BuildConfig.DEBUG && encryptedPrefs.contains(KEY_DEBUG_PREMIUM)) {
            encryptedPrefs.getBoolean(KEY_DEBUG_PREMIUM, false)
        } else {
            encryptedPrefs.getBoolean(KEY_IS_PREMIUM, false)
        }
    )
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _debugOverride = MutableStateFlow<Boolean?>(
        if (com.vinithius.dex10.BuildConfig.DEBUG && encryptedPrefs.contains(KEY_DEBUG_PREMIUM)) {
            encryptedPrefs.getBoolean(KEY_DEBUG_PREMIUM, false)
        } else {
            null
        }
    )

    // Event to show upsell
    private val _showUpsell = MutableStateFlow(false)
    val showUpsell: StateFlow<Boolean> = _showUpsell.asStateFlow()

    private val _showDonation = MutableStateFlow(false)
    val showDonation: StateFlow<Boolean> = _showDonation.asStateFlow()

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null

    private val _premiumPrice = MutableStateFlow<String?>(null)
    val premiumPrice: StateFlow<String?> = _premiumPrice.asStateFlow()

    private val _premiumOriginalPrice = MutableStateFlow<String?>(null)
    val premiumOriginalPrice: StateFlow<String?> = _premiumOriginalPrice.asStateFlow()

    private val _discountPercent = MutableStateFlow<Int?>(null)
    val discountPercent: StateFlow<Int?> = _discountPercent.asStateFlow()

    private val _coffeePrice = MutableStateFlow<String?>(null)
    val coffeePrice: StateFlow<String?> = _coffeePrice.asStateFlow()

    private var coffeeProductDetails: ProductDetails? = null

    /**
     * Initialize and connect the BillingClient.
     */
    fun setupBillingClient(activity: Activity) {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryExistingPurchases()
                    queryProductDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    /**
     * Query existing purchases to verify one-time purchase is active.
     */
    fun queryExistingPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchasesList.any { purchase ->
                    purchase.products.contains(SKU_PREMIUM) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                updatePremiumStatus(hasPremium)

                // Acknowledge any un-acknowledged purchases
                purchasesList.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                }.forEach { acknowledgePurchase(it) }

                Log.d(TAG, "Existing purchases queried. Premium: $hasPremium")
            } else {
                Log.e(TAG, "Query purchases failed: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Query product details for the one-time purchase SKU.
     */
    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PREMIUM)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_COFFEE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = productDetailsList.find { it.productId == SKU_PREMIUM }
                val offerDetails = productDetails?.oneTimePurchaseOfferDetails
                val currentPriceMicros = offerDetails?.priceAmountMicros ?: 0L
                _premiumPrice.value = offerDetails?.formattedPrice

                // Quando o Play Console tem promoção ativa, a API retorna o preço promocional.
                // Comparamos com o preço base (BASE_PRICE_AMOUNT_MICROS) para detectar desconto.
                if (currentPriceMicros in 1 until BASE_PRICE_AMOUNT_MICROS) {
                    val currencyCode = offerDetails?.priceCurrencyCode ?: "BRL"
                    _premiumOriginalPrice.value = formatMicrosAsCurrency(BASE_PRICE_AMOUNT_MICROS, currencyCode)
                    val discountPct = ((BASE_PRICE_AMOUNT_MICROS - currentPriceMicros) * 100L / BASE_PRICE_AMOUNT_MICROS).toInt()
                    _discountPercent.value = discountPct
                    Log.d(TAG, "Discount active: ${_discountPercent.value}% off (${_premiumOriginalPrice.value} → ${_premiumPrice.value})")
                } else {
                    _premiumOriginalPrice.value = null
                    _discountPercent.value = null
                }

                coffeeProductDetails = productDetailsList.find { it.productId == SKU_COFFEE }
                _coffeePrice.value = coffeeProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice

                Log.d(TAG, "Premium Price: ${_premiumPrice.value}")
            }
        }
    }

    /**
     * Launch the purchase flow for the premium one-time purchase.
     */
    fun launchPurchaseFlow(activity: Activity) {
        launchBillingFlow(activity, productDetails, SKU_PREMIUM)
    }

    fun launchDonationFlow(activity: Activity) {
        launchBillingFlow(activity, coffeeProductDetails, SKU_COFFEE)
    }

    private fun launchBillingFlow(activity: Activity, details: ProductDetails?, sku: String) {
        if (details == null) {
            Log.e(TAG, "Product details not available yet for $sku")
            com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseFail("${sku}_details_null")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseAttempt(sku)
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Callback for purchase updates.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (purchase.products.contains(SKU_PREMIUM)) {
                            updatePremiumStatus(true)
                            com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseSuccess()
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        } else if (purchase.products.contains(SKU_COFFEE)) {
                            consumeCoffeePurchase(purchase)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Purchase cancelled by user")
                com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseFail("user_canceled")
            }
            else -> {
                Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
                com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseFail(billingResult.debugMessage)
            }
        }
    }

    /**
     * Acknowledge a purchase (required for subscriptions).
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            billingClient?.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                } else {
                    Log.e(TAG, "Acknowledge failed: ${result.debugMessage}")
                }
            }
        }
    }

    private fun consumeCoffeePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            billingClient?.consumeAsync(params) { result, _ ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Coffee purchase consumed successfully")
                    com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseSuccess()
                } else {
                    Log.e(TAG, "Consume failed: ${result.debugMessage}")
                }
            }
        }
    }



    /**
     * Show the upsell bottom sheet.
     */
    fun triggerUpsell() {
        _showUpsell.value = true
    }

    /**
     * Dismiss the upsell bottom sheet.
     */
    fun dismissUpsell() {
        _showUpsell.value = false
    }

    /**
     * Show the donation bottom sheet.
     */
    fun triggerDonation() {
        _showDonation.value = true
    }

    /**
     * Dismiss the donation bottom sheet.
     */
    fun dismissDonation() {
        _showDonation.value = false
    }

    /**
     * Force restore purchases from Play Store.
     */
    fun restorePurchases() {
        queryExistingPurchases()
    }

    /**
     * Check if the user can perform a premium action, triggers upsell if not.
     */
    fun canPerformPremiumAction(): Boolean {
        if (_isPremium.value) return true
        triggerUpsell()
        return false
    }

    private fun updatePremiumStatus(isPremium: Boolean) {
        // If debug override is active AND in debug build, do not overwrite with actual status
        if (com.vinithius.dex10.BuildConfig.DEBUG && _debugOverride.value != null) {
            Log.d(TAG, "Ignoring actual status update ($isPremium) due to debug override (${_debugOverride.value})")
            return
        }
        encryptedPrefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
        _isPremium.value = isPremium

        // Keep FCM topics and Analytics user property in sync with premium status.
        // This enables targeting push notifications and in-app messages by tier.
        appPreferences?.updateFcmTopics(isPremium)
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(context)
            .setUserProperty("subscription_tier", if (isPremium) "premium" else "free")
    }

    private fun formatMicrosAsCurrency(micros: Long, currencyCode: String): String {
        val amount = micros / 1_000_000.0
        return try {
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.currency = Currency.getInstance(currencyCode)
            formatter.format(amount)
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance().format(amount)
        }
    }

    // --- DEBUG FEATURES ---

    /**
     * Set a debug override for premium status.
     */
    fun setDebugPremiumStatus(isPremium: Boolean) {
        if (!com.vinithius.dex10.BuildConfig.DEBUG) return
        _debugOverride.value = isPremium
        _isPremium.value = isPremium
        encryptedPrefs.edit().putBoolean(KEY_DEBUG_PREMIUM, isPremium).apply()
        Log.d(TAG, "Debug override set to: $isPremium (persisted)")
    }

    fun setDebugPricing(price: String, originalPrice: String, discountPercent: Int) {
        if (!com.vinithius.dex10.BuildConfig.DEBUG) return
        _premiumPrice.value = price
        _premiumOriginalPrice.value = originalPrice
        _discountPercent.value = discountPercent
        Log.d(TAG, "Debug pricing: $price (was $originalPrice, -$discountPercent%)")
    }

    fun clearDebugPricing() {
        if (!com.vinithius.dex10.BuildConfig.DEBUG) return
        // Restore real Play Store values if the billing client is ready, otherwise null
        if (billingClient?.isReady == true) {
            queryProductDetails()
        } else {
            _premiumPrice.value = null
            _premiumOriginalPrice.value = null
            _discountPercent.value = null
        }
        Log.d(TAG, "Debug pricing cleared — real values restored")
    }

    /**
     * Clear debug override and restore actual status.
     */
    fun clearDebugPremiumStatus() {
        if (!com.vinithius.dex10.BuildConfig.DEBUG) return
        _debugOverride.value = null
        encryptedPrefs.edit().remove(KEY_DEBUG_PREMIUM).apply()
        // Restore from prefs immediately
        _isPremium.value = encryptedPrefs.getBoolean(KEY_IS_PREMIUM, false)
        queryExistingPurchases() // Refresh actual status
        Log.d(TAG, "Debug override cleared")
    }

    fun onDestroy() {
        billingClient?.endConnection()
    }
}
