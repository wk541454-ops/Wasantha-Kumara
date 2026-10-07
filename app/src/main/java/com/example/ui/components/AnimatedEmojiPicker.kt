package com.example.ui.components

import android.content.Context
import androidx.compose.ui.window.Popup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data model for emoji with variations
data class AnimatedEmoji(
    val emoji: String,
    val variations: List<Pair<String, String?>> = emptyList() // Pair(Emoji, EffectName)
)

@Composable
fun AnimatedEmojiPicker(
    onEmojiSelected: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Animated, 1: Normal, 2: Recent
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Simple Recent Emojis System
    val prefs = remember { context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE) }
    var recentEmojis by remember { 
        mutableStateOf(prefs.getString("recent", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()) 
    }

    val updateRecent = { emoji: String ->
        val newList = (listOf(emoji) + recentEmojis).distinct().take(30)
        recentEmojis = newList
        prefs.edit().putString("recent", newList.joinToString(",")).apply()
    }

    val onSelect = { emoji: String, effect: String? ->
        updateRecent(emoji)
        onEmojiSelected(emoji, effect)
    }

    val animatedEmojis = listOf(
        AnimatedEmoji("❤️", listOf(
            "❤️" to "normal", "💖" to "burst", "💓" to "bouncing", "💘" to "floating"
        )),
        AnimatedEmoji("😂", listOf(
            "😂" to "normal", "🤣" to "bouncing", "😆" to "burst", "😅" to "normal"
        )),
        AnimatedEmoji("🔥", listOf(
            "🔥" to "normal", "💥" to "burst", "✨" to "floating", "⚡" to "burst"
        )),
        AnimatedEmoji("👍", listOf(
            "👍" to "normal", "🙌" to "burst", "👊" to "normal", "✅" to "normal"
        )),
        AnimatedEmoji("👏", listOf(
            "👏" to "normal", "🙌" to "burst", "🙏" to "normal", "💪" to "normal"
        )),
        AnimatedEmoji("🎉", listOf(
            "🎉" to "burst", "🎊" to "burst", "🎈" to "floating", "🎁" to "normal"
        )),
        AnimatedEmoji("😍", listOf(
            "😍" to "floating", "🥰" to "burst", "😘" to "floating", "😻" to "normal"
        )),
        AnimatedEmoji("😮", listOf(
            "😮" to "burst", "😲" to "burst", "😯" to "normal", "😧" to "normal"
        )),
        AnimatedEmoji("😢", listOf(
            "😢" to "tears", "😭" to "tears", "💧" to "tears", "🌧️" to "tears"
        )),
        AnimatedEmoji("😡", listOf(
            "😡" to "shake", "🤬" to "shake", "👿" to "shake", "💢" to "shake"
        )),
        AnimatedEmoji("🤣", listOf(
            "🤣" to "bouncing", "😂" to "normal", "😹" to "normal", "🙃" to "normal"
        )),
        AnimatedEmoji("👋", listOf(
            "👋" to "waving", "🤚" to "waving", "🖖" to "waving", "🖐️" to "waving"
        ))
    )

    val normalEmojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
        "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗",
        "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯",
        "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐"
    )

    val filteredAnimated = animatedEmojis.filter { it.emoji.contains(searchQuery) }
    val filteredNormal = normalEmojis.filter { it.contains(searchQuery) }

    val colors = LocalFriendHubColors.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(
                Brush.verticalGradient(
                    listOf(colors.cardBackground, colors.background)
                ),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .border(
                1.dp, 
                colors.cardBorder.copy(alpha = 0.5f), 
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(top = 12.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(colors.textSecondary.copy(alpha = 0.2f), CircleShape)
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search FriendHub Emojis...", color = colors.textSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.secondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.background.copy(alpha = 0.5f),
                    unfocusedContainerColor = colors.background.copy(alpha = 0.5f),
                    focusedBorderColor = colors.secondary,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Reaction Emojis
        Text(
            "QUICK REACTIONS", 
            color = colors.tertiary, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("❤️", "😂", "🔥", "👍", "👏", "🎉", "😍", "😮").forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardSelected.copy(alpha = 0.5f))
                        .clickable { onSelect(emoji, "burst") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmojiTab(text = "ANIMATED", isSelected = selectedTab == 0) { selectedTab = 0 }
            EmojiTab(text = "NORMAL", isSelected = selectedTab == 1) { selectedTab = 1 }
            EmojiTab(text = "RECENT", isSelected = selectedTab == 2) { selectedTab = 2 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emoji Grid
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            when (selectedTab) {
                0 -> AnimatedEmojiGrid(filteredAnimated, onSelect)
                1 -> NormalEmojiGrid(filteredNormal) { onSelect(it, null) }
                2 -> NormalEmojiGrid(recentEmojis) { onSelect(it, null) }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun EmojiTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = LocalFriendHubColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.secondary.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (isSelected) colors.secondary else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.secondary else colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedEmojiGrid(emojis: List<AnimatedEmoji>, onEmojiSelected: (String, String?) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(emojis) { item ->
            EmojiItemWithVariations(item, onEmojiSelected)
        }
    }
}

@Composable
fun NormalEmojiGrid(emojis: List<String>, onEmojiSelected: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(emojis) { emoji ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
        }
    }
}

@Composable
fun SubtleAnimatedEmoji(emoji: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "emoji_anim")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .graphicsLayer(rotationZ = rotation),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 32.sp)
    }
}

@Composable
fun EmojiItemWithVariations(item: AnimatedEmoji, onEmojiSelected: (String, String?) -> Unit) {
    var showVariations by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .scale(scale.value)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1B2E))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            coroutineScope.launch {
                                scale.animateTo(1.3f, spring(Spring.DampingRatioMediumBouncy))
                                scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                                onEmojiSelected(item.emoji, "normal")
                            }
                        },
                        onLongPress = {
                            showVariations = true
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            SubtleAnimatedEmoji(emoji = item.emoji)
        }

        if (showVariations) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = androidx.compose.ui.unit.IntOffset(0, -180),
                onDismissRequest = { showVariations = false }
            ) {
                EmojiVariationsPanel(item.variations) { variation, effect ->
                    onEmojiSelected(variation, effect)
                    showVariations = false
                }
            }
        }
    }
}

@Composable
fun EmojiVariationsPanel(variations: List<Pair<String, String?>>, onSelect: (String, String?) -> Unit) {
    val colors = LocalFriendHubColors.current
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.secondary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            variations.forEach { (variation, effect) ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.background.copy(alpha = 0.3f))
                        .clickable { onSelect(variation, effect) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = variation, fontSize = 24.sp)
                }
            }
        }
    }
}
