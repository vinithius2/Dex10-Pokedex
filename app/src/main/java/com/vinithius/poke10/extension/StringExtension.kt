package com.vinithius.poke10.extension

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.vinithius.poke10.R
import com.vinithius.poke10.datasource.response.FlavorText
import java.util.Locale
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

fun List<FlavorText>?.getFlavorTextForLanguage(languageCode: String): String? {
    return this
        ?.filter { it.language.name == languageCode }
        ?.distinctBy { it.flavor_text }
        ?.joinToString(separator = "\n\n") { item ->
            val cleanedText = item.flavor_text
                .replace(Regex("[\r\n]+"), " ") // remove qualquer quebra de linha
                .replace("\\s+".toRegex(), " ") // remove espaços extras
                .trim()
            "• $cleanedText"
        }
}

fun String.getStringStat(context: Context): String {
    val stringMap = mapOf(
        "hp" to context.getString(R.string.hp),
        "attack" to context.getString(R.string.attack),
        "defense" to context.getString(R.string.defense),
        "special-attack" to context.getString(R.string.special_attack),
        "special-defense" to context.getString(R.string.special_defense),
        "speed" to context.getString(R.string.speed)
    )
    return stringMap[this.lowercase()] ?: context.getString(R.string.unknow)
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
    return drawableMap[this.lowercase()] ?: R.drawable.unknow_habitat
}

fun String.getStringHabitat(context: Context): String {
    val stringMap = mapOf(
        "cave" to context.getString(R.string.cave),
        "forest" to context.getString(R.string.forest),
        "grassland" to context.getString(R.string.grassland),
        "mountain" to context.getString(R.string.mountain),
        "rare" to context.getString(R.string.rare),
        "rough-terrain" to context.getString(R.string.rough_terrain),
        "sea" to context.getString(R.string.sea),
        "urban" to context.getString(R.string.urban),
        "waters-edge" to context.getString(R.string.waters_edge)
    )
    return stringMap[this.lowercase()] ?: context.getString(R.string.unknow)
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
    return drawableMap[this.lowercase()] ?: R.drawable.unknow
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
    return drawableMap[this.lowercase()] ?: Color(0xFF75525C)
}

fun String.getStringType(context: Context): String {
    val stringMap = mapOf(
        "bug" to context.getString(R.string.bug),
        "dark" to context.getString(R.string.dark),
        "dragon" to context.getString(R.string.dragon),
        "electric" to context.getString(R.string.electric),
        "fairy" to context.getString(R.string.fairy),
        "fighting" to context.getString(R.string.fighting),
        "fire" to context.getString(R.string.fire),
        "flying" to context.getString(R.string.flying),
        "ghost" to context.getString(R.string.ghost),
        "grass" to context.getString(R.string.grass),
        "ground" to context.getString(R.string.ground),
        "ice" to context.getString(R.string.ice),
        "poison" to context.getString(R.string.poison),
        "psychic" to context.getString(R.string.psychic),
        "rock" to context.getString(R.string.rock),
        "steel" to context.getString(R.string.steel),
        "water" to context.getString(R.string.water),
        "unknow" to context.getString(R.string.unknow)
    )
    return stringMap[this.lowercase()] ?: context.getString(R.string.unknow)
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
        colorMap[this.lowercase()]?.let { Color(it) } ?: Color(ParseColor.BLACK)
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
        colorMap[this.lowercase()] ?: Color.Black
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
        colorMap[this.lowercase()] ?: Color.Black
    } catch (e: IllegalArgumentException) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Color.Black
    }
}

/**
 * Traduz esta String do inglês para o idioma passado, se for suportado.
 * Requer que o modelo já tenha sido baixado previamente (ex: na MainActivity).
 *
 * @param languageCode Código do idioma de destino (ex.: "pt", "es", "fr", "hi")
 * @param onResult Resultado da tradução, ou o texto original se idioma não suportado
 * @param onError Callback de erro, se a tradução falhar
 */
fun String.translateIfSupported(
    languageCode: String = Locale.getDefault().language,
    onResult: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val supportedLanguages = setOf("pt", "es", "fr", "hi")

    if (supportedLanguages.contains(languageCode)) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(languageCode)
            .build()

        val translator = Translation.getClient(options)

        translator.translate(this)
            .addOnSuccessListener { translatedText ->
                onResult(translatedText)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    } else {
        onResult(this)
    }
}

