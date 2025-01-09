package com.vinithius.poke10.extension

import android.text.Spanned
import androidx.core.text.HtmlCompat
import com.vinithius.poke10.datasource.response.FlavorText

/**
 * Transform entries in HTML format.
 */
/*
fun List<FlavorText>.getHtmlCompat(): Spanned {
    val entries = this.filter { it.language.name == PokemonDetailScreen.EN }
        .groupBy { it.flavor_text }
    var output = ""
    for ((value, versions) in entries) {
        versions.forEach {
            output += " <b>${it.version.name.capitalize()}</b> |"
        }
        output.last().toString().takeIf { it == "|" }?.apply {
            output = output.dropLast(2)
        }
        output += "<p>$value</p>"
    }
    return HtmlCompat.fromHtml(output, HtmlCompat.FROM_HTML_MODE_LEGACY)
}
*/