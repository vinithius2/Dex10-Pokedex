package com.vinithius.dex10.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.text.Spanned
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer
import com.vinithius.dex10.R
import com.vinithius.dex10.admobbanners.AdAdvancedNative
import com.vinithius.dex10.datasource.data.AppPreferences
import org.koin.androidx.compose.get
import com.vinithius.dex10.components.ErrorStatus
import com.vinithius.dex10.components.TypeItem
import com.vinithius.dex10.components.TypeItemShimmer
import com.vinithius.dex10.components.TypeListResponse
import com.vinithius.dex10.datasource.mapper.fromDefaultToListType
import com.vinithius.dex10.datasource.response.Pokemon
import com.vinithius.dex10.datasource.response.Type
import com.vinithius.dex10.datasource.response.TcgCard
import com.vinithius.dex10.datasource.response.JikanAnimeInfo
import com.vinithius.dex10.extension.LoadGifWithCoil
import com.vinithius.dex10.extension.LoadGifWithCoilToSprite
import com.vinithius.dex10.extension.SpriteItem
import com.vinithius.dex10.extension.capitalize
import com.vinithius.dex10.extension.convertPounds
import com.vinithius.dex10.extension.converterIntToDouble
import com.vinithius.dex10.extension.getColorByString
import com.vinithius.dex10.extension.getDrawableHabitat
import com.vinithius.dex10.extension.getFlavorTextForLanguage
import com.vinithius.dex10.extension.getHtmlCompat
import com.vinithius.dex10.extension.getIdIntoUrl
import com.vinithius.dex10.extension.EvoDisplayEntry
import com.vinithius.dex10.extension.EvoStage
import com.vinithius.dex10.extension.toEvoStages
import com.vinithius.dex10.extension.getSpriteItems
import com.vinithius.dex10.extension.getStringEggGroup
import com.vinithius.dex10.extension.getStringHabitat
import com.vinithius.dex10.extension.getStringShape
import com.vinithius.dex10.extension.getStringStat
import com.vinithius.dex10.extension.getWindowColumns
import com.vinithius.dex10.extension.translateIfSupported
import com.vinithius.dex10.ui.MainActivity
import com.vinithius.dex10.ui.theme.text
import com.vinithius.dex10.ui.viewmodel.PokemonViewModel
import com.vinithius.dex10.ui.viewmodel.rememberPokemonViewModel
import com.vinithius.dex10.ui.viewmodel.RequestStateDetail
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
private fun StateRequest(
    viewModel: PokemonViewModel,
    loading: @Composable () -> Unit,
    success: @Composable () -> Unit,
    error: @Composable () -> Unit,
) {
    val requestState by viewModel.stateDetail.observeAsState(RequestStateDetail.Loading)
    when (requestState) {
        is RequestStateDetail.Loading -> {
            loading.invoke()
        }

        is RequestStateDetail.Success -> {
            success.invoke()
        }

        is RequestStateDetail.Error -> {
            error.invoke()
        }
    }
}

@Composable
private fun DefaultLoadingComposable(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        ),
        modifier = Modifier.shimmer()
    )
    Text(
        text = stringResource(R.string.three_dots),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        ),
        modifier = Modifier.shimmer()
    )
}

@Composable
private fun SetAnalyticScreenName(pokemonName: String) {
    com.vinithius.dex10.analytics.AnalyticsManager.logScreenView(
        "pokemon_detail",
        "PokemonDetailScreen"
    )
    com.vinithius.dex10.analytics.AnalyticsManager.logEvent(
        "view_pokemon",
        "pokemon_name",
        pokemonName
    )
}

@Composable
private fun getActivity(): MainActivity? {
    val context = LocalContext.current
    val activity = context as? MainActivity
    return activity
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PokemonDetailScreen(
    navController: NavController?,
    pokemonId: Int,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    // Observes
    val id by viewModel.idPokemon.observeAsState()
    val pokemonDetail by viewModel.pokemonDetail.observeAsState()
    val color by viewModel.pokemonColor.observeAsState()
    val choiceOfTheDayStatus by viewModel.choiceOfTheDay.observeAsState(false)
    val painter = viewModel.getSharedImage(pokemonId.toString())
    val columns = (LocalContext.current as MainActivity).getWindowColumns()

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPokemonDetail()
        }
    }

    LaunchedEffect(id) {
        if (id != null) {
            viewModel.getPokemonDetail()
        }
    }

    LaunchedEffect(pokemonId) {
        viewModel.setIdPokemon(pokemonId)
    }

    pokemonDetail?.name?.let { name ->
        SetAnalyticScreenName(name)
    }

    StateRequest(
        viewModel = viewModel,
        loading = {
            if (columns == 1) {
                MainCard(
                    navController,
                    pokemonId,
                    animatedVisibilityScope,
                    choiceOfTheDayStatus,
                    pokemonDetail,
                    color.toString(),
                    painter,
                )
            } else {
                MainCardLargeScreen(
                    navController,
                    pokemonId,
                    animatedVisibilityScope,
                    choiceOfTheDayStatus,
                    pokemonDetail,
                    color.toString(),
                    painter,
                    columns,
                )
            }
        },
        success = {
            if (columns == 1) {
                MainCard(
                    navController,
                    pokemonId,
                    animatedVisibilityScope,
                    choiceOfTheDayStatus,
                    pokemonDetail,
                    color.toString(),
                    painter,
                )
            } else {
                MainCardLargeScreen(
                    navController,
                    pokemonId,
                    animatedVisibilityScope,
                    choiceOfTheDayStatus,
                    pokemonDetail,
                    color.toString(),
                    painter,
                    columns,
                )
            }
        },
        error = {
            ErrorStatus()
        }
    )

}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainCard(
    navController: NavController?,
    pokemonId: Int,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    choiceOfTheDayStatus: Boolean,
    pokemonDetail: Pokemon?,
    color: String,
    painter: AsyncImagePainter? = null,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.premiumManager.isPremium.collectAsState(initial = false)
    val adUnitIdAdAdvancedNative by viewModel.adUnitIdAdAdvancedNative.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ChoiceOfTheDay(choiceOfTheDayStatus)
        Card(
            modifier = Modifier
                .height(320.dp)
                .padding(8.dp),
            elevation = CardDefaults.elevatedCardElevation(5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Habitat and Pokémon Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Gives weight to expand proportionally
                ) {
                    // Habitat Image
                    PokemonHabitat(viewModel, pokemonDetail)
                    // Pokemon Image
                    if (painter != null) {
                        Image(
                            painter = painter,
                            contentDescription = "Pokemon",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 50.dp)
                                .size(180.dp)
                                .sharedElement(
                                    state = rememberSharedContentState(key = "$pokemonId"),
                                    animatedVisibilityScope = animatedVisibilityScope!!,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 1000)
                                    }
                                ),
                        )
                    } else {
                        pokemonId.LoadGifWithCoil(viewModel)
                    }

                    // Zenith Cry Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        ZenithCryButton(
                            viewModel = viewModel,
                            color = color.getColorByString(isSystemInDarkTheme())
                        )
                    }

                    // weight and height
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                StateRequest(
                                    viewModel = viewModel,
                                    loading = { HeightLoadingComposable() },
                                    success = { HeightSuccessComposable(pokemonDetail) },
                                    error = {
                                        // Do nothing yet
                                    }
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                StateRequest(
                                    viewModel = viewModel,
                                    loading = { WeightLoadingComposable() },
                                    success = { WeightSuccessComposable(pokemonDetail) },
                                    error = {
                                        // Do nothing yet
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(5.dp))
                        // Generation and Base Experience
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DefaultFirstCardData(
                                        viewModel = viewModel,
                                        title = stringResource(R.string.generation),
                                        value = pokemonDetail?.specie?.generation?.name
                                            ?.split("-")
                                            ?.last()
                                            ?.uppercase()
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DefaultFirstCardData(
                                        viewModel = viewModel,
                                        title = stringResource(R.string.base_exp),
                                        value = pokemonDetail?.base_experience?.toString()
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.size(5.dp))
                        // Shape and Base Capture rate
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DefaultFirstCardData(
                                        viewModel = viewModel,
                                        title = stringResource(R.string.shape),
                                        value = pokemonDetail?.specie?.shape?.name?.getStringShape(
                                            context
                                        ) ?: stringResource(R.string.three_dots)
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DefaultFirstCardData(
                                        viewModel = viewModel,
                                        title = stringResource(R.string.capture_rate),
                                        value = pokemonDetail?.specie?.capture_rate?.toString()
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            TypeListResponse(pokemonDetail?.types ?: listOf())
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        PokemonArts(viewModel, pokemonDetail)
        val appPreferences: AppPreferences = get()
        val tcgFavorites by appPreferences.tcgFavorites.collectAsState()
        PokemonTcgSection(
            pokemonDetail = pokemonDetail,
            color = color.getColorByString(isSystemInDarkTheme()),
            isPremium = isPremium,
            tcgFavorites = tcgFavorites,
            onToggleFavorite = { appPreferences.toggleTcgFavorite(it) },
            onPremiumRequired = { viewModel.premiumManager.triggerUpsell() }
        )
        PokemonChart(viewModel, color, pokemonDetail)
        PokemonIsABaby()

        val colorObj = color.getColorByString(isSystemInDarkTheme())

        // Variations
        val varieties = pokemonDetail?.specie?.varieties?.filter { !it.is_default }
        if (!varieties.isNullOrEmpty()) {
            SectionTitle(
                title = stringResource(R.string.variations_and_forms),
                color = colorObj,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start = 12.dp)
            )
            PokemonVariations(navController, pokemonDetail, viewModel)
        }

        // Evolutions
        pokemonDetail?.evolution?.toEvoStages()?.takeIf { it.size > 1 }?.let {
            SectionTitle(
                title = stringResource(R.string.evolutions),
                color = colorObj,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start = 12.dp)
            )
            PokemonEvolution(navController, pokemonDetail, viewModel)
        }

        if (!isPremium) {
            AdAdvancedNative(
                adUnitIdProd = adUnitIdAdAdvancedNative,
                isTablet = false,
            )
            com.vinithius.dex10.ui.components.PremiumPromoBanner(
                onUpgradeClick = { viewModel.premiumManager.triggerUpsell() }
            )
        }

        // Tabs
        TabWithPagerExample(navController, pokemonDetail, color, viewModel)
    }
}


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainCardLargeScreen(
    navController: NavController?,
    pokemonId: Int,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    choiceOfTheDayStatus: Boolean,
    pokemonDetail: Pokemon?,
    color: String,
    painter: AsyncImagePainter? = null,
    columns: Int = 1,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.premiumManager.isPremium.collectAsState(initial = false)
    val adUnitIdAdAdvancedNative by viewModel.adUnitIdAdAdvancedNative.observeAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            ChoiceOfTheDay(choiceOfTheDayStatus)
            Card(
                modifier = Modifier
                    .height(320.dp)
                    .padding(8.dp),
                elevation = CardDefaults.elevatedCardElevation(5.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: Habitat and Pokémon Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Gives weight to expand proportionally
                    ) {
                        // Habitat Image
                        PokemonHabitat(viewModel, pokemonDetail)
                        // Pokemon Image
                        if (painter != null) {
                            Image(
                                painter = painter,
                                contentDescription = "Pokemon",
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 50.dp)
                                    .size(180.dp)
                                    .sharedElement(
                                        state = rememberSharedContentState(key = "$pokemonId"),
                                        animatedVisibilityScope = animatedVisibilityScope!!,
                                        boundsTransform = { _, _ ->
                                            tween(durationMillis = 1000)
                                        }
                                    ),
                            )
                        } else {
                            pokemonId.LoadGifWithCoil(viewModel)
                        }

                        // Zenith Cry Button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            ZenithCryButton(
                                viewModel = viewModel,
                                color = color.getColorByString(isSystemInDarkTheme())
                            )
                        }

                        // weight and height
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    StateRequest(
                                        viewModel = viewModel,
                                        loading = { HeightLoadingComposable() },
                                        success = { HeightSuccessComposable(pokemonDetail) },
                                        error = {
                                            // Do nothing yet
                                        }
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    StateRequest(
                                        viewModel = viewModel,
                                        loading = { WeightLoadingComposable() },
                                        success = { WeightSuccessComposable(pokemonDetail) },
                                        error = {
                                            // Do nothing yet
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.size(5.dp))
                            // Generation and Base Experience
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        DefaultFirstCardData(
                                            viewModel = viewModel,
                                            title = stringResource(R.string.generation),
                                            value = pokemonDetail?.specie?.generation?.name
                                                ?.split("-")
                                                ?.last()
                                                ?.uppercase()
                                        )
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        DefaultFirstCardData(
                                            viewModel = viewModel,
                                            title = stringResource(R.string.base_exp),
                                            value = pokemonDetail?.base_experience?.toString()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.size(5.dp))
                            // Shape and Base Capture rate
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        DefaultFirstCardData(
                                            viewModel = viewModel,
                                            title = stringResource(R.string.shape),
                                            value = pokemonDetail?.specie?.shape?.name?.getStringShape(
                                                context
                                            ) ?: stringResource(R.string.three_dots)
                                        )
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        DefaultFirstCardData(
                                            viewModel = viewModel,
                                            title = stringResource(R.string.capture_rate),
                                            value = pokemonDetail?.specie?.capture_rate?.toString()
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                TypeListResponse(pokemonDetail?.types ?: listOf())
                            }
                        }
                    }
                }
            }
        }
        item {
            PokemonChart(viewModel, color, pokemonDetail)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            PokemonIsABaby()
        }
        // Secondary items (grid cells)
        item(span = { GridItemSpan(maxLineSpan) }) {
            PokemonArts(viewModel, pokemonDetail)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            val appPreferencesLarge: AppPreferences = get()
            val tcgFavoritesLarge by appPreferencesLarge.tcgFavorites.collectAsState()
            PokemonTcgSection(
                pokemonDetail = pokemonDetail,
                color = color.getColorByString(isSystemInDarkTheme()),
                isPremium = isPremium,
                tcgFavorites = tcgFavoritesLarge,
                onToggleFavorite = { appPreferencesLarge.toggleTcgFavorite(it) },
                onPremiumRequired = { viewModel.premiumManager.triggerUpsell() }
            )
        }
        // Variations
        val varieties = pokemonDetail?.specie?.varieties?.filter { !it.is_default }
        if (!varieties.isNullOrEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SectionTitle(
                        title = stringResource(R.string.variations_and_forms),
                        color = color.getColorByString(isSystemInDarkTheme())
                    )
                    PokemonVariations(navController, pokemonDetail, viewModel)
                }
            }
        }

        // Evolutions
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                SectionTitle(
                    title = stringResource(R.string.evolutions),
                    color = color.getColorByString(isSystemInDarkTheme())
                )
                PokemonEvolution(navController, pokemonDetail, viewModel)
            }
        }

        // Ads
        if (!isPremium) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AdAdvancedNative(
                    adUnitIdProd = adUnitIdAdAdvancedNative,
                    isTablet = true,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                com.vinithius.dex10.ui.components.PremiumPromoBanner(
                    onUpgradeClick = { viewModel.premiumManager.triggerUpsell() }
                )
            }
        }
        // Tabs full-width
        item(span = { GridItemSpan(maxLineSpan) }) {
            TabWithPagerExample(navController, pokemonDetail, color, viewModel)
        }
    }
}

@Composable
fun TabWithPagerExample(
    navController: NavController?,
    pokemonDetail: Pokemon?,
    color: String,
    viewModel: PokemonViewModel = rememberPokemonViewModel(),
) {
    val hasAnimeDubber = !pokemonDetail?.animeInfo?.voiceActorName.isNullOrBlank()
    val tabItems = buildList {
        add(TabItem(stringResource(R.string.damage), TabPage.DAMAGE))
        add(TabItem(stringResource(R.string.encounters), TabPage.ENCOUNTERS))
        if (hasAnimeDubber) {
            add(TabItem(stringResource(R.string.anime_info), TabPage.ANIME))
        }
        add(TabItem(stringResource(R.string.eggs), TabPage.EGGS))
        add(TabItem(stringResource(R.string.abilities), TabPage.ABILITIES))
        add(TabItem(stringResource(R.string.entries), TabPage.ENTRIES))
        add(TabItem(stringResource(R.string.moves), TabPage.MOVES))
    }
    val pagerState = rememberPagerState(pageCount = { tabItems.size })
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(tabItems.size) { index ->
                val isSelected = pagerState.currentPage == index
                val color = getButtonColor(isSelected, color)

                StateRequest(
                    viewModel = viewModel,
                    loading = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(color.first)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .shimmer()
                        ) {
                            Text(
                                text = tabItems[index].title,
                                color = color.second,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    success = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(color.first)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tabItems[index].title,
                                color = color.second,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    error = { /* Do nothing yet */ }
                )
            }
        }
        val activity = getActivity()
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                when (tabItems[page].page) {
                    TabPage.DAMAGE -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        PokemonDamage(pokemonDetail, viewModel)
                    }

                    TabPage.ENCOUNTERS -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        PokemonEncounters(pokemonDetail, color, viewModel)
                    }

                    TabPage.ANIME -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        AnimeInfoSection(pokemonDetail, color.getColorByString(isSystemInDarkTheme()))
                    }

                    TabPage.EGGS -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        PokemonEggs(pokemonDetail, color, viewModel)
                    }

                    TabPage.ABILITIES -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        PokemonAbilities(pokemonDetail, color, viewModel)
                    }

                    TabPage.ENTRIES -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        PokemonEntries(navController, pokemonDetail, viewModel)
                    }

                    TabPage.MOVES -> {
                        activity?.trackButtonClick(tabItems[page].title)
                        PokemonMoves(color, viewModel)
                    }
                }
            }
        }
    }
}

private data class TabItem(val title: String, val page: TabPage)

private enum class TabPage {
    DAMAGE,
    ENCOUNTERS,
    ANIME,
    EGGS,
    ABILITIES,
    ENTRIES,
    MOVES,
}

private const val TCG_FREE_LIMIT = 3

@Composable
fun PokemonTcgSection(
    pokemonDetail: Pokemon?,
    color: Color,
    isPremium: Boolean,
    tcgFavorites: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onPremiumRequired: () -> Unit,
) {
    val cards = pokemonDetail?.tcgCards
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedTitle by remember { mutableStateOf("") }

    val visibleCards = when {
        cards == null -> null
        isPremium -> cards
        else -> cards.take(TCG_FREE_LIMIT)
    }
    val lockedCount = if (!isPremium && cards != null) (cards.size - TCG_FREE_LIMIT).coerceAtLeast(0) else 0

    // Favourites first, keeping relative order within each group
    val sortedCards = remember(visibleCards, tcgFavorites) {
        visibleCards?.sortedByDescending { it.id in tcgFavorites }
    }

    // Carousel scroll state — used to reveal a newly pinned favourite at the front
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        SectionTitle(
            title = stringResource(R.string.tcg_cards),
            color = color,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        if (visibleCards == null) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(210.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer()
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
        } else if (!sortedCards.isNullOrEmpty() || lockedCount > 0) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedCards ?: emptyList(), key = { it.id }) { card ->
                    var isVisible by remember { mutableStateOf(true) }

                    if (isVisible) {
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .height(210.dp)
                                .animateItem()
                                .clickable {
                                    selectedImageUrl = card.getLargeImage()
                                    selectedTitle = card.name
                                },
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(data = card.getSmallImage())
                                        .crossfade(true)
                                        .build()
                                )

                                if (painter.state is AsyncImagePainter.State.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(32.dp),
                                        color = color
                                    )
                                }

                                if (painter.state is AsyncImagePainter.State.Error) {
                                    isVisible = false
                                }

                                Image(
                                    painter = painter,
                                    contentDescription = card.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (isPremium) {
                                    TcgFavoriteStar(
                                        isFavorite = card.id in tcgFavorites,
                                        onToggle = {
                                            val wasFavorite = card.id in tcgFavorites
                                            onToggleFavorite(card.id)
                                            // When pinning a new favourite, slide the carousel
                                            // back to the front so it becomes visible.
                                            if (!wasFavorite) {
                                                scope.launch { listState.animateScrollToItem(0) }
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                }

                if (lockedCount > 0) {
                    item {
                        TcgLockedCard(
                            lockedCount = lockedCount,
                            color = color,
                            onClick = onPremiumRequired
                        )
                    }
                }
            }
        } else {
            Text(
                "No cards found",
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

    selectedImageUrl?.let { url ->
        ZoomableImageDialog(imageUrl = url, title = selectedTitle, onDismiss = { selectedImageUrl = null })
    }
}

@Composable
private fun TcgFavoriteStar(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isFavorite) {
        scale.animateTo(1.5f, animationSpec = tween(110))
        scale.animateTo(1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ))
    }

    Box(
        modifier = modifier
            .padding(5.dp)
            .size(26.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                onClick = onToggle,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = if (isFavorite) Color(0xFFFFD700) else Color.White.copy(alpha = 0.45f),
            modifier = Modifier
                .size(15.dp)
                .scale(scale.value)
        )
    }
}

@Composable
private fun TcgLockedCard(lockedCount: Int, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.85f),
                            color.copy(alpha = 0.55f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "+$lockedCount",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.tcg_unlock_label),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Premium",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableImageDialog(imageUrl: String, title: String, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        val extraWidth = (scale - 1) * size.width
                        val extraHeight = (scale - 1) * size.height
                        val maxX = extraWidth / 2
                        val maxY = extraHeight / 2
                        offset = Offset(
                            x = (offset.x + pan.x * scale).coerceIn(-maxX, maxX),
                            y = (offset.y + pan.y * scale).coerceIn(-maxY, maxY)
                        )
                    }
                }
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = imageUrl)
                    .crossfade(true)
                    .build()
            )

            if (painter.state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    color = Color.White
                )
            }

            Image(
                painter = painter,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun AnimeInfoSection(pokemonDetail: Pokemon?, color: Color) {
    val isLoaded = pokemonDetail != null
    val animeInfo = pokemonDetail?.animeInfo
    val hasDubber = !animeInfo?.voiceActorName.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoaded && !hasDubber) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.voice_actor),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (animeInfo == null) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer()
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            } else {
                Card(
                    modifier = Modifier.size(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(animeInfo.voiceActorImageUrl),
                        contentDescription = "Voice Actor",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if (animeInfo == null) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(200.dp)
                    .height(20.dp)
                    .shimmer()
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        } else {
            Text(
                text = animeInfo.voiceActorName ?: stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun getButtonColor(isSelected: Boolean, pokemonColor: String): Pair<Color, Color> {
    val result = Pair(
        if (isSelected) pokemonColor.getColorByString(isSystemInDarkTheme()) else MaterialTheme.colorScheme.secondary,
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
    )
    return result
}


@SuppressLint("DefaultLocale")
private fun convertWeightHeight(
    value: Int?,
    resource: Int,
    context: Context,
    maskKl: String = "%.1f",
    maskLbs: String = "%.1f",
): String {
    val resultKl = String.format(maskKl, value?.converterIntToDouble())
    val resultLbs = String.format(maskLbs, value?.convertPounds())
    return context.getString(resource, resultKl, resultLbs)
}

@Composable
private fun DefaultSuccessComposable(title: String, value: String?) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        )
    )
    Text(
        text = value ?: stringResource(R.string.three_dots),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        )
    )
}

@Composable
private fun PokemonHabitat(
    viewModel: PokemonViewModel,
    pokemonDetail: Pokemon?,
) {
    StateRequest(
        viewModel = viewModel,
        loading = { PokemonHabitatLoadingComposable() },
        success = { PokemonHabitatSuccessComposable(pokemonDetail) },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonHabitatSuccessComposable(pokemonDetail: Pokemon?) {
    val context = LocalContext.current
    val habitatImg =
        pokemonDetail?.specie?.habitat?.name?.getDrawableHabitat() ?: R.drawable.unknow_habitat
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        habitatImg.run {
            Image(
                painter = painterResource(id = this),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = pokemonDetail?.name?.capitalize() ?: String(),
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .align(Alignment.TopStart),
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
                    blurRadius = 5f
                )
            ),
        )
        val habitat = pokemonDetail?.specie?.habitat?.name?.capitalize() ?: "?"
        Text(
            text = habitat.getStringHabitat(context),
            modifier = Modifier
                .padding(end = 12.dp, bottom = 12.dp)
                .align(Alignment.BottomEnd),
            color = Color.White,
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
            ),
        )
    }
}

@Composable
private fun PokemonHabitatLoadingComposable() {
    Box(
        modifier = Modifier
            .shimmer()
            .background(Color.Gray)
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.loading),
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .align(Alignment.TopStart),
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
                    blurRadius = 5f
                )
            ),
        )
        Text(
            text = stringResource(R.string.loading_three_dots),
            modifier = Modifier
                .padding(end = 12.dp, bottom = 12.dp)
                .align(Alignment.BottomEnd),
            color = Color.White,
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
            ),
        )
    }
}

// Height

@Composable
private fun HeightSuccessComposable(pokemonDetail: Pokemon?) {
    val context = LocalContext.current
    Image(
        painter = painterResource(id = R.drawable.height),
        contentDescription = "height",
        modifier = Modifier.size(20.dp),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.text)
    )
    Spacer(modifier = Modifier.size(2.dp))
    Text(
        text = convertWeightHeight(
            pokemonDetail?.weight,
            R.string.kg_lbs,
            context
        ),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        )
    )
}

@Composable
private fun HeightLoadingComposable() {
    Image(
        painter = painterResource(id = R.drawable.height),
        contentDescription = "height",
        modifier = Modifier
            .size(20.dp)
            .shimmer(),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.text)
    )
    Spacer(modifier = Modifier.size(2.dp))
    Text(
        text = stringResource(R.string.kg_lbs_loading),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        ),
        modifier = Modifier.shimmer()
    )
}

// Weight

@Composable
private fun WeightSuccessComposable(pokemonDetail: Pokemon?) {
    val context = LocalContext.current
    Image(
        painter = painterResource(id = R.drawable.weight),
        contentDescription = "height",
        modifier = Modifier.size(20.dp),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.text)
    )
    Spacer(modifier = Modifier.size(2.dp))
    Text(
        text = convertWeightHeight(
            pokemonDetail?.height,
            R.string.m_inch,
            context,
            "%.1f",
            "%.2f"
        ),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        )
    )
}

@Composable
private fun WeightLoadingComposable() {
    Image(
        painter = painterResource(id = R.drawable.weight),
        contentDescription = "weight",
        modifier = Modifier
            .size(20.dp)
            .shimmer(),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.text)
    )
    Spacer(modifier = Modifier.size(2.dp))
    Text(
        text = stringResource(R.string.m_inch_loading),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
        ),
        modifier = Modifier.shimmer()
    )
}

// First Card

@Composable
private fun DefaultFirstCardData(
    viewModel: PokemonViewModel,
    title: String,
    value: String?
) {
    StateRequest(
        viewModel = viewModel,
        loading = { DefaultLoadingComposable(title) },
        success = { DefaultSuccessComposable(title, value) },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun ChoiceOfTheDay(
    choiceOfTheDayStatus: Boolean
) {
    AnimatedVisibility(
        visible = choiceOfTheDayStatus,
        enter = slideInHorizontally() + fadeIn(),
        exit = slideOutHorizontally() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pokeball_01),
                    contentDescription = "is baby",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = stringResource(R.string.choice_of_the_day),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
                Image(
                    painter = painterResource(id = R.drawable.pokeball_01),
                    contentDescription = "is baby",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun PokemonArts(
    viewModel: PokemonViewModel,
    pokemonDetail: Pokemon?,
) {
    val color = viewModel.getPokemonColor()?.getColorByString(isSystemInDarkTheme()) ?: Color.Black
    Column {
        SectionTitle(
            title = stringResource(R.string.all_images),
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp, start = 12.dp)
        )
    var dataBottomSheet: SpriteItem? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val activity = getActivity()
    var showBottomSheet by remember { mutableStateOf(false) }
    StateRequest(
        viewModel = viewModel,
        loading = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(6.dp)
                    .shimmer(),
            ) {
                val itemsLoading = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)
                itemsLoading.let { sprites ->
                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        itemsIndexed(sprites) { _, _ ->
                            Card(
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(4.dp),
                                onClick = {
                                    // Do nothing
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .shimmer()
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        success = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(6.dp),
            ) {
                pokemonDetail?.getSpriteItems(context)?.let { sprites ->
                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        itemsIndexed(sprites) { _, data ->
                            Card(
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(4.dp),
                                onClick = {
                                    showBottomSheet = showBottomSheet.not()
                                    dataBottomSheet = data
                                    activity?.trackButtonClick("Art: ${data.title}")
                                }
                            ) {
                                data.LoadGifWithCoilToSprite(context, false)
                            }
                        }
                    }
                }
            }
        },
        error = { /* Do nothing yet */ }
    )

    if (showBottomSheet) {
        dataBottomSheet?.let { data ->
            ZoomableImageDialog(
                imageUrl = data.url,
                title = data.title,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
    }
}

@Composable
private fun PokemonChart(
    viewModel: PokemonViewModel,
    color: String?,
    pokemonDetail: Pokemon?,
) {
    Card(
        modifier = Modifier
            .height(300.dp)
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        StateRequest(
            viewModel = viewModel,
            loading = { ChartLoadingComposable() },
            success = { ChartSuccessComposable(pokemonDetail, color) },
            error = { /* Do nothing yet */ }
        )
    }
}

private fun getStatsLabels(pokemonDetail: Pokemon?, context: Context): List<String> {
    return pokemonDetail?.stats?.map { stat ->
        "${stat.stat.name?.getStringStat(context)} (${stat.base_stat})"
    } ?: listOf()
}

private fun getStats(
    pokemonDetail: Pokemon?,
    pokemonColor: String?,
    context: Context,
    isDark: Boolean,
): List<Bars> {
    if (pokemonDetail != null) {
        val labels = getStatsLabels(pokemonDetail, context)
        return pokemonDetail.stats?.mapIndexed { index, stat ->
            Bars(
                label = labels[index],
                values = listOf(
                    Bars.Data(
                        label = stat.stat.name?.uppercase(),
                        value = stat.base_stat.toDouble(),
                        color = SolidColor(pokemonColor?.getColorByString(isDark) ?: Color.Black)
                    ),
                ),
            )
        } ?: listOf()
    } else {
        return listOf()
    }
}

@Composable
private fun ChartSuccessComposable(
    pokemonDetail: Pokemon?,
    color: String?
) {
    // 1) Early return para não desenhar o chart até teres cor
    if (pokemonDetail == null || color == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val stats: List<Bars> = remember(color, pokemonDetail) {
        getStats(pokemonDetail, color, context, isDark)
    }

    RowChart(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        data = stats,
        barProperties = BarProperties(
            cornerRadius = Bars.Data.Radius.Rectangle(
                topRight = 3.dp,
                topLeft = 3.dp,
                bottomRight = 3.dp,
                bottomLeft = 3.dp
            ),
            spacing = 3.dp,
        ),
        labelProperties = LabelProperties(
            enabled = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            ),
            padding = 12.dp,
            labels = getStatsLabels(pokemonDetail, context),
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        labelHelperProperties = LabelHelperProperties(
            enabled = false,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            ),
        ),
    )
}


@Composable
private fun ChartLoadingComposable() {
    val barList = mutableListOf<Bars>()
    while (barList.size <= 6) {
        barList.add(
            Bars(
                label = stringResource(R.string.loading_three_dots),
                values = listOf(
                    Bars.Data(
                        label = stringResource(R.string.loading_three_dots),
                        value = 0.0,
                        color = SolidColor(Color.Red)
                    ),
                ),
            )
        )
    }
    Box(
        modifier = Modifier
            .shimmer()
            .fillMaxWidth()
    ) {
        RowChart(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            data = remember { barList },
            barProperties = BarProperties(
                cornerRadius = Bars.Data.Radius.Rectangle(
                    topRight = 3.dp,
                    topLeft = 3.dp,
                    bottomRight = 3.dp,
                    bottomLeft = 3.dp
                ),
                spacing = 3.dp,
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.labelSmall,
                padding = 12.dp,
                labels = listOf(
                    stringResource(R.string.loading_three_dots),
                    stringResource(R.string.loading_three_dots),
                    stringResource(R.string.loading_three_dots),
                    stringResource(R.string.loading_three_dots),
                    stringResource(R.string.loading_three_dots),
                    stringResource(R.string.loading_three_dots),
                )
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = false
            )
        )
    }
}

@Composable
private fun PokemonIsABaby(
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val pokemonDetail by viewModel.pokemonDetail.observeAsState()
    AnimatedVisibility(
        visible = pokemonDetail?.specie?.is_baby ?: false,
        enter = slideInHorizontally() + fadeIn(),
        exit = slideOutHorizontally() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pokemon_baby_egg),
                    contentDescription = "is baby",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = stringResource(R.string.is_a_baby),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    )
                )
                Image(
                    painter = painterResource(id = R.drawable.pokemon_baby_egg),
                    contentDescription = "is baby",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PokemonIsABabyPreview() {
    PokemonIsABaby()
}

@Composable
private fun PokemonEvolution(
    navController: NavController?,
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val activity = getActivity()
    StateRequest(
        viewModel = viewModel,
        loading = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(6.dp),
            ) {
                val listShimmer = listOf(1, 2, 3)
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(listShimmer) { index, _ ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .shimmer(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .shimmer()
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(30.dp)
                                )
                            }
                        }
                        val arrowVisible = listShimmer.size == index + 1
                        if (arrowVisible.not()) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_ios_24),
                                contentDescription = "Arrow right",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier
                                    .size(25.dp)
                                    .shimmer()
                            )
                        }
                    }
                }
            }
        },
        success = {
            val pokemonId = pokemonDetail?.id ?: 0
            val color = viewModel.getPokemonColor()?.getColorByString(isSystemInDarkTheme())
                ?: Color.Black
            val stages = pokemonDetail?.evolution?.toEvoStages()
            val displayStages = stages?.let { viewModel.getEvoDisplayStages(it) }

            if (!displayStages.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    displayStages.forEachIndexed { stageIndex, stage ->
                        if (stageIndex > 0) {
                            val allSameTrigger = stage.map { it.triggerText }.toSet().size <= 1
                            val sharedTrigger = if (allSameTrigger) stage.firstOrNull()?.triggerText else null
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (sharedTrigger != null) {
                                    Text(
                                        text = sharedTrigger,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                        val showIndividualTriggers = stageIndex > 0 &&
                            stage.map { it.triggerText }.toSet().size > 1
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            items(stage) { entry ->
                                EvoCard(
                                    entry = entry,
                                    pokemonId = pokemonId,
                                    accentColor = color,
                                    showTrigger = showIndividualTriggers,
                                    navController = navController,
                                    viewModel = viewModel,
                                    activity = activity
                                )
                            }
                        }
                    }
                }
            }
        },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonVariations(
    navController: NavController?,
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val activity = getActivity()

    StateRequest(
        viewModel = viewModel,

        loading = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(6.dp),
            ) {
                val listShimmer = listOf(1, 2, 3)

                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(listShimmer) { _, _ ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .shimmer(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .shimmer(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }
            }
        },

        success = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(6.dp),
            ) {
                val currentPokemonId = pokemonDetail?.id ?: 0
                val color = viewModel.getPokemonColor()
                    ?.getColorByString(isSystemInDarkTheme())
                    ?: Color.Black

                val varieties: List<Pair<Int, String>> =
                    pokemonDetail?.specie?.varieties
                        ?.filter { it.is_default == false }
                        ?.mapNotNull { variety ->
                            val id = variety.pokemon.url?.getIdIntoUrl()?.toIntOrNull()
                            val name = variety.pokemon.name
                            if (id != null && id > 0 && !name.isNullOrBlank()) id to name else null
                        }
                        ?: emptyList()

                if (varieties.isEmpty()) return@Column

                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(varieties) { _, data ->
                        val varietyId = data.first
                        val varietyName = data.second

                        Card(
                            modifier = if (currentPokemonId == varietyId) {
                                Modifier
                                    .padding(8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = color,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            } else {
                                Modifier.padding(8.dp)
                            },
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                            onClick = {
                                if (currentPokemonId != varietyId) {
                                    viewModel.setIdPokemon(varietyId)
                                    navController?.navigate("pokemonDetail/$varietyId")
                                }
                                activity?.trackButtonClick(
                                    "Variation: ${varietyName.replaceFirstChar { it.uppercase() }}"
                                )
                            }
                        ) {
                            val imageUrl =
                                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$varietyId.png"

                            Box(
                                modifier = Modifier.size(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = varietyName,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                }
            }
        },

        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun GenericBox(
    isShimmer: Boolean = false,
    callComponent: @Composable () -> Unit
) {
    val modifier = if (isShimmer) {
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shimmer()
    } else {
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            callComponent.invoke()
        }
    }
}

@Composable
private fun PokemonDamage(
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    StateRequest(
        viewModel = viewModel,
        loading = {
            GenericBox(true) {
                Spacer(modifier = Modifier.size(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TypeItemShimmer()
                }
                Spacer(modifier = Modifier.size(6.dp))
                DefaultDamageFromToShimmer()
                DefaultDamageFromToShimmer()
                DefaultDamageFromToShimmer()
            }
        },
        success = {
            if (pokemonDetail?.damage?.isNotEmpty() == true) {

                Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) { // Use uma Column para organizar os itens
                    pokemonDetail.damage?.forEach { damageItem ->
                        GenericBox {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TypeItem(damageItem.type?.name ?: String())
                            }
                            Spacer(modifier = Modifier.size(6.dp))
                            DefaultDamageFromTo(
                                stringResource(R.string.no_damage),
                                damageItem.damage_relations.no_damage_to.fromDefaultToListType(),
                                damageItem.damage_relations.no_damage_from.fromDefaultToListType()
                            )
                            DefaultDamageFromTo(
                                stringResource(R.string.effective_damage),
                                damageItem.damage_relations.effective_damage_to?.fromDefaultToListType()
                                    ?: listOf(),
                                damageItem.damage_relations.effective_damage_from.fromDefaultToListType()
                            )
                            DefaultDamageFromTo(
                                stringResource(R.string.ineffective_damage),
                                damageItem.damage_relations.ineffective_damage_to.fromDefaultToListType(),
                                damageItem.damage_relations.ineffective_damage_from.fromDefaultToListType()
                            )
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                    }
                }


            } else {
                Text(
                    text = stringResource(R.string.no_data),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonEncounters(
    pokemonDetail: Pokemon?,
    color: String?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    StateRequest(
        viewModel = viewModel,
        loading = {
            GenericBox(true) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color?.getColorByString(isSystemInDarkTheme()) ?: Color.Black
                            )
                            .padding(8.dp)
                    )
                }
            }
        },
        success = {
            GenericBox {
                if (pokemonDetail?.encounters?.isNotEmpty() == true) {
                    pokemonDetail.encounters?.forEach {

                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = it.location_area.name?.replace("-", " ")?.capitalize()
                                    ?: "?",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White
                                ),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        color?.getColorByString(isSystemInDarkTheme())
                                            ?: Color.Black
                                    )
                                    .padding(8.dp)
                            )
                        }

                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_data),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonEggs(
    pokemonDetail: Pokemon?,
    color: String?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val context = LocalContext.current
    StateRequest(
        viewModel = viewModel,
        loading = {
            GenericBox(true) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color?.getColorByString(isSystemInDarkTheme()) ?: Color.Black
                            )
                            .padding(8.dp)
                    )
                }
            }
        },
        success = {
            GenericBox {
                if (pokemonDetail?.specie?.egg_groups?.isNotEmpty() == true) {

                    pokemonDetail.specie?.let { specie ->
                        specie.egg_groups?.forEach {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = it.name?.getStringEggGroup(context) ?: "?",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            color?.getColorByString(isSystemInDarkTheme())
                                                ?: Color.Black
                                        )
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_data),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonAbilities(
    pokemonDetail: Pokemon?,
    color: String?,
    viewModel: PokemonViewModel = rememberPokemonViewModel()
) {
    val context = LocalContext.current
    StateRequest(
        viewModel = viewModel,
        loading = {
            GenericBox(true) {
                Text(
                    text = stringResource(R.string.about_abilities_hidden),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(5.dp))
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color?.getColorByString(isSystemInDarkTheme()) ?: Color.Black
                            )
                            .padding(8.dp)
                    )
                }
            }
        },
        success = {
            if (pokemonDetail?.abilities?.isNotEmpty() == true) {
                GenericBox {
                    Text(
                        text = stringResource(R.string.about_abilities_hidden),
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.size(5.dp))

                    pokemonDetail.abilities?.forEach { abilityItem ->

                        val originalName = abilityItem.ability.name?.capitalize() ?: "?"
                        val translatedText = remember { mutableStateOf<String?>(null) }
                        val hidden = stringResource(R.string.hidden)

                        LaunchedEffect(originalName) {
                            originalName.translateIfSupported(
                                onResult = { result ->
                                    translatedText.value = if (abilityItem.is_hidden) {
                                        "$result - $hidden"
                                    } else {
                                        result
                                    }
                                },
                                onError = {
                                    translatedText.value = originalName // fallback
                                },
                                context = context
                            )
                        }

                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = translatedText.value
                                    ?: stringResource(R.string.loading_translate),
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        color?.getColorByString(isSystemInDarkTheme())
                                            ?: Color.Black
                                    )
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_data),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonEntries(
    navController: NavController?,
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = rememberPokemonViewModel(),
) {
    val loading = stringResource(R.string.loading_translate)
    var encounterText by remember { mutableStateOf(loading) }
    StateRequest(
        viewModel = viewModel,
        loading = {
            GenericBox(true) {
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        success = {
            GenericBox {
                pokemonDetail?.specie?.let { specie ->
                    val color = viewModel.getPokemonColor()?.getColorByString(isSystemInDarkTheme())
                        ?: Color.Black

                    specie.flavor_text_entries?.let { flavorTextEntries ->
                        flavorTextEntries.getFlavorTextForLanguage("en")?.run {
                            translateIfSupported(
                                onResult = { translatedText ->
                                    encounterText = translatedText
                                },
                                onError = { exception ->
                                    encounterText = this
                                },
                                context = LocalContext.current
                            )
                        }
                        HtmlText(text = encounterText)
                    }
                } ?: run {
                    Text(
                        text = stringResource(R.string.no_data),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        },
        error = { /* Do nothing yet */ }
    )
}

/**
 * Adapts the HTML text for use in Compose.
 */
fun Spanned.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(this@toAnnotatedString.toString())
    }
}

@Composable
fun HtmlText(text: String) {
    val spanned = text.getHtmlCompat()
    Text(
        text = spanned.toAnnotatedString(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(8.dp),
        softWrap = true
    )
}

@Composable
private fun DefaultDamageFromTo(
    title: String,
    damageTo: List<Type>,
    damageFrom: List<Type>,
) {
    if (damageFrom.isNotEmpty() || damageTo.isNotEmpty()) {
        Column {
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            damageFrom.takeIf { it.isNotEmpty() }?.let {
                Spacer(modifier = Modifier.size(6.dp))
                Row {
                    Text(stringResource(R.string.from))
                    Spacer(modifier = Modifier.size(2.dp))
                    TypeListResponse(damageFrom)
                }
            }
            damageTo.takeIf { it.isNotEmpty() }?.let {
                Spacer(modifier = Modifier.size(6.dp))
                Row {
                    Text(stringResource(R.string.to))
                    Spacer(modifier = Modifier.size(2.dp))
                    TypeListResponse(damageTo)
                }
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
    }
}

@Composable
private fun DefaultDamageFromToShimmer() {
    Column {
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = stringResource(R.string.three_dots),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.shimmer()
        )
        Spacer(modifier = Modifier.size(6.dp))
        Row {
            Text(stringResource(R.string.from))
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
        }
        Spacer(modifier = Modifier.size(6.dp))
        Row {
            Text(stringResource(R.string.to))
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
            Spacer(modifier = Modifier.size(2.dp))
            TypeItemShimmer()
        }
    }
    Spacer(modifier = Modifier.size(6.dp))
}

@Preview
@Composable
private fun PokemonHabitatPreview(pokemonDetail: Pokemon?) {
    PokemonDamage(pokemonDetail)
}

@Composable
private fun EvoCard(
    entry: EvoDisplayEntry,
    pokemonId: Int,
    accentColor: Color,
    showTrigger: Boolean,
    navController: NavController?,
    viewModel: PokemonViewModel,
    activity: MainActivity?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        if (showTrigger) {
            Text(
                text = entry.triggerText ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .heightIn(min = 32.dp),
                maxLines = 3
            )
        }
        Card(
            modifier = if (pokemonId == entry.id) {
                Modifier.border(2.dp, accentColor, RoundedCornerShape(8.dp))
            } else Modifier,
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.elevatedCardElevation(4.dp),
            onClick = {
                if (pokemonId != entry.id) {
                    viewModel.setIdPokemon(entry.id)
                    navController?.navigate("pokemonDetail/${entry.id}")
                }
                activity?.trackButtonClick("Evolution: ${entry.name.capitalize()}")
            }
        ) {
            LoadGifWithCoilToEvolution(entry.id to entry.name)
        }
        Text(
            text = entry.name.capitalize(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            maxLines = 2
        )
    }
}

@Composable
fun LoadGifWithCoilToEvolution(
    pokemonEvolution: Pair<Int, String>,
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
        .data("$URL_IMAGE/${pokemonEvolution.first}.png")
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
            contentDescription = pokemonEvolution.second,
            modifier = Modifier
                .size(70.dp)
        )
    }
}

// MOCKUP ////////////////////////////////////////////////////////////////////////////////////////

@Composable
private fun SectionTitle(
    title: String,
    color: Color,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 8.dp)
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

private fun getMockupPokemon(): Pokemon {
    return Pokemon(
        id = 1,
        name = "Teste",
        url = null,
        color = null,
        habitat = null,
        height = null,
        weight = null,
        base_experience = null,
        stats = null,
        types = null,
        abilities = null,
        sprites = null,
        encounters = null,
        evolution = null,
        characteristic = null,
        specie = null,
        damage = listOf(),
        favorite = false,
    )
}

@Composable
fun ZenithCryButton(viewModel: PokemonViewModel, color: Color) {
    val cryUrl by viewModel.cryUrl.observeAsState()

    IconButton(
        onClick = {
            cryUrl?.let { url ->
                try {
                    MediaPlayer().apply {
                        setDataSource(url)
                        prepareAsync()
                        setOnPreparedListener { start() }
                        setOnCompletionListener { release() }
                        setOnErrorListener { _, _, _ ->
                            release()
                            false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        modifier = Modifier
            .size(48.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Play Cry",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun PokemonMoves(color: String, viewModel: PokemonViewModel) {
    val moves by viewModel.pokemonMoves.observeAsState()
    val isDark = isSystemInDarkTheme()
    val pokemonColor = color.getColorByString(isDark)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (moves.isNullOrEmpty()) {
            Text(
                text = "No moves found",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            moves?.forEach { moveItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = pokemonColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = moveItem.move?.name?.capitalize() ?: "Unknown",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = pokemonColor
                            )
                            val method =
                                moveItem.version_group_details?.firstOrNull()?.move_learn_method?.name
                                    ?: "Unknown"
                            Text(
                                text = "Method: ${method.capitalize()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        val level =
                            moveItem.version_group_details?.firstOrNull()?.level_learned_at ?: 0
                        if (level > 0) {
                            Text(
                                text = "Lvl $level",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}