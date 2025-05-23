package com.vinithius.dex10.admobbanners

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.vinithius.dex10.BuildConfig

@Composable
fun AdAdvancedNative(
    modifier: Modifier = Modifier.fillMaxWidth(),
    adUnitIdTest: String = "ca-app-pub-3940256099942544/2247696110",
    adUnitIdProd: String?,
    isTablet: Boolean = false
) {
    val ctx = LocalContext.current
    val adUnitId = if (BuildConfig.DEBUG) adUnitIdTest else adUnitIdProd.orEmpty()
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(adUnitId) {
        if (adUnitId.isNotBlank()) {
            AdLoader.Builder(ctx, adUnitId)
                .forNativeAd { ad ->
                    nativeAd?.destroy()
                    nativeAd = ad
                }
                .build()
                .loadAd(AdRequest.Builder().build())
        }
    }

    nativeAd?.let { ad ->
        AndroidView(
            modifier = modifier,
            factory = { context ->
                if (isTablet) {
                    createNativeAdViewForTablet(context, ad)
                } else {
                    createNativeAdView(context, ad)
                }
            }
        )
    }
}

/**
 * For smartphones and small screens
 */
fun createNativeAdView(context: Context, ad: NativeAd): NativeAdView {
    val density = context.resources.displayMetrics.density
    val mediaHeight = (120 * density).toInt()
    val iconSize = (32 * density).toInt()
    val padding = (12 * density).toInt()
    val ctaHeight = (48 * density).toInt()

    // 1) NativeAdView raiz
    val adView = NativeAdView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // 2) Container vertical para tudo
    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(padding, padding, padding, padding)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // 3) MediaView (topo)
    val mediaView = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            mediaHeight
        )
        ad.mediaContent?.let { mediaContent = it }
    }
    adView.mediaView = mediaView
    root.addView(mediaView)

    // 4) Row com ícone + textos
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = (8 * density).toInt()
        }
    }

    // 4.1) Ícone
    ad.icon?.drawable?.let { drawable ->
        val iv = ImageView(context).apply {
            setImageDrawable(drawable)
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
        }
        adView.iconView = iv
        row.addView(iv)
    }

    // 4.2) Coluna de textos
    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            marginStart = (8 * density).toInt()
        }
    }

    ad.headline?.let {
        TextView(context).apply {
            text = it
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }.also { tv ->
            adView.headlineView = tv
            textCol.addView(tv)
        }
    }

    ad.advertiser?.let {
        TextView(context).apply {
            text = it
            textSize = 12f
        }.also { tv ->
            adView.advertiserView = tv
            textCol.addView(tv)
        }
    }

    ad.body?.let {
        TextView(context).apply {
            text = it
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (4 * density).toInt()
            }
        }.also { tv ->
            adView.bodyView = tv
            textCol.addView(tv)
        }
    }

    row.addView(textCol)
    root.addView(row)

    // 5) Botão CTA (abaixo)
    ad.callToAction?.let { cta ->
        Button(context).apply {
            text = cta
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ctaHeight
            ).apply {
                topMargin = (8 * density).toInt()
            }
        }.also { btn ->
            adView.callToActionView = btn
            root.addView(btn)
        }
    }

    // 6) Adiciona tudo no adView
    adView.addView(root)
    adView.setNativeAd(ad)
    return adView
}

/**
 * Cria um NativeAdView ajustado para tablets e telas grandes
 */
fun createNativeAdViewForTablet(context: Context, ad: NativeAd): NativeAdView {
    val density = context.resources.displayMetrics.density
    val iconSize = (32 * density).toInt()     // Tamanho do ícone
    val padding = (12 * density).toInt()      // Padding geral
    val ctaHeight = (48 * density).toInt()    // Altura do botão CTA
    val minSizePx = (120 * density).toInt()   // Tamanho mínimo de 120dp em pixels

    // 1) NativeAdView raiz
    val adView = NativeAdView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // 2) Root layout: horizontal para tablets
    val root = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(padding, padding, padding, padding)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // 3) MediaView (à esquerda)
    val mediaView = MediaView(context).apply {
        // Calcula a largura desejada (40% da tela) e garante o mínimo de 120dp
        val screenWidthPx = context.resources.displayMetrics.widthPixels
        val desiredWidthPx = (screenWidthPx * 0.4).toInt()
        val finalWidthPx = maxOf(desiredWidthPx, minSizePx)
        layoutParams = LinearLayout.LayoutParams(
            finalWidthPx,
            minSizePx  // Altura fixa de pelo menos 120dp
        )
        ad.mediaContent?.let { mediaContent = it }
    }
    adView.mediaView = mediaView
    root.addView(mediaView)

    // 4) Container para o conteúdo (à direita)
    val contentContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f // Ocupa o espaço restante
        )
    }

    // 5) Row com ícone + textos
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = (8 * density).toInt()
        }
    }

    // 5.1) Ícone
    ad.icon?.drawable?.let { drawable ->
        val iv = ImageView(context).apply {
            setImageDrawable(drawable)
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
        }
        adView.iconView = iv
        row.addView(iv)
    }

    // 5.2) Coluna de textos
    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            marginStart = (8 * density).toInt()
        }
    }

    ad.headline?.let {
        TextView(context).apply {
            text = it
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }.also { tv ->
            adView.headlineView = tv
            textCol.addView(tv)
        }
    }

    ad.advertiser?.let {
        TextView(context).apply {
            text = it
            textSize = 12f
        }.also { tv ->
            adView.advertiserView = tv
            textCol.addView(tv)
        }
    }

    ad.body?.let {
        TextView(context).apply {
            text = it
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (4 * density).toInt()
            }
        }.also { tv ->
            adView.bodyView = tv
            textCol.addView(tv)
        }
    }

    row.addView(textCol)
    contentContainer.addView(row)

    // 6) Botão CTA (abaixo dos textos)
    ad.callToAction?.let { cta ->
        Button(context).apply {
            text = cta
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ctaHeight
            ).apply {
                topMargin = (8 * density).toInt()
            }
        }.also { btn ->
            adView.callToActionView = btn
            contentContainer.addView(btn)
        }
    }

    // 7) Adiciona o contentContainer ao root
    root.addView(contentContainer)

    // 8) Adiciona o root ao adView
    adView.addView(root)
    adView.setNativeAd(ad)
    return adView
}

@Preview(showBackground = true)
@Composable
fun PokemonListScreenPreview() {
    val ctx = LocalContext.current
    val adUnitId = "ca-app-pub-3940256099942544/2247696110"
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var modifier: Modifier = Modifier.fillMaxWidth()

    LaunchedEffect(adUnitId) {
        if (adUnitId.isNotBlank()) {
            AdLoader.Builder(ctx, adUnitId)
                .forNativeAd { ad ->
                    nativeAd?.destroy()
                    nativeAd = ad
                }
                .build()
                .loadAd(AdRequest.Builder().build())
        }
    }

    nativeAd?.let { ad ->
        AndroidView(
            modifier = modifier,
            factory = { context -> createNativeAdView(context, ad) }
        )
    }
}
