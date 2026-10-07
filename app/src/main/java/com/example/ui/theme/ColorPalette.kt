package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

// Exact colors matching the photo (WhatsApp Image 2026-10-06 at 4.43.23 PM.jpeg)
val BlackBg = Color(0xFF060A17) // Ultra-deep obsidian midnight navy
val CardBg = Color(0xFF0E1528)  // Deep dark navy post card
val PillBg = Color(0xFF141C33)  // Dark navy pill background
val BorderGray = Color(0xFF222F4E) // Subtle dark navy border
val PurpleMain = Color(0xFFA855F7) // Vibrant neon purple
val PinkMain = Color(0xFFEC4899)   // Vibrant neon pink
val BlueFeed = Color(0xFF0075FF)   // Electric blue active pill
val WhiteText = Color(0xFFFFFFFF)  // Pure white text
val GrayText = Color(0xFF94A3B8)   // Refined light gray-blue text

data class FriendHubColors(
    val background: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val cardSelected: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val iconPrimary: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val onlineGreen: Color = Color(0xFF22C55E),
    val purpleGradient: Brush = Brush.linearGradient(listOf(PurpleMain, PinkMain)),
    val orangeGradient: Brush = Brush.linearGradient(listOf(Color(0xFFFFB800), Color(0xFFFF3B30))),
    val likeGradient: Brush = Brush.linearGradient(listOf(Color(0xFFFF1493), Color(0xFFA855F7)))
)

val LocalFriendHubColors = staticCompositionLocalOf<FriendHubColors> {
    error("No FriendHubColors provided")
}

val DarkFriendHubColors = FriendHubColors(
    background = BlackBg,
    cardBackground = CardBg,
    cardBorder = BorderGray,
    cardSelected = PillBg,
    textPrimary = WhiteText,
    textSecondary = GrayText,
    iconPrimary = Color(0xFFCBD5E1),
    primary = PurpleMain,
    secondary = BlueFeed,
    tertiary = PinkMain,
    error = Color(0xFFFF4B4B)
)

val LightFriendHubColors = FriendHubColors(
    background = Color(0xFFF8FAFC),
    cardBackground = Color.White,
    cardBorder = Color(0xFFE2E8F0),
    cardSelected = Color(0xFFF1F5F9),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    iconPrimary = Color(0xFF64748B),
    primary = Color(0xFF8B5CF6),
    secondary = Color(0xFF0075FF),
    tertiary = Color(0xFFEC4899),
    error = Color(0xFFB91C1C)
)
