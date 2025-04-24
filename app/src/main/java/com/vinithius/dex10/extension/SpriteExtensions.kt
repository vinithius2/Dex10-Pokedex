package com.vinithius.dex10.extension

import com.vinithius.dex10.R
import android.content.Context
import com.vinithius.dex10.datasource.response.BaseSprite
import com.vinithius.dex10.datasource.response.Pokemon
import com.vinithius.dex10.datasource.response.Sprites


// Data class para armazenar título e URL da sprite
data class SpriteItem(
    val title: String,
    val url: String
)

// Extensão para BaseSprite
fun BaseSprite?.toSpriteItemList(context: Context): List<SpriteItem> {
    if (this == null) return emptyList()

    val list = mutableListOf<SpriteItem>()

    this.front_default?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_default), it))
    }
    this.back_default?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_default), it))
    }
    this.front_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_female), it))
    }
    this.back_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_female), it))
    }
    this.front_shiny?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_shiny), it))
    }
    this.back_shiny?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_shiny), it))
    }
    this.front_shiny_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_shiny_female), it))
    }
    this.back_shiny_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_shiny_female), it))
    }

    return list
}

// Extensão para Sprites
fun Sprites?.toSpriteItemList(context: Context): List<SpriteItem> {
    if (this == null) return emptyList()

    val list = mutableListOf<SpriteItem>()

    // Sprites principais
    this.front_default?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_default), it))
    }
    this.back_default?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_default), it))
    }
    this.front_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_female), it))
    }
    this.back_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_female), it))
    }
    this.front_shiny?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_shiny), it))
    }
    this.back_shiny?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_shiny), it))
    }
    this.front_shiny_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_front_shiny_female), it))
    }
    this.back_shiny_female?.let {
        list.add(SpriteItem(context.getString(R.string.title_back_shiny_female), it))
    }

    // Sprites de "other" (Dream World, Home, Official Artwork, Showdown)
    this.other?.let { other ->
        other.dream_world?.toSpriteItemList(context)?.let {
            list.addAll(it)
        }
        other.home?.toSpriteItemList(context)?.let {
            list.addAll(it)
        }
        other.official_artwork?.toSpriteItemList(context)?.let {
            list.addAll(it)
        }
        other.showdown?.toSpriteItemList(context)?.let {
            list.addAll(it)
        }
    }

    return list
}

// Extensão para Pokemon
fun Pokemon.getSpriteItems(context: Context): List<SpriteItem> {
    return this.sprites.toSpriteItemList(context)
}
