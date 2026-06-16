package com.vinithius.dex10.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinithius.dex10.datasource.data.PremiumManager
import com.vinithius.dex10.scanner.PokemonClassifier
import com.vinithius.dex10.scanner.PokemonClassifier.Prediction
import com.vinithius.dex10.scanner.ScannerModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val modelManager: ScannerModelManager,
    private val premiumManager: PremiumManager,
    private val appContext: Context,
) : ViewModel() {

    val modelState: StateFlow<ScannerModelManager.ModelState> = modelManager.state

    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())
    val predictions: StateFlow<List<Prediction>> = _predictions

    /** Set once the same Pokémon stays on top with high confidence for a few frames. */
    private val _stableMatch = MutableStateFlow<Prediction?>(null)
    val stableMatch: StateFlow<Prediction?> = _stableMatch

    /**
     * Remaining scanner uses for free users today (null = unlimited / premium).
     * Updated every time a scan is consumed or the screen is opened.
     */
    private val _scansRemaining = MutableStateFlow(premiumManager.scannerUsesRemainingToday())
    val scansRemaining: StateFlow<Int?> = _scansRemaining

    private var classifier: PokemonClassifier? = null
    private var lastInferenceAt = 0L
    private var candidateDexId = -1
    private var candidateStreak = 0

    fun downloadModel() {
        viewModelScope.launch(Dispatchers.IO) { modelManager.download() }
    }

    /** Runs inference synchronously; must be called from the camera analyzer thread. */
    @WorkerThread
    fun onFrame(bitmap: Bitmap) {
        val readyState = modelState.value as? ScannerModelManager.ModelState.Ready ?: return
        if (_stableMatch.value != null) return // paused while the result card is shown
        val now = SystemClock.elapsedRealtime()
        if (now - lastInferenceAt < MIN_INTERVAL_MS) return
        lastInferenceAt = now

        val activeClassifier = classifier
            ?: runCatching { PokemonClassifier(appContext, readyState.file) }
                .getOrNull()
                ?.also { classifier = it }
            ?: return
        val results = runCatching { activeClassifier.classify(bitmap) }.getOrElse { return }

        val top = results.firstOrNull()
        if (top == null || top.confidence < MIN_DISPLAY_CONFIDENCE) {
            _predictions.value = emptyList()
            resetStability()
            return
        }
        _predictions.value = results
        if (top.confidence >= STABLE_CONFIDENCE) {
            if (top.dexId == candidateDexId) candidateStreak++ else {
                candidateDexId = top.dexId
                candidateStreak = 1
            }
            if (candidateStreak >= STABLE_FRAMES) {
                // Show the card; the scan is only consumed when the user actually
                // navigates to the detail screen (see consumeForNavigation).
                _stableMatch.value = top
                resetStability()
            }
        } else {
            resetStability()
        }
    }

    fun triggerUpsell() = premiumManager.triggerUpsell()

    /**
     * Called right before navigating to the detail screen (from card button or chip tap).
     * Consumes one daily scan for free users. Returns false and fires the upsell if the
     * daily limit has already been reached; the caller should NOT navigate in that case.
     */
    fun consumeForNavigation(): Boolean {
        val allowed = premiumManager.consumeScannerUseOrTriggerUpsell()
        _scansRemaining.value = premiumManager.scannerUsesRemainingToday()
        return allowed
    }

    fun dismissMatch() {
        _stableMatch.value = null
        _predictions.value = emptyList()
        resetStability()
        _scansRemaining.value = premiumManager.scannerUsesRemainingToday()
    }

    private fun resetStability() {
        candidateDexId = -1
        candidateStreak = 0
    }

    override fun onCleared() {
        classifier?.close()
        classifier = null
    }

    companion object {
        private const val MIN_INTERVAL_MS = 400L
        private const val MIN_DISPLAY_CONFIDENCE = 0.15f
        private const val STABLE_CONFIDENCE = 0.45f
        private const val STABLE_FRAMES = 2
    }
}
