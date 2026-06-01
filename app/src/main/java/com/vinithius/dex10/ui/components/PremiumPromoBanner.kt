package com.vinithius.dex10.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.data.PremiumManager
import org.koin.androidx.compose.get

private val GradientStart = Color(0xFF6A1B9A)
private val GradientMid   = Color(0xFF9C27B0)
private val GradientEnd   = Color(0xFFAD1457)
private val GoldColor     = Color(0xFFFFD54F)

@Composable
fun PremiumPromoBanner(
    onUpgradeClick: () -> Unit,
    lazyListState: LazyListState? = null,
    premiumManager: PremiumManager = get(),
) {
    val discountPercent by premiumManager.discountPercent.collectAsState()
    val isExpanded = lazyListState?.isScrollingUp() ?: true
    PremiumPromoBannerContent(
        discountPercent = discountPercent,
        isExpanded = isExpanded,
        onUpgradeClick = onUpgradeClick,
    )
}

@Composable
fun PremiumPromoBannerContent(
    discountPercent: Int?,
    isExpanded: Boolean = true,
    onUpgradeClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_shimmer")
    val crownComposition by rememberLottieComposition(LottieCompositionSpec.Asset("crown.json"))
    val crownProgress by animateLottieCompositionAsState(
        composition = crownComposition,
        iterations = LottieConstants.IterateForever
    )

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    val leadingIconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse"
    )

    // Collapse/expand animations
    val verticalPadding by animateDpAsState(
        targetValue = if (isExpanded) 14.dp else 5.dp,
        animationSpec = tween(300),
        label = "banner_v_padding"
    )
    val buttonHeight by animateDpAsState(
        targetValue = if (isExpanded) 36.dp else 28.dp,
        animationSpec = tween(300),
        label = "button_height"
    )
    val crownSizeScale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.68f,
        animationSpec = tween(300),
        label = "crown_size_scale"
    )
    // Badge always shows when there is a discount, regardless of expanded state
    val showBadge = discountPercent != null
    // Pulse is disabled in collapsed state so the crown stays still
    val effectiveCrownScale = if (isExpanded) leadingIconScale else 1f

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
            .padding(horizontal = 16.dp, vertical = verticalPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LottieAnimation(
                composition = crownComposition,
                progress = { crownProgress },
                modifier = Modifier
                    .size((32 * effectiveCrownScale * crownSizeScale).dp)
                    .padding(end = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.promo_banner_title),
                    color = GoldColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(1f, 1f),
                            blurRadius = 3f
                        )
                    )
                )
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(150))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.promo_banner_subtitle),
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box {
                Button(
                    onClick = onUpgradeClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldColor,
                        contentColor = Color(0xFF1A0033)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 5.dp
                    ),
                    modifier = Modifier
                        .padding(top = if (showBadge) 9.dp else 0.dp)
                        .height(buttonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.promo_banner_cta),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFFF8C00), Color(0xFFFF1744))
                                )
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$discountPercent% Off",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    val isScrollingUp = remember { mutableStateOf(true) }
    LaunchedEffect(this) {
        var previousIndex = firstVisibleItemIndex
        var previousScrollOffset = firstVisibleItemScrollOffset
        snapshotFlow { firstVisibleItemIndex to firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                isScrollingUp.value = when {
                    index != previousIndex -> index < previousIndex
                    else -> offset <= previousScrollOffset
                }
                previousIndex = index
                previousScrollOffset = offset
            }
    }
    return isScrollingUp.value
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE, name = "Expanded – no discount")
@Composable
private fun BannerExpandedPreview() {
    Surface { PremiumPromoBannerContent(discountPercent = null, isExpanded = true, onUpgradeClick = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE, name = "Collapsed – no discount")
@Composable
private fun BannerCollapsedPreview() {
    Surface { PremiumPromoBannerContent(discountPercent = null, isExpanded = false, onUpgradeClick = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE, name = "Expanded – 25% Off")
@Composable
private fun BannerExpandedDiscountPreview() {
    Surface { PremiumPromoBannerContent(discountPercent = 25, isExpanded = true, onUpgradeClick = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE, name = "Collapsed – 25% Off")
@Composable
private fun BannerCollapsedDiscountPreview() {
    Surface { PremiumPromoBannerContent(discountPercent = 25, isExpanded = false, onUpgradeClick = {}) }
}
