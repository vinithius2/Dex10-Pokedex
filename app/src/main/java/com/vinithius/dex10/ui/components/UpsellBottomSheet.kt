package com.vinithius.dex10.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsellBottomSheet(
    onDismiss: () -> Unit,
    onPurchaseClick: () -> Unit,
    premiumManager: PremiumManager = get()
) {
    val premiumPrice by premiumManager.premiumPrice.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }
    ) {
        UpsellSheetContent(
            premiumPrice = premiumPrice,
            onPurchaseClick = onPurchaseClick,
        )
    }
}

@Composable
fun UpsellSheetContent(
    premiumPrice: String?,
    onPurchaseClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            HeaderSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Comparison Table
            ComparisonTable()

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Fixed Action Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            
            ActionSection(
                premiumPrice = premiumPrice,
                onPurchaseClick = onPurchaseClick,
            )
        }
    }
}

@Preview
@Composable
fun UpsellBottomSheetPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            UpsellSheetContent(
                premiumPrice = "R$36.99",
                onPurchaseClick = {},
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("crown.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Glowing Background for Lottie
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFBC02D).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.premium_upsell_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.premium_upsell_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ComparisonTable() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)) // glass-panel
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.feature_ads).uppercase(),
                modifier = Modifier.weight(1.2f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.plan_trainer_header).uppercase(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.plan_master_header).uppercase(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        // Features Rows
        ComparisonRow(
            stringResource(R.string.feature_ads),
            stringResource(R.string.val_trainer_ads),
            stringResource(R.string.val_master_ads)
        )
        ComparisonRow(
            stringResource(R.string.feature_team_builder),
            stringResource(R.string.val_trainer_teams),
            stringResource(R.string.val_master_teams)
        )
        ComparisonRow(
            stringResource(R.string.feature_favorites),
            stringResource(R.string.val_trainer_favs),
            stringResource(R.string.val_master_favs)
        )
        ComparisonRow(
            stringResource(R.string.feature_appearance),
            stringResource(R.string.val_trainer_appearance),
            stringResource(R.string.val_master_appearance)
        )
        ComparisonRow(
            stringResource(R.string.feature_updates),
            stringResource(R.string.val_trainer_updates),
            stringResource(R.string.val_master_updates)
        )
    }
}

@Composable
private fun ComparisonRow(feature: String, trainerVal: String, mestreVal: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feature,
                modifier = Modifier.weight(1.2f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = trainerVal,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mestreVal,
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
    }
}

@Composable
private fun ActionSection(
    premiumPrice: String?,
    onPurchaseClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onPurchaseClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    8.dp,
                    RoundedCornerShape(16.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.cta_upgrade, premiumPrice ?: "---"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.footer_google_play).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

    }
}
