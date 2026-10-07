package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlowingBadgeLogo
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val steps = 5
        for (i in 1..steps) {
            delay(60)
            progress = i / steps.toFloat()
        }
        delay(100)
        onSplashFinished()
    }

    NeonBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Central Branding matching Photo 4
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlowingBadgeLogo(size = 140.dp)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FriendHub",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.tagline),
                    color = NeonTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Spinner & Progress Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(220.dp)
            ) {
                // 12-petal radial loading spinner
                RadialPetalSpinner()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.connecting_cloud),
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gradient Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E1A2E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(NeonPinkGlow, NeonPurple, NeonCyan)
                                )
                            )
                    )
                }
            }

            // Footer
            Text(
                text = stringResource(R.string.connect_chat_share),
                color = NeonTextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}

@Composable
fun RadialPetalSpinner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(42.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val numPetals = 12
        val radiusInner = size.width * 0.22f
        val radiusOuter = size.width * 0.42f

        for (i in 0 until numPetals) {
            val angleDeg = (i * 30f + rotationAngle) % 360f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val alpha = (i + 1) / numPetals.toFloat()

            val start = Offset(
                x = center.x + (radiusInner * cos(angleRad)).toFloat(),
                y = center.y + (radiusInner * sin(angleRad)).toFloat()
            )
            val end = Offset(
                x = center.x + (radiusOuter * cos(angleRad)).toFloat(),
                y = center.y + (radiusOuter * sin(angleRad)).toFloat()
            )

            drawLine(
                color = NeonPinkGlow.copy(alpha = alpha),
                start = start,
                end = end,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
