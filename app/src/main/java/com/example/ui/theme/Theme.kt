package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Cosmic Slate theme (🌌 كوزميك سيلفر)
val CosmicSlatePrimary = Color(0xFFD8E2DC)
val CosmicSlateSecondary = Color(0xFF9E9E9E)
val CosmicSlateBg = Color(0xFF121824)
val CosmicSlateSurface = Color(0xFF1F2937)

// 2. Charcoal Gold theme (✨ الذهبي الفاخر)
val CharcoalGoldPrimary = Color(0xFFFFD700) // Gold
val CharcoalGoldSecondary = Color(0xFFC5B358)
val CharcoalGoldBg = Color(0xFF0F0F10) // Dark Charcoal
val CharcoalGoldSurface = Color(0xFF1C1D21)

// 3. Royal Emerald theme (🟢 الزمردي الراقي)
val RoyalEmeraldPrimary = Color(0xFF2ECC71) // Emerald
val RoyalEmeraldSecondary = Color(0xFF27AE60)
val RoyalEmeraldBg = Color(0xFF0D1E15)
val RoyalEmeraldSurface = Color(0xFF173022)

// Custom colors request
val SmokyBlack = Color(0xFF0A0F0D)
val LightPink = Color(0xFFFFE3E1)
val GoldWhite = Color(0xFFF9F6F0)

@Composable
fun MyApplicationTheme(
    themeName: String = "🌌 كوزميك سيلفر",
    content: @Composable () -> Unit
) {
    // Determine color schemes dynamically
    val colorScheme = when {
        themeName.contains("الذهبي") -> {
            darkColorScheme(
                primary = CharcoalGoldPrimary,
                secondary = CharcoalGoldSecondary,
                background = CharcoalGoldBg,
                surface = CharcoalGoldSurface,
                onPrimary = Color.Black,
                onBackground = Color(0xFFF9F6F0),
                onSurface = Color(0xFFF9F6F0)
            )
        }
        themeName.contains("الزمرد") -> {
            darkColorScheme(
                primary = RoyalEmeraldPrimary,
                secondary = RoyalEmeraldSecondary,
                background = RoyalEmeraldBg,
                surface = RoyalEmeraldSurface,
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
        }
        themeName.contains("الأسود الدخاني") || themeName.contains("الدخاني") -> {
            darkColorScheme(
                primary = Color(0xFFE0E0E0),
                secondary = Color(0xFFB0B0B0),
                background = SmokyBlack,
                surface = Color(0xFF1E1E1E),
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
        }
        themeName.contains("الزهري") || themeName.contains("وردي") -> {
            lightColorScheme(
                primary = Color(0xFFE8949B),
                secondary = Color(0xFFDDA6A9),
                background = LightPink,
                surface = Color(0xFFFFF0F0),
                onPrimary = Color.White,
                onBackground = Color(0xFF2C1B1B),
                onSurface = Color(0xFF2C1B1B)
            )
        }
        themeName.contains("الأبيض الذهبي") || themeName.contains("ذهبي فاتح") -> {
            lightColorScheme(
                primary = Color(0xFFC5B358),
                secondary = Color(0xFFA67C00),
                background = GoldWhite,
                surface = Color.White,
                onPrimary = Color.White,
                onBackground = Color(0xFF333333),
                onSurface = Color(0xFF333333)
            )
        }
        else -> { // Default is Cosmic Slate
            darkColorScheme(
                primary = CosmicSlatePrimary,
                secondary = CosmicSlateSecondary,
                background = CosmicSlateBg,
                surface = CosmicSlateSurface,
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
