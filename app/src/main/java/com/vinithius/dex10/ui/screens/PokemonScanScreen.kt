package com.vinithius.dex10.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
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
import androidx.compose.ui.graphics.Color
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

private const val ARTWORK_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

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

    val modelState by viewModel.modelState.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val stableMatch by viewModel.stableMatch.collectAsState()
    val scansRemaining by viewModel.scansRemaining.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    // Include isAnalyzing in hasResults so the limit screen never flashes mid-capture
    val hasResults = predictions.isNotEmpty() || stableMatch != null || isAnalyzing
    val isOutOfScans = scansRemaining != null && scansRemaining == 0 && !hasResults

    val imageCaptureRef: MutableState<ImageCapture?> = remember { mutableStateOf(null) }
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

            isOutOfScans -> {
                ScannerMessageContent(
                    message = stringResource(R.string.scanner_limit_reached),
                    buttonLabel = stringResource(R.string.go_premium),
                    onClick = {
                        activity?.trackButtonClick("Scanner: upsell from limit screen")
                        navController.popBackStack()
                        viewModel.triggerUpsell()
                    }
                )
            }

            else -> {
                CameraPreviewWithCapture(imageCaptureRef = imageCaptureRef)
                ScannerOverlay(
                    predictions = predictions,
                    hasMatch = stableMatch != null,
                    isAnalyzing = isAnalyzing,
                    scansRemaining = scansRemaining,
                    onShutter = {
                        imageCaptureRef.value?.takePicture(
                            captureExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = image.toRotatedBitmap()
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
                        viewModel.dismissMatch()
                        navController.navigate("pokemonDetail/${prediction.dexId}")
                    },
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
                val provider = providerFuture.get()
                providerHolder.value = provider
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                imageCaptureRef.value = imageCapture
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )

    DisposableEffect(Unit) {
        onDispose { providerHolder.value?.unbindAll() }
    }
}

private fun ImageProxy.toRotatedBitmap(): Bitmap {
    val bitmap = toBitmap()
    val rotationDegrees = imageInfo.rotationDegrees
    if (rotationDegrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@Composable
private fun ScannerOverlay(
    predictions: List<Prediction>,
    hasMatch: Boolean,
    isAnalyzing: Boolean,
    scansRemaining: Int?,
    onShutter: () -> Unit,
    onPredictionClick: (Prediction) -> Unit,
    onDismissResults: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Hint + remaining badge — top center
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
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
            if (scansRemaining != null) {
                val isLow = scansRemaining <= 1
                Text(
                    text = stringResource(R.string.scanner_uses_remaining, scansRemaining),
                    color = if (isLow) Color(0xFFFFB74D) else Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(
                            color = if (isLow) Color(0xFF7B3F00).copy(alpha = 0.75f)
                                    else Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Viewfinder frame — true center of screen
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.78f)
                .aspectRatio(1f)
                .border(
                    width = 3.dp,
                    color = if (hasMatch) Color(0xFF66BB6A) else Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Analyzing spinner — shown while the model is running on the captured frame
        if (isAnalyzing) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
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

        // Shutter button — shown when no results are visible and not currently analyzing
        if (!hasMatch && !isAnalyzing && predictions.isEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .size(72.dp)
                    .background(Color.White, CircleShape)
                    .clickable(onClick = onShutter),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.scan_pokemon),
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
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
