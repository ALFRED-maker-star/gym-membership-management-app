package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = Charcoal900,
    primaryContainer = Charcoal800,
    onPrimaryContainer = ElectricLimeLight,
    secondary = ElectricLime,
    onSecondary = Charcoal900,
    background = Charcoal900,
    onBackground = PureWhite,
    surface = Charcoal800,
    onSurface = PureWhite,
    surfaceVariant = Charcoal700,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    error = DangerRed,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Charcoal900,
    onPrimary = PureWhite,
    primaryContainer = Charcoal800,
    onPrimaryContainer = ElectricLimeLight,
    secondary = ElectricLimeDark,
    onSecondary = PureWhite,
    secondaryContainer = ElectricLimeSoft,
    onSecondaryContainer = Charcoal900,
    background = Slate50, // #F9FAFB
    onBackground = Charcoal900,
    surface = PureWhite,
    onSurface = Charcoal900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300,
    error = DangerRed,
    onError = PureWhite
)

@Composable
fun FitAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
