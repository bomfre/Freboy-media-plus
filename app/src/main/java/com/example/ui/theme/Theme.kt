package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val IptvDarkColorScheme = darkColorScheme(
    primary = CyanBright,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFFA6EEFF),
    secondary = VioletNeon,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = AmberLive,
    onTertiary = Color.Black,
    background = SlateDark900,
    onBackground = TextPrimary,
    surface = SlateDark800,
    onSurface = TextPrimary,
    surfaceVariant = SlateDark700,
    onSurfaceVariant = SlateMuted,
    error = RedLiveBadge,
    onError = Color.White
)

private val IptvLightColorScheme = darkColorScheme(
    // IPTV player looks best in dark cinematic mode regardless of system light theme
    primary = CyanNeon,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = CyanBright,
    secondary = VioletNeon,
    onSecondary = Color.White,
    background = SlateDark900,
    onBackground = TextPrimary,
    surface = SlateDark800,
    onSurface = TextPrimary,
    surfaceVariant = SlateDark700,
    onSurfaceVariant = SlateMuted,
    error = RedLiveBadge,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek IPTV dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) IptvDarkColorScheme else IptvLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
