package com.example.ui.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

data class FrameItem(
    val id: String,
    val name: String,
    val colors: List<Color>,
    val description: String = ""
)

object FrameOverlay {

    val FRAMES_LIST = listOf(
        FrameItem("frame_none", "None", listOf(Color.Transparent), "Original avatar without frame"),
        FrameItem("frame_purple_neon", "Purple Neon", listOf(Color(0xFFD946EF), Color(0xFF8B5CF6)), "Glowing magenta-purple ring"),
        FrameItem("frame_blue_gradient", "Blue Gradient", listOf(Color(0xFF00D1FF), Color(0xFF2A5BD7)), "Cool cyan-blue gradient border"),
        FrameItem("frame_gold_crown", "Gold Crown", listOf(Color(0xFFFFD700), Color(0xFFFFA500)), "Metallic gold ring with crown accent"),
        FrameItem("frame_rainbow", "Rainbow", listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta), "Vibrant rainbow aura"),
        FrameItem("frame_verified_black", "Verified Black", listOf(Color.Black, Color(0xFF1D9BF0)), "Black border with verified checkmark ring"),
        FrameItem("frame_cyberpunk", "Cyberpunk Glow", listOf(Color(0xFFFF007F), Color(0xFF00FFFF)), "Neon pink & cyan dual glow"),
        FrameItem("frame_golden_ring", "Golden Ring", listOf(Color(0xFFFFDF00), Color(0xFFD4AF37)), "Royal gold metallic border"),
        FrameItem("frame_fire_rim", "Fire Rim", listOf(Color(0xFFFF4500), Color(0xFFFF8C00), Color(0xFFFF0000)), "Fiery orange-red burning aura"),
        FrameItem("frame_emerald_glow", "Emerald Glow", listOf(Color(0xFF00FF7F), Color(0xFF10B981)), "Luminous green emerald ring"),
        FrameItem("frame_pink_rose", "Pink Rose", listOf(Color(0xFFFF69B4), Color(0xFFFF1493)), "Pastel pink rose ring"),
        FrameItem("frame_neon_cyan", "Neon Cyan", listOf(Color(0xFF00FFFF), Color(0xFF0088FF)), "Electric cyan glowing border"),
        FrameItem("frame_sunset_orange", "Sunset Orange", listOf(Color(0xFFFF5722), Color(0xFFFF9800)), "Warm sunset gradient ring"),
        FrameItem("frame_diamond_ring", "Diamond Ring", listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF00E5FF)), "Sparkling diamond accent ring"),
        FrameItem("frame_white_minimal", "Minimal White", listOf(Color.White, Color(0xFFCCCCCC)), "Clean minimal white border"),
        FrameItem("frame_futuristic_hud", "Futuristic HUD", listOf(Color(0xFF00F0FF), Color(0xFF7000FF)), "Sci-fi tech HUD overlay circle"),
        FrameItem("frame_sri_lanka", "Sri Lanka Pride", listOf(Color(0xFF800000), Color(0xFFFFD700), Color(0xFF008080), Color(0xFFFF8C00)), "Maroon, Gold & Teal Sri Lankan Pride"),
        FrameItem("frame_heart_glow", "Heart Glow", listOf(Color(0xFFFF1493), Color(0xFFFF69B4)), "Glowing pink heart ring"),
        FrameItem("frame_star_cluster", "Star Cluster", listOf(Color(0xFFFFD700), Color(0xFF1E90FF)), "Starry night border with star sparkles"),
        FrameItem("frame_royal_purple", "Royal Purple", listOf(Color(0xFF4B0082), Color(0xFF9370DB)), "Deep royal purple metallic ring")
    )

    fun drawFrame(frameId: String, scope: DrawScope, size: Size) {
        val strokeWidth = size.width * 0.05f
        val radius = (size.width - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        val frameItem = FRAMES_LIST.find { it.id == frameId } ?: return
        if (frameItem.id == "frame_none") return

        val brush = Brush.sweepGradient(
            colors = if (frameItem.colors.size == 1) listOf(frameItem.colors[0], frameItem.colors[0]) else frameItem.colors,
            center = center
        )

        // Outer Glow Circle
        scope.drawCircle(
            brush = brush,
            radius = radius + (strokeWidth * 0.2f),
            center = center,
            style = Stroke(width = strokeWidth * 0.4f),
            alpha = 0.4f
        )

        // Main Frame Circle
        scope.drawCircle(
            brush = brush,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Special Accents
        when (frameId) {
            "frame_gold_crown" -> {
                // Crown Top Accent
                val crownPath = Path().apply {
                    val cx = size.width / 2f
                    val topY = size.height * 0.04f
                    val botY = size.height * 0.16f
                    moveTo(cx - 20f, botY)
                    lineTo(cx - 30f, topY + 10f)
                    lineTo(cx - 10f, topY + 20f)
                    lineTo(cx, topY)
                    lineTo(cx + 10f, topY + 20f)
                    lineTo(cx + 30f, topY + 10f)
                    lineTo(cx + 20f, botY)
                    close()
                }
                scope.drawPath(
                    path = crownPath,
                    color = Color(0xFFFFD700)
                )
            }

            "frame_verified_black" -> {
                // Bottom Verified Badge Accent
                scope.drawCircle(
                    color = Color(0xFF1D9BF0),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.82f, size.height * 0.82f)
                )
            }

            "frame_sri_lanka" -> {
                // Lion Flag Gold Corner Dot
                scope.drawCircle(
                    color = Color(0xFFFFD700),
                    radius = size.width * 0.06f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
            }
        }
    }
}
