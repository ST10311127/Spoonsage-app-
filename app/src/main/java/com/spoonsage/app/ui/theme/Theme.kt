package com.spoonsage.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SageGreen,
    onPrimary = Cream,
    primaryContainer = SageGreenLight,
    onPrimaryContainer = SageGreenDark,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal,
    secondary = WarmGray
)

private val DarkColors = darkColorScheme(
    primary = SageGreenLight,
    onPrimary = SageGreenDark,
    primaryContainer = SageGreenDark,
    onPrimaryContainer = SageGreenLight,
    background = Charcoal,
    onBackground = Cream,
    surface = Charcoal,
    onSurface = Cream,
    secondary = WarmGray
)

@Composable
fun SpoonSageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = SpoonSageTypography,
        content = content
    )
}
