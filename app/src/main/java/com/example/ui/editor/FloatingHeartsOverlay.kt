package com.example.ui.editor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

data class FlyingEmoji(
    val id: Long,
    val emoji: String,
    val initialX: Float,
    val scale: Float
)

@Composable
fun FloatingHeartsOverlay(
    flyingEmojis: List<FlyingEmoji>,
    onFinished: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        flyingEmojis.forEach { emojiItem ->
            val animY = remember { Animatable(1f) } // 1f = bottom, 0f = top
            val animXOffset = remember { Animatable(0f) }

            LaunchedEffect(emojiItem.id) {
                // Animate Y upwards (to 0f) and X with a wiggle
                animXOffset.animateTo(
                    targetValue = Random.nextInt(-60, 60).toFloat(),
                    animationSpec = tween(durationMillis = 2000)
                )
            }

            LaunchedEffect(emojiItem.id) {
                animY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 2000)
                )
                onFinished(emojiItem.id)
            }

            val yOffset = (animY.value * 700).roundToInt()
            val alphaVal = animY.value.coerceIn(0f, 1f)

            Text(
                text = emojiItem.emoji,
                fontSize = (32 * emojiItem.scale).sp,
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = (emojiItem.initialX + animXOffset.value).roundToInt(),
                            y = yOffset + 200
                        )
                    }
                    .alpha(alphaVal)
            )
        }
    }
}
