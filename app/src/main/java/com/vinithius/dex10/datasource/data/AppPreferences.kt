package com.vinithius.dex10.datasource.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Centralized app preferences using SharedPreferences.
 * Provides reactive StateFlows for Compose integration.
 */
class AppPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "dex10_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_LOW_QUALITY = "low_quality_images"

        // FCM topics — send to all three from Firebase Console to reach everyone,
        // or target individually to reach only premium / only free users.
        const val TOPIC_ALL     = "general"          // all users with notifications on
        const val TOPIC_PREMIUM = "general_premium"  // paying users only
        const val TOPIC_FREE    = "general_free"     // non-paying users only

        // Dark mode values
        const val DARK_MODE_SYSTEM = 0
        const val DARK_MODE_ON = 1
        const val DARK_MODE_OFF = 2
        private const val KEY_VIEW_MODE = "view_mode"
    }

    enum class ViewMode(val value: Int) {
        LIST(0), AUTO(1);

        companion object {
            fun fromInt(value: Int) = values().firstOrNull { it.value == value } ?: LIST
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Dark Mode ---
    private val _darkMode = MutableStateFlow(prefs.getInt(KEY_DARK_MODE, DARK_MODE_SYSTEM))
    val darkMode: StateFlow<Int> = _darkMode.asStateFlow()

    fun setDarkMode(value: Int) {
        prefs.edit().putInt(KEY_DARK_MODE, value).apply()
        _darkMode.value = value
    }

    // --- Notifications ---
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // In-memory cache of the last known premium status, used when notifications
    // are re-enabled so the correct topic pair is restored without needing a
    // round-trip to PremiumManager.
    private var cachedIsPremium: Boolean = false

    fun setNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
        _notificationsEnabled.value = value
        val messaging = FirebaseMessaging.getInstance()
        if (value) {
            // Re-subscribe using the last known premium status
            updateFcmTopics(cachedIsPremium)
        } else {
            // Unsubscribe from every topic
            listOf(TOPIC_ALL, TOPIC_PREMIUM, TOPIC_FREE).forEach {
                messaging.unsubscribeFromTopic(it)
            }
            Log.d("AppPreferences", "FCM: unsubscribed from all topics")
        }
    }

    /**
     * Called by PremiumManager whenever the premium status changes.
     * Subscribes the user to the right pair of topics:
     *   - TOPIC_ALL always (so you can reach everyone with one send)
     *   - TOPIC_PREMIUM or TOPIC_FREE depending on [isPremium]
     */
    fun updateFcmTopics(isPremium: Boolean) {
        cachedIsPremium = isPremium
        if (!_notificationsEnabled.value) return
        val messaging = FirebaseMessaging.getInstance()
        messaging.subscribeToTopic(TOPIC_ALL)
        if (isPremium) {
            messaging.subscribeToTopic(TOPIC_PREMIUM)
            messaging.unsubscribeFromTopic(TOPIC_FREE)
        } else {
            messaging.subscribeToTopic(TOPIC_FREE)
            messaging.unsubscribeFromTopic(TOPIC_PREMIUM)
        }
        Log.d("AppPreferences", "FCM topics → $TOPIC_ALL + ${if (isPremium) TOPIC_PREMIUM else TOPIC_FREE}")
    }

    // --- Image Quality ---
    private val _lowQualityImages = MutableStateFlow(prefs.getBoolean(KEY_LOW_QUALITY, false))
    val lowQualityImages: StateFlow<Boolean> = _lowQualityImages.asStateFlow()

    fun setLowQualityImages(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_QUALITY, value).apply()
        _lowQualityImages.value = value
    }

    // --- View Mode ---
    private val _viewMode = MutableStateFlow(ViewMode.fromInt(prefs.getInt(KEY_VIEW_MODE, ViewMode.LIST.value)))
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    fun setViewMode(value: ViewMode) {
        prefs.edit().putInt(KEY_VIEW_MODE, value.value).apply()
        _viewMode.value = value
    }
}
