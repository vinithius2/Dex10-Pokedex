package com.vinithius.dex10.extension

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.compose.ui.graphics.Color
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.response.FlavorText
import com.vinithius.dex10.ui.theme.DarkBlack
import com.vinithius.dex10.ui.theme.DarkBlue
import com.vinithius.dex10.ui.theme.DarkBrown
import com.vinithius.dex10.ui.theme.DarkGray
import com.vinithius.dex10.ui.theme.DarkGreen
import com.vinithius.dex10.ui.theme.DarkOnSurface
import com.vinithius.dex10.ui.theme.DarkPink
import com.vinithius.dex10.ui.theme.DarkPurple
import com.vinithius.dex10.ui.theme.DarkRed
import com.vinithius.dex10.ui.theme.DarkText
import com.vinithius.dex10.ui.theme.DarkWhite
import com.vinithius.dex10.ui.theme.DarkYellow
import com.vinithius.dex10.ui.theme.LightBlack
import com.vinithius.dex10.ui.theme.LightBlue
import com.vinithius.dex10.ui.theme.LightBrown
import com.vinithius.dex10.ui.theme.LightGray
import com.vinithius.dex10.ui.theme.LightGreen
import com.vinithius.dex10.ui.theme.LightOnSurface
import com.vinithius.dex10.ui.theme.LightPink
import com.vinithius.dex10.ui.theme.LightPurple
import com.vinithius.dex10.ui.theme.LightRed
import com.vinithius.dex10.ui.theme.LightText
import com.vinithius.dex10.ui.theme.LightWhite
import com.vinithius.dex10.ui.theme.LightYellow
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

fun String.getStringEggGroup(context: Context): String {
    val eggGroupMap = mapOf(
        "monster" to context.getString(R.string.egg_group_monster),
        "water1" to context.getString(R.string.egg_group_water1),
        "bug" to context.getString(R.string.egg_group_bug),
        "flying" to context.getString(R.string.egg_group_flying),
        "ground" to context.getString(R.string.egg_group_ground),
        "fairy" to context.getString(R.string.egg_group_fairy),
        "plant" to context.getString(R.string.egg_group_plant),
        "humanshape" to context.getString(R.string.egg_group_humanshape),
        "water3" to context.getString(R.string.egg_group_water3),
        "mineral" to context.getString(R.string.egg_group_mineral),
        "indeterminate" to context.getString(R.string.egg_group_indeterminate),
        "water2" to context.getString(R.string.egg_group_water2),
        "ditto" to context.getString(R.string.egg_group_ditto),
        "dragon" to context.getString(R.string.egg_group_dragon),
        "no-eggs" to context.getString(R.string.egg_group_no_eggs)
    )
    return eggGroupMap[this.lowercase()] ?: context.getString(R.string.unknow)
}

fun String.getStringShape(context: Context): String {
    val shapeMap = mapOf(
        "ball" to context.getString(R.string.shape_ball),
        "squiggle" to context.getString(R.string.shape_squiggle),
        "fish" to context.getString(R.string.shape_fish),
        "arms" to context.getString(R.string.shape_arms),
        "blob" to context.getString(R.string.shape_blob),
        "upright" to context.getString(R.string.shape_upright),
        "legs" to context.getString(R.string.shape_legs),
        "quadruped" to context.getString(R.string.shape_quadruped),
        "wings" to context.getString(R.string.shape_wings),
        "tentacles" to context.getString(R.string.shape_tentacles),
        "heads" to context.getString(R.string.shape_heads),
        "humanoid" to context.getString(R.string.shape_humanoid),
        "bug-wings" to context.getString(R.string.shape_bug_wings),
        "armor" to context.getString(R.string.shape_armor)
    )
    return shapeMap[this.lowercase()] ?: context.getString(R.string.unknow)
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
        "normal" to R.drawable.normal,
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
        "normal" to Color(0xFFA8A878),
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
        "normal" to context.getString(R.string.normal),
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

/**
 * Retorna a cor da Toolbar baseada na string e no modo (dark ou light).
 */
fun String.getToolBarColorByString(isDark: Boolean): Color {
    return when (this.lowercase()) {
        "black"  -> if (isDark) DarkBlack else LightBlack
        "blue"   -> if (isDark) DarkBlue else LightBlue
        "brown"  -> if (isDark) DarkBrown else LightBrown
        "gray"   -> if (isDark) DarkGray else LightGray
        "green"  -> if (isDark) DarkGreen else LightGreen
        "pink"   -> if (isDark) DarkPink else LightPink
        "purple" -> if (isDark) DarkPurple else LightPurple
        "red"    -> if (isDark) DarkRed else LightRed
        "white"  -> if (isDark) DarkWhite else LightWhite
        "yellow" -> if (isDark) DarkYellow else LightYellow
        else       -> if (isDark) DarkText else LightText
    }
}

/**
 * Retorna a cor de elementos gerais baseada na string e no modo (dark ou light).
 */
fun String.getColorByString(isDark: Boolean): Color {
    return when (this.lowercase()) {
        "black"  -> if (isDark) DarkBlack else LightBlack
        "blue"   -> if (isDark) DarkBlue else LightBlue
        "brown"  -> if (isDark) DarkBrown else LightBrown
        "gray"   -> if (isDark) DarkGray else LightGray
        "green"  -> if (isDark) DarkGreen else LightGreen
        "pink"   -> if (isDark) DarkPink else LightPink
        "purple" -> if (isDark) DarkPurple else LightPurple
        "red"    -> if (isDark) DarkRed else LightRed
        "white"  -> if (isDark) DarkWhite else LightWhite
        "yellow" -> if (isDark) DarkYellow else LightYellow
        else       -> if (isDark) DarkOnSurface else LightOnSurface
    }
}

/**
 * Traduz esta String do inglês para o idioma passado, se for suportado.
 * Requer que o modelo já tenha sido baixado previamente (ex: na MainActivity).
 *
 * @param languageCode Código do idioma de destino (ex.: "pt", "es", "fr", "hi" e etc...)
 * @param onResult Resultado da tradução, ou o texto original se idioma não suportado
 * @param onError Callback de erro, se a tradução falhar
 */
fun String.formatLocationName(): String =
    replace("-", " ")
        .split(" ")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

fun String.formatVersionName(): String = when (this.lowercase()) {
    "firered"         -> "FireRed"
    "leafgreen"       -> "LeafGreen"
    "heartgold"       -> "HeartGold"
    "soulsilver"      -> "SoulSilver"
    "black-2"         -> "Black 2"
    "white-2"         -> "White 2"
    "omega-ruby"      -> "Omega Ruby"
    "alpha-sapphire"  -> "Alpha Sapphire"
    "ultra-sun"       -> "Ultra Sun"
    "ultra-moon"      -> "Ultra Moon"
    "lets-go-pikachu" -> "Let's Go Pikachu"
    "lets-go-eevee"   -> "Let's Go Eevee"
    else -> replace("-", " ").split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
}

fun String.getVersionColor(): Color = when (this.lowercase()) {
    "red"             -> Color(0xFFCC0000)
    "blue"            -> Color(0xFF1565C0)
    "yellow"          -> Color(0xFFF57F17)
    "gold"            -> Color(0xFFFF8F00)
    "silver"          -> Color(0xFF607D8B)
    "crystal"         -> Color(0xFF0288D1)
    "ruby"            -> Color(0xFFD32F2F)
    "sapphire"        -> Color(0xFF1565C0)
    "emerald"         -> Color(0xFF2E7D32)
    "firered"         -> Color(0xFFE64A19)
    "leafgreen"       -> Color(0xFF388E3C)
    "diamond"         -> Color(0xFF1976D2)
    "pearl"           -> Color(0xFFC2185B)
    "platinum"        -> Color(0xFF546E7A)
    "heartgold"       -> Color(0xFFFF8F00)
    "soulsilver"      -> Color(0xFF546E7A)
    "black"           -> Color(0xFF37474F)
    "white"           -> Color(0xFF78909C)
    "black-2"         -> Color(0xFF37474F)
    "white-2"         -> Color(0xFF78909C)
    "x"               -> Color(0xFF1565C0)
    "y"               -> Color(0xFFC62828)
    "omega-ruby"      -> Color(0xFFBF360C)
    "alpha-sapphire"  -> Color(0xFF0D47A1)
    "sun"             -> Color(0xFFEF6C00)
    "moon"            -> Color(0xFF283593)
    "ultra-sun"       -> Color(0xFFE65100)
    "ultra-moon"      -> Color(0xFF1A237E)
    "lets-go-pikachu" -> Color(0xFFF57F17)
    "lets-go-eevee"   -> Color(0xFF6D4C41)
    "sword"           -> Color(0xFF1565C0)
    "shield"          -> Color(0xFF6A1B9A)
    "scarlet"         -> Color(0xFFB71C1C)
    "violet"          -> Color(0xFF4A148C)
    else              -> Color(0xFF607D8B)
}

fun String.translateIfSupported(
    languageCode: String = Locale.getDefault().language,
    onResult: (String) -> Unit,
    onError: (Exception) -> Unit,
    context: Context
) {

    val supportedLanguages = context.resources.getStringArray(
        R.array.supported_languages
    ).toSet()

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
