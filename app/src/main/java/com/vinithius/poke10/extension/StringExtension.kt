package com.vinithius.poke10.extension

import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vinithius.poke10.R
import com.vinithius.poke10.datasource.response.FlavorText
import android.graphics.Color as ParseColor

/**
 * Make the first letter Uppercase.
 */
fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar(Char::uppercase)
}

/**
 * Get ID from URL details.
 */
fun String.getIdIntoUrl(): String? {
    try {
        val parse = Uri.parse(this)
        parse.pathSegments.getOrNull(3)?.let {
            return it
        }
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        return null
    }
    return null
}

/**
 * Extension function to format HTML text and return a Spanned.
 * Compatible with different Android API levels.
 */
fun String.getHtmlCompat(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(this)
    }
}

@Composable
fun List<FlavorText>?.getFlavorTextForLanguage(languageCode: String): List<String>? {
    return this?.filter { it.language.name == languageCode }?.distinctBy { it.flavor_text }
        ?.map { item -> "• ${item.flavor_text}" }
}

fun String.getDrawableHabitat(): Int {
    val drawableMap = mapOf(
        "cave" to R.drawable.cave,
        "forest" to R.drawable.forest,
        "grassland" to R.drawable.grassland,
        "mountain" to R.drawable.mountain,
        "rare" to R.drawable.rare,
        "rough-terrain" to R.drawable.rough_terrain,
        "sea" to R.drawable.sea,
        "urban" to R.drawable.urban,
        "waters-edge" to R.drawable.waters_edge,
    )
    return drawableMap[this] ?: R.drawable.unknow_habitat
}

fun String.getDrawableIco(): Int {
    val drawableMap = mapOf(
        "bug" to R.drawable.bug,
        "dark" to R.drawable.dark,
        "dragon" to R.drawable.dragon,
        "electric" to R.drawable.electric,
        "fairy" to R.drawable.fairy,
        "fighting" to R.drawable.fighting,
        "fire" to R.drawable.fire,
        "flying" to R.drawable.flying,
        "ghost" to R.drawable.ghost,
        "grass" to R.drawable.grass,
        "ground" to R.drawable.ground,
        "ice" to R.drawable.ice,
        "poison" to R.drawable.poison,
        "psychic" to R.drawable.psychic,
        "rock" to R.drawable.rock,
        "steel" to R.drawable.steel,
        "water" to R.drawable.water,
        "unknow" to R.drawable.unknow,
    )
    return drawableMap[this] ?: R.drawable.unknow
}

fun String.getDrawableIcoColor(): Color {
    val drawableMap = mapOf(
        "bug" to Color(0xFF1C4B27),
        "dark" to Color(0xFF595978),
        "dragon" to Color(0xFF448A95),
        "electric" to Color(0xFFE2E32B),
        "fairy" to Color(0xFF961A45),
        "fighting" to Color(0xFF994025),
        "fire" to Color(0xFFAB1F24),
        "flying" to Color(0xFF4A677D),
        "ghost" to Color(0xFF33336B),
        "grass" to Color(0xFF147B3D),
        "ground" to Color(0xFFA8702D),
        "ice" to Color(0xFF86D2F5),
        "poison" to Color(0xFF5E2D89),
        "psychic" to Color(0xFFA52A6C),
        "rock" to Color(0xFF48190B),
        "steel" to Color(0xFF60756E),
        "water" to Color(0xFF1552E1),
        "unknow" to Color(0xFF75525C)
    )
    return drawableMap[this] ?: Color(0xFF75525C)
}

fun String.getParseColorByString(): Color {
    return try {
        val colorMap = mapOf(
            "black" to ParseColor.BLACK,
            "blue" to ParseColor.BLUE,
            "brown" to ParseColor.parseColor("#8B4513"),
            "gray" to ParseColor.GRAY,
            "green" to ParseColor.GREEN,
            "pink" to ParseColor.parseColor("#FFC0CB"),
            "purple" to ParseColor.parseColor("#800080"),
            "red" to ParseColor.RED,
            "white" to ParseColor.WHITE,
            "yellow" to ParseColor.YELLOW
        )
        colorMap[this]?.let { Color(it) } ?: Color(ParseColor.BLACK)
    } catch (e: IllegalArgumentException) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Color(ParseColor.BLACK)
    }
}

fun String.getToolBarColorByString(): Color {
    return try {
        val colorMap = mapOf(
            "black" to Color(ParseColor.BLACK),
            "blue" to Color(ParseColor.BLUE),
            "brown" to Color(0xFF8B4513),
            "gray" to Color(ParseColor.GRAY),
            "green" to Color(ParseColor.GREEN),
            "pink" to Color(0xFFFFC0CB),
            "purple" to Color(0xFF800080),
            "red" to Color(ParseColor.RED),
            "white" to Color(0xFFB0B0B0),
            "yellow" to Color(0xADC2C200)
        )
        colorMap[this] ?: Color.Black
    } catch (e: IllegalArgumentException) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Color.Black
    }
}

fun String.getColorByString(): Color {
    return try {
        val colorMap = mapOf(
            "black" to Color.Black,
            "blue" to Color(0xFF00008C),
            "brown" to Color(0xFF8B3D05),
            "gray" to Color(0xFF565656),
            "green" to Color(0xFF00C400),
            "pink" to Color(0xFFE2A5B0),
            "purple" to Color(0xFF650067),
            "red" to Color(0xFF770000),
            "white" to Color(0xFF8C8C8C),
            "yellow" to Color(0xFFC0B525)
        )
        colorMap[this] ?: Color.Black
    } catch (e: IllegalArgumentException) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Color.Black
    }
}
