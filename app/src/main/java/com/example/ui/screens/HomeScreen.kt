package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CloudStory
import com.example.data.model.PostItem
import com.example.data.model.StoryItem
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.components.FloatingLike
import com.example.ui.components.FriendHubGlowingNeonLikeIcon
import com.example.ui.theme.*
import com.example.utils.ShareUtils
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<PostItem>,
    stories: List<StoryItem>,
    activeCloudStories: List<CloudStory>,
    floatingLikes: MutableList<FloatingLike>,
    unreadNotificationCount: Int = 3,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateClick: () -> Unit,
    onLiveTabClick: () -> Unit,
    onMarketplaceTabClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onOpenComments: (PostItem) -> Unit,
    onSharePost: (PostItem) -> Unit,
    onAddStoryClick: () -> Unit,
    onViewStoryClick: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("My Feed") }
    var postToShare by remember { mutableStateOf<PostItem?>(null) }
    val tabs = listOf("LIVE", "Friends", "My Feed", "Explore", "Market Place")
    val context = LocalContext.current
    val currentUid = try { FirebaseAuth.getInstance().currentUser?.uid ?: "current_user" } catch (e: Exception) { "current_user" }

    val displayPosts = posts

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val colors = LocalFriendHubColors.current
            // Top Bar matching exact screenshot design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glowing FH Logo + FriendHub Brand Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigate("home") }
                ) {
                    // Glowing circular FH badge with magenta/purple neon halo
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = colors.tertiary,
                                spotColor = colors.primary
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors.tertiary.copy(alpha = 0.5f),
                                        colors.primary.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(colors.tertiary, colors.primary, colors.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FH",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Brand Text: "Friend" (White) + "Hub" (Vibrant Magenta)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                                append("Friend")
                            }
                            withStyle(SpanStyle(color = colors.tertiary, fontWeight = FontWeight.Bold)) {
                                append("Hub")
                            }
                        },
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Search Icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.iconPrimary,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { onSearchClick() }
                )

                Spacer(modifier = Modifier.width(18.dp))

                // Notification Bell
                Box(
                    modifier = Modifier.clickable { onNotificationClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = colors.orangeGradient.run { Color(0xFFFFB800) }, // Use amber color for bell
                        modifier = Modifier.size(28.dp)
                    )
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colors.error, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Settings Gear
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.primary,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onSettingsClick() }
                )
            }
        },
        bottomBar = {
            CustomBottomNavBar(
                selectedRoute = "home",
                onNavigate = onNavigate
            )
        },
        containerColor = LocalFriendHubColors.current.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalFriendHubColors.current.background),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 32.dp,
                start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
            )
        ) {
            // Story Bar
            item {
                StoryBarSection(
                    currentUid = currentUid,
                    activeStories = activeCloudStories,
                    onAddStoryClick = onAddStoryClick,
                    onViewStoryClick = onViewStoryClick,
                    onProfileClick = { onNavigate("profile") }
                )
            }

            // Filter Tabs (LIVE, Friends, My Feed, Explore, Market Place)
            item {
                FilterTabsSection(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        if (tab == "LIVE") onLiveTabClick()
                        else if (tab == "Market Place") onMarketplaceTabClick()
                    }
                )
            }

            // Feed Posts List
            items(
                items = displayPosts.filter { post ->
                    when (selectedTab) {
                        "My Feed" -> true
                        "Friends" -> post.category == "Friends" || post.category == "My Feed"
                        else -> true
                    }
                },
                key = { it.id }
            ) { post ->
                PostCard(
                    post = post,
                    onLikeClick = { emoji ->
                        onToggleLike(post.id)
                        com.example.ui.components.spawnFloatingLikesBurst(
                            floatingLikes,
                            count = 28,
                            primaryEmoji = emoji ?: "👍"
                        )
                    },
                    onShareClick = {
                        postToShare = post
                        onSharePost(post)
                    },
                    onCommentClick = { onOpenComments(post) },
                    onProfileClick = { onNavigate("profile") }
                )
            }
        }
    }

    // Share Bottom Sheet matching exact screenshot design
    postToShare?.let { post ->
        val colors = LocalFriendHubColors.current
        ModalBottomSheet(
            onDismissRequest = { postToShare = null },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.cardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Share",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareOptionBtn(
                        label = "WhatsApp",
                        color = Color(0xFF25D366),
                        iconRes = R.drawable.ic_whatsapp
                    ) {
                        ShareUtils.shareToWhatsApp(context, post.caption, post.id)
                        postToShare = null
                    }

                    ShareOptionBtn(
                        label = "Facebook",
                        color = Color(0xFF1877F2),
                        iconRes = R.drawable.ic_facebook
                    ) {
                        ShareUtils.shareToFacebook(context, post.id)
                        postToShare = null
                    }

                    ShareOptionBtn(
                        label = "Copy Link",
                        color = colors.textSecondary,
                        iconRes = R.drawable.ic_link
                    ) {
                        ShareUtils.copyLink(context, post.id)
                        postToShare = null
                    }

                    ShareOptionBtn(
                        label = "More",
                        color = colors.primary,
                        iconRes = R.drawable.ic_more
                    ) {
                        ShareUtils.sharePost(context, post.id, post.caption)
                        postToShare = null
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryBarSection(
    currentUid: String,
    activeStories: List<CloudStory>,
    onAddStoryClick: () -> Unit,
    onViewStoryClick: (String) -> Unit,
    onProfileClick: () -> Unit = {}
) {


    val colors = LocalFriendHubColors.current
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. PROFILE SQUARE ITEM (FIRST)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Radiant purple/pink neon glow aura
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0xFFD946EF),
                                spotColor = Color(0xFFA855F7)
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFD946EF).copy(alpha = 0.55f),
                                        Color(0xFFA855F7).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                    )

                    // The vibrant magenta/purple gradient square button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE879F9), // light magenta/lavender at top
                                        Color(0xFFC026D3), // rich magenta in center
                                        Color(0xFF7C3AED)  // deep violet at bottom
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFF5D0FE).copy(alpha = 0.9f),
                                        Color(0xFFA855F7).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
                Text(
                    text = "Profile",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // 2. ADD STORY / YOUR STORY ITEM (SECOND)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onAddStoryClick() }
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                            .border(1.5.dp, colors.primary.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Status/Story",
                            tint = colors.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(
                    text = "Add Story",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // 2. USERS STORIES (CIRCLES WITH VIBRANT GRADIENT BORDER)
        items(activeStories, key = { it.storyId }) { story ->
            val borderBrush = Brush.sweepGradient(
                listOf(
                    Color(0xFFA855F7),
                    Color(0xFFEC4899),
                    Color(0xFFF59E0B),
                    Color(0xFFA855F7)
                )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.combinedClickable(
                    onClick = { onViewStoryClick(story.ownerId) },
                    onLongClick = { onViewStoryClick(story.ownerId) }
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(brush = borderBrush, shape = CircleShape)
                        .padding(2.5.dp)
                        .background(colors.background, CircleShape)
                        .padding(2.0.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = story.ownerAvatar.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80" },
                        contentDescription = story.ownerName,
                        placeholder = painterResource(R.drawable.ic_fh_logo_new),
                        error = painterResource(R.drawable.ic_fh_logo_new),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = story.ownerName,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FilterTabsSection(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val colors = LocalFriendHubColors.current
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(tabs) { name ->
            val selected = name == selectedTab
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .then(
                        if (selected) {
                            Modifier.shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = colors.secondary,
                                spotColor = colors.secondary
                            )
                        } else Modifier
                    )
                    .background(
                        color = if (selected) colors.secondary else colors.cardBackground,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = if (selected) 0.dp else 0.5.dp,
                        color = if (selected) Color.Transparent else colors.cardBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(name) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (name == "LIVE") {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(colors.error, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = name,
                        color = if (selected) Color.White else colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostItem,
    onLikeClick: (String?) -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val colors = LocalFriendHubColors.current
    var isLiked by remember(post.id, post.isLiked) { mutableStateOf(post.isLiked) }
    var likesCount by remember(post.id, post.likesCount) { mutableIntStateOf(post.likesCount) }
    var currentReaction by remember(post.id) { mutableStateOf("👍") }
    var showReactionMenu by remember { mutableStateOf(false) }
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val reactionEmojis = listOf("👍", "❤️", "🔥", "😂", "😮", "💜", "👏", "😍")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = BorderStroke(0.5.dp, colors.cardBorder)
    ) {
        Column {
            val context = LocalContext.current
            var showPostMenu by remember { mutableStateOf(false) }
            
            // Post Header (Avatar, Name, Time, Privacy, Three-Dot Option Menu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Avatar with initials (Clickable to open profile)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onProfileClick() }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = post.timeAgo,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(colors.textSecondary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Friends",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Three-dot button
                Box {
                    IconButton(onClick = { showPostMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = colors.textPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showPostMenu,
                        onDismissRequest = { showPostMenu = false },
                        modifier = Modifier.background(colors.cardBackground)
                    ) {
                        val isOwner = post.authorName.lowercase() == "sophiaa" || post.authorName.contains("sophia", ignoreCase = true)
                        if (isOwner) {
                            DropdownMenuItem(
                                text = { Text("✏️ Edit Post", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Edit Post opened! ✏️", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔒 Edit Privacy", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Privacy privileges updated ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("💬 Turn Comments On/Off", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post comment permissions toggled ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 Turn Reactions On/Off", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post reaction permissions toggled ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⬇️ Allow/Disable Media Download", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Media download permissions toggled ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📥 Save to Archive", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post saved to Archive! 📥✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📌 Pin to Profile", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post pinned to top of Profile! 📌✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Move to Trash", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Moved to Trash (Restoreable in Settings) 🗑️", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("❌ Delete Post Permanently", color = colors.error) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post permanently deleted ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("🔖 Save Post", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post saved to your Collection! 🔖✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("👤 Follow / Unfollow Author", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Author follow status updated! 👤✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔇 Mute Author", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Author muted 🔇", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🚫 Block Author", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Author blocked 🚫", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔔 Turn On Post Notifications", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Notifications enabled for this post 🔔✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔗 Copy Post Link", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post link copied to Clipboard! 🔗✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⬇️ Download Media", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    coroutineScope.launch {
                                        com.example.utils.DownloadUtils.downloadMediaToGallery(
                                            context = context,
                                            mediaUrl = post.imageUrl,
                                            isVideo = post.category == "Video"
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🚩 Report Post", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post reported to FriendHub Admins 🚩✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🙈 Hide Post", color = colors.textPrimary) },
                                onClick = {
                                    showPostMenu = false
                                    Toast.makeText(context, "Post hidden from Feed 🙈", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            // Description / Caption ABOVE photo/video (with expand/collapse)
            var isExpanded by remember { mutableStateOf(false) }
            val canExpand = post.caption.length > 80
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isExpanded || !canExpand) post.caption else post.caption.take(80) + "...",
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
                if (canExpand) {
                    Text(
                        text = if (isExpanded) "See less" else "See more",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Post Image with rounded corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black)
            ) {
                if (post.imageUrl.isEmpty() || post.imageUrl.startsWith("android.resource://") || post.imageUrl.contains("post_architecture")) {
                    Image(
                        painter = painterResource(R.drawable.post_architecture),
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                } else {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.post_architecture),
                        error = painterResource(R.drawable.post_architecture),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                }
            }

            // Post content and actions below image
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Floating Reaction Picker Bar
                AnimatedVisibility(
                    visible = showReactionMenu,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .background(Color(0xFF151E36), RoundedCornerShape(28.dp))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                                                scale.animateTo(
                                                    targetValue = 1.4f,
                                                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                                                )
                                                scale.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                                                )
                                            }
                                            onLikeClick(emoji)
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // Like row with larger 3D purple like + ">_ COMMAND" + Share arrow circle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Purple 3D Like + mixed emoji reaction
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                coroutineScope.launch {
                                    scale.animateTo(
                                        targetValue = 1.35f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    scale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                isLiked = !isLiked
                                likesCount = if (isLiked) likesCount + 1 else (likesCount - 1).coerceAtLeast(0)
                                onLikeClick(if (isLiked) currentReaction else "👍")
                            },
                            onLongClick = {
                                showReactionMenu = !showReactionMenu
                            }
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            FriendHubGlowingNeonLikeIcon(
                                isLiked = isLiked,
                                sizeDp = 38.dp,
                                scaleAnim = scale.value
                            )
                            if (isLiked && currentReaction != "👍") {
                                Text(
                                    text = currentReaction,
                                    fontSize = 13.sp,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "%,d".format(likesCount),
                            color = if (isLiked) Color(0xFF38BDF8) else WhiteText,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mini quick reaction trigger button
                    IconButton(
                        onClick = { showReactionMenu = !showReactionMenu },
                        modifier = Modifier.size(28.dp).padding(start = 2.dp)
                    ) {
                        Text(
                            text = currentReaction,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Comment Button
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF151E36), CircleShape)
                            .border(1.dp, Color(0xFF253356), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comments",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Share Arrow Circle with Curved Share Vector
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF151E36), CircleShape)
                            .border(1.dp, Color(0xFF253356), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share_arrow_curved),
                            contentDescription = "Share",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // "Liked by sophiaa and 1,249 others"
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = WhiteText)) {
                            append("Liked by ${post.authorHandle.removePrefix("@").ifEmpty { "sophiaa" }} ")
                        }
                        withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                            append("and ${"%,d".format((likesCount - 1).coerceAtLeast(0))} others")
                        }
                    },
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // View comments
                Text(
                    text = "View all ${post.commentsCount} comments",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onCommentClick() }
                )

                // Time ago
                Text(
                    text = "${post.timeAgo.uppercase()} AGO",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ShareOptionBtn(
    label: String,
    color: Color,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            color = WhiteText,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}


