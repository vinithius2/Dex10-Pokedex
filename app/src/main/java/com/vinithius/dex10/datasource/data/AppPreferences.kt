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
        private const val FCM_TOPIC = "general"

        // Dark mode values
        const val DARK_MODE_SYSTEM = 0
        const val DARK_MODE_ON = 1
        const val DARK_MODE_OFF = 2
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

    fun setNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
        _notificationsEnabled.value = value
        // Subscribe/unsubscribe from FCM topic
        val messaging = FirebaseMessaging.getInstance()
        if (value) {
            messaging.subscribeToTopic(FCM_TOPIC)
                .addOnCompleteListener { Log.d("AppPreferences", "Subscribed to $FCM_TOPIC") }
        } else {
            messaging.unsubscribeFromTopic(FCM_TOPIC)
                .addOnCompleteListener { Log.d("AppPreferences", "Unsubscribed from $FCM_TOPIC") }
        }
    }

    // --- Image Quality ---
    private val _lowQualityImages = MutableStateFlow(prefs.getBoolean(KEY_LOW_QUALITY, false))
    val lowQualityImages: StateFlow<Boolean> = _lowQualityImages.asStateFlow()

    fun setLowQualityImages(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_QUALITY, value).apply()
        _lowQualityImages.value = value
    }
}
