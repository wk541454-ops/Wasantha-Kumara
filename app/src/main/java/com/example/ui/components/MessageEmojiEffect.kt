package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MessageEmojiEffect(
    emoji: String,
    trigger: Any?, // Change this to trigger animation
    effectType: String? = null,
    modifier: Modifier = Modifier
) {
    val particles = remember { mutableStateListOf<EmojiParticle>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        
        // Supported emojis for large effects
        val emojiGroups = mapOf(
            "❤️" to listOf("❤️", "💖", "💓", "💘", "💝", "💕"),
            "😂" to listOf("😂", "🤣", "😆", "😅", "😹"),
            "🔥" to listOf("🔥", "💥", "✨", "⚡"),
            "👍" to listOf("👍", "🙌", "👊", "✅"),
            "👏" to listOf("👏", "💪"),
            "🎉" to listOf("🎉", "🎊", "🎈"),
            "😍" to listOf("😍", "🥰", "😘"),
            "😮" to listOf("😮", "😲", "😯"),
            "😢" to listOf("😢", "😭", "💧"),
            "😡" to listOf("😡", "🤬", "👿"),
            "👋" to listOf("👋", "🤚")
        )
        
        val baseEmoji = emojiGroups.entries.find { group ->
            group.value.any { variation -> emoji.contains(variation) }
        }?.key ?: return@LaunchedEffect

        val finalEffectType = effectType ?: when (baseEmoji) {
            "❤️" -> "rising"
            "😂" -> "bouncing"
            "🔥" -> "burst"
            "😢" -> "tears"
            "😡" -> "shake"
            "👋" -> "waving"
            "🎉" -> "burst"
            else -> "rising"
        }

        // Spawn particles based on emoji type and effect
        val count = when (finalEffectType) {
            "burst" -> 40
            "floating" -> 15
            else -> 20
        }

        repeat(count) { i ->
            coroutineScope.launch {
                val delayTime = when (finalEffectType) {
                    "burst" -> Random.nextLong(0, 500)
                    else -> i * 40L
                }
                delay(delayTime)
                
                val particle = EmojiParticle(
                    id = System.currentTimeMillis() + i + Random.nextLong(10000),
                    emoji = emoji,
                    offsetX = when (finalEffectType) {
                        "bouncing" -> Random.nextInt(-100, 100).toFloat()
                        "burst" -> Random.nextInt(-150, 150).toFloat()
                        else -> Random.nextInt(-80, 80).toFloat()
                    },
                    offsetY = when (finalEffectType) {
                        "tears" -> Random.nextInt(-20, 20).toFloat()
                        "burst" -> Random.nextInt(-200, 0).toFloat()
                        else -> Random.nextInt(-60, 0).toFloat()
                    },
                    scale = Random.nextFloat() * 0.4f + 0.6f,
                    duration = when (finalEffectType) {
                        "burst" -> Random.nextInt(800, 1500)
                        "bouncing" -> Random.nextInt(1000, 1800)
                        else -> Random.nextInt(1200, 2500)
                    },
                    type = finalEffectType
                )
                particles.add(particle)
                delay(particle.duration.toLong())
                particles.remove(particle)
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        particles.forEach { particle ->
            key(particle.id) {
                EmojiParticleItem(particle)
            }
        }
    }
}

data class EmojiParticle(
    val id: Long,
    val emoji: String,
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float,
    val duration: Int,
    val type: String
)

@Composable
fun EmojiParticleItem(particle: EmojiParticle) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = when (particle.type) {
                "bouncing" -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                "burst" -> tween(particle.duration, easing = FastOutSlowInEasing)
                else -> tween(particle.duration, easing = LinearOutSlowInEasing)
            }
        )
    }

    val yOffset = when (particle.type) {
        "tears" -> particle.offsetY + (animProgress.value * 150f) // Falling tears
        "bouncing" -> particle.offsetY - (kotlin.math.sin(animProgress.value * 10f) * 40f) // Bouncing
        "shake" -> particle.offsetY + (Random.nextInt(-5, 5).toFloat() * animProgress.value) // Shaking
        else -> particle.offsetY - (animProgress.value * 250f) // Rising
    }
    
    val xOffset = when (particle.type) {
        "bouncing" -> particle.offsetX + (kotlin.math.cos(animProgress.value * 5f) * 20f)
        "waving" -> particle.offsetX + (kotlin.math.sin(animProgress.value * 15f) * 15f) // Waving
        else -> particle.offsetX
    }

    val alpha = 1f - animProgress.value
    val scale = particle.scale * when (particle.type) {
        "burst" -> (1f + animProgress.value * 1.5f)
        "rising" -> (1f + animProgress.value * 0.5f)
        else -> (1f + animProgress.value * 0.3f)
    }

    Box(
        modifier = Modifier
            .offset(x = xOffset.dp, y = yOffset.dp)
            .alpha(alpha)
            .scale(scale)
    ) {
        Text(text = particle.emoji, fontSize = 24.sp)
    }
}

// Utility to check if text is a single emoji
fun String.isPureEmoji(): Boolean {
    if (this.isBlank()) return false
    val trimmed = this.trim()
    if (trimmed.length > 8) return false
    // Most emojis are in the surrogate range or specific symbol blocks
    return trimmed.all { it.isSurrogate() || !it.isLetterOrDigit() }
}
