package com.vinithius.poke10.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import com.vinithius.poke10.R
import kotlinx.coroutines.delay


@Composable
fun PokeballComponent(
    favorite: Boolean = false,
    frameDurationMillis: Long = 200L,
    frameResources: List<Int> = listOf(
        R.drawable.pokeball_01,
        R.drawable.pokeball_02_gray,
        R.drawable.pokeball_03_gray
    ),
    isShimmer: Boolean = false,
    choiceOfTheDayStatus : Boolean = false,
    hidePokemonOfTheDay : Boolean = false,
    onCallBackFinishAnimation: () -> Unit = {},
    onCallBack: () -> Unit = {},
) {
    // Initial frame
    var currentFrame by remember { mutableIntStateOf(if (favorite) 0 else frameResources.lastIndex) }
    // Launch
    var isPlaying by remember { mutableStateOf(false) }
    // Direction
    var isForward by remember { mutableStateOf(favorite.not()) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(frameDurationMillis)
            currentFrame = (currentFrame + if (isForward) 1 else -1)
                .coerceIn(0, frameResources.lastIndex)
            if (currentFrame == 0 || currentFrame == frameResources.lastIndex) {
                isPlaying = false
                onCallBackFinishAnimation.invoke()
            }
        }
    }
    if (isShimmer) {
        Image(
            painter = painterResource(id = frameResources[currentFrame]),
            contentDescription = "Pokeball animation",
            modifier = Modifier
                .clickable {
                    isForward = isForward.not()
                    isPlaying = true
                    onCallBack.invoke()
                }
                .size(30.dp)
                .clip(CircleShape)
                .shimmer()
        )
    } else {
        Image(
            painter = painterResource(id = frameResources[currentFrame]),
            contentDescription = "Pokeball animation",
            modifier = Modifier
                .clickable {
                    if (hidePokemonOfTheDay.not()) {
                        isForward = isForward.not()
                        isPlaying = true
                        onCallBack.invoke()
                    }
                }
                .size(30.dp)
                .clip(CircleShape)
                .alpha(if (hidePokemonOfTheDay && choiceOfTheDayStatus) 0f else 1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokeballComponentPreview() {
    PokeballComponent()
}
