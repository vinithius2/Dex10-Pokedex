package com.vinithius.dex10.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonVariant {
    Solid, Outline, Text
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Solid,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = if (variant == ButtonVariant.Solid) Color.White else containerColor,
    enabled: Boolean = true,
    fontSize: Int = 16,
    icon: (@Composable () -> Unit)? = null
) {
    when (variant) {
        ButtonVariant.Solid -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                ButtonContent(text, fontSize, icon)
            }
        }
        ButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = enabled,
                border = BorderStroke(2.dp, containerColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor
                )
            ) {
                ButtonContent(text, fontSize, icon)
            }
        }
        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor
                )
            ) {
                ButtonContent(text, fontSize, icon)
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, fontSize: Int, icon: (@Composable () -> Unit)?) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}
