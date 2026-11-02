package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = CosmicNeonPurple,
    onPrimary = Color.White,
    primaryContainer = CosmicSurfaceHighlight,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = CosmicNeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381447),
    onSecondaryContainer = Color(0xFFFCE7F3),
    tertiary = CosmicNeonCyan,
    background = CosmicBackground,
    onBackground = CosmicTextPrimary,
    surface = CosmicSurfaceSolid,
    onSurface = CosmicTextPrimary,
    surfaceVariant = CosmicSurfaceElevated,
    onSurfaceVariant = CosmicTextSecondary,
    outline = CosmicCardBorder,
    outlineVariant = CosmicGlassStroke
)

@Composable
fun DocMasterTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
