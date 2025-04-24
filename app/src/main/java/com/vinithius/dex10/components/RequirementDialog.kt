package com.vinithius.dex10.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vinithius.dex10.R

@Composable
fun AdRequirementDialog(
    onDismiss: () -> Unit,
    onDismissButton: () -> Unit,
    onConfirm: () -> Unit,
    dontShowAgain: Boolean,
    onDontShowAgainChanged: (Boolean) -> Unit,
    title: String = stringResource(R.string.know_this_first),
    message: String = stringResource(R.string.ad_message),
    dontShowAgainLabel: String = stringResource(R.string.dont_show_again),
    confirmButtonText: String = stringResource(R.string.ok),
    dismissButtonText: String? = null
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = {
            Text(text = title)
        },
        text = {
            Column {
                Text(
                    text = message
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { onDontShowAgainChanged(it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = dontShowAgainLabel)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                enabled = dontShowAgain
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            dismissButtonText?.run {
                TextButton(
                    onClick = onDismissButton
                ) {
                    Text(text = this@run)
                }
            }
        }
    )
}
