package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MansourLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldAccent,
    onSecondary = Color(0xFF332500),
    secondaryContainer = GoldContainer,
    onSecondaryContainer = Color(0xFF4A3700),
    tertiary = EmeraldDark,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFC2D0CB)
)

val MansourDarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color.White,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = Color(0xFFA6E8D5),
    secondary = GoldAccent,
    onSecondary = Color(0xFF281C00),
    secondaryContainer = Color(0xFF483713),
    onSecondaryContainer = Color(0xFFFFE09C),
    tertiary = EmeraldPrimary,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF3B4A44)
)

val MansourSepiaColorScheme = lightColorScheme(
    primary = SepiaAccent,
    onPrimary = Color.White,
    primaryContainer = SepiaSurface,
    onPrimaryContainer = SepiaTextPrimary,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = SepiaCard,
    onSecondaryContainer = SepiaTextPrimary,
    tertiary = EmeraldPrimary,
    onTertiary = Color.White,
    background = SepiaBackground,
    onBackground = SepiaTextPrimary,
    surface = SepiaCard,
    onSurface = SepiaTextPrimary,
    surfaceVariant = SepiaSurface,
    onSurfaceVariant = SepiaTextSecondary,
    outline = Color(0xFFD6C8B5)
)

enum class AppThemeMode {
    LIGHT,
    DARK,
    SEPIA,
    SYSTEM
}

@Composable
fun MansourHadithTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> MansourLightColorScheme
        AppThemeMode.DARK -> MansourDarkColorScheme
        AppThemeMode.SEPIA -> MansourSepiaColorScheme
        AppThemeMode.SYSTEM -> if (systemInDark) MansourDarkColorScheme else MansourLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
