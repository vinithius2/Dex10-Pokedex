package com.vinithius.dex10.extension

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

fun Int.converterIntToDouble(): Double {
    return this.toDouble().div(10)
}

fun Int.convertPounds(): Double {
    val value = this.toDouble().div(10)
    return value.times(2.20462262)
}

fun Int.convertInch(): Double {
    val value = this.toDouble().div(10)
    return 39.370 * value
}

fun Int.getDominantColorFromDrawableRes(context: Context): Int? {
    val bitmap = BitmapFactory.decodeResource(context.resources, this)
    val palette = Palette.from(bitmap).generate()
    return palette.dominantSwatch?.rgb
}

fun Int.getDominantColorFromDrawableResWithAlpha(context: Context, alphaPercentage: Float): Int? {
    return getDominantColorFromDrawableRes(context)?.let {
        val alpha = (alphaPercentage * 255).toInt()
        ColorUtils.setAlphaComponent(it, alpha)
    }
}
