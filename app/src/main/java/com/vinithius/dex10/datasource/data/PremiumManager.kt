package com.vinithius.dex10.datasource.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PremiumManager(
    private val context: Context,
    private val billingHandler: BillingHandler,
    injectedPrefs: android.content.SharedPreferences? = null,
    private val appPreferences: AppPreferences? = null,
) : BillingListener {

    companion object {
        private const val TAG = "PremiumManager"
        private const val PREFS_NAME = "dex10_premium_prefs"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_DEBUG_PREMIUM = "debug_premium"
        const val SKU_PREMIUM = "dex10_pro_lifetime"
        const val SKU_COFFEE = "donation_coffee_small"
        const val FREE_TEAM_LIMIT = 1
        const val FREE_FAVORITE_LIMIT = 50

        // Free scanner allowance: [FREE_SCANNER_LIMIT] scans per rolling [SCANNER_WINDOW_MILLIS]
        // window. Tweak these two to make the limit looser/tighter — e.g. 1h = 60*60*1000L.
        const val FREE_SCANNER_LIMIT = 5
        const val SCANNER_WINDOW_MILLIS = 2L * 60L * 60L * 1000L // 2 hours
    }

    private val encryptedPrefs: android.content.SharedPreferences =
        injectedPrefs ?: buildSecurePrefs(context)

    /**
     * Builds the encrypted prefs, recovering from a corrupted Tink keyset instead of crashing.
     *
     * `EncryptedSharedPreferences.create()` throws (e.g. "Protocol message contained an invalid
     * tag (zero)") when the stored keyset can't be read — classically after the app is restored
     * from backup onto a new device (the Android Keystore master key is NOT backed up) or after a
     * Keystore reset. Since this runs in the constructor, the unhandled throw crashed the app on
     * launch. We wipe the keyset + master key and rebuild once; if that still fails we fall back
     * to plain prefs so the app starts (the only stored value is the premium flag, which Play
     * Billing restores).
     */
    private fun buildSecurePrefs(context: Context): android.content.SharedPreferences {
        fun create(): android.content.SharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            return EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return try {
            create()
        } catch (e: Exception) {
            Log.w(TAG, "EncryptedSharedPreferences corrupt — resetting keyset/master key", e)
            runCatching { context.deleteSharedPreferences(PREFS_NAME) }
            runCatching {
                val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                keyStore.deleteEntry("_androidx_security_master_key_")
            }
            try {
                create()
            } catch (e2: Exception) {
                Log.e(TAG, "EncryptedSharedPreferences unrecoverable — using plain prefs", e2)
                context.getSharedPreferences("${PREFS_NAME}_plain", Context.MODE_PRIVATE)
            }
        }
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

    private val _showUpsell = MutableStateFlow(false)
    val showUpsell: StateFlow<Boolean> = _showUpsell.asStateFlow()

    private val _showDonation = MutableStateFlow(false)
    val showDonation: StateFlow<Boolean> = _showDonation.asStateFlow()

    private val _premiumPrice = MutableStateFlow<String?>(null)
    val premiumPrice: StateFlow<String?> = _premiumPrice.asStateFlow()

    private val _premiumOriginalPrice = MutableStateFlow<String?>(null)
    val premiumOriginalPrice: StateFlow<String?> = _premiumOriginalPrice.asStateFlow()

    private val _discountPercent = MutableStateFlow<Int?>(null)
    val discountPercent: StateFlow<Int?> = _discountPercent.asStateFlow()

    private val _coffeePrice = MutableStateFlow<String?>(null)
    val coffeePrice: StateFlow<String?> = _coffeePrice.asStateFlow()

    fun setupBillingClient(activity: Activity) {
        billingHandler.setup(activity, this)
    }

    fun queryExistingPurchases() {
        billingHandler.queryExistingPurchases()
    }

    fun launchPurchaseFlow(activity: Activity) {
        billingHandler.launchPurchaseFlow(activity)
    }

    fun launchDonationFlow(activity: Activity) {
        billingHandler.launchDonationFlow(activity)
    }

    fun restorePurchases() {
        billingHandler.queryExistingPurchases()
    }

    fun triggerUpsell() { _showUpsell.value = true }
    fun dismissUpsell() { _showUpsell.value = false }
    fun triggerDonation() { _showDonation.value = true }
    fun dismissDonation() { _showDonation.value = false }

    fun canPerformPremiumAction(): Boolean {
        if (_isPremium.value) return true
        triggerUpsell()
        return false
    }

    /**
     * Returns how many scanner uses remain in the current window for a free user.
     * Always returns null for premium users (unlimited).
     */
    fun scannerUsesRemaining(): Int? {
        if (_isPremium.value) return null
        val used = appPreferences?.getScannerUsage(SCANNER_WINDOW_MILLIS) ?: 0
        return (FREE_SCANNER_LIMIT - used).coerceAtLeast(0)
    }

    /**
     * Epoch millis when the free scanner allowance refills, or null when there is nothing to
     * count down (premium user, or no window currently open). Drives the on-screen countdown.
     */
    fun scannerResetAt(): Long? {
        if (_isPremium.value) return null
        return appPreferences?.getScannerResetAt(SCANNER_WINDOW_MILLIS)
    }

    /**
     * Tries to consume one scanner use. Returns true if allowed.
     * For free users, increments the counter and triggers the upsell when the
     * limit is reached. Premium users always get true without any side effects.
     */
    fun consumeScannerUseOrTriggerUpsell(): Boolean {
        if (_isPremium.value) return true
        val remaining = scannerUsesRemaining() ?: return true
        if (remaining <= 0) {
            triggerUpsell()
            return false
        }
        appPreferences?.incrementScannerUsage(SCANNER_WINDOW_MILLIS)
        return true
    }

    override fun onPremiumStatusChecked(isPremium: Boolean) {
        updatePremiumStatus(isPremium)
    }

    override fun onPremiumPurchased() {
        updatePremiumStatus(true)
        com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseSuccess()
    }

    override fun onDonationConsumed() {
        com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseSuccess()
        Log.d(TAG, "Coffee donation consumed successfully")
    }

    override fun onPurchaseFailed(reason: String) {
        com.vinithius.dex10.analytics.AnalyticsManager.logPurchaseFail(reason)
        Log.e(TAG, "Purchase failed: $reason")
    }

    override fun onPricesLoaded(premiumPrice: String?, coffeePrice: String?) {
        _premiumPrice.value = premiumPrice
        _coffeePrice.value = coffeePrice
        _discountPercent.value = null
        _premiumOriginalPrice.value = null
    }

    private fun updatePremiumStatus(isPremium: Boolean) {
        if (com.vinithius.dex10.BuildConfig.DEBUG && _debugOverride.value != null) {
            Log.d(TAG, "Ignoring actual status ($isPremium) due to debug override (${_debugOverride.value})")
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

    fun setDebugPremiumStatus(isPremium: Boolean) {
        if (!com.vinithius.dex10.BuildConfig.DEBUG) return
        _debugOverride.value = isPremium
        _isPremium.value = isPremium
        encryptedPrefs.edit().putBoolean(KEY_DEBUG_PREMIUM, isPremium).apply()
        Log.d(TAG, "Debug override set to: $isPremium")
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
        _premiumPrice.value = null
        _premiumOriginalPrice.value = null
        _discountPercent.value = null
        billingHandler.queryExistingPurchases()
        Log.d(TAG, "Debug pricing cleared")
    }

    fun clearDebugPremiumStatus() {
        if (!com.vinithius.dex10.BuildConfig.DEBUG) return
        _debugOverride.value = null
        encryptedPrefs.edit().remove(KEY_DEBUG_PREMIUM).apply()
        _isPremium.value = encryptedPrefs.getBoolean(KEY_IS_PREMIUM, false)
        billingHandler.queryExistingPurchases()
        Log.d(TAG, "Debug override cleared")
    }

    fun onDestroy() {
        billingHandler.destroy()
    }
}
