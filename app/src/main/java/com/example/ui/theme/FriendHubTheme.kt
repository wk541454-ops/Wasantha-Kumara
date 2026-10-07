package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import com.example.ui.theme.FriendHubColors

private val DarkColorScheme = darkColorScheme(
    primary = PurpleMain,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B0060),
    onPrimaryContainer = Color(0xFFEADBFF),
    secondary = BlueFeed,
    onSecondary = Color.White,
    tertiary = PinkMain,
    background = BlackBg,
    onBackground = WhiteText,
    surface = CardBg,
    onSurface = WhiteText,
    surfaceVariant = PillBg,
    onSurfaceVariant = GrayText,
    outline = BorderGray,
    error = Color(0xFFFF4B4B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8B5CF6), // Slightly softer purple for light mode
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADBFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF0075FF),
    onSecondary = Color.White,
    tertiary = Color(0xFFEC4899),
    background = Color(0xFFF8FAFC), // Very light gray-blue background
    onBackground = Color(0xFF0F172A), // Dark slate for text
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), // Light variant
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    error = Color(0xFFB91C1C)
)

@Composable
fun FriendHubTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val friendHubColors = if (darkTheme) DarkFriendHubColors else LightFriendHubColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalFriendHubColors provides friendHubColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    FriendHubTheme(darkTheme = darkTheme, content = content)
}
