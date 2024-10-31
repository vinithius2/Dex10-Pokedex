package com.vinithius.poke10.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vinithius.poke10.R
//import com.vinithius.poke10.extension.getIsFavorite

@SuppressLint("StaticFieldLeak")
private lateinit var mContext: Context

@Composable
fun PokeballComponent(
    context: Context,
    pokemonName: String,
    modifier: Modifier = Modifier
) {
    /*
    mContext = context
    var isFavorite by remember { mutableStateOf(pokemonName.getIsFavorite(context)) }
    var imageResource by remember {
        mutableStateOf(
            if (isFavorite) R.drawable.pokeball_01
            else R.drawable.pokeball_03_gray
        )
    }

    // Animação simples de "fade" na transição entre os estados de favorito e não favorito
    val alpha by animateFloatAsState(
        targetValue = if (isFavorite) 1f else 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(700)
        )
    )
    /*
    // Configura e atualiza a preferência do usuário
    fun setPreferences(name: String, value: Boolean) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(PokemonListAdapter.FAVORITES, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(name, value)
            commit()
        }
    }
    */
    // Alterna entre as imagens da pokebola e realiza a animação
    fun setAnimation() {
        val draw = if (isFavorite) R.drawable.animation_click_off else R.drawable.animation_click_on
        imageResource = draw
        val frameAnimation = ContextCompat.getDrawable(context, draw) as? AnimationDrawable
        frameAnimation?.start()
    }
    /*
    // Configura a imagem e executa o clique
    Image(
        painter = painterResource(id = imageResource),
        contentDescription = "Pokeball Image",
        modifier = modifier
            .size(100.dp)
            .alpha(alpha)
            .clickable {
                isFavorite = isFavorite.not()
                setPreferences(pokemonName, isFavorite)
                setAnimation()
            }
    )
    */
    */
}

@Preview(showBackground = true)
@Composable
fun PokeballComponentPreview() {
    // Context is required; replace it with a preview context if needed
    // Here it is just for example purposes
    PokeballComponent(context = mContext, pokemonName = "Pikachu")
}
