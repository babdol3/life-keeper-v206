package com.ultratul.lifekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LifeMint = Color(0xFF26A69A)
private val LifeBlue = Color(0xFF1976D2)
private val LifeBg = Color(0xFFF8FBFB)
private val LifeSurface2 = Color(0xFFF2F8F8)
private val LifeText = Color(0xFF172326)
private val LifeMuted = Color(0xFF6F8285)

private val LightColors: ColorScheme = lightColorScheme(
    primary = LifeMint,
    secondary = LifeBlue,
    tertiary = Color(0xFF27AE60),
    background = LifeBg,
    surface = Color.White,
    surfaceVariant = LifeSurface2,
    primaryContainer = Color(0xFFDFF5F2),
    secondaryContainer = Color(0xFFE7F2FF),
    tertiaryContainer = Color(0xFFFFF4DC),
    errorContainer = Color(0xFFFFE7E7),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LifeText,
    onSurface = LifeText,
    onSurfaceVariant = LifeMuted,
    onPrimaryContainer = Color(0xFF087267),
    onSecondaryContainer = Color(0xFF1765AD)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    secondary = Color(0xFF64B5F6),
    tertiary = Color(0xFF81C784),
    background = Color(0xFF0F1719),
    surface = Color(0xFF182326),
    surfaceVariant = Color(0xFF223033),
    primaryContainer = Color(0xFF123F3A),
    secondaryContainer = Color(0xFF12304E),
    tertiaryContainer = Color(0xFF40351E),
    errorContainer = Color(0xFF4B1D1D),
    onPrimary = Color(0xFF003733),
    onSecondary = Color(0xFF082A4C),
    onBackground = Color(0xFFE7F2F1),
    onSurface = Color(0xFFE7F2F1),
    onSurfaceVariant = Color(0xFFB8C9C8)
)

@Composable
fun LifeKeeperTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
