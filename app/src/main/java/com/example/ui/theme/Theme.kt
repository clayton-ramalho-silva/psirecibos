package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB59D),
    onPrimary = Color(0xFF5D1905),
    primaryContainer = Color(0xFF7E2B16),
    onPrimaryContainer = Color(0xFFFFDBD0),
    secondary = Color(0xFFB1CCC0),
    onSecondary = Color(0xFF1B352B),
    secondaryContainer = Color(0xFF324B40),
    onSecondaryContainer = Color(0xFFCDE8DC),
    background = Color(0xFF1A1817),
    surface = Color(0xFF23201F),
    onBackground = Color(0xFFECE0DB),
    onSurface = Color(0xFFECE0DB),
    surfaceVariant = Color(0xFF312C2A),
    onSurfaceVariant = Color(0xFFD7C2B9),
    outline = Color(0xFF9E8D86)
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = SageSecondary,
    onSecondary = Color.White,
    secondaryContainer = SageContainer,
    onSecondaryContainer = SageOnContainer,
    background = WarmBackground,
    surface = WarmSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = WarmOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand terracotta palette consistently
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
