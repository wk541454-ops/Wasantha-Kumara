package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.R
import com.example.ui.theme.NeonDarkSurface
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.NeonPurpleGlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun FriendHubLikeButton(
    isLiked: Boolean,
    onLikeToggle: (Boolean) -> Unit,
    onReactionSelect: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isLikedState by remember(isLiked) { mutableStateOf(isLiked) }
    val buttonScale = remember { Animatable(1f) }

    // Emoji Bar state
    var showEmojiBar by remember { mutableStateOf(false) }
    var showPlusPicker by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.wrapContentSize()
    ) {
        // Main Like Icon Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .scale(buttonScale.value)
                .pointerInput(isLikedState) {
                    detectTapGestures(
                        onTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isLikedState = !isLikedState
                            onLikeToggle(isLikedState)
                            if (isLikedState) {
                                coroutineScope.launch {
                                    buttonScale.animateTo(
                                        targetValue = 1.4f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    buttonScale.animateTo(1.0f)
                                }
                            }
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showEmojiBar = true
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_like_3d_glossy),
                contentDescription = "Like",
                modifier = Modifier
                    .size(30.dp)
                    .then(if (isLikedState) Modifier.shadow(8.dp, CircleShape, ambientColor = Color.White) else Modifier),
                colorFilter = if (!isLikedState) ColorFilter.tint(Color.Gray).let { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) } else null
            )
        }
        // ... (Emoji Bar/Picker Popups) ...
    }
}
