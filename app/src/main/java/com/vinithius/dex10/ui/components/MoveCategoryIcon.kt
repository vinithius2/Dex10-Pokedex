package com.vinithius.dex10.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Display move category icon (Physical, Special, Status)
 * Based on competitive Pokemon UI standards
 */
@Composable
fun MoveCategoryIcon(
    category: String,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (category.lowercase()) {
        "physical" -> Color(0xFFE74C3C) to "🔴" // Red circle
        "special" -> Color(0xFF3498DB) to "🔵"   // Blue circle
        "status" -> Color(0xFF95A5A6) to "⚪"     // Gray circle
        else -> Color.Gray to "?"
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // We could use material icons or custom icons here
        // For now, using a simple colored circle is enough
    }
}
