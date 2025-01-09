package com.vinithius.poke10.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinithius.poke10.R

@Composable
fun PikachuComponent() {
    // Configuração da animação de escala
    var animatedScale by remember { mutableStateOf(1f) }

    // Define o valor animado de escala com um efeito de "pulsação"
    val scale by animateFloatAsState(
        targetValue = animatedScale,
        animationSpec = infiniteRepeatable(
            animation = tween(500) // Duração em ms
        )
    )

    // Inicia a animação
    LaunchedEffect (Unit) {
        animatedScale = 1.2f // Ajuste conforme o efeito desejado
    }

    Image(
        painter = painterResource(id = R.drawable.pikachu_sad), // Use o recurso drawable
        contentDescription = "Pikachu Error",
        modifier = Modifier
            .size(100.dp)
            .scale(scale), // Aplica a escala animada
        alignment = Alignment.Center
    )
}

@Preview(showBackground = true)
@Composable
fun PikachuComponentPreview() {
    PikachuComponent()
}
