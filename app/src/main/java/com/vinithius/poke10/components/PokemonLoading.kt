package com.vinithius.poke10.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinithius.poke10.R

@Composable
fun PokemonLoadingComponent() {
    // Controle de animação para alpha (opacidade)
    var animatedAlpha by remember { mutableStateOf(1f) }
    val alpha by animateFloatAsState(
        targetValue = animatedAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(1000) // duração em ms
        )
    )
    // Controle de animação para escala
    var animatedScale by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = animatedScale,
        animationSpec = infiniteRepeatable(
            animation = tween(700) // duração em ms
        )
    )

    // Inicia as animações
    LaunchedEffect(Unit) {
        animatedAlpha = 0.5f // Define a opacidade desejada
        animatedScale = 1.1f // Define a escala desejada
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.pokemon_logo),
            contentDescription = "Pokemon Logo",
            modifier = Modifier
                .size(100.dp)
                .alpha(alpha)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading...",
            modifier = Modifier
                .scale(scale)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonLoadingComponentPreview() {
    PokemonLoadingComponent()
}
