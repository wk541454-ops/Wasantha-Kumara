package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.components.FloatingLike
import com.example.ui.components.FloatingLikesOverlay
import com.example.ui.components.FriendHubGlowingNeonLikeIcon
import com.example.ui.components.spawnFloatingLikesBurst
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoReelsScreen(
    posts: List<com.example.data.model.PostItem> = emptyList(),
    floatingLikes: MutableList<FloatingLike>? = null,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val videoList = remember(posts) { posts.filter { it.category == "Video" } }
    val currentReel = videoList.firstOrNull()

    var currentReaction by remember { mutableStateOf("👍") }
    var showReactionMenu by remember { mutableStateOf(false) }
    val reactionEmojis = listOf("❤️", "😂", "🔥", "😍", "👏", "🎉", "😮", "👍")
    val likeScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val localFloatingLikes = remember { mutableStateListOf<FloatingLike>() }
    val activeFloatingLikes = floatingLikes ?: localFloatingLikes
    var showCommentSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { CustomBottomNavBar(selectedRoute = "video", onNavigate = onNavigate) },
        containerColor = BlackBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentReel != null) {
                var isLiked by remember(currentReel.id) { mutableStateOf(currentReel.isLiked) }
                var likesCount by remember(currentReel.id) { mutableIntStateOf(currentReel.likesCount) }

                // Full screen video background
                if (currentReel.imageUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = currentReel.imageUrl,
                        contentDescription = "Reels Video",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.post_architecture),
                        contentDescription = "Reels Video",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Gradient shade for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
                        )
                )

                // Reaction bar popup for Reels
                AnimatedVisibility(
                    visible = showReactionMenu,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 68.dp, bottom = 100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A).copy(alpha = 0.95f), RoundedCornerShape(28.dp))
                            .border(1.dp, PurpleMain.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            reactionEmojis.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .clickable {
                                            currentReaction = emoji
                                            showReactionMenu = false
                                            if (!isLiked) {
                                                isLiked = true
                                                likesCount += 1
                                            }
                                            coroutineScope.launch {
                                                likeScale.animateTo(1.4f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                                                likeScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                                            }
                                            spawnFloatingLikesBurst(activeFloatingLikes, count = 28, primaryEmoji = emoji)
                                        }
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }

                // Right Actions Column
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // 3D Like Action with Spring Bounce & Floating Likes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                coroutineScope.launch {
                                    likeScale.animateTo(
                                        targetValue = 1.4f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    likeScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                isLiked = !isLiked
                                likesCount += if (isLiked) 1 else -1
                                spawnFloatingLikesBurst(
                                    activeFloatingLikes,
                                    count = 28,
                                    primaryEmoji = if (isLiked) currentReaction else "👍"
                                )
                            },
                            onLongClick = {
                                showReactionMenu = !showReactionMenu
                            }
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            FriendHubGlowingNeonLikeIcon(
                                isLiked = isLiked,
                                sizeDp = 42.dp,
                                scaleAnim = likeScale.value
                            )
                            if (isLiked && currentReaction != "👍") {
                                Text(
                                    text = currentReaction,
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%,d".format(likesCount),
                            color = if (isLiked) Color(0xFF38BDF8) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Comment Action
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showCommentSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Comments",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${currentReel.commentsCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Action
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            com.example.utils.ShareUtils.sharePost(context, currentReel.id, currentReel.caption)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share_arrow_curved),
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Share", color = Color.White, fontSize = 11.sp)
                    }
                }

                // Bottom Caption & Creator Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp, end = 80.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("profile") }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PurpleMain)
                        ) {
                            Text(currentReel.authorName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentReel.authorName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentReel.caption,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                }

                // Floating likes overlay on video
                if (floatingLikes == null && activeFloatingLikes.isNotEmpty()) {
                    FloatingLikesOverlay(likes = activeFloatingLikes)
                }

                // Comment Sheet for Video Reels
                if (showCommentSheet) {
                    com.example.ui.components.CommentSheet(
                        post = currentReel,
                        onDismiss = { showCommentSheet = false }
                    )
                }
            } else {
                // Clean empty state placeholder
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF020617)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = PurpleMain,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Reels available 🎬",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Be the first to post a Video Reel!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
