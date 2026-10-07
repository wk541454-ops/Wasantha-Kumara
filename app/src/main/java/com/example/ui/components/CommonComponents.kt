package com.example.ui.components

import androidx.compose.animation.core.*
import com.example.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NeonBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalFriendHubColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "background_pulse")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Floating ambient neon flares
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top Left Flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.4f),
                        colors.tertiary.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.1f + animOffset),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.15f, size.height * 0.1f + animOffset),
                radius = size.width * 0.7f
            )

            // Right Upper Flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.tertiary.copy(alpha = 0.3f),
                        colors.primary.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.22f - animOffset * 0.5f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.22f - animOffset * 0.5f),
                radius = size.width * 0.5f
            )
        }

        content()
    }
}

@Composable
fun GlowingBadgeLogo(
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        // Outer Neon Rim Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(16.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonPinkGlow, NeonPurpleGlow, Color.Transparent)
                    )
                )
        )

        // 3D Glossy Button Circle with FH Logo Image
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize(0.92f)
                .shadow(16.dp, CircleShape, ambientColor = NeonPinkGlow, spotColor = NeonPurpleGlow)
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_fh_logo_new),
                contentDescription = "FH Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TopNavBar(
    unreadNotificationCount: Int = 3,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoClick: () -> Unit = {}
) {
    val colors = LocalFriendHubColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo + FriendHub
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLogoClick() }
        ) {
            Image(
                painter = painterResource(R.drawable.ic_fh_logo_new),
                contentDescription = "FriendHub Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FriendHub",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Search
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = colors.textSecondary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onSearchClick() }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Notifications
        Box(
            modifier = Modifier.clickable { onNotificationClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFFFF8C42),
                modifier = Modifier.size(28.dp)
            )
            if (unreadNotificationCount > 0) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Settings
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = colors.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onSettingsClick() }
        )
    }
}

@Composable
fun CustomBottomNavBar(
    selectedRoute: String,
    unreadMessageCount: Int = 3,
    isAdmin: Boolean = false,
    onNavigate: (String) -> Unit
) {
    val colors = LocalFriendHubColors.current
    val isHome = selectedRoute == "home"
    val isPost = selectedRoute == "create_post"
    val isCommand = selectedRoute == "command"
    val isVideo = selectedRoute == "video"
    val isMessages = selectedRoute == "messages"

    NavigationBar(
        containerColor = colors.cardBackground,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 0.dp
    ) {
        // 1. Home
        NavigationBarItem(
            selected = isHome,
            onClick = { onNavigate("home") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (isHome) Color(0xFFFF1493) else colors.textSecondary,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Home",
                        color = if (isHome) colors.textPrimary else colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isHome) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(2.5.dp)
                                .background(colors.primary, RoundedCornerShape(2.dp))
                        )
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        // 2. Post
        NavigationBarItem(
            selected = isPost,
            onClick = { onNavigate("create_post") },
            icon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(2.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Post",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Post",
                    color = if (isPost) Color(0xFF00E5FF) else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isPost) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        // 3. Command
        NavigationBarItem(
            selected = isCommand,
            onClick = { onNavigate("command") },
            icon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isCommand) Color(0xFFFF1493).copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                        .border(1.dp, if (isCommand) Color(0xFFFF1493) else Color(0xFFE879F9).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ">_",
                        color = if (isCommand) Color(0xFFFF1493) else Color(0xFFE879F9),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            label = {
                Text(
                    text = "Command",
                    color = if (isCommand) Color(0xFFFF1493) else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isCommand) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        // 4. Video
        NavigationBarItem(
            selected = isVideo,
            onClick = { onNavigate("video") },
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_custom_video_1791305243021),
                    contentDescription = "Video",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            label = {
                Text(
                    text = "Video",
                    color = if (isVideo) colors.primary else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isVideo) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        // 5. Messages
        NavigationBarItem(
            selected = isMessages,
            onClick = { onNavigate("messages") },
            icon = {
                Box(modifier = Modifier.size(34.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_custom_msg_1791305270609),
                        contentDescription = "Messages",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                    if (unreadMessageCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFFF2222), CircleShape)
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$unreadMessageCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = {
                Text(
                    text = "Messages",
                    color = if (isMessages) colors.primary else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isMessages) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}

/**
 * 3D Glassy Neon Cyan Like Orb with illuminated light turning-on radiance effect!
 */
@Composable
fun FriendHubGlowingNeonLikeIcon(
    isLiked: Boolean,
    sizeDp: Dp = 40.dp,
    scaleAnim: Float = 1f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_glow_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(sizeDp + 8.dp)
            .scale(scaleAnim),
        contentAlignment = Alignment.Center
    ) {
        // Radiant glowing light aura when liked (Light turned-on effect!)
        if (isLiked) {
            // Ambient Cyan Light Radiance
            Box(
                modifier = Modifier
                    .size(sizeDp + 12.dp)
                    .shadow(
                        elevation = 22.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFF00E5FF),
                        ambientColor = Color(0xFF00B0FF)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = pulseAlpha * 0.75f),
                                Color(0xFF00B0FF).copy(alpha = pulseAlpha * 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Outer Neon Cyan Ring Light
            Box(
                modifier = Modifier
                    .size(sizeDp + 4.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF38BDF8),
                                Color(0xFF00E5FF),
                                Color(0xFF80D8FF),
                                Color(0xFF00E5FF)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // 3D Glassy Neon Cyan Like Orb from Photo
        Image(
            painter = painterResource(R.drawable.ic_like_neon_cyan),
            contentDescription = "3D Neon Like",
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
        )
    }
}
