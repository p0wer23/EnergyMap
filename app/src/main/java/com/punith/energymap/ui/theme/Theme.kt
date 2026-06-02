package com.punith.energymap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7BD389),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF0E1A12),
    secondary = androidx.compose.ui.graphics.Color(0xFF9FC9FF),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF102033),
    background = androidx.compose.ui.graphics.Color(0xFF101316),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    surface = androidx.compose.ui.graphics.Color(0xFF171B1F),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF22272C),
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFF1B2024),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF2A3036),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFB9C1C9),
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
