package com.vinithius.poke10x.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.valentinilk.shimmer.shimmer
import com.vinithius.poke10x.R
import com.vinithius.poke10x.extension.capitalize

// Page list Loading

@Composable
fun LoadingPokemonList() {
    val listMockup = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GetFilterLoading()
        LazyColumn {
            items(
                items = listMockup,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(5.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    HolderPokemonList()
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun GetFilterLoading() {
    val filterMap = mapOf(
        stringResource(R.string.type) to mutableStateMapOf(),
        stringResource(R.string.ability) to mutableStateMapOf(),
        stringResource(R.string.color) to mutableStateMapOf(),
        stringResource(R.string.habitat) to mutableStateMapOf<String, Boolean>(),
    )
    val filterList = mutableListOf<String>().apply {
        add("first")
        addAll(filterMap.keys)
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        itemsIndexed(filterList) { _, filter ->
            when (filter) {
                "first" -> ViewHolderFirst()

                else -> {
                    ViewHolder(
                        label = filter,
                        filterMap = filterMap[filter],
                    )
                }
            }
        }
    }
}

@Composable
fun ViewHolder(
    label: String,
    filterMap: SnapshotStateMap<String, Boolean>?,
) {
    BadgedBox(
        badge = {
            val count = filterMap?.values?.filter { it }?.count()
            count?.takeIf { it > 0 }?.run {
                Badge(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ) {
                    Text(count.toString())
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .shimmer(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label.capitalize(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(4.dp),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun ViewHolderFirst() {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shimmer(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.clear_all),
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(4.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                )
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.clear_all),
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HolderPokemonList() {
    val context = LocalContext.current
    Box(
        Modifier.background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .shimmer()
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startX = size.width * 0.1f,
                            endX = size.width
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )

        Box {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    val numberPokemon = String.format("Nº%03d", 0)
                    Text(
                        text = numberPokemon,
                        color = Color.White,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .shimmer(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(
                                    2f,
                                    2f
                                ),
                                blurRadius = 1f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = stringResource(R.string.loading),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .shimmer(),
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(
                                    2f,
                                    2f
                                ),
                                blurRadius = 1f
                            )
                        ),
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    StatComponent()
                    Spacer(modifier = Modifier.size(5.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        TypeItemShimmer()
                    }
                }
                Box(
                    modifier = Modifier
                        .size(70.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(30.dp)
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    PokeballComponent(
                        favorite = false,
                        isShimmer = true
                    )
                }
            }
        }

    }
}

@Composable
fun StatComponent() {
    Column(
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HP: Loading",
                modifier = Modifier.shimmer(),
                style = TextStyle(
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(
                            1f,
                            1f
                        ),
                        blurRadius = 1f
                    )
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Attack: Loading",
                modifier = Modifier.shimmer(),
                style = TextStyle(
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(
                            1f,
                            1f
                        ),
                        blurRadius = 1f
                    )
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Defense: Loading",
                modifier = Modifier.shimmer(),
                style = TextStyle(
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(
                            1f,
                            1f
                        ),
                        blurRadius = 1f
                    )
                )
            )
        }
    }
}

@Preview
@Composable
private fun LoadingPokemonListPreview() {
    LoadingPokemonList()
}

// Empty list

@Composable
fun EmptyListStatus() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pokeball_03_gray),
                    contentDescription = stringResource(R.string.empty_list),
                    modifier = Modifier
                        .size(100.dp),
                    alignment = Alignment.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.no_pokemons),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun EmptyListStatusPreview() {
    EmptyListStatus()
}

// Loading Default

@Composable
fun LoadingPokeball() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("pokeball_animation_1.json"))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centraliza o Column no Box
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.loading),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoadingPokeballPreview() {
    LoadingPokeball()
}

@Composable
fun LoadingProgress(
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("pokeball_animation_1.json")
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadGifWithCoil()
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = Color.Red,
                    strokeCap = StrokeCap.Butt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
                Text(
                    text = stringResource(R.string.loading),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.dont_close_app),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal
                    )
                )
            }
        }
    }
}

@Composable
fun LoadGifWithCoil() {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val imageRequest = ImageRequest.Builder(context)
        .data(R.drawable.jigglypuff_songs)
        .crossfade(true)
        .error(android.R.drawable.ic_menu_report_image)
        .build()

    Box(
        modifier = Modifier
            .size(70.dp)
    ) {

        val painter = rememberAsyncImagePainter(
            model = imageRequest,
            imageLoader = imageLoader
        )

        // Loading
        if (painter.state is AsyncImagePainter.State.Loading) {
            androidx.compose.material.CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
            )
        }
        // Final result
        Image(
            painter = painter,
            contentDescription = "TESTE",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Preview
@Composable
private fun LoadingProgressPreview() {
    LoadingProgress(0.7f)
}

// Error

@Composable
fun ErrorStatus(
    msg: String,
    e: Exception? = null,
    callBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("icon_error_animation.json")
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(180.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = msg,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            e?.message?.run {
                Text(
                    text = this,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ElevatedButton(
                onClick = { callBackClick.invoke() },
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(R.string.try_again))
            }
        }
    }
}

@Preview
@Composable
private fun ErrorStatusPreview() {
    val context = LocalContext.current
    val exception = try {
        throw Exception("Algo deu errado!")
    } catch (e: Exception) {
        e
    }
    ErrorStatus(
        stringResource(R.string.is_general_error),
        exception
    )
}
