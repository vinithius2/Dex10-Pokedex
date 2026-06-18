package com.vinithius.dex10.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
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

    private val _stableMatch = MutableStateFlow<Prediction?>(null)
    val stableMatch: StateFlow<Prediction?> = _stableMatch

    private val _scansRemaining = MutableStateFlow(premiumManager.scannerUsesRemainingToday())
    val scansRemaining: StateFlow<Int?> = _scansRemaining

    /** True while the captured photo is being run through the classifier. */
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    /** The bitmap currently being analyzed; shown frozen inside the viewfinder. */
    private val _capturedFrame = MutableStateFlow<Bitmap?>(null)
    val capturedFrame: StateFlow<Bitmap?> = _capturedFrame

    private var classifier: PokemonClassifier? = null

    fun downloadModel() {
        viewModelScope.launch(Dispatchers.IO) { modelManager.download() }
    }

    /**
     * Consumes one daily scan, then classifies the captured bitmap.
     * Sets [predictions] (top-3) and [stableMatch] (if top-1 confidence ≥ threshold).
     * Must be called from any thread; switches to IO internally.
     */
    fun captureAndClassify(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val readyState = modelState.value as? ScannerModelManager.ModelState.Ready ?: return@launch
            _capturedFrame.value = bitmap  // freeze inside viewfinder immediately
            _isAnalyzing.value = true
            try {
                val allowed = premiumManager.consumeScannerUseOrTriggerUpsell()
                _scansRemaining.value = premiumManager.scannerUsesRemainingToday()
                if (!allowed) return@launch

                val activeClassifier = classifier
                    ?: runCatching { PokemonClassifier(appContext, readyState.file) }
                        .getOrNull()
                        ?.also { classifier = it }
                    ?: return@launch

                val results = runCatching { activeClassifier.classify(bitmap) }.getOrElse { return@launch }
                val top = results.firstOrNull()

                if (top == null || top.confidence < MIN_DISPLAY_CONFIDENCE) {
                    _predictions.value = emptyList()
                    _stableMatch.value = null
                } else {
                    _predictions.value = results
                    if (top.confidence >= STABLE_CONFIDENCE) {
                        _stableMatch.value = top
                    }
                }
            } finally {
                _isAnalyzing.value = false
                _capturedFrame.value = null  // unfreeze viewfinder
            }
        }
    }

    fun triggerUpsell() = premiumManager.triggerUpsell()

    fun dismissMatch() {
        _stableMatch.value = null
        _predictions.value = emptyList()
        _capturedFrame.value = null
        _scansRemaining.value = premiumManager.scannerUsesRemainingToday()
    }

    override fun onCleared() {
        classifier?.close()
        classifier = null
    }

    companion object {
        private const val MIN_DISPLAY_CONFIDENCE = 0.15f
        private const val STABLE_CONFIDENCE = 0.45f
    }
}
