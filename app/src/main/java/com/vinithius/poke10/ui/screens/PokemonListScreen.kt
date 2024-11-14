package com.vinithius.poke10.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.vinithius.poke10.components.PokeballComponent
import com.vinithius.poke10.datasource.database.Ability
import com.vinithius.poke10.datasource.database.PokemonEntity
import com.vinithius.poke10.datasource.database.PokemonWithDetails
import com.vinithius.poke10.datasource.database.Stat
import com.vinithius.poke10.datasource.database.StatType
import com.vinithius.poke10.datasource.database.Type
import com.vinithius.poke10.extension.capitalize
import com.vinithius.poke10.extension.getColorByString
import com.vinithius.poke10.extension.getDrawableHabitat
import com.vinithius.poke10.extension.getDrawableIco
import com.vinithius.poke10.extension.getDrawableIcoColor
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

const val URL_IMAGE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"

@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonViewModel = getViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getPokemonList(context)
    }
    val pokemonItems by viewModel.pokemonList.observeAsState(emptyList())
    val isFavoriteFilter by viewModel.isFavoriteFilter.observeAsState(false)

    LazyColumn {
        items(
            items = pokemonItems,
            key = { data -> data.pokemon.id }
        ) { pokemonData ->
            var isVisible by remember { mutableStateOf(true) }
            AnimatedVisibility(
                visible = isVisible,
                exit = scaleOut(animationSpec = tween(durationMillis = 300))
            ) {
                PokemonListItem(
                    pokemonData = pokemonData,
                    onCallBackFinishAnimation = {
                        if (isFavoriteFilter) {
                            isVisible = false
                            viewModel.removeItemIfNotIsFavorite()
                        }
                    },
                    onClickDetail = { id ->
                        viewModel.setIdPokemon(id)
                        navController.navigate("pokemonDetail/$id")
                    },
                    onClickFavorite = { pokemonFavorite ->
                        //viewModel.setFavorite(pokemonFavorite, context)
                    }
                )
            }
        }
    }
}

@Composable
fun PokemonListItem(
    pokemonData: PokemonWithDetails,
    onCallBackFinishAnimation: (() -> Unit)?,
    onClickDetail: ((Int) -> Unit)?,
    onClickFavorite: ((PokemonWithDetails) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                pokemonData.pokemon.id.let {
                    if (onClickDetail != null) {
                        onClickDetail(it)
                    }
                }
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Holder(pokemonData, onClickFavorite, onCallBackFinishAnimation)
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun Holder(
    pokemonData: PokemonWithDetails,
    onClickFavorite: ((PokemonWithDetails) -> Unit)?,
    onCallBackFinishAnimation: (() -> Unit)?,
) {
    Box(
        Modifier.background(Color.White)
    ) {
        Image(
            painter = painterResource(id = pokemonData.pokemon.habitat.getDrawableHabitat()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
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

        Box(
            modifier = Modifier
                .drawBehind {
                    val strokeWidth = 8f
                    val yPosition = (size.height - strokeWidth) + 4
                    drawLine(
                        color = pokemonData.pokemon.color.getColorByString(),
                        start = Offset(0f, yPosition),
                        end = Offset(size.width, yPosition),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    val numberPokemon = String.format("Nº%03d", pokemonData.pokemon.id)
                    Text(
                        text = numberPokemon,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp),
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
                        text = pokemonData.pokemon.name.capitalize(),
                        modifier = Modifier.padding(start = 8.dp),
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
                    StatComponent(pokemonData)
                    Spacer(modifier = Modifier.size(5.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        items(
                            items = pokemonData.types,
                            key = { data -> data.id }
                        ) { type ->
                            TypeItem(type)
                        }
                    }
                }
                LoadGifWithCoil(pokemonData)
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    PokeballComponent(
                        favorite = pokemonData.pokemon.favorite,
                        onCallBackFinishAnimation = {
                            onCallBackFinishAnimation?.invoke()
                        }
                    ) {
                        onClickFavorite?.run {
                            onClickFavorite(pokemonData)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun StatComponent(pokemonData: PokemonWithDetails) {
    Column(
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pokemonData.stats[0].name.value.capitalize()}: ${pokemonData.stats[0].baseStat}",
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
                text = "${pokemonData.stats[1].name.value.capitalize()}: ${pokemonData.stats[2].baseStat}",
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
                text = "${pokemonData.stats[2].name.value.capitalize()}: ${pokemonData.stats[2].baseStat}",
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

@Composable
private fun TypeItem(type: Type) {
    type.typeName.getDrawableIcoColor()
    Box(
        modifier = Modifier
            .background(
                color = type.typeName.getDrawableIcoColor(),
                shape = RoundedCornerShape(100)
            )
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .padding(horizontal = 5.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(
                    id = type.typeName.getDrawableIco()
                ),
                contentDescription = type.typeName,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = type.typeName.capitalize(),
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(
                            1f,
                            1f
                        ),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}

@Composable
fun LoadGifWithCoil(pokemonData: PokemonWithDetails) {
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
        .data(pokemonData.pokemon.imagePath ?: "$URL_IMAGE/${pokemonData.pokemon.id}.png")
        .crossfade(true)
        .error(android.R.drawable.ic_menu_report_image)
        .build()

    Box(modifier = Modifier.size(70.dp)) {
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
            contentDescription = pokemonData.pokemon.name,
            modifier = Modifier.size(70.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonListScreenPreview() {
    PokemonListItem(setMockupPokemon(), null, null, null)
}

private fun setMockupPokemon(): PokemonWithDetails {
    return PokemonWithDetails(
        pokemon = PokemonEntity(
            1,
            "bulbasaur",
            "green",
            "grassland",
            true,
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/1.gif",
        ),
        types = listOf(Type(1, "grass"), Type(2, "poison")),
        abilities = listOf(
            Ability(1, "overgrow", true, 10),
            Ability(2, "chlorophyll", false, 20)
        ),
        stats = listOf(
            Stat(1, StatType.HP, 10, 10),
            Stat(2, StatType.ATTACK, 10, 10),
            Stat(3, StatType.DEFENSE, 10, 10),
            Stat(4, StatType.SPECIAL_ATTACK, 10, 10),
            Stat(5, StatType.SPECIAL_DEFENSE, 10, 10),
            Stat(6, StatType.SPEED, 10, 10),
        )
    )
}
