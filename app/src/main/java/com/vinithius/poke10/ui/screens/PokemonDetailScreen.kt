package com.vinithius.poke10.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Spanned
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
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
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer
import com.vinithius.poke10.R
import com.vinithius.poke10.components.TypeItem
import com.vinithius.poke10.components.TypeItemShimmer
import com.vinithius.poke10.components.TypeListResponse
import com.vinithius.poke10.datasource.mapper.fromDefaultToListType
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.datasource.response.Type
import com.vinithius.poke10.extension.LoadGifWithCoilToSprite
import com.vinithius.poke10.extension.SpriteItem
import com.vinithius.poke10.extension.capitalize
import com.vinithius.poke10.extension.convertPounds
import com.vinithius.poke10.extension.converterIntToDouble
import com.vinithius.poke10.extension.getColorByString
import com.vinithius.poke10.extension.getDrawableHabitat
import com.vinithius.poke10.extension.getFlavorTextForLanguage
import com.vinithius.poke10.extension.getHtmlCompat
import com.vinithius.poke10.extension.getListEvolutions
import com.vinithius.poke10.extension.getSpriteItems
import com.vinithius.poke10.extension.getStringHabitat
import com.vinithius.poke10.extension.getStringStat
import com.vinithius.poke10.extension.translateIfSupported
import com.vinithius.poke10.ui.MainActivity
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import com.vinithius.poke10.ui.viewmodel.RequestStateDetail
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
    val context = LocalContext.current
    val activity = context as? MainActivity
    activity?.trackScreenView("Screen Detail: $pokemonName")
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
    pokemonName: String,
    pokemonColor: String,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    viewModel: PokemonViewModel = getViewModel()
) {
    SetAnalyticScreenName(pokemonName)
    LaunchedEffect(Unit) {
        viewModel.getPokemonDetail()
        viewModel.setPokemonColor(pokemonColor)
    }

    BackHandler {
        viewModel.setDetailsScreen(false)
        navController?.popBackStack()
    }
    val context = LocalContext.current
    // Observes
    val pokemonDetail by viewModel.pokemonDetail.observeAsState()
    val choiceOfTheDayStatus by viewModel.choiceOfTheDay.observeAsState(false)
    val painter = viewModel.getSharedImage(pokemonId.toString())
    val loadingShape = stringResource(R.string.three_dots)
    var shapeName by remember { mutableStateOf(loadingShape) }

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
                    PokemonHabitat(viewModel, pokemonDetail, pokemonName)
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
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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

                                pokemonDetail?.specie?.shape?.name?.let { data ->
                                    //shapeName = stringResource(R.string.three_dots)
                                    data.translateIfSupported(
                                        onResult = { translatedText ->
                                            shapeName = translatedText
                                        },
                                        onError = { exception ->
                                            shapeName = data
                                        }
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DefaultFirstCardData(
                                        viewModel = viewModel,
                                        title = stringResource(R.string.shape),
                                        value = shapeName.capitalize()
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
        PokemonChart(viewModel, pokemonDetail, pokemonColor)
        PokemonIsABaby()
        PokemonEvolution(navController, pokemonDetail)
        // Tabs
        TabWithPagerExample(pokemonDetail, viewModel, pokemonColor, context)
    }
}

@Composable
fun TabWithPagerExample(
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = getViewModel(),
    pokemonColor: String,
    context: Context,
) {
    val tabTitles = listOf(
        stringResource(R.string.damage),
        stringResource(R.string.encounters),
        stringResource(R.string.eggs),
        stringResource(R.string.abilities),
        stringResource(R.string.entries)
    )
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(tabTitles.size) { index ->
            val isSelected = pagerState.currentPage == index
            val color = getButtonColor(isSelected, pokemonColor)

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
                            text = tabTitles[index],
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
                            text = tabTitles[index],
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
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                0 -> {
                    activity?.trackButtonClick(tabTitles[0])
                    PokemonDamage(pokemonDetail, viewModel)
                }

                1 -> {
                    activity?.trackButtonClick(tabTitles[1])
                    PokemonEncounters(pokemonDetail, pokemonColor, viewModel)
                }

                2 -> {
                    activity?.trackButtonClick(tabTitles[2])
                    PokemonEggs(pokemonDetail, pokemonColor, viewModel)
                }

                3 -> {
                    activity?.trackButtonClick(tabTitles[3])
                    PokemonAbilities(pokemonDetail, pokemonColor, viewModel)
                }

                4 -> {
                    activity?.trackButtonClick(tabTitles[4])
                    PokemonEntries(pokemonDetail, context, viewModel)
                }
            }
        }
    }
}

@Composable
private fun getButtonColor(isSelected: Boolean, pokemonColor: String): Pair<Color, Color> {
    val result = Pair(
        if (isSelected) pokemonColor.getColorByString() else MaterialTheme.colorScheme.secondary,
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
    pokemonName: String
) {
    StateRequest(
        viewModel = viewModel,
        loading = { PokemonHabitatLoadingComposable(pokemonName) },
        success = { PokemonHabitatSuccessComposable(pokemonDetail, pokemonName) },
        error = { /* Do nothing yet */ }
    )
}

@Composable
private fun PokemonHabitatSuccessComposable(pokemonDetail: Pokemon?, pokemonName: String) {
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
            text = pokemonName.capitalize(),
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
private fun PokemonHabitatLoadingComposable(pokemonName: String) {
    Box(
        modifier = Modifier
            .shimmer()
            .background(Color.Gray)
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = pokemonName.capitalize(),
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
        modifier = Modifier.size(20.dp)
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
            .shimmer()
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
        modifier = Modifier.size(20.dp)
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
            .shimmer()
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
        Dialog(onDismissRequest = { showBottomSheet = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                dataBottomSheet?.let { data ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = data.title.capitalize(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                IconButton(
                                    onClick = {
                                        showBottomSheet = false
                                        activity?.trackButtonClick("Close dialog image detail")
                                    },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.close),
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        data.LoadGifWithCoilToSprite(context, true)
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonChart(
    viewModel: PokemonViewModel,
    pokemonDetail: Pokemon?,
    pokemonColor: String
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
            success = { ChartSuccessComposable(pokemonDetail, pokemonColor) },
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
    pokemonColor: String,
    context: Context
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
                        color = SolidColor(pokemonColor.getColorByString())
                    ),
                ),
            )
        } ?: listOf()
    } else {
        return listOf()
    }
}

@Composable
private fun ChartSuccessComposable(pokemonDetail: Pokemon?, pokemonColor: String) {
    val context = LocalContext.current
    val stats = getStats(pokemonDetail, pokemonColor, context)
    RowChart(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        data = remember { stats.toList() },
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
            labels = getStatsLabels(pokemonDetail, context),
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
    viewModel: PokemonViewModel = getViewModel()
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
private fun PokemonIsABabyPreview(
    viewModel: PokemonViewModel = getViewModel()
) {
    PokemonIsABaby()
}

@Composable
private fun PokemonEvolution(
    navController: NavController?,
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = getViewModel()
) {
    val context = LocalContext.current
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
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(6.dp),
            ) {
                val pokemonId = pokemonDetail?.id ?: 0
                val color = viewModel.getPokemonColor()?.getColorByString() ?: Color.Black
                val evolutions = pokemonDetail?.evolution?.getListEvolutions()
                evolutions?.let { evolutionsItem ->
                    viewModel.getIdByNames(evolutionsItem)?.run {
                        LazyRow(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            itemsIndexed(this@run) { index, data ->
                                Card(
                                    modifier = if (pokemonId == data.first) {
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
                                        /*
                                        // AINDA POR FAZER!!!
                                        if (pokemonId != data.first) {
                                            viewModel.pokemonList.value?.find { item ->
                                                item.pokemon.name == data.second
                                            }?.run {
                                                val id = this.pokemon.id
                                                val name = this.pokemon.name
                                                val colorPokemon = this.pokemon.color
                                                navController?.navigate("pokemonDetail/$id/$name/$colorPokemon/${false}")
                                            }
                                        }
                                        */
                                        Toast.makeText(
                                            context,
                                            data.second.capitalize(),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        activity?.trackButtonClick("Evolution: ${data.second.capitalize()}")
                                    }
                                ) {
                                    LoadGifWithCoilToEvolution(data)
                                }
                                val arrowVisible = evolutions.size == index + 1
                                if (arrowVisible.not()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_ios_24),
                                        contentDescription = "Arrow right",
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
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
    viewModel: PokemonViewModel = getViewModel()
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
                    pokemonDetail.damage.forEach { damageItem ->
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
    pokemonColor: String,
    viewModel: PokemonViewModel = getViewModel()
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
                            .background(pokemonColor.getColorByString())
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
                                    .background(pokemonColor.getColorByString())
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
    pokemonColor: String,
    viewModel: PokemonViewModel = getViewModel()
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
                            .background(pokemonColor.getColorByString())
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
                                    text = it.name?.capitalize() ?: "?",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(pokemonColor.getColorByString())
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
    pokemonColor: String,
    viewModel: PokemonViewModel = getViewModel()
) {
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
                            .background(pokemonColor.getColorByString())
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
                    pokemonDetail?.abilities?.forEach {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            val text = if (it.is_hidden) {
                                "${it.ability.name?.capitalize()} - ${stringResource(R.string.hidden)}"
                            } else {
                                it.ability.name?.capitalize()
                            }
                            Text(
                                text = text ?: "?",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White
                                ),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(pokemonColor.getColorByString())
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
    pokemonDetail: Pokemon?,
    context: Context,
    viewModel: PokemonViewModel = getViewModel(),
) {
    val loading = stringResource(R.string.loading_translater)
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
                    specie.flavor_text_entries?.let { flavorTextEntries ->
                        flavorTextEntries.getFlavorTextForLanguage("en")?.run {
                            translateIfSupported(
                                onResult = { translatedText ->
                                    encounterText = translatedText
                                },
                                onError = { exception ->
                                    encounterText = this
                                }
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