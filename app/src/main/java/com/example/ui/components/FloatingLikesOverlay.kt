package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class FloatingLike(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String = "👍",
    val startXRatio: Float = Random.nextFloat().coerceIn(0.12f, 0.88f),
    val startYRatio: Float = Random.nextFloat().coerceIn(0.65f, 0.90f),
    val sizeDp: Float = Random.nextInt(18, 26).toFloat(), // Neat, small & compact size
    val rotationStart: Float = Random.nextInt(-20, 20).toFloat(),
    val rotationEnd: Float = Random.nextInt(-35, 35).toFloat(),
    val driftX: Float = Random.nextInt(-60, 60).toFloat(),
    val travelY: Float = Random.nextInt(650, 950).toFloat(),
    val durationMs: Int = Random.nextInt(1600, 2300),
    val delayMs: Long = 0L
)

/**
 * Spawns a fountain of floating emojis in a clean, compact, refined size!
 */
fun spawnFloatingLikesBurst(
    list: MutableList<FloatingLike>,
    count: Int = 24,
    primaryEmoji: String? = "👍"
) {
    if (list.size > 80) {
        list.clear()
    }
    val popularEmojis = listOf(
        "👍", "💜", "❤️", "🔥", "✨", "👏", "😍", "🎉",
        "🚀", "💖", "💯", "🤩", "🥳", "💎", "🌟", "😮"
    )
    val activePrimary = primaryEmoji?.ifBlank { "👍" } ?: "👍"

    repeat(count) { index ->
        val staggerDelay = (index * 26L) + Random.nextLong(0, 40)
        val chosenEmoji: String = if (index % 2 == 0) {
            activePrimary
        } else {
            popularEmojis.random()
        }
        list.add(
            FloatingLike(
                emoji = chosenEmoji,
                delayMs = staggerDelay
            )
        )
    }
}

@Composable
fun FloatingLikesOverlay(likes: MutableList<FloatingLike>) {
    if (likes.isEmpty()) return

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    // Pass-through container without consuming touch events
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Render up to 70 simultaneous floating emoji items smoothly
        likes.toList().take(70).forEach { like ->
            key(like.id) {
                FloatingLikeItem(
                    like = like,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    onFinished = {
                        try {
                            likes.removeAll { it.id == like.id }
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun FloatingLikeItem(
    like: FloatingLike,
    screenWidth: Float,
    screenHeight: Float,
    onFinished: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var alpha by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(0.35f) }
    var rotation by remember { mutableFloatStateOf(like.rotationStart) }

    LaunchedEffect(like.id) {
        if (like.delayMs > 0) {
            delay(like.delayMs)
        }
        alpha = 1f

        // Upward floating motion
        launch {
            animate(
                initialValue = 0f,
                targetValue = -like.travelY,
                animationSpec = tween(durationMillis = like.durationMs, easing = FastOutSlowInEasing)
            ) { value, _ -> offsetY = value }
        }

        // Horizontal drift motion
        launch {
            animate(
                initialValue = 0f,
                targetValue = like.driftX,
                animationSpec = tween(durationMillis = like.durationMs, easing = LinearOutSlowInEasing)
            ) { value, _ -> offsetX = value }
        }

        // Smooth pop-in scale to compact size
        launch {
            animate(
                initialValue = 0.35f,
                targetValue = 1.05f,
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            ) { value, _ -> scale = value }
        }

        // Gentle rotation spin
        launch {
            animate(
                initialValue = like.rotationStart,
                targetValue = like.rotationEnd,
                animationSpec = tween(durationMillis = like.durationMs, easing = LinearEasing)
            ) { value, _ -> rotation = value }
        }

        // Fade out in second half
        launch {
            delay((like.durationMs * 0.48f).toLong())
            animate(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = (like.durationMs * 0.52f).toInt(),
                    easing = FastOutLinearInEasing
                )
            ) { value, _ -> alpha = value }
        }

        delay(like.durationMs.toLong() + 50L)
        onFinished()
    }

    val posX = (like.startXRatio * screenWidth) + offsetX
    val posY = (like.startYRatio * screenHeight) + offsetY

    if (alpha > 0f) {
        Text(
            text = like.emoji,
            fontSize = like.sizeDp.sp,
            modifier = Modifier
                .offset(x = posX.dp, y = posY.dp)
                .scale(scale)
                .rotate(rotation)
                .alpha(alpha)
        )
    }
}
