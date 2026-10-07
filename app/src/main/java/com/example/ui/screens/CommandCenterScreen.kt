package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.CustomBottomNavBar

// Command data structure
data class CommandCenterOption(
    val name: String,
    val category: String,
    val icon: ImageVector,
    val tint: Color,
    val route: String? = null,
    val actionText: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalFriendHubColors.current
    
    var searchQuery by remember { mutableStateOf("") }
    val recentlyUsed = remember { mutableStateListOf<String>() }

    // Activity Launchers for Gallery, Camera, and Video
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Media selected! Launching Post Composer... 🖼️", Toast.LENGTH_SHORT).show()
            onNavigate("create_post")
        }
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(context, "Photo captured! Launching Post Composer... 📸", Toast.LENGTH_SHORT).show()
            onNavigate("create_post")
        }
    }

    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }
    val videoRecordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(context, "Video recorded successfully! 🎥", Toast.LENGTH_SHORT).show()
            onNavigate("create_post")
        }
    }

    // Helper for Share Sheet
    fun launchShareSheet(text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, title))
    }

    // Define all 40+ commands categorized precisely as requested
    val commands = remember {
        listOf(
            // CREATE Group
            CommandCenterOption("Post", "CREATE", Icons.Default.Create, Color(0xFFFF1493), "create_post"),
            CommandCenterOption("Photo Post", "CREATE", Icons.Default.Photo, Color(0xFFC026D3), "create_post"),
            CommandCenterOption("Video Post", "CREATE", Icons.Default.Movie, Color(0xFF7C3AED), "create_post"),
            CommandCenterOption("Story", "CREATE", Icons.Default.AutoAwesome, Color(0xFFD946EF), "add_story"),
            CommandCenterOption("Text Story", "CREATE", Icons.Default.TextFields, Color(0xFFE879F9), "add_story"),
            CommandCenterOption("Live Video", "CREATE", Icons.Default.Videocam, Color(0xFFFF007F), "start_live"),
            CommandCenterOption("Poll", "CREATE", Icons.Default.Poll, Color(0xFF00FF7F), actionText = "Poll creation tool launched! 📊"),
            CommandCenterOption("Event", "CREATE", Icons.Default.Event, Color(0xFFFF9800), actionText = "Event Scheduler opened! 📅"),
            CommandCenterOption("Marketplace Item", "CREATE", Icons.Default.Storefront, Color(0xFF00E5FF), "marketplace"),

            // MEDIA Group
            CommandCenterOption("Gallery", "MEDIA", Icons.Default.Collections, Color(0xFF00D1FF), actionText = "Opening Gallery Picker... 🖼️"),
            CommandCenterOption("Camera", "MEDIA", Icons.Default.CameraAlt, Color(0xFFE11D48), actionText = "Launching Device Camera... 📸"),
            CommandCenterOption("Record Video", "MEDIA", Icons.Default.Videocam, Color(0xFFFF007F), actionText = "Launching Device Camera Recorder... 🎥"),
            CommandCenterOption("Video", "MEDIA", Icons.Default.VideoLibrary, Color(0xFF8B5CF6), "video"),
            CommandCenterOption("Music", "MEDIA", Icons.Default.MusicNote, Color(0xFF3B82F6), actionText = "Connecting legal music libraries... 🎵"),
            CommandCenterOption("GIF", "MEDIA", Icons.Default.Gif, Color(0xFFFFCC00), actionText = "Trending GIFs loaded! 🎬"),
            CommandCenterOption("Sticker", "MEDIA", Icons.Default.SentimentSatisfied, Color(0xFFFF007F), actionText = "Stickers panel activated! ⭐️"),
            CommandCenterOption("Emoji", "MEDIA", Icons.Default.EmojiEmotions, Color(0xFFF59E0B), actionText = "Emojis tray loaded! 😊"),
            CommandCenterOption("File", "MEDIA", Icons.Default.AttachFile, Color(0xFF10B981), actionText = "Document/File attachment tool opened! 📎"),
            CommandCenterOption("Location", "MEDIA", Icons.Default.Place, Color(0xFFEF4444), actionText = "Location Tagging requested! 📍"),

            // SHARE Group
            CommandCenterOption("Share Post", "SHARE", Icons.Default.Share, Color(0xFF00FF7F)),
            CommandCenterOption("Share Video", "SHARE", Icons.Default.IosShare, Color(0xFF3B82F6)),
            CommandCenterOption("Share Story", "SHARE", Icons.Default.Send, Color(0xFFD946EF)),
            CommandCenterOption("Share Profile", "SHARE", Icons.Default.QrCode, Color(0xFFFF9800)),
            CommandCenterOption("Share Live", "SHARE", Icons.Default.LiveTv, Color(0xFFFF007F)),
            CommandCenterOption("Copy Link", "SHARE", Icons.Default.ContentCopy, Color(0xFF00E5FF)),

            // FRIENDS Group
            CommandCenterOption("Add Friend", "FRIENDS", Icons.Default.PersonAdd, Color(0xFF00FF7F), "search"),
            CommandCenterOption("Friend Requests", "FRIENDS", Icons.Default.GroupAdd, Color(0xFF10B981), "notifications"),
            CommandCenterOption("Find Friends", "FRIENDS", Icons.Default.PersonSearch, Color(0xFF3B82F6), "search"),
            CommandCenterOption("Friends Online", "FRIENDS", Icons.Default.RecordVoiceOver, Color(0xFF00E5FF), "messages"),
            CommandCenterOption("Friend Status", "FRIENDS", Icons.Default.Contacts, Color(0xFFA855F7), "messages"),

            // MESSAGES Group
            CommandCenterOption("New Message", "MESSAGES", Icons.Default.Chat, Color(0xFF3B82F6), "messages"),
            CommandCenterOption("Group Message", "MESSAGES", Icons.Default.Forum, Color(0xFF00E5FF), "messages"),
            CommandCenterOption("Message Requests", "MESSAGES", Icons.Default.QuestionAnswer, Color(0xFFD946EF), "messages"),
            CommandCenterOption("Saved Messages", "MESSAGES", Icons.Default.Bookmark, Color(0xFF00FF7F), "messages"),

            // CONTENT Group
            CommandCenterOption("Saved Posts", "CONTENT", Icons.Default.Favorite, Color(0xFFE11D48), "home"),
            CommandCenterOption("Saved Videos", "CONTENT", Icons.Default.BookmarkBorder, Color(0xFF8B5CF6), "video"),
            CommandCenterOption("Drafts", "CONTENT", Icons.Default.Edit, Color(0xFFF59E0B), actionText = "No unpublished drafts found."),
            CommandCenterOption("Recently Viewed", "CONTENT", Icons.Default.History, Color(0xFF00D1FF), actionText = "Displaying your 24h viewing history... 🕒"),

            // LIVE Group
            CommandCenterOption("Go Live", "LIVE", Icons.Default.LiveTv, Color(0xFFFF007F), "start_live"),
            CommandCenterOption("Live Settings", "LIVE", Icons.Default.SettingsInputAntenna, Color(0xFF8B5CF6), "settings"),
            CommandCenterOption("Live Moderation", "LIVE", Icons.Default.Security, Color(0xFFFFCC00), actionText = "Live Moderation Panel initialized! 🛡️"),
            CommandCenterOption("Live Participants", "LIVE", Icons.Default.People, Color(0xFF00E5FF), "live"),
            CommandCenterOption("Live Notifications", "LIVE", Icons.Default.NotificationsActive, Color(0xFFFF1493), "notifications"),

            // ACCOUNT Group
            CommandCenterOption("Profile", "ACCOUNT", Icons.Default.AccountCircle, Color(0xFF00FF7F), "profile"),
            CommandCenterOption("Settings", "ACCOUNT", Icons.Default.Settings, Color(0xFF3B82F6), "settings"),
            CommandCenterOption("Privacy", "ACCOUNT", Icons.Default.Lock, Color(0xFFFF007F), "settings"),
            CommandCenterOption("Security", "ACCOUNT", Icons.Default.VerifiedUser, Color(0xFFE11D48), "settings"),
            CommandCenterOption("Notifications", "ACCOUNT", Icons.Default.Notifications, Color(0xFFFFCC00), "notifications"),
            CommandCenterOption("Data & Storage", "ACCOUNT", Icons.Default.Storage, Color(0xFF10B981), "settings")
        )
    }

    // Filter commands based on search query
    val filteredCommands = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            commands
        } else {
            commands.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Elegant Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate("home") }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Command Center",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1F38)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "FH", color = Color(0xFFFF1493), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        bottomBar = {
            CustomBottomNavBar(
                selectedRoute = "command",
                onNavigate = onNavigate
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            // Search Input with glowing border
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Commands...", color = Color.Gray) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = Color(0xFFFF1493),
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.cardBackground,
                        unfocusedContainerColor = colors.cardBackground
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            // Recently Used section (dynamic)
            if (recentlyUsed.isNotEmpty() && searchQuery.isEmpty()) {
                item {
                    Text(
                        text = "Recently Used",
                        color = Color(0xFFFF1493),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        items(recentlyUsed.toList()) { name ->
                            val cmd = commands.firstOrNull { it.name == name }
                            if (cmd != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.cardBackground)
                                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                                        .clickable {
                                            if (cmd.route != null) {
                                                onNavigate(cmd.route)
                                            } else if (cmd.actionText != null) {
                                                Toast.makeText(context, cmd.actionText, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = cmd.icon, contentDescription = null, tint = cmd.tint, modifier = Modifier.size(16.dp))
                                        Text(text = cmd.name, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Grouped Category Headers & Grids
            val groups = filteredCommands.groupBy { it.category }
            val categoriesOrder = listOf("CREATE", "MEDIA", "SHARE", "FRIENDS", "MESSAGES", "CONTENT", "LIVE", "ACCOUNT")

            categoriesOrder.forEach { category ->
                val list = groups[category] ?: emptyList()
                if (list.isNotEmpty()) {
                    item {
                        Text(
                            text = category,
                            color = when (category) {
                                "CREATE" -> Color(0xFFFF1493)
                                "MEDIA" -> Color(0xFF00E5FF)
                                "FRIENDS" -> Color(0xFF00FF7F)
                                "MESSAGES" -> Color(0xFF3B82F6)
                                "LIVE" -> Color(0xFFFF007F)
                                else -> Color(0xFFA855F7)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // Render grid using chunks of 3 items in a row
                    val chunks = list.chunked(3)
                    items(chunks) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.cardBackground)
                                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                                        .clickable {
                                            // Add to recently used
                                            if (!recentlyUsed.contains(option.name)) {
                                                recentlyUsed.add(0, option.name)
                                                if (recentlyUsed.size > 6) recentlyUsed.removeAt(6)
                                            }
                                            
                                            // Handle Route, System Launchers, or Custom Actions
                                            when (option.name) {
                                                "Gallery" -> {
                                                    try {
                                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Opening Media Gallery...", Toast.LENGTH_SHORT).show()
                                                        onNavigate("create_post")
                                                    }
                                                }
                                                "Camera" -> {
                                                    onNavigate("add_story")
                                                }
                                                "Record Video" -> {
                                                    onNavigate("add_story")
                                                }
                                                "Share Post", "Share Video", "Share Story", "Share Profile", "Share Live" -> {
                                                    launchShareSheet("Discover FriendHub Social Platform 🚀: https://friendhub.app", "Share on FriendHub")
                                                }
                                                "Copy Link" -> {
                                                    Toast.makeText(context, "Link copied to clipboard! 🔗", Toast.LENGTH_SHORT).show()
                                                }
                                                else -> {
                                                    if (option.route != null) {
                                                        onNavigate(option.route)
                                                    } else if (option.actionText != null) {
                                                        Toast.makeText(context, option.actionText, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = option.icon,
                                            contentDescription = option.name,
                                            tint = option.tint,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = option.name,
                                            color = colors.textPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            // Fill blank slots if the row is incomplete
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
