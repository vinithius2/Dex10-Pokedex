package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.data.AppPreferences
import com.vinithius.dex10.datasource.data.PremiumManager
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.foundation.clickable

@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    premiumManager: PremiumManager? = null,
    onBack: () -> Unit
) {
    val darkMode by appPreferences.darkMode.collectAsState()
    val notificationsEnabled by appPreferences.notificationsEnabled.collectAsState()
    val lowQuality by appPreferences.lowQualityImages.collectAsState()
    val isPremium by (premiumManager?.isPremium ?: MutableStateFlow(false)).collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Premium Section ---
        /*
        if (premiumManager != null) {
            SectionHeader(stringResource(R.string.premium))
            SettingClickableItem(
                icon = Icons.Default.Settings, // Could be replaced with a star or specific icon
                title = stringResource(R.string.restore_purchases),
                description = stringResource(R.string.restore_purchases_desc),
                onClick = { premiumManager.restorePurchases() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
        */
        // --- Appearance Section ---
        SectionHeader(stringResource(R.string.appearance))

        // Dark Mode
        SettingItemHeader(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.dark_mode),
            description = stringResource(R.string.dark_mode_desc)
        )
        DarkModeRadioGroup(
            selected = darkMode,
            onSelect = { appPreferences.setDarkMode(it) },
            isPremium = isPremium,
            onShowUpsell = { premiumManager?.triggerUpsell() }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // --- General Section ---
        SectionHeader(stringResource(R.string.general))

        // Notifications
        SettingToggleItem(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.notifications),
            description = stringResource(R.string.notifications_desc),
            checked = notificationsEnabled,
            onCheckedChange = { appPreferences.setNotificationsEnabled(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // --- Performance Section ---
        SectionHeader(stringResource(R.string.performance))

        // Image Quality
        SettingToggleItem(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.image_quality),
            description = if (lowQuality) {
                stringResource(R.string.image_quality_low)
            } else {
                stringResource(R.string.image_quality_high)
            },
            subtitle = stringResource(R.string.image_quality_desc),
            checked = lowQuality,
            onCheckedChange = { appPreferences.setLowQualityImages(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingItemHeader(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DarkModeRadioGroup(
    selected: Int,
    onSelect: (Int) -> Unit,
    isPremium: Boolean,
    onShowUpsell: () -> Unit
) {
    val context = LocalContext.current
    val options = listOf(
        AppPreferences.DARK_MODE_SYSTEM to stringResource(R.string.dark_mode_system),
        AppPreferences.DARK_MODE_ON to stringResource(R.string.dark_mode_on),
        AppPreferences.DARK_MODE_OFF to stringResource(R.string.dark_mode_off)
    )
    Column(modifier = Modifier.padding(start = 56.dp)) {
        options.forEach { (value, label) ->
            val isLocked = !isPremium && value != AppPreferences.DARK_MODE_OFF
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clickable { 
                        if (isLocked) {
                            onShowUpsell()
                        } else {
                            onSelect(value)
                        }
                    }
            ) {
                RadioButton(
                    selected = selected == value,
                    onClick = { 
                        if (isLocked) {
                             onShowUpsell()
                        } else {
                            onSelect(value) 
                        }
                    },
                    enabled = !isLocked,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                )
                if (isLocked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.locked),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
