package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcherDarkColorScheme = darkColorScheme(
    primary = MemoryCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberBgElevated,
    onPrimaryContainer = MemoryCyan,
    secondary = SettingsGreen,
    onSecondary = Color.Black,
    tertiary = ChatOrange,
    background = CyberBgDark,
    onBackground = Color.White,
    surface = CyberCardBg,
    onSurface = Color.White,
    surfaceVariant = CyberBgElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = CyberBorder,
    error = StopRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ArcherDarkColorScheme,
        typography = Typography,
        content = content
    )
}
