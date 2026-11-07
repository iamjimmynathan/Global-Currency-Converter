package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangePrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = KeypadMediumGrey,
    onSecondary = Color.White,
    tertiary = LiveRateGreen,
    background = CharcoalDeepBlack,
    onBackground = TextWhitePrimary,
    surface = CharcoalSecondaryBg,
    onSurface = TextWhitePrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextLightGreySecondary,
    outline = CharcoalDivider,
    outlineVariant = Color(0xFF1E2028)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangePrimaryLight,
    onPrimaryContainer = Color.Black,
    secondary = LightKeypadNum,
    onSecondary = LightKeypadNumText,
    tertiary = LiveRateGreen,
    background = LightBackground,
    onBackground = Color(0xFF111317),
    surface = LightSurface,
    onSurface = Color(0xFF111317),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF555A67),
    outline = LightDivider,
    outlineVariant = Color(0xFFE5E7EB)
)

@Composable
fun CurrencyConverterTheme(
    themePreference: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
