package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ZenForestPrimary,
    secondary = ZenSageSecondary,
    tertiary = ZenMintTertiary,
    background = ZenBackgroundDark,
    surface = ZenSurfaceDark,
    surfaceVariant = ZenSurfaceVariantDark,
    onPrimary = ZenOnPrimary,
    onBackground = ZenOnBackground,
    onSurface = ZenOnSurface,
    onSurfaceVariant = ZenOnBackground,
    error = ZenError
)

private val LightColorScheme = lightColorScheme(
    primary = ZenPrimaryLight,
    secondary = ZenSecondaryLight,
    tertiary = ZenMintTertiary,
    background = ZenBackgroundLight,
    surface = ZenSurfaceLight,
    onPrimary = ZenOnBackgroundLight,
    onBackground = ZenOnBackgroundLight,
    onSurface = ZenOnSurfaceLight,
    error = ZenError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally disable dynamic system colors to preserve the custom hand-crafted Zen aesthetics
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
