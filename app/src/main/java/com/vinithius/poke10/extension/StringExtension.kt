package com.vinithius.poke10.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.Coil
import coil.request.ImageRequest
import com.squareup.picasso.Picasso
import com.vinithius.poke10.R
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
        return null
    }
    return null
}

/*
/**
 * Get if the pokemon is favorite or not, if null, returns false.
 */
fun String.getIsFavorite(context: Context): Boolean {
    val sharedPref = context.getSharedPreferences(
        PokemonListAdapter.FAVORITES,
        Context.MODE_PRIVATE
    )
    return sharedPref.getBoolean(this, false)
}
*/
fun String.getDominantColorPalette(
    context: Context,
    onResult: (HashMap<String, Palette.Swatch?>) -> Unit
) {
    val hashMapColor = hashMapOf<String, Palette.Swatch?>()

    // Cria a requisição para carregar a imagem
    val request = ImageRequest.Builder(context)
        .data(this) // URL da imagem
        .target { drawable ->
            // Conversão para BitmapDrawable
            val bitmap = (drawable as? BitmapDrawable)?.bitmap

            // Processa o Bitmap com a Palette para extrair as cores
            bitmap?.let { bmp ->
                val palette = Palette.from(bmp).generate()
                val light = palette.lightVibrantSwatch ?: palette.lightMutedSwatch
                val dominant = palette.dominantSwatch ?: light
                hashMapColor["dominant"] = dominant
                hashMapColor["light"] = light

                // Retorna o resultado usando o Callback
                onResult(hashMapColor)
            }
        }
        .build()

    // Executa a requisição de imagem usando Coil
    Coil.imageLoader(context).enqueue(request)
}

/**
 * Get dominant color Pallete.
 */
fun String.getDominantColorPalleteq(): HashMap<String, Palette.Swatch?> {
    val hashMapColor = hashMapOf<String, Palette.Swatch?>()
    Picasso.get().load(this).into(object : com.squareup.picasso.Target {

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            bitmap?.let { bitmap_to_palette ->
                val palette = Palette.from(bitmap_to_palette).generate()
                val light = palette.lightVibrantSwatch ?: palette.lightMutedSwatch
                val dominant = palette.dominantSwatch ?: light
                hashMapColor["dominant"] = dominant
                hashMapColor["light"] = light
            }
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
    })
    return hashMapColor
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
        Color.Black
    }
}
