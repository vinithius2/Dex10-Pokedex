package com.vinithius.dex10.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.data.PremiumManager
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsellBottomSheet(
    onDismiss: () -> Unit,
    onPurchaseClick: () -> Unit,
    onRestoreClick: () -> Unit,
    premiumManager: PremiumManager = get()
) {
    val premiumPrice by premiumManager.premiumPrice.collectAsState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Premium Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.premium_upsell_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.premium_upsell_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Comparison Table
            ComparisonTable()

            Spacer(modifier = Modifier.height(32.dp))

            // Purchase Button (AppButton)
            AppButton(
                text = stringResource(R.string.cta_upgrade, premiumPrice ?: "---"),
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Solid
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.payment_one_time),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Text(
                text = stringResource(R.string.footer_google_play),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Restore Button
            AppButton(
                text = stringResource(R.string.restore_purchases),
                onClick = onRestoreClick,
                variant = ButtonVariant.Text,
                fontSize = 14
            )
        }
    }
}

@Composable
private fun ComparisonTable() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(vertical = 16.dp)
    ) {
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.feature_ads), 
                modifier = Modifier.weight(1f), 
                style = MaterialTheme.typography.labelMedium,
                color = Color.Transparent // Placeholder for alignment
            )
            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.plan_trainer_header), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(stringResource(R.string.plan_trainer_sub), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.plan_master_header), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.plan_master_sub), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

        // Features
        ComparisonRow(stringResource(R.string.feature_ads), stringResource(R.string.val_trainer_ads), stringResource(R.string.val_master_ads), isTrainerPro = false)
        ComparisonRow(stringResource(R.string.feature_team_builder), stringResource(R.string.val_trainer_teams), stringResource(R.string.val_master_teams), isTrainerPro = false)
        ComparisonRow(stringResource(R.string.feature_favorites), stringResource(R.string.val_trainer_favs), stringResource(R.string.val_master_favs), isTrainerPro = false)
        ComparisonRow(stringResource(R.string.feature_appearance), stringResource(R.string.val_trainer_appearance), stringResource(R.string.val_master_appearance), isTrainerPro = false)
        ComparisonRow(stringResource(R.string.feature_security), stringResource(R.string.val_trainer_security), stringResource(R.string.val_master_security), isTrainerPro = false)
        ComparisonRow(stringResource(R.string.feature_updates), stringResource(R.string.val_trainer_updates), stringResource(R.string.val_master_updates), isTrainerPro = false)
    }
}

@Composable
private fun ComparisonRow(feature: String, freeVal: String, proVal: String, isTrainerPro: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Feature Name
        Text(
            text = feature,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Free Value
        Row(
            modifier = Modifier.weight(0.8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(freeVal, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        // Pro Value
        Row(
            modifier = Modifier
                .weight(1.1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50), // Pokémon Green
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = proVal, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.ExtraBold, 
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}
