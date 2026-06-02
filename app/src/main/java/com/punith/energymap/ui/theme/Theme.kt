package com.punith.energymap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2F3F5),
    onPrimary = Color(0xFF090A0C),
    secondary = Color(0xFFB7BDC7),
    onSecondary = Color(0xFF0D0F12),
    background = Color(0xFF08090B),
    onBackground = Color(0xFFF5F5F6),
    surface = Color(0xFF0D0F12),
    onSurface = Color(0xFFF5F5F6),
    surfaceContainer = Color(0xFF121418),
    surfaceContainerLow = Color(0xFF101216),
    surfaceContainerHigh = Color(0xFF171A1F),
    onSurfaceVariant = Color(0xFF9BA2AD),
)

@Composable
fun EnergyMapTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content,
    )
}
