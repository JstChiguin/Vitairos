package com.example.vitairosapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VitairosColors = lightColorScheme(
    primary = Deep,
    onPrimary = Color.White,
    secondary = Copper,
    onSecondary = OnCopper,
    tertiary = Gold,
    background = Bone,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Muted,
    outline = FieldBorder,
    outlineVariant = Line,
    error = Danger,
    errorContainer = DangerBg,
)

@Composable
fun VitairosAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VitairosColors,
        typography = Typography,
        content = content
    )
}
