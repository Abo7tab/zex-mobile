package com.zex.tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = tactical_primary,
    onPrimary = tactical_onPrimary,
    secondary = tactical_secondary,
    onSecondary = tactical_onSecondary,
    tertiary = tactical_tertiary,
    onTertiary = tactical_onTertiary,
    background = tactical_background,
    onBackground = tactical_onBackground,
    surface = tactical_surface,
    onSurface = tactical_onSurface,
    primaryContainer = Color(0xFF164E63), // Darker cyan box
    onPrimaryContainer = tactical_primary,
    secondaryContainer = Color(0xFF0F172A),
    onSecondaryContainer = Color(0xFF94A3B8),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = tactical_error,
    onError = tactical_onError,
    errorContainer = tactical_errorContainer,
    onErrorContainer = tactical_onErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = tactical_primary,
    onPrimary = tactical_onPrimary,
    secondary = tactical_secondary,
    onSecondary = tactical_onSecondary,
    tertiary = tactical_tertiary,
    onTertiary = tactical_onTertiary,
    background = tactical_background,
    onBackground = tactical_onBackground,
    surface = tactical_surface,
    onSurface = tactical_onSurface,
    primaryContainer = Color(0xFF164E63),
    onPrimaryContainer = tactical_primary,
    secondaryContainer = Color(0xFF0F172A),
    onSecondaryContainer = Color(0xFF94A3B8),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = tactical_error,
    onError = tactical_onError,
    errorContainer = tactical_errorContainer,
    onErrorContainer = tactical_onErrorContainer
)

@Composable
fun ZexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
