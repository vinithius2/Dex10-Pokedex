package com.vinithius.dex10.extension

import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.datasource.data.AppPreferences
import org.koin.java.KoinJavaComponent.inject

@Composable
fun SpriteItem.LoadGifWithCoilToSprite(
    context: Context,
    supportZoom: Boolean
) {
    if (supportZoom) {
        LoadGifWithCoilToSpriteSupportZoom(
            context,
            this.url,
            this.title
        )
    } else {
        LoadGifWithCoilToSpriteNoSupportZoom(
            context,
            this.url,
            this.title
        )
    }
}

@Composable
private fun LoadGifWithCoilToSpriteNoSupportZoom(
    context: Context,
    url: String,
    title: String?
) {
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
            add(SvgDecoder.Factory())
        }
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .build()

    Box(
        modifier = Modifier
            .size(70.dp)
            .padding(6.dp)
    ) {
        val painter = rememberAsyncImagePainter(
            model = imageRequest,
            imageLoader = imageLoader
        )
        // Loading
        if (painter.state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
            )
        }
        // Final result
        Image(
            painter = painter,
            contentDescription = title,
            modifier = Modifier.size(70.dp)
        )
    }
}

@Composable
private fun LoadGifWithCoilToSpriteSupportZoom(
    context: Context,
    url: String,
    title: String?
) {
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
            add(SvgDecoder.Factory())
        }
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .build()

// Estados para controlar o zoom e o deslocamento
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            // Detecta os gestos de transformação (zoom e pan)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offset += pan
                }
            }
    ) {
        val painter = rememberAsyncImagePainter(
            model = imageRequest,
            imageLoader = imageLoader
        )

        // Indicador de carregamento enquanto a imagem é processada
        if (painter.state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
            )
        }
        // Exibição da imagem com suporte a zoom e pan
        Image(
            painter = painter,
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}

@Composable
fun Int.LoadGifWithCoil(
    viewModel: PokemonViewModel?,
    modifier: Modifier = Modifier,
    fallbackDrawableRes: Int = android.R.drawable.ic_menu_report_image
) {
    val urlShowdown = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/"
    val urlPixel    = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
    val urlOfficial = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
    val urlHome     = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/"

    val appPreferences: AppPreferences by inject(AppPreferences::class.java)
    val spriteType by appPreferences.spriteType.collectAsState()

    val gifUrl      = "$urlShowdown$this.gif"
    val officialUrl = "$urlOfficial$this.png"
    val homeUrl     = "$urlHome$this.png"
    val pixelUrl    = "$urlPixel$this.png"

    val primaryUrl = when (spriteType) {
        AppPreferences.SpriteType.GIF              -> gifUrl
        AppPreferences.SpriteType.OFFICIAL_ARTWORK -> officialUrl
        AppPreferences.SpriteType.HOME             -> homeUrl
        AppPreferences.SpriteType.PIXEL            -> pixelUrl
    }

    var currentData by remember(spriteType) { mutableStateOf<Any>(primaryUrl) }

    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }
        .build()

    fun buildRequest(data: Any): ImageRequest =
        ImageRequest.Builder(context).data(data).crossfade(true).build()

    val painter = rememberAsyncImagePainter(
        model = buildRequest(currentData),
        imageLoader = imageLoader
    )

    LaunchedEffect(painter.state) {
        when (painter.state) {
            is AsyncImagePainter.State.Error -> {
                // Fallback chain: primary → pixel → drawable icon
                currentData = when (currentData) {
                    primaryUrl -> if (primaryUrl != pixelUrl) pixelUrl else fallbackDrawableRes
                    pixelUrl   -> fallbackDrawableRes
                    else       -> fallbackDrawableRes
                }
            }
            is AsyncImagePainter.State.Success -> {
                viewModel?.updateSharedImage(this@LoadGifWithCoil.toString(), painter)
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (currentData) {
            is Int -> {
                Icon(
                    painter = painterResource(id = currentData as Int),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                if (painter.state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Image(
                    painter = painter,
                    contentDescription = "Pokemon $this",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
