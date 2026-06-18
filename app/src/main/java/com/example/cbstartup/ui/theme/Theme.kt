package com.example.cbstartup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Blue600,
    secondary = Blue700,
    tertiary = Success,
    background = Ink900,
    surface = Ink800,
    onPrimary = Paper,
    onSecondary = Paper,
    onBackground = Paper,
    onSurface = Paper,
    outline = Gray500
)

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    secondary = Blue700,
    tertiary = Success,
    background = PaperMuted,
    surface = Paper,
    surfaceVariant = Color(0xFFEAF1FB),
    onPrimary = Paper,
    onSecondary = Paper,
    onBackground = Ink900,
    onSurface = Ink900,
    onSurfaceVariant = Gray500,
    outline = Gray200,
    error = Danger
)

@Composable
fun CBstartupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
