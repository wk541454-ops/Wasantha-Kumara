package com.example.ui.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

data class StickerItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String = ""
)

object StickerOverlay {

    val STICKERS_LIST = listOf(
        StickerItem("sticker_none", "None", "🚫", "No sticker overlay"),
        StickerItem("sticker_heart", "Heart ❤️", "❤️", "Animated pink heart badge"),
        StickerItem("sticker_fire", "Fire 🔥", "🔥", "Fiery aura badge"),
        StickerItem("sticker_crown", "Crown 👑", "👑", "Golden royal crown"),
        StickerItem("sticker_star", "Star ⭐", "⭐", "Shining golden star"),
        StickerItem("sticker_founder", "Founder", "🏆", "Official Founder Badge"),
        StickerItem("sticker_traveler", "Traveler ✈️", "✈️", "Globe Trotter Traveler Badge"),
        StickerItem("sticker_sl_flag", "Sri Lanka 🇱🇰", "🇱🇰", "Sri Lankan Flag Badge"),
        StickerItem("sticker_verified", "Verified Badge", "☑️", "Blue verified checkmark")
    )

    fun drawSticker(stickerId: String, scope: DrawScope, size: Size) {
        if (stickerId == "sticker_none") return

        when (stickerId) {
            "sticker_verified" -> {
                // Draw Verified Check Circle bottom-right
                scope.drawCircle(
                    color = Color(0xFF1D9BF0),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.82f, size.height * 0.82f)
                )
            }
            "sticker_crown" -> {
                // Draw Crown Circle top-center
                scope.drawCircle(
                    color = Color(0xFFFFD700),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.5f, size.height * 0.12f)
                )
            }
            "sticker_heart" -> {
                // Heart Circle bottom-right
                scope.drawCircle(
                    color = Color(0xFFFF1493),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.82f, size.height * 0.82f)
                )
            }
            "sticker_fire" -> {
                // Fire Circle top-right
                scope.drawCircle(
                    color = Color(0xFFFF4500),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.82f, size.height * 0.18f)
                )
            }
            "sticker_star" -> {
                // Star Circle top-left
                scope.drawCircle(
                    color = Color(0xFFFFD700),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.18f, size.height * 0.18f)
                )
            }
            "sticker_sl_flag" -> {
                // SL Flag Circle bottom-right
                scope.drawCircle(
                    color = Color(0xFF008080),
                    radius = size.width * 0.13f,
                    center = Offset(size.width * 0.82f, size.height * 0.82f)
                )
            }
            "sticker_founder" -> {
                // Founder Badge Circle bottom-center
                scope.drawCircle(
                    color = Color(0xFF8B5CF6),
                    radius = size.width * 0.13f,
                    center = Offset(size.width * 0.5f, size.height * 0.86f)
                )
            }
            "sticker_traveler" -> {
                // Traveler Badge Circle bottom-left
                scope.drawCircle(
                    color = Color(0xFF00D1FF),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.18f, size.height * 0.82f)
                )
            }
        }
    }
}
