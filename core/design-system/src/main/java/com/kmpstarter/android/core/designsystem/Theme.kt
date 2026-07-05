package com.kmpstarter.android.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A60),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF62DCCE),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
)

@Composable
fun KmpStarterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KmpStarterTypography,
        shapes = KmpStarterShapes,
        content = content,
    )
}
