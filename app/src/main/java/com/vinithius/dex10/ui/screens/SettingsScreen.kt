package com.vinithius.dex10.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    premiumManager: PremiumManager? = null,
    onBack: () -> Unit
) {
    val darkMode by appPreferences.darkMode.collectAsState()
    val notificationsEnabled by appPreferences.notificationsEnabled.collectAsState()
    val spriteType by appPreferences.spriteType.collectAsState()
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
        val effectiveDarkMode = if (isPremium) darkMode else AppPreferences.DARK_MODE_OFF

        SettingItemHeader(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.dark_mode),
            description = stringResource(R.string.dark_mode_desc)
        )
        DarkModeRadioGroup(
            selected = effectiveDarkMode,
            onSelect = {
                if (!isPremium && it != AppPreferences.DARK_MODE_OFF) {
                    premiumManager?.triggerUpsell()
                    com.vinithius.dex10.analytics.AnalyticsManager.logFeatureRestricted("dark_mode")
                } else {
                    appPreferences.setDarkMode(it)
                }
            },
            isPremium = isPremium,
            onShowUpsell = {
                 premiumManager?.triggerUpsell()
                 com.vinithius.dex10.analytics.AnalyticsManager.logFeatureRestricted("dark_mode")
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // --- General Section ---
        SectionHeader(stringResource(R.string.general))

        // Notifications (Persisted via AppPreferences)
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

        // Art Type
        SettingItemHeader(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.image_quality),
            description = stringResource(R.string.image_quality_desc)
        )
        SpriteTypeSelector(
            selected = spriteType,
            onSelect = { appPreferences.setSpriteType(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // --- Voice / TTS Section ---
        val ttsSpeed by appPreferences.ttsSpeed.collectAsState()
        val ttsPitch by appPreferences.ttsPitch.collectAsState()
        val ttsAutoPlay by appPreferences.ttsAutoPlay.collectAsState()

        SectionHeader(stringResource(R.string.tts_section_title))

        SettingToggleItem(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            title = stringResource(R.string.tts_auto_play),
            description = stringResource(R.string.tts_auto_play_desc),
            checked = ttsAutoPlay,
            onCheckedChange = { appPreferences.setTtsAutoPlay(it) }
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.tts_speed),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "%.1f×".format(ttsSpeed),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = ttsSpeed,
                onValueChange = { appPreferences.setTtsSpeed(it) },
                valueRange = 0.5f..2.0f,
                steps = 14, // 0.1 increments
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.tts_pitch),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "%.1f×".format(ttsPitch),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = ttsPitch,
                onValueChange = { appPreferences.setTtsPitch(it) },
                valueRange = 0.5f..2.0f,
                steps = 14, // 0.1 increments — default 1.4 (Pokédex tone) lands on a tick
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Developer Options (Debug Only) ---
        if (com.vinithius.dex10.BuildConfig.DEBUG) {
            SectionHeader("Developer Options 🛠️")

            SettingToggleItem(
                icon = Icons.Default.Lock,
                title = "Force Premium Status",
                description = if (isPremium) "Premium is ON (Debug or Real)" else "Premium is OFF",
                checked = isPremium,
                onCheckedChange = { isEnabled ->
                    if (isEnabled) {
                         premiumManager?.setDebugPremiumStatus(true)
                    } else {
                         premiumManager?.setDebugPremiumStatus(false)
                    }
                }
            )

            SettingClickableItem(
                icon = Icons.Default.Share,
                title = "Reset Premium Override",
                description = "Clear override and check real status",
                onClick = { premiumManager?.clearDebugPremiumStatus() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            DebugPricingSection(premiumManager = premiumManager)

            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- Version footer ---
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(
                R.string.app_version_fmt,
                com.vinithius.dex10.BuildConfig.VERSION_NAME,
                com.vinithius.dex10.BuildConfig.VERSION_CODE
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SpriteTypeSelector(
    selected: AppPreferences.SpriteType,
    onSelect: (AppPreferences.SpriteType) -> Unit,
) {
    val context = LocalContext.current
    val options = listOf(
        AppPreferences.SpriteType.OFFICIAL_ARTWORK to Pair(
            stringResource(R.string.sprite_type_official_artwork),
            stringResource(R.string.sprite_type_official_artwork_desc)
        ),
        AppPreferences.SpriteType.GIF to Pair(
            stringResource(R.string.sprite_type_gif),
            stringResource(R.string.sprite_type_gif_desc)
        ),
        AppPreferences.SpriteType.HOME to Pair(
            stringResource(R.string.sprite_type_home),
            stringResource(R.string.sprite_type_home_desc)
        ),
        AppPreferences.SpriteType.PIXEL to Pair(
            stringResource(R.string.sprite_type_pixel),
            stringResource(R.string.sprite_type_pixel_desc)
        ),
    )

    // URL of the preview Pokémon (Pikachu) for each type
    fun previewUrl(type: AppPreferences.SpriteType) = when (type) {
        AppPreferences.SpriteType.GIF ->
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/25.gif"
        AppPreferences.SpriteType.OFFICIAL_ARTWORK ->
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"
        AppPreferences.SpriteType.HOME ->
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/25.png"
        AppPreferences.SpriteType.PIXEL ->
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
    }

    Column(modifier = Modifier.padding(start = 56.dp, end = 16.dp)) {
        options.forEach { (type, pair) ->
            val (label, desc) = pair
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(type) }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(desc, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Pikachu preview for each art type
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(previewUrl(type))
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = label,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugPricingSection(premiumManager: PremiumManager?) {
    var mockOriginalPrice by remember { mutableStateOf("€10.00") }
    var mockPrice by remember { mutableStateOf("€9.00") }
    var mockDiscount by remember { mutableFloatStateOf(10f) }

    SettingItemHeader(
        icon = Icons.Default.Settings,
        title = "Simulate Discount",
        description = "Preview the pricing UI with a mock promotion"
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = mockOriginalPrice,
                onValueChange = { mockOriginalPrice = it },
                label = { Text("Full price") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = mockPrice,
                onValueChange = { mockPrice = it },
                label = { Text("Sale price") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${mockDiscount.roundToInt()}% OFF",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(64.dp)
            )
            Slider(
                value = mockDiscount,
                onValueChange = { mockDiscount = it },
                valueRange = 1f..80f,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    premiumManager?.setDebugPricing(
                        price = mockPrice,
                        originalPrice = mockOriginalPrice,
                        discountPercent = mockDiscount.roundToInt()
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
            OutlinedButton(
                onClick = { premiumManager?.clearDebugPricing() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { premiumManager?.triggerUpsell() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Preview Upsell Sheet")
        }
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
