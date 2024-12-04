package com.vinithius.poke10.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.vinithius.poke10.R
import com.vinithius.poke10.datasource.response.Pokemon
import com.vinithius.poke10.extension.capitalize
import com.vinithius.poke10.extension.convertInch
import com.vinithius.poke10.extension.convertPounds
import com.vinithius.poke10.extension.converterIntToDouble
import com.vinithius.poke10.extension.getDrawableHabitat
import com.vinithius.poke10.ui.viewmodel.PokemonViewModel
import org.koin.androidx.compose.getViewModel

// PREVIEW ///////////////////////////////////////////////////////////////////////////////////////

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
                val resultWeight = context.getString(R.string.kg_lbs, weightKl, weightLbs)
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
                }
            }
        }
    }
}


// CODE //////////////////////////////////////////////////////////////////////////////////////////

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PokemonDetailScreen(
    navController: NavController?,
    pokemonId: Int,
    pokemonName: String,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    viewModel: PokemonViewModel = getViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getPokemonDetail()
    }
    val pokemonDetail by viewModel.pokemonDetail.observeAsState()
    val painter = viewModel.getSharedImage(pokemonId.toString())
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
                PokemonHabitat(pokemonDetail, pokemonName)
                // Pokemon Image
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = "Teste",
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
                val weightKl = String.format("%.1f", pokemonDetail?.weight?.converterIntToDouble())
                val weightLbs = String.format("%.1f", pokemonDetail?.weight?.convertPounds())
                val resultWeight = context.getString(R.string.kg_lbs, weightKl, weightLbs)
                val heightM = String.format("%.1f", pokemonDetail?.height?.converterIntToDouble())
                val heightInc = String.format("%.2f", pokemonDetail?.height?.convertInch())
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
                        pokemonDetail?.specie?.generation?.name?.split("-")?.last()?.uppercase()
                            ?: "?"
                    val baseExperience = pokemonDetail?.base_experience.toString()
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
                    val shape = pokemonDetail?.specie?.shape?.name?.capitalize() ?: "?"
                    val captureRate = pokemonDetail?.specie?.capture_rate?.toString() ?: "?"
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
                }
            }
        }
    }
}

@Composable
private fun PokemonHabitat(
    pokemonDetail: Pokemon?,
    pokemonName: String
) {
    val habitatImg = pokemonDetail?.specie?.habitat?.name?.getDrawableHabitat()
        ?: R.drawable.unknow_habitat

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = habitatImg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
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