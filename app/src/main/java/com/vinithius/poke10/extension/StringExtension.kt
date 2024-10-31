package com.vinithius.poke10.extension

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import com.squareup.picasso.Picasso

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
/**
 * Get dominant color Pallete.
 */
fun String.getDominantColorPallete(): HashMap<String, Palette.Swatch?> {
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

/**
 * Get drawable by name in android resource and put image in ImageView.
 */
fun String.setDrawableIco(context: Context, image: ImageView) {
    try {
        val resource = context.resources
        val drawableRes = resource.getIdentifier(this, "drawable", context.packageName)
        val drawable = ResourcesCompat.getDrawable(resource, drawableRes, null)
        image.setImageDrawable(drawable)
    } catch (e: Resources.NotFoundException) {
        Log.e("ERROR Ico type", "No type image")
    }
}
