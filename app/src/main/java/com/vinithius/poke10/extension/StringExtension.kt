package com.vinithius.poke10.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.drawable.MovieDrawable
import com.squareup.picasso.Picasso
import com.vinithius.poke10.R

import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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
fun String.getDominantColorPalette(context: Context, onResult: (HashMap<String, Palette.Swatch?>) -> Unit) {
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
