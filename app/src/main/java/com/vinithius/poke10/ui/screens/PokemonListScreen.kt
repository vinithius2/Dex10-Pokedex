package com.vinithius.poke10.ui.screens

import GetFilterBar
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import com.vinithius.poke10.components.TypeListDataBase
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PokemonListScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: PokemonViewModel = getViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getPokemonList(context)
    }
    val pokemonItems by viewModel.pokemonList.observeAsState(emptyList())
    val pokemonItemsBackup by viewModel.pokemonListBackup.observeAsState(emptyList())
    val isFavoriteFilter by viewModel.isFavoriteFilter.observeAsState(false)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        GetFilterBar(pokemonItemsBackup) {
            getFilterBarData(it, viewModel)
        }
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
                        viewModel = viewModel,
                        pokemonData = pokemonData,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onCallBackFinishAnimation = {
                            if (isFavoriteFilter) {
                                isVisible = false
                                viewModel.removeItemIfNotIsFavorite()
                            }
                        },
                        onClickDetail = { id, name ->
                            viewModel.setIdPokemon(id)
                            navController.navigate("pokemonDetail/$id/$name")
                        },
                        onClickFavorite = { pokemonFavorite ->
                            viewModel.setFavorite(pokemonFavorite)
                        }
                    )
                }
            }
        }
    }
}

fun getFilterBarData(
    filter: Map<String, SnapshotStateMap<String, Boolean>>,
    viewModel: PokemonViewModel
) {
    viewModel.updateFilterState(filter)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PokemonListItem(
    viewModel: PokemonViewModel?,
    pokemonData: PokemonWithDetails,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onCallBackFinishAnimation: (() -> Unit)?,
    onClickDetail: ((Int, String) -> Unit)?,
    onClickFavorite: ((PokemonWithDetails) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                pokemonData.pokemon.let {
                    if (onClickDetail != null) {
                        onClickDetail(it.id, it.name)
                    }
                }
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Holder(
            viewModel,
            pokemonData,
            animatedVisibilityScope,
            onClickFavorite,
            onCallBackFinishAnimation
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun SharedTransitionScope.Holder(
    viewModel: PokemonViewModel?,
    pokemonData: PokemonWithDetails,
    animatedVisibilityScope: AnimatedVisibilityScope?,
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
                    TypeListDataBase(pokemonData.types)
                }
                LoadGifWithCoil(viewModel, pokemonData, animatedVisibilityScope)
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
                text = "${pokemonData.stats[1].name.value.capitalize()}: ${pokemonData.stats[1].baseStat}",
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LoadGifWithCoil(
    viewModel: PokemonViewModel?,
    pokemonData: PokemonWithDetails,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
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

    Box(
        modifier = Modifier
            .size(70.dp)
    ) {

        val painter = rememberAsyncImagePainter(
            model = imageRequest,
            imageLoader = imageLoader
        )
        LaunchedEffect(painter) {
            snapshotFlow { painter.state }
                .collect { state ->
                    if (state is AsyncImagePainter.State.Success) {
                        viewModel?.updateSharedImage(
                            pokemonData.pokemon.id.toString(),
                            painter
                        )
                    }
                }
        }

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
            modifier = Modifier
                .size(70.dp)
                .sharedElement(
                    state = rememberSharedContentState(key = "${pokemonData.pokemon.id}"),
                    animatedVisibilityScope = animatedVisibilityScope!!,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 1000)
                    }
                )
        )
    }
}

// PREVIEW ///////////////////////////////////////////////////////////////////////////////////////

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun PokemonListScreenPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            PokemonListItem(
                null,
                setMockupPokemon(),
                null,
                null,
                null,
                null
            )
        }
    }
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
