package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * OnlineGreenRing: Custom FriendHub Online Presence Ring
 * Renders a bright green arc segment that smoothly travels 360 degrees around the circular avatar.
 * Animation starts from the TOP (-90 degrees) and rotates continuously.
 * The profile content inside remains 100% stationary and stable.
 */
@Composable
fun OnlineGreenRing(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 3.dp,
    paddingPx: Dp = 2.5.dp,
    content: @Composable () -> Unit
) {
    if (!isOnline) {
        content()
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "OnlineGreenRingRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Rotating bright green arc segment
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = ringWidth.toPx()
            val arcPadding = paddingPx.toPx() + strokePx / 2f
            val arcSize = Size(size.width - arcPadding * 2, size.height - arcPadding * 2)
            val topLeft = Offset(arcPadding, arcPadding)

            // 1. Subtle dark green background border track
            drawArc(
                color = Color(0xFF064E3B).copy(alpha = 0.45f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Bright neon green gradient brush
            val greenGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF059669).copy(alpha = 0.2f),
                    Color(0xFF10B981),
                    Color(0xFF34D399),
                    Color(0xFF6EE7B7)
                )
            )

            // 3. Smooth 360 degree rotation around pivot center
            rotate(degrees = rotationAngle, pivot = center) {
                // Arc segment starting at top (-90 degrees) with 110 degrees sweep
                drawArc(
                    brush = greenGradient,
                    startAngle = -90f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Profile image / avatar inside remains stationary
        content()
    }
}
