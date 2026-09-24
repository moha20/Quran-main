package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val titleArabic: String, val titleEnglish: String) {
    LIGHT("الوضع الفاتح ☀️", "Light"),
    DARK("الوضع الداكن 🌙", "Dark"),
    SYSTEM("تلقائي (حسب النظام) ⚙️", "System")
}

private val DarkColorScheme = darkColorScheme(
    primary = IslamicEmeraldLight,
    onPrimary = IslamicEmeraldDark,
    primaryContainer = IslamicEmeraldPrimary,
    onPrimaryContainer = IslamicEmeraldContainer,
    secondary = QuranGoldLight,
    onSecondary = QuranGoldDark,
    secondaryContainer = QuranGoldDark,
    onSecondaryContainer = QuranGoldLight,
    tertiary = IslamicEmeraldMedium,
    onTertiary = Color.White,
    background = NightBgDark,
    onBackground = NightTextPrimary,
    surface = NightSurfaceDark,
    onSurface = NightTextPrimary,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextSecondary,
    outline = Color(0xFF2C4A3E),
    outlineVariant = Color(0xFF1C362C)
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = IslamicEmeraldDark,
    secondary = QuranGoldDark,
    onSecondary = Color.White,
    secondaryContainer = QuranGoldContainer,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = IslamicEmeraldMedium,
    onTertiary = Color.White,
    background = ParchmentBgLight,
    onBackground = ParchmentTextPrimary,
    surface = ParchmentSurfaceLight,
    onSurface = ParchmentTextPrimary,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = ParchmentTextSecondary,
    outline = ParchmentBorderLight,
    outlineVariant = Color(0xFFECE4D6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Islamic emerald & gold branding consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
