package com.vinithius.dex10.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import com.vinithius.dex10.R



@Composable
fun PokeballComponent(
    favorite: Boolean = false,
    frameDurationMillis: Long = 150L,
    frameResources: List<Int> = listOf(
        R.drawable.pokeball_01,
        R.drawable.pokeball_02_gray,
        R.drawable.pokeball_03_gray
    ),
    isShimmer: Boolean = false,
    choiceOfTheDayStatus: Boolean = false,
    hidePokemonOfTheDay: Boolean = false,
    onCallBackFinishAnimation: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    // Current frame state
    val targetFrame = if (favorite) 0 else frameResources.lastIndex
    val currentFrame by animateIntAsState(
        targetValue = targetFrame,
        animationSpec = tween(durationMillis = frameDurationMillis.toInt()),
        finishedListener = {
            onCallBackFinishAnimation()
        },
        label = "pokeballAnimation"
    )

    if (isShimmer) {
        Image(
            painter = painterResource(id = frameResources[currentFrame]),
            contentDescription = "Pokeball animation",
            modifier = Modifier
                .clickable { onClick() }
                .size(30.dp)
                .clip(CircleShape)
                .shimmer()
        )
    } else {
        val shouldHidePokemonOfTheDay = hidePokemonOfTheDay && choiceOfTheDayStatus
        Image(
            painter = painterResource(id = frameResources[currentFrame]),
            contentDescription = "Pokeball animation",
            modifier = Modifier
                .clickable {
                    if (!shouldHidePokemonOfTheDay) {
                        onClick()
                    }
                }
                .size(30.dp)
                .clip(CircleShape)
                .alpha(if (shouldHidePokemonOfTheDay) 0f else 1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokeballComponentPreview() {
    PokeballComponent()
}
