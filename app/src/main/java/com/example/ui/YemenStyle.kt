package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * YemenStyle StyleSheet object
 * Central CSS stylesheet-style definitions layer.
 */
object YemenStyle {
    val marginSmall: Dp = 8.dp
    val marginMedium: Dp = 16.dp
    val marginLarge: Dp = 24.dp
    val marginExtraLarge: Dp = 32.dp

    val radiusSmall = RoundedCornerShape(8.dp)
    val radiusMedium = RoundedCornerShape(16.dp)
    val radiusLarge = RoundedCornerShape(24.dp)
    val radiusPill = RoundedCornerShape(50.dp)

    fun Modifier.responsiveContainer(): Modifier = this.composed {
        this.padding(horizontal = 16.dp, vertical = 12.dp)
            .widthIn(max = 600.dp)
    }

    fun Modifier.yemenPremiumCard(): Modifier = this.composed {
        this.shadow(2.dp, radiusMedium)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = radiusMedium
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                ),
                shape = radiusMedium
            )
            .padding(marginMedium)
    }
}
