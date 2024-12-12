package com.vinithius.poke10.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
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
import com.valentinilk.shimmer.shimmer
import com.vinithius.poke10.R
import com.vinithius.poke10.components.TypeItem
import com.vinithius.poke10.components.TypeItemShimmer
import com.vinithius.poke10.components.TypeListResponse
import com.vinithius.poke10.datasource.mapper.fromDefaultToListType
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.datasource.response.Type
import com.vinithius.poke10.extension.capitalize
import com.vinithius.poke10.extension.convertPounds
import com.vinithius.poke10.extension.converterIntToDouble
import com.vinithius.poke10.extension.getColorByString
import com.vinithius.poke10.extension.getDrawableHabitat
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import com.vinithius.poke10.ui.viewmodel.RequestStateDetail
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import org.koin.androidx.compose.getViewModel

// PREVIEW ///////////////////////////////////////////////////////////////////////////////////////
/*
@SuppressLint("DefaultLocale")
@Preview(showBackground = true)
@Composable
fun PokemonDetailScreenPreview() {
    val context = LocalContext.current
    val pokemonDetail = getMockupPokemon()
    Card(
        modifier = Modifier
            .height(300.dp)
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween // Espaço entre os elementos
        ) {
            // Topo: Habitat e Imagem do Pokémon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Dá peso para expandir proporcionalmente
            ) {
                // Habitat Image
                PokemonHabitat(pokemonDetail, "venusaur")
                // Pokemon Image
                Image(
                    painter = painterResource(id = R.drawable.mockup_gif),
                    contentDescription = "venusaur",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(180.dp)
                        .padding(bottom = 30.dp)
                )
                // weight and height
                val weightKl = String.format("%.1f", pokemonDetail.weight?.converterIntToDouble())
                val weightLbs = String.format("%.1f", pokemonDetail.weight?.convertPounds())
                val resultWeight = stringResource(R.string.kg_lbs, weightKl, weightLbs)
                val heightM = String.format("%.1f", pokemonDetail.height?.converterIntToDouble())
                val heightInc = String.format("%.2f", pokemonDetail.height?.convertInch())
                val resultHeight = context.getString(R.string.m_inch, heightM, heightInc)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter) // Alinha o Row ao fundo da Box
                        .padding(bottom = 12.dp) // Ajuste do padding na parte inferior
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp), // Espaçamento horizontal do Row
                        horizontalArrangement = Arrangement.SpaceBetween, // Espaçamento entre os itens
                        verticalAlignment = Alignment.CenterVertically // Alinha verticalmente os itens
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.height),
                                contentDescription = "height",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(
                                text = resultWeight,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.weight),
                                contentDescription = "height",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(
                                text = resultHeight,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(5.dp))
                    // Generation and Base Experience
                    val generation =
                        pokemonDetail.specie?.generation?.name?.split("-")?.last()?.uppercase()
                            ?: "?"
                    val baseExperience = pokemonDetail.base_experience.toString()
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
                            Row {
                                Text(
                                    text = context.getString(R.string.generation),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                                Text(
                                    text = generation,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row {
                                Text(
                                    text = context.getString(R.string.base_exp),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                                Text(
                                    text = baseExperience,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(5.dp))
                    // Shape and Base Capture rate
                    val shape = pokemonDetail.specie?.shape?.name?.capitalize() ?: "?"
                    val captureRate = pokemonDetail.specie?.capture_rate?.toString() ?: "?"
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
                            Row {
                                Text(
                                    text = context.getString(R.string.shape),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                                Text(
                                    text = shape,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row {
                                Text(
                                    text = context.getString(R.string.capture_rate),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                                Text(
                                    text = captureRate,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                    )
                                )
                            }
                        }
                    }
                    TypeListResponse(pokemonDetail.types!!)
                }
            }
        }
    }
}
*/

// CODE //////////////////////////////////////////////////////////////////////////////////////////

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
    LaunchedEffect(Unit) {
        viewModel.getPokemonDetail()
        viewModel.setPokemonColor(pokemonColor)
    }
    BackHandler {
        viewModel.setDetailsScreen(false)
        navController?.popBackStack()
    }
    val pokemonDetail by viewModel.pokemonDetail.observeAsState()
    val painter = viewModel.getSharedImage(pokemonId.toString())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
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
                                )
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
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DefaultFirstCardData(
                                        viewModel = viewModel,
                                        title = stringResource(R.string.shape),
                                        value = pokemonDetail?.specie?.shape?.name?.capitalize()
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
        PokemonChart(viewModel, pokemonDetail, pokemonColor)
        PokemonDamage(pokemonDetail, viewModel)
        /*
        setInfo(pokemon)
        setIsBaby(pokemon)
        setEncounters(pokemon)
        setEggGroups(pokemon)
        setDamage(pokemon)
        setTextEntries(pokemon)
        setStats(pokemon)
        setTypes(pokemon)
        setAbilities(pokemon)
        setEvolutions(pokemon)
        setIconsMythicalAndLegendary(pokemon)
        */
    }
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
    val habitatImg = pokemonDetail?.specie?.habitat?.name?.getDrawableHabitat()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        habitatImg?.run {
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
            text = habitat,
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

private fun getStatsLabels(pokemonDetail: Pokemon?): List<String> {
    return pokemonDetail?.stats?.map { stat ->
        "${stat.stat.name?.capitalize()?.replace("-", " ")} (${stat.base_stat})"
    } ?: listOf()
}

private fun getStats(pokemonDetail: Pokemon?, pokemonColor: String): List<Bars> {
    if (pokemonDetail != null) {
        val labels = getStatsLabels(pokemonDetail)
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
    val stats = getStats(pokemonDetail, pokemonColor)
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
            labels = getStatsLabels(pokemonDetail),
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
private fun PokemonDamage(
    pokemonDetail: Pokemon?,
    viewModel: PokemonViewModel = getViewModel()
) {
    StateRequest(
        viewModel = viewModel,
        loading = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TypeItemShimmer()
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    DefaultDamageFromToShimmer()
                    DefaultDamageFromToShimmer()
                    DefaultDamageFromToShimmer()
                }
            }
        },
        success = {
            pokemonDetail?.damage?.forEach {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TypeItem(it.type?.name ?: String())
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                        DefaultDamageFromTo(
                            stringResource(R.string.no_damage),
                            it.damage_relations.no_damage_to.fromDefaultToListType(),
                            it.damage_relations.no_damage_from.fromDefaultToListType()
                        )
                        DefaultDamageFromTo(
                            stringResource(R.string.effective_damage),
                            it.damage_relations.effective_damage_to?.fromDefaultToListType()
                                ?: listOf(),
                            it.damage_relations.effective_damage_from.fromDefaultToListType()
                        )
                        DefaultDamageFromTo(
                            stringResource(R.string.ineffective_damage),
                            it.damage_relations.ineffective_damage_to.fromDefaultToListType(),
                            it.damage_relations.ineffective_damage_from.fromDefaultToListType()
                        )
                    }
                }
            }
        },
        error = { /* Do nothing yet */ }
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