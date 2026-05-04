package com.vinithius.dex10.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinithius.dex10.R

private val GradientStart = Color(0xFF6A1B9A)   // deep violet
private val GradientMid   = Color(0xFF9C27B0)   // purple
private val GradientEnd   = Color(0xFFAD1457)   // deep pink
private val GoldColor     = Color(0xFFFFD54F)

@Composable
fun PremiumPromoBanner(onUpgradeClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_shimmer")
    val crownComposition by rememberLottieComposition(LottieCompositionSpec.Asset("crown.json"))
    val crownProgress by animateLottieCompositionAsState(
        composition = crownComposition,
        iterations = LottieConstants.IterateForever
    )

    // Shimmer sweep: a bright diagonal stripe that crosses the banner every ~3.5 s
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    // Leading premium icon pulse: subtly scales between 1.0 and 1.25
    val leadingIconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientMid, GradientEnd)
                )
            )
            // Shimmer overlay drawn on top of the gradient
            .drawWithContent {
                drawContent()
                val sweepWidth = size.width * 0.35f
                val x = size.width * shimmerProgress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        start = Offset(x - sweepWidth / 2, 0f),
                        end = Offset(x + sweepWidth / 2, size.height)
                    )
                )
            }
            .clickable(onClick = onUpgradeClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Crown animation reused from the premium bottom sheet
            LottieAnimation(
                composition = crownComposition,
                progress = { crownProgress },
                modifier = Modifier
                    .size((32 * leadingIconScale).dp)
                    .padding(end = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Text block
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.promo_banner_title),
                    color = GoldColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(1f, 1f),
                            blurRadius = 3f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.promo_banner_subtitle),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // CTA button
            Button(
                onClick = onUpgradeClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldColor,
                    contentColor = Color(0xFF1A0033)
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.promo_banner_cta),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun PremiumPromoBannerPreview() {
    Surface {
        PremiumPromoBanner(onUpgradeClick = {})
    }
}
