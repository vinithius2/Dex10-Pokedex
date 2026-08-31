package com.vinithius.dex10.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Rational
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.asImageBitmap
import com.vinithius.dex10.R
import com.vinithius.dex10.scanner.PokemonClassifier.Prediction
import com.vinithius.dex10.scanner.ScannerModelManager
import com.vinithius.dex10.ui.MainActivity
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.ScannerViewModel
import com.vinithius.dex10.ui.viewmodel.rememberPokemonViewModel
import org.koin.androidx.compose.getViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

private const val ARTWORK_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

/** Side of the square viewfinder as a fraction of the preview width. */
private const val VIEWFINDER_FRACTION = 0.78f

@Composable
fun PokemonScanScreen(
    navController: NavHostController,
    viewModel: ScannerViewModel = getViewModel(),
    pokemonViewModel: PokemonViewModel = rememberPokemonViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Re-read remaining/reset on entry so a window that elapsed while away shows fresh values.
    LaunchedEffect(Unit) { viewModel.refreshScannerState() }

    val modelState by viewModel.modelState.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val stableMatch by viewModel.stableMatch.collectAsState()
    val scansRemaining by viewModel.scansRemaining.collectAsState()
    val scansResetAt by viewModel.scansResetAt.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val capturedFrame by viewModel.capturedFrame.collectAsState()
    val photoTooSmall by viewModel.photoTooSmall.collectAsState()

    val imageCaptureRef: MutableState<ImageCapture?> = remember { mutableStateOf(null) }
    var cameraError by remember { mutableStateOf(false) }
    val captureExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { captureExecutor.shutdown() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            !hasCameraPermission -> {
                ScannerMessageContent(
                    message = stringResource(R.string.scanner_camera_permission_message),
                    buttonLabel = stringResource(R.string.scanner_grant_permission),
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }

            modelState !is ScannerModelManager.ModelState.Ready -> {
                ModelDownloadContent(
                    modelState = modelState,
                    onDownload = {
                        activity?.trackButtonClick("Scanner: download model")
                        viewModel.downloadModel()
                    }
                )
            }

            cameraError -> {
                ScannerMessageContent(
                    message = stringResource(R.string.scanner_camera_unavailable),
                    buttonLabel = stringResource(R.string.back),
                    onClick = { navController.popBackStack() }
                )
            }

            else -> {
                CameraPreviewWithCapture(
                    imageCaptureRef = imageCaptureRef,
                    onError = { cameraError = true }
                )
                ScannerOverlay(
                    predictions = predictions,
                    hasMatch = stableMatch != null,
                    isAnalyzing = isAnalyzing,
                    capturedFrame = capturedFrame,
                    scansRemaining = scansRemaining,
                    scansResetAt = scansResetAt,
                    onCountdownFinished = { viewModel.refreshScannerState() },
                    onOutOfScans = {
                        // Free session spent: the shutter NEVER captures — it opens the premium
                        // sheet. Pop back to the list first so the sheet reliably shows: a modal
                        // bottom sheet can't draw over the camera's SurfaceView preview.
                        activity?.trackButtonClick("Scanner: upsell from shutter (out of scans)")
                        navController.popBackStack()
                        viewModel.triggerUpsell()
                    },
                    onShutter = {
                        imageCaptureRef.value?.takePicture(
                            captureExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = image.toViewfinderBitmap(VIEWFINDER_FRACTION)
                                    image.close()
                                    viewModel.captureAndClassify(bitmap)
                                }
                                override fun onError(e: ImageCaptureException) {}
                            }
                        )
                    },
                    onPredictionClick = { prediction ->
                        activity?.trackButtonClick("Scanner: open detail ID: ${prediction.dexId}")
                        pokemonViewModel.setIdPokemon(prediction.dexId)
                        pokemonViewModel.setOpenedFromScanner(true)
                        viewModel.dismissMatch()
                        navController.navigate("pokemonDetail/${prediction.dexId}")
                    },
                    photoTooSmall = photoTooSmall,
                    onDismissResults = { viewModel.dismissMatch() }
                )
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White
            )
        }

        stableMatch?.let { match ->
            MatchCard(
                match = match,
                onOpenDetails = {
                    activity?.trackButtonClick("Scanner: open detail ID: ${match.dexId}")
                    pokemonViewModel.setIdPokemon(match.dexId)
                    pokemonViewModel.setOpenedFromScanner(true)
                    viewModel.dismissMatch()
                    navController.navigate("pokemonDetail/${match.dexId}")
                },
                onDismiss = { viewModel.dismissMatch() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun CameraPreviewWithCapture(
    imageCaptureRef: MutableState<ImageCapture?>,
    onError: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val providerHolder = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                // CameraX init / binding can throw when the device exposes no usable camera
                // (emulators report 0 cameras, some devices have only a front lens, or the
                // provider future itself fails). Catch it and surface a message instead of
                // crashing on the main thread.
                try {
                    val provider = providerFuture.get()
                    providerHolder.value = provider
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCaptureRef.value = imageCapture

                    // Share a single ViewPort between Preview and ImageCapture so the
                    // captured frame has the EXACT same field of view (FILL_CENTER) the
                    // user sees on screen. Without this, ImageCapture returns the full
                    // sensor frame (different aspect ratio), making the white-square crop
                    // land on a different region than what was framed.
                    val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
                    val metrics = ctx.resources.displayMetrics
                    val vpWidth = if (previewView.width > 0) previewView.width else metrics.widthPixels
                    val vpHeight = if (previewView.height > 0) previewView.height else metrics.heightPixels
                    val viewPort = ViewPort.Builder(Rational(vpWidth, vpHeight), rotation)
                        .setScaleType(ViewPort.FILL_CENTER)
                        .build()
                    val useCaseGroup = UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageCapture)
                        .setViewPort(viewPort)
                        .build()

                    // Rear camera only: the scanner is meant to be pointed at real objects, so it
                    // always uses the back lens. A device without a back camera shows the
                    // "unavailable" message instead of falling back to the (useless) front lens.
                    if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            useCaseGroup
                        )
                    } else {
                        onError()
                    }
                } catch (e: Exception) {
                    android.util.Log.w("PokemonScanScreen", "Camera unavailable", e)
                    onError()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )

    DisposableEffect(Unit) {
        onDispose { runCatching { providerHolder.value?.unbindAll() } }
    }
}

/**
 * Produces the bitmap that matches *exactly* what the white viewfinder frames.
 *
 * 1. Crops the captured image to [ImageProxy.cropRect] — the region actually shown
 *    by the preview (defined by the shared [ViewPort], FILL_CENTER).
 * 2. Rotates it to the display orientation.
 * 3. Center-crops the square whose side is [fraction] of the width, matching the
 *    `fillMaxWidth(VIEWFINDER_FRACTION).aspectRatio(1f)` viewfinder box.
 *
 * The same square is what gets frozen on screen and fed to the classifier, so the
 * framing never "jumps" between live preview and capture.
 */
private fun ImageProxy.toViewfinderBitmap(fraction: Float): Bitmap {
    val full = toBitmap()

    // 1) Crop to the visible preview region (the shared ViewPort).
    val rect = cropRect
    val base = if (
        rect.left >= 0 && rect.top >= 0 &&
        rect.width() in 1..full.width && rect.height() in 1..full.height &&
        (rect.width() != full.width || rect.height() != full.height)
    ) {
        Bitmap.createBitmap(full, rect.left, rect.top, rect.width(), rect.height())
    } else {
        full
    }

    // 2) Rotate to the display orientation.
    val rotationDegrees = imageInfo.rotationDegrees
    val upright = if (rotationDegrees == 0) {
        base
    } else {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        Bitmap.createBitmap(base, 0, 0, base.width, base.height, matrix, true)
    }

    // 3) Center-crop the square viewfinder.
    val side = (fraction * upright.width).roundToInt()
        .coerceIn(1, minOf(upright.width, upright.height))
    val left = ((upright.width - side) / 2).coerceAtLeast(0)
    val top = ((upright.height - side) / 2).coerceAtLeast(0)
    return Bitmap.createBitmap(upright, left, top, side, side)
}

@Composable
private fun ScannerOverlay(
    predictions: List<Prediction>,
    hasMatch: Boolean,
    isAnalyzing: Boolean,
    capturedFrame: Bitmap?,
    scansRemaining: Int?,
    scansResetAt: Long?,
    photoTooSmall: Boolean,
    onCountdownFinished: () -> Unit,
    onOutOfScans: () -> Unit,
    onShutter: () -> Unit,
    onPredictionClick: (Prediction) -> Unit,
    onDismissResults: () -> Unit,
) {
    // Free tier with the session spent → shutter becomes a premium gate (scansRemaining == 0).
    // Premium users have scansRemaining == null, so this is always false for them.
    val outOfScans = scansRemaining == 0
    Box(modifier = Modifier.fillMaxSize()) {
        // Hint + remaining badge + reset countdown — top center.
        // Everything here is free-tier only: premium users have scansRemaining == null.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.scanner_hint),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // One chip in a fixed spot below the hint, swapped IN PLACE so it never stacks (and
            // so the countdown never slips behind the viewfinder): while scans remain it's the
            // "X restantes" counter; once the session is spent it becomes the reset countdown;
            // when that hits zero it flips back to the counter — the cycle repeats. Premium
            // (scansRemaining == null) shows neither.
            if (scansRemaining != null) {
                if (outOfScans && scansResetAt != null) {
                    ScanResetChip(resetAt = scansResetAt, onFinished = onCountdownFinished)
                } else {
                    val isLow = scansRemaining <= 1
                    Text(
                        text = stringResource(R.string.scanner_uses_remaining, scansRemaining),
                        color = if (isLow) Color(0xFFFFB74D) else Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                color = if (isLow) Color(0xFF7B3F00).copy(alpha = 0.75f)
                                        else Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // "Photo too small" warning — shown briefly when the captured frame is below 1000 px.
        if (photoTooSmall) {
            Text(
                text = stringResource(R.string.scanner_photo_too_small),
                color = Color(0xFFFFB74D),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 148.dp, start = 24.dp, end = 24.dp)
                    .background(Color(0xFF7B3F00).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        // Viewfinder frame — true center of screen
        // While analyzing: shows the captured photo frozen inside; live feed visible outside
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(VIEWFINDER_FRACTION)
                .aspectRatio(1f)
        ) {
            if (capturedFrame != null && isAnalyzing) {
                androidx.compose.foundation.Image(
                    bitmap = capturedFrame.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            // Border always on top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        color = if (hasMatch) Color(0xFF66BB6A) else Color.White.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
        }

        // Prediction chips — shown after capture when there is no high-confidence card
        if (!hasMatch && !isAnalyzing && predictions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    predictions.forEach { prediction ->
                        PredictionChip(
                            prediction = prediction,
                            onClick = { onPredictionClick(prediction) }
                        )
                    }
                }
                IconButton(
                    onClick = onDismissResults,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.scanner_dismiss),
                        tint = Color.White
                    )
                }
            }
        }

        // Shutter button — shown when no results are visible and not currently analyzing.
        // When the free session is spent it turns into a premium gate: tapping it (or the
        // label below) opens the upsell sheet instead of capturing.
        if (!hasMatch && !isAnalyzing && predictions.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(if (outOfScans) Color(0xFFFFD54F) else Color.White, CircleShape)
                        .clickable(onClick = if (outOfScans) onOutOfScans else onShutter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (outOfScans) Icons.Default.Lock else Icons.Default.CameraAlt,
                        contentDescription = stringResource(
                            if (outOfScans) R.string.scanner_go_premium else R.string.scan_pokemon
                        ),
                        tint = Color.Black,
                        modifier = Modifier.size(if (outOfScans) 30.dp else 36.dp)
                    )
                }
                if (outOfScans) {
                    Text(
                        text = stringResource(R.string.scanner_go_premium),
                        color = Color.Black,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .clickable(onClick = onOutOfScans)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionChip(prediction: Prediction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${prediction.dexId}.png",
            contentDescription = prediction.name,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "${prediction.name} ${(prediction.confidence * 100).toInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun MatchCard(
    match: Prediction,
    onOpenDetails: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "$ARTWORK_URL${match.dexId}.png",
                contentDescription = match.name,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "#${match.dexId} · " + stringResource(
                        R.string.scanner_match_confidence,
                        (match.confidence * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onOpenDetails) {
                    Text(stringResource(R.string.scanner_open_details))
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.scanner_dismiss)
                )
            }
        }
    }
}

@Composable
private fun ModelDownloadContent(
    modelState: ScannerModelManager.ModelState,
    onDownload: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.scanner_model_title),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.scanner_model_message,
                ScannerModelManager.MODEL_SIZE_MB
            ),
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        when (modelState) {
            is ScannerModelManager.ModelState.Downloading -> {
                LinearProgressIndicator(
                    progress = { modelState.progress },
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.scanner_downloading,
                        (modelState.progress * 100).toInt()
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            is ScannerModelManager.ModelState.Error -> {
                Text(
                    text = stringResource(R.string.scanner_download_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDownload) {
                    Text(stringResource(R.string.scanner_try_again))
                }
            }

            is ScannerModelManager.ModelState.Ready -> {
                CircularProgressIndicator()
            }

            else -> {
                Button(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.scanner_download))
                }
            }
        }
    }
}

@Composable
private fun ScannerMessageContent(
    message: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onClick) {
            Text(buttonLabel)
        }
    }
}

/**
 * Pill shown in the camera overlay once the free session is spent: an explicit label plus the
 * live countdown to when the attempts refill, so the wait is unmistakable.
 */
@Composable
private fun ScanResetChip(resetAt: Long, onFinished: () -> Unit) {
    val countdown = rememberResetCountdown(resetAt, onFinished)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(Color(0xFF7B3F00).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.scanner_limit_resets_label),
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = countdown,
            color = Color(0xFFFFD54F),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Ticks once per second toward [resetAt], returning the formatted remaining time. Invokes
 * [onFinished] exactly once when it hits zero so the caller can refill the allowance. Keyed on
 * [resetAt] so a new window restarts the timer cleanly.
 */
@Composable
private fun rememberResetCountdown(resetAt: Long, onFinished: () -> Unit): String {
    var remaining by remember(resetAt) {
        mutableStateOf((resetAt - System.currentTimeMillis()).coerceAtLeast(0L))
    }
    LaunchedEffect(resetAt) {
        while (true) {
            remaining = (resetAt - System.currentTimeMillis()).coerceAtLeast(0L)
            if (remaining <= 0L) {
                onFinished()
                break
            }
            kotlinx.coroutines.delay(1000L)
        }
    }
    return formatResetCountdown(remaining)
}

/** "1:59:32" when at least an hour remains, otherwise "12:07". */
private fun formatResetCountdown(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }
}
