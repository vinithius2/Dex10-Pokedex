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
        private const val KEY_SCANNER_COUNT = "scanner_daily_count"
        private const val KEY_SCANNER_DATE = "scanner_daily_date"
        private const val KEY_TTS_SPEED = "tts_speed"
        private const val KEY_TTS_PITCH = "tts_pitch"
        private const val KEY_TTS_AUTO_PLAY = "tts_auto_play"

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
        private const val KEY_TCG_FAVORITES = "tcg_favorite_cards"
        private const val KEY_SPRITE_TYPE = "sprite_type"
        private const val KEY_APP_OPEN_COUNT = "app_open_count"
        private const val KEY_REVIEW_LAST_PROMPT = "review_last_prompt_date"
    }

    enum class SpriteType(val key: Int) {
        OFFICIAL_ARTWORK(1), GIF(0), HOME(2), PIXEL(3);
        companion object {
            fun fromInt(v: Int) = values().firstOrNull { it.key == v } ?: OFFICIAL_ARTWORK
        }
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

    // --- Sprite Type ---
    private val _spriteType = MutableStateFlow(
        SpriteType.fromInt(prefs.getInt(KEY_SPRITE_TYPE, SpriteType.OFFICIAL_ARTWORK.key))
    )
    val spriteType: StateFlow<SpriteType> = _spriteType.asStateFlow()

    fun setSpriteType(value: SpriteType) {
        prefs.edit().putInt(KEY_SPRITE_TYPE, value.key).apply()
        _spriteType.value = value
    }

    // --- View Mode ---
    private val _viewMode = MutableStateFlow(ViewMode.fromInt(prefs.getInt(KEY_VIEW_MODE, ViewMode.LIST.value)))
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    fun setViewMode(value: ViewMode) {
        prefs.edit().putInt(KEY_VIEW_MODE, value.value).apply()
        _viewMode.value = value
    }

    // --- TTS / Voice ---
    private val _ttsSpeed = MutableStateFlow(prefs.getFloat(KEY_TTS_SPEED, 1.0f))
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    fun setTtsSpeed(value: Float) {
        prefs.edit().putFloat(KEY_TTS_SPEED, value).apply()
        _ttsSpeed.value = value
    }

    private val _ttsPitch = MutableStateFlow(prefs.getFloat(KEY_TTS_PITCH, 1.0f))
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    fun setTtsPitch(value: Float) {
        prefs.edit().putFloat(KEY_TTS_PITCH, value).apply()
        _ttsPitch.value = value
    }

    private val _ttsAutoPlay = MutableStateFlow(prefs.getBoolean(KEY_TTS_AUTO_PLAY, false))
    val ttsAutoPlay: StateFlow<Boolean> = _ttsAutoPlay.asStateFlow()

    fun setTtsAutoPlay(value: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_AUTO_PLAY, value).apply()
        _ttsAutoPlay.value = value
    }

    // --- Scanner Daily Usage ---

    private fun todayIso(): String =
        java.time.LocalDate.now().toString() // "2026-06-12"

    /** Returns how many scanner identifications the user has used today. */
    fun getScannerUsageToday(): Int {
        if (prefs.getString(KEY_SCANNER_DATE, null) != todayIso()) return 0
        return prefs.getInt(KEY_SCANNER_COUNT, 0)
    }

    /** Increments the daily scanner counter and returns the new count. */
    fun incrementScannerUsage(): Int {
        val today = todayIso()
        val count = if (prefs.getString(KEY_SCANNER_DATE, null) == today) {
            prefs.getInt(KEY_SCANNER_COUNT, 0) + 1
        } else {
            1
        }
        prefs.edit().putString(KEY_SCANNER_DATE, today).putInt(KEY_SCANNER_COUNT, count).apply()
        return count
    }

    // --- In-App Review gating ---
    // We only auto-offer the Play in-app review to engaged users and never more than once per
    // cooldown window. Play enforces its own per-user quota on top of this, so the card may
    // still not appear even when we ask.

    /** Increments and returns the number of cold app opens. */
    fun incrementAppOpenCount(): Int {
        val next = prefs.getInt(KEY_APP_OPEN_COUNT, 0) + 1
        prefs.edit().putInt(KEY_APP_OPEN_COUNT, next).apply()
        return next
    }

    /** True only after [minOpens] opens and at least [cooldownDays] since the last prompt. */
    fun shouldRequestReview(minOpens: Int = 4, cooldownDays: Long = 45): Boolean {
        if (prefs.getInt(KEY_APP_OPEN_COUNT, 0) < minOpens) return false
        val last = prefs.getString(KEY_REVIEW_LAST_PROMPT, null) ?: return true
        return try {
            val lastDay = java.time.LocalDate.parse(last).toEpochDay()
            java.time.LocalDate.now().toEpochDay() - lastDay >= cooldownDays
        } catch (e: Exception) {
            true
        }
    }

    /** Records that we asked today, so the cooldown is respected next time. */
    fun markReviewPrompted() {
        prefs.edit().putString(KEY_REVIEW_LAST_PROMPT, todayIso()).apply()
    }

    // --- TCG Card Favourites (premium only) ---
    private val _tcgFavorites = MutableStateFlow<Set<String>>(
        prefs.getStringSet(KEY_TCG_FAVORITES, emptySet())?.toSet() ?: emptySet()
    )
    val tcgFavorites: StateFlow<Set<String>> = _tcgFavorites.asStateFlow()

    fun toggleTcgFavorite(cardId: String) {
        val updated = _tcgFavorites.value.toMutableSet()
        if (cardId in updated) updated.remove(cardId) else updated.add(cardId)
        prefs.edit().putStringSet(KEY_TCG_FAVORITES, updated).apply()
        _tcgFavorites.value = updated
    }
}
