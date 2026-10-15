package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FugaDarkColorScheme = darkColorScheme(
    primary = FugaPurple,
    onPrimary = Color.White,
    primaryContainer = FugaViolet,
    onPrimaryContainer = Color.White,
    secondary = FugaCyan,
    onSecondary = SpaceBlack,
    secondaryContainer = SpaceCard,
    onSecondaryContainer = FugaNeonCyan,
    tertiary = FugaPink,
    onTertiary = Color.White,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SpaceBorder,
    error = FugaError,
    onError = Color.White
)

@Composable
fun FugaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Fuga AI always features its signature futuristic dark cosmic aesthetic
    MaterialTheme(
        colorScheme = FugaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
