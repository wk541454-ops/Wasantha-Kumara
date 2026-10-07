package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CloudStory
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.example.ui.theme.NeonDarkSurface
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.NeonPureBlack
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.editor.FlyingEmoji
import com.example.ui.editor.FloatingHeartsOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    activeStories: List<CloudStory>,
    initialUserId: String,
    currentUid: String,
    currentUserProfile: UserProfile,
    onNavigate: (String) -> Unit,
    onAddStoryToHighlight: (CloudStory) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Group stories by owner
    val groupedStories = remember(activeStories) {
        activeStories.groupBy { it.ownerId }.entries.toList()
    }

    if (groupedStories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(NeonPureBlack), contentAlignment = Alignment.Center) {
            Text("No active stories found.", color = Color.White)
        }
        LaunchedEffect(Unit) { delay(1000); onNavigate("home") }
        return
    }

    // Find starting user index
    var userIndex by remember {
        val idx = groupedStories.indexOfFirst { it.key == initialUserId }
        mutableStateOf(if (idx != -1) idx else 0)
    }

    val currentUserStories = groupedStories.getOrNull(userIndex)?.value ?: emptyList()
    var storyIndex by remember(userIndex) { mutableStateOf(0) }
    val currentStory = currentUserStories.getOrNull(storyIndex)

    var progress by remember { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    // Flying emojis state
    val flyingEmojis = remember { mutableStateListOf<FlyingEmoji>() }

    // Viewer states
    var showViewersSheet by remember { mutableStateOf(false) }
    var viewerProfiles by remember { mutableStateOf<List<Pair<UserProfile, String?>>>(emptyList()) } // Profile + Reaction emoji

    // Text reply state
    var replyText by remember { mutableStateOf("") }

    // Timer effect to advance progress bar
    LaunchedEffect(userIndex, storyIndex, isPaused) {
        if (isPaused) return@LaunchedEffect
        progress = 0f
        val duration = 5000L
        val interval = 50L
        val steps = duration / interval
        for (i in 1..steps) {
            delay(interval)
            progress = i.toFloat() / steps
        }
        // Advance story when timer finishes
        if (storyIndex < currentUserStories.size - 1) {
            storyIndex++
        } else if (userIndex < groupedStories.size - 1) {
            userIndex++
        } else {
            onNavigate("home")
        }
    }

    // Save story view locally & remotely
    LaunchedEffect(currentStory) {
        currentStory?.let {
            if (it.ownerId != currentUid) {
                AppRepository.addStoryViewer(it.ownerId, it.storyId, currentUid) {}
            }
        }
    }

    // Retrieve viewer profiles when viewer sheet is opened
    LaunchedEffect(showViewersSheet, currentStory) {
        if (showViewersSheet && currentStory != null) {
            val list = mutableListOf<Pair<UserProfile, String?>>()
            currentStory.viewers.forEach { (viewerId, _) ->
                AppRepository.getUserProfile(viewerId) { map ->
                    if (map != null) {
                        val p = UserProfile(
                            id = viewerId,
                            name = map["name"] as? String ?: "FriendHub User",
                            handle = map["handle"] as? String ?: "friend",
                            avatarUrl = map["profilePicUrl"] as? String ?: ""
                        )
                        val reactionEmoji = currentStory.reactions[viewerId]
                        list.add(Pair(p, reactionEmoji))
                    }
                }
            }
            viewerProfiles = list
        }
    }

    fun goNext() {
        if (storyIndex < currentUserStories.size - 1) {
            storyIndex++
        } else if (userIndex < groupedStories.size - 1) {
            userIndex++
        } else {
            onNavigate("home")
        }
    }

    fun goPrev() {
        if (storyIndex > 0) {
            storyIndex--
        } else if (userIndex > 0) {
            userIndex--
            storyIndex = (groupedStories[userIndex].value.size - 1).coerceAtLeast(0)
        } else {
            onNavigate("home")
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        if (currentStory == null) return@Scaffold

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Full Screen Image
            AsyncImage(
                model = currentStory.mediaUrl,
                contentDescription = "Story Media",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = { offset ->
                                if (offset.x < size.width / 3f) {
                                    goPrev()
                                } else {
                                    goNext()
                                    // Tap to throw floating heart
                                    val newId = System.currentTimeMillis()
                                    flyingEmojis.add(
                                        FlyingEmoji(
                                            id = newId,
                                            emoji = "❤️",
                                            initialX = offset.x - size.width / 2f,
                                            scale = Random.nextFloat() * 0.5f + 0.8f
                                        )
                                    )
                                }
                            }
                        )
                    },
                contentScale = ContentScale.Crop
            )

            // Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currentUserStories.forEachIndexed { idx, _ ->
                    val barProgress = when {
                        idx < storyIndex -> 1f
                        idx == storyIndex -> progress
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(barProgress)
                                .background(NeonPinkGlow)
                        )
                    }
                }
            }

            // Top Header: User Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonPurpleGlow, CircleShape)
                            .clickable { onNavigate("profile") }
                    ) {
                        AsyncImage(
                            model = currentStory.ownerAvatar.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80" },
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.clickable { onNavigate("profile") }) {
                        Text(
                            text = currentStory.ownerName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active Story • 24h",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Highlight or Add button
                    if (currentStory.ownerId == currentUid) {
                        IconButton(
                            onClick = {
                                onAddStoryToHighlight(currentStory)
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Highlight", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                AppRepository.deleteStory(currentStory.ownerId, currentStory.storyId) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Story Deleted! ✓", Toast.LENGTH_SHORT).show()
                                        onNavigate("home")
                                    }
                                }
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }

                    IconButton(
                        onClick = { onNavigate("home") },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Floating Hearts layer
            FloatingHeartsOverlay(
                flyingEmojis = flyingEmojis.toList(),
                onFinished = { id -> flyingEmojis.removeAll { it.id == id } }
            )

            // Bottom Actions (Interactive Emoji Stream Bar - No Command Text Bar)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .padding(bottom = 28.dp, start = 12.dp, end = 12.dp)
            ) {
                // Owner Views Badge if story belongs to current user
                if (currentStory.ownerId == currentUid) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { showViewersSheet = true }
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .border(1.dp, NeonPurpleGlow.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "Views", tint = NeonPinkGlow, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${currentStory.viewers.size} Story Views • Tap to list 👁️",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Scrollable Emoji Reaction Bar (20+ Unlimited-Tap Emojis)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    val fullEmojiList = listOf(
                        "❤️", "🔥", "😂", "😍", "👏", "😮", "🎉", "💯", 
                        "👑", "👍", "💜", "✨", "🙌", "🤩", "🥳", "😎", 
                        "🚀", "💖", "🌸", "⭐"
                    )
                    items(fullEmojiList) { emoji ->
                        val hasReacted = currentStory.reactions[currentUid] == emoji
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (hasReacted) NeonPurpleGlow.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f))
                                .border(
                                    width = if (hasReacted) 2.dp else 1.dp,
                                    color = if (hasReacted) NeonPinkGlow else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    // Spawn 30 animated flying emojis streaming upwards across the screen
                                    repeat(30) { index ->
                                        flyingEmojis.add(
                                            FlyingEmoji(
                                                id = System.currentTimeMillis() + index * 100000L + Random.nextLong(1, 9999999L),
                                                emoji = emoji,
                                                initialX = Random.nextInt(-240, 240).toFloat(),
                                                scale = Random.nextFloat() * 0.6f + 0.8f
                                            )
                                        )
                                    }
                                    AppRepository.addStoryReaction(
                                        storyOwnerId = currentStory.ownerId,
                                        storyId = currentStory.storyId,
                                        reactorUid = currentUid,
                                        emoji = emoji
                                    ) { success ->
                                        if (success) {
                                            Toast.makeText(context, "$emoji Stream Sent! 🚀", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }

            // Viewers Bottom Sheet
            if (showViewersSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showViewersSheet = false },
                    containerColor = NeonDarkSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Story Viewers (👁️ ${currentStory.viewers.size})",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (viewerProfiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No viewers yet. Share your story with friends! 🌿", color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.heightIn(max = 400.dp)
                            ) {
                                items(viewerProfiles) { (profile, reactionEmoji) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .clickable { 
                                                        showViewersSheet = false
                                                        onNavigate("profile") 
                                                    }
                                            ) {
                                                AsyncImage(
                                                    model = profile.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80" },
                                                    contentDescription = "Avatar",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Column(modifier = Modifier.clickable { 
                                                showViewersSheet = false
                                                onNavigate("profile") 
                                            }) {
                                                Text(profile.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Text("@${profile.handle} • viewed just now", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }

                                        if (reactionEmoji != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(NeonPurpleGlow.copy(alpha = 0.2f))
                                                    .border(1.dp, NeonPurpleGlow, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(reactionEmoji, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
