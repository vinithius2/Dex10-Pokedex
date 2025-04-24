package com.vinithius.dex10.extension

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import androidx.palette.graphics.Palette
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.vinithius.dex10.R
import com.vinithius.dex10.datasource.response.Pokemon

private const val urlSprite =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
private const val urlArtwork =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
private const val fileType = ".png"

/**
 * Set pokemon image in imageView.
 */
fun ImageView.setPokemonImage(pokemon: Pokemon, invert: Boolean = false) {
    val pokemonId = pokemon.id ?: pokemon.url?.getIdIntoUrl()
    val urlSprite = "$urlSprite${pokemonId}$fileType"
    val urlArtwork = "$urlArtwork${pokemonId}$fileType"
    Picasso.get()
        .load(if (invert) urlArtwork else urlSprite)
        .into(this, object : Callback {

            override fun onSuccess() {
                // Nothing
            }

            override fun onError(e: Exception?) {
                Picasso.get()
                    .load(if (invert) urlSprite else urlArtwork)
                    .error(R.drawable.ic_error_image)
                    .into(this@setPokemonImage)
            }

        })
}

/**
 * Set color in background is GradientDrawable View.
 */
fun View.setColorBackgroundGradientDrawable(color: Palette.Swatch?) {
    val background = this.background
    if (background is GradientDrawable) {
        color?.let {
            background.setColor(it.rgb)
        }
    }
}
