package com.vinithius.poke10.ui.screens

import GetFilterBar
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.vinithius.poke10.R
import com.vinithius.poke10.components.AdRequirementDialog
import com.vinithius.poke10.components.EmptyListStatus
import com.vinithius.poke10.components.ErrorStatus
import com.vinithius.poke10.components.LoadingPokemonList
import com.vinithius.poke10.components.LoadingProgress
import com.vinithius.poke10.components.PokeballComponent
import com.vinithius.poke10.components.TypeListDataBase
import com.vinithius.poke10.datasource.database.Ability
import com.vinithius.poke10.datasource.database.PokemonEntity
import com.vinithius.poke10.datasource.database.PokemonWithDetails
import com.vinithius.poke10.datasource.database.Stat
import com.vinithius.poke10.datasource.database.StatType
import com.vinithius.poke10.datasource.database.Type
import com.vinithius.poke10.extension.capitalize
import com.vinithius.poke10.extension.getDrawableHabitat
import com.vinithius.poke10.extension.getParseColorByString
import com.vinithius.poke10.ui.MainActivity
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import com.vinithius.poke10.ui.viewmodel.RequestStateList
import org.koin.androidx.compose.getViewModel

const val URL_IMAGE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"

@Composable
private fun StateRequest(
    viewModel: PokemonViewModel,
    loadingFirebase: @Composable () -> Unit,
    loading: @Composable () -> Unit,
    success: @Composable () -> Unit,
    error: @Composable () -> Unit,
) {
    val requestState by viewModel.stateList.observeAsState(RequestStateList.Loading)
    when (requestState) {
        is RequestStateList.LoadingFirebase -> {
            loadingFirebase.invoke()
        }

        is RequestStateList.Loading -> {
            loading.invoke()
        }

        is RequestStateList.Success -> {
            success.invoke()
        }

        is RequestStateList.Error -> {
            error.invoke()
        }
    }
}

@Composable
private fun SetAnalyticScreenName() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    activity?.trackScreenView("Screen list")
}

@Composable
private fun getActivity(): MainActivity? {
    val context = LocalContext.current
    val activity = context as? MainActivity
    return activity
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PokemonListScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: PokemonViewModel = getViewModel()
) {
    val activity = getActivity()
    val context = LocalContext.current
    SetAnalyticScreenName()
    LaunchedEffect(Unit) {
        if (viewModel.pokemonList.value == null || viewModel.pokemonList.value?.isEmpty() == true) {
            viewModel.getPokemonList(context)
        }
        viewModel.setPokemonColor(null)
    }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val pokemonItems by viewModel.pokemonList.observeAsState(emptyList())
    val isFavoriteFilter by viewModel.isFavoriteFilter.observeAsState(false)
    val sharedPreferences = context.getSharedPreferences("pokemon_prefs", Context.MODE_PRIVATE)
    val isRewarded by viewModel.isRewarded.observeAsState(true)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StateRequest(
            viewModel = viewModel,
            loadingFirebase = {
                val progress by viewModel.loadingPercent.observeAsState(0f)
                LoadingProgress(progress)
            },
            loading = {
                LoadingPokemonList()
            },
            success = {
                GetFilterBar(
                    onCallBackClearFavoriteFilter = {
                        viewModel.getPokemonFavoriteList(false)
                    },
                    onCallBackFilter = {
                        getFilterBarData(it, viewModel)
                    }
                )
                if (pokemonItems.isNotEmpty()) {
                    val pokemonOfTheDayName =
                        sharedPreferences.getString("pokemon_of_the_day", null)
                    LazyColumn(state = listState) {
                        itemsIndexed(
                            items = pokemonItems,
                            key = { _, data -> data.pokemon.id }
                        ) { _, pokemonData ->
                            var isVisible by remember { mutableStateOf(true) }
                            val choiceOfTheDay = pokemonOfTheDayName == pokemonData.pokemon.name
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
                                    choiceOfTheDayStatus = choiceOfTheDay,
                                    onClickDetail = { id, name, color, choiceOfTheDayStatus ->
                                        if (choiceOfTheDay) {
                                            if (isRewarded) {
                                                viewModel.adUnitIdChoiceOfTheDayRewardedShow(
                                                    choiceOfTheDayStatus
                                                )
                                            } else {
                                                viewModel.adUnitIdChoiceOfTheDayInterstitialShow(
                                                    choiceOfTheDayStatus
                                                )
                                            }
                                        } else {
                                            goToDetails(
                                                navController,
                                                activity,
                                                viewModel,
                                                id,
                                                name,
                                                color,
                                                choiceOfTheDayStatus
                                            )
                                        }
                                        activity?.trackButtonClick("Click button detail: $name")
                                        viewModel.setIdPokemon(id)
                                        viewModel.setChoiceOfTheDay(choiceOfTheDayStatus)
                                        navController.navigate("pokemonDetail/$id/$name/$color")

                                    },
                                    onClickFavorite = { pokemonFavorite ->
                                        pokemonFavorite.pokemon.name
                                        activity?.trackButtonClick("Click favorite item list: ${pokemonFavorite.pokemon.name}")
                                        viewModel.setFavorite(pokemonFavorite.pokemon.id)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    EmptyListStatus()
                }
            },
            error = {
                ErrorStatus(
                    stringResource(R.string.is_general_error),
                ) {
                    viewModel.getPokemonList(context)
                }
            }
        )
    }
}

private fun goToDetails(
    navController: NavController,
    activity: MainActivity?,
    viewModel: PokemonViewModel,
    id: Int,
    name: String,
    color: String,
    choiceOfTheDayStatus: Boolean
) {
    activity?.trackButtonClick("Click button detail: $name")
    viewModel.setIdPokemon(id)
    viewModel.setChoiceOfTheDay(choiceOfTheDayStatus)
    navController.navigate("pokemonDetail/$id/$name/$color")
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
    choiceOfTheDayStatus: Boolean = false,
    onCallBackFinishAnimation: (() -> Unit)?,
    onClickDetail: ((Int, String, String, Boolean) -> Unit)?,
    onClickFavorite: ((PokemonWithDetails) -> Unit)?
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pokemon_prefs", Context.MODE_PRIVATE)
    var dontShowAgain by remember { mutableStateOf(false) }
    dontShowAgain = sharedPreferences.getBoolean("dont_show_again", false)
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AdRequirementDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                clickAndGoToDetails(
                    viewModel,
                    pokemonData,
                    choiceOfTheDayStatus,
                    onClickDetail,
                )
            },
            dontShowAgain = dontShowAgain,
            onDontShowAgainChanged = {
                with(sharedPreferences.edit()) {
                    putBoolean("dont_show_again", it)
                    apply()
                }
                dontShowAgain = it
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (choiceOfTheDayStatus && dontShowAgain.not()) {
                    showDialog = true
                } else {
                    clickAndGoToDetails(
                        viewModel,
                        pokemonData,
                        choiceOfTheDayStatus,
                        onClickDetail,
                    )
                }
            },
        elevation = 5.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Holder(
            viewModel,
            pokemonData,
            animatedVisibilityScope,
            choiceOfTheDayStatus,
            onClickFavorite,
            onCallBackFinishAnimation
        )
    }
}

private fun clickAndGoToDetails(
    viewModel: PokemonViewModel?,
    pokemonData: PokemonWithDetails,
    choiceOfTheDayStatus: Boolean = false,
    onClickDetail: ((Int, String, String, Boolean) -> Unit)?
) {
    pokemonData.pokemon.let {
        if (onClickDetail != null) {
            viewModel?.setDetailsScreen(true)
            onClickDetail(it.id, it.name, it.color, choiceOfTheDayStatus)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun SharedTransitionScope.Holder(
    viewModel: PokemonViewModel?,
    pokemonData: PokemonWithDetails,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    choiceOfTheDayStatus: Boolean = false,
    onClickFavorite: ((PokemonWithDetails) -> Unit)?,
    onCallBackFinishAnimation: (() -> Unit)?,
) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pokemon_prefs", Context.MODE_PRIVATE)
    val hidePokemonOfTheDay = sharedPreferences.getBoolean("hide_pokemon_of_the_day", true)

    // Normal
    var habitat = pokemonData.pokemon.habitat.getDrawableHabitat()

    val modifierBackground = if (choiceOfTheDayStatus && hidePokemonOfTheDay) {
        Modifier
    } else {
        Modifier.drawWithContent {
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
    }

    val modifierBox = if (choiceOfTheDayStatus && hidePokemonOfTheDay) {
        Modifier
            .background(Color.White)
            .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    val habitatRes = if (choiceOfTheDayStatus && hidePokemonOfTheDay) {
        R.drawable.backgroundchoiceoftheday
    } else {
        habitat
    }

        Box(modifier = modifierBox) {
            Image(
                painter = painterResource(id = habitatRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifierBackground.then(Modifier.matchParentSize())
            )
        Box(
            modifier = Modifier
                .drawBehind {
                    val strokeWidth = 8f
                    val yPosition = (size.height - strokeWidth) + 4
                    drawLine(
                        color = pokemonData.pokemon.color.getParseColorByString(),
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
                    var result = String.format("Nº%03d", pokemonData.pokemon.id)
                    var name = pokemonData.pokemon.name.capitalize()
                    if (choiceOfTheDayStatus && hidePokemonOfTheDay) {
                        result = String()
                        name = "????"
                    }
                    if (choiceOfTheDayStatus) {
                        val choiceOfTheDay = stringResource(R.string.choice_of_the_day)
                        result = "$result ($choiceOfTheDay)"
                    }
                    Text(
                        text = result,
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
                        text = name,
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
                    StatComponent(pokemonData, choiceOfTheDayStatus, hidePokemonOfTheDay)
                    Spacer(modifier = Modifier.size(5.dp))
                    TypeListDataBase(pokemonData.types, choiceOfTheDayStatus, hidePokemonOfTheDay)
                }
                LoadGifWithCoil(
                    viewModel,
                    pokemonData,
                    animatedVisibilityScope,
                    choiceOfTheDayStatus,
                    hidePokemonOfTheDay
                )
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    PokeballComponent(
                        favorite = pokemonData.pokemon.favorite,
                        choiceOfTheDayStatus = choiceOfTheDayStatus,
                        hidePokemonOfTheDay = hidePokemonOfTheDay,
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
fun StatComponent(
    pokemonData: PokemonWithDetails,
    choiceOfTheDayStatus: Boolean = false,
    hidePokemonOfTheDay: Boolean = false
) {
    Column(
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pokemonData.stats.take(3).forEachIndexed { index, stat ->
                var result = "${stat.name.value.capitalize()}: ${stat.baseStat}"
                if (hidePokemonOfTheDay && choiceOfTheDayStatus) {
                    result = "${stat.name.value.capitalize()}: ??"
                }
                Text(
                    text = result,
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(1f, 1f),
                            blurRadius = 1f
                        )
                    )
                )
                if (index < 2) {
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LoadGifWithCoil(
    viewModel: PokemonViewModel?,
    pokemonData: PokemonWithDetails,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    choiceOfTheDayStatus: Boolean = false,
    hidePokemonOfTheDay: Boolean = false
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
                ),
            colorFilter = if (hidePokemonOfTheDay && choiceOfTheDayStatus) ColorFilter.tint(Color.Black) else null
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
                false,
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
