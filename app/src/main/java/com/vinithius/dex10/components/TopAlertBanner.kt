package com.vinithius.dex10.components

import AlertMessage
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.R
import com.vinithius.dex10.extension.getVersionCode
import java.util.Locale

@Composable
fun TopAlertBanner(
    alert: AlertMessage?,
    context: Context,
    modifier: Modifier = Modifier,
    onButtonClick: (String) -> Unit,
    onClose: () -> Unit
) {
    if (alert?.show != true) return

    val appVersionCode = context.getVersionCode()

    val shouldShow = when {
        alert.version_code == null -> true
        alert.version_code > appVersionCode -> true
        else -> false
    }

    if (!shouldShow) return

    // Obtem o idioma atual
    val languageCode = Locale.getDefault().language
    val localized = alert.getLocalizedContent(languageCode)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = localized.title,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onClose() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = localized.message,
                    style = MaterialTheme.typography.subtitle1
                )

                if (!localized.title_button.isNullOrBlank() && !alert.url_action.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onButtonClick(alert.url_action!!) },
                        colors = ButtonDefaults.buttonColors(Color.DarkGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = localized.title_button,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

