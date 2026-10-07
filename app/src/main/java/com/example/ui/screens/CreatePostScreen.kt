package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.remote.GeminiService
import com.example.ui.components.NeonBackground
import com.example.ui.editor.CameraHelper
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onCreatePost: (String, String, String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "user_123"
    val scope = rememberCoroutineScope()

    var caption by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("My Feed") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isVideoMode by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // FEATURE 3: Privacy Mode Selector & Post Toggles
    var selectedPrivacy by remember { mutableStateOf("public") } // "public", "friends", "followers", "only_me", "custom"
    var commentsEnabled by remember { mutableStateOf(true) }
    var reactionsEnabled by remember { mutableStateOf(true) }
    var downloadAllowed by remember { mutableStateOf(true) }

    // FEATURE 2: Voice Post States
    var voicePostUri by remember { mutableStateOf<Uri?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceDurationSeconds by remember { mutableIntStateOf(0) }

    // Feature 3: Location, Mood & Filter for Photo Posts
    var selectedLocation by remember { mutableStateOf("Pelmadulla, Sri Lanka") }
    var selectedMood by remember { mutableStateOf("Feeling Blessed ✨") }

    val categories = listOf("My Feed", "Friends", "Explore")
    val locationsList = listOf("Pelmadulla, Sri Lanka", "Colombo, Sri Lanka", "Kandy, Sri Lanka", "Galle, Sri Lanka", "San Francisco, CA")
    val moodsList = listOf("Feeling Blessed ✨", "Exploring 🏞️", "Building & Growing 🚀", "Neon Vibes 💜", "Good Times 🎉")

    // Voice Post Simulated Timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceDurationSeconds = 0
            while (voiceDurationSeconds < 30 && isRecordingVoice) {
                delay(1000)
                voiceDurationSeconds++
            }
            if (isRecordingVoice) {
                isRecordingVoice = false
                voicePostUri = Uri.parse("simulated_voice_post_${System.currentTimeMillis()}.mp3")
                Toast.makeText(context, "30s Voice Post Recorded successfully! 🎤✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Media launchers
    val photoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            selectedVideoUri = null
            voicePostUri = null
        }
    }

    val videoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedImageUri = null
            voicePostUri = null
        }
    }

    val photoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
            selectedVideoUri = null
            voicePostUri = null
        }
    }

    val videoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedVideoUri = tempCameraUri
            selectedImageUri = null
            voicePostUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val (file, uri) = CameraHelper.createImageFile(context, false, uid)
                tempCameraUri = uri
                if (isVideoMode) {
                    videoCameraLauncher.launch(uri)
                } else {
                    photoCameraLauncher.launch(uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error launching camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Post", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            isUploading = true
                            scope.launch(Dispatchers.Main) {
                                // If Privacy is Only Me + AI, call Gemini API in background!
                                var aiReply = ""
                                if (selectedPrivacy == "private") {
                                    Toast.makeText(context, "Consulting Gemini AI Vibe Partner... 🤖🧠", Toast.LENGTH_SHORT).show()
                                    aiReply = GeminiService.generateMotivationalReply(caption)
                                }

                                uploadCompletePost(
                                    context = context,
                                    uid = uid,
                                    text = caption,
                                    category = selectedCategory,
                                    location = selectedLocation,
                                    mood = selectedMood,
                                    privacy = selectedPrivacy,
                                    aiReply = aiReply,
                                    imageUri = selectedImageUri,
                                    videoUri = selectedVideoUri,
                                    voiceUri = voicePostUri
                                ) { finalCaption, mediaUrl ->
                                    // Local database mapping callback
                                    onCreatePost(finalCaption, mediaUrl, selectedCategory)
                                    isUploading = false
                                    Toast.makeText(context, "Post shared successfully! ✓", Toast.LENGTH_SHORT).show()
                                    onNavigate("home")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Post", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
            )
        },
        containerColor = NeonPureBlack
    ) { innerPadding ->
        NeonBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // IMAGE / VIDEO / VOICE PREVIEW BOX
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(NeonDarkSurface)
                        .border(1.dp, NeonCardBorder, RoundedCornerShape(18.dp))
                        .clickable {
                            isVideoMode = false
                            showMediaSheet = true
                        }
                ) {
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Clear button
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else if (selectedVideoUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🎥 Video Selected:\n${selectedVideoUri?.lastPathSegment ?: "video.mp4"}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // Clear button
                            IconButton(
                                onClick = { selectedVideoUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else if (voicePostUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F0B1E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = NeonPinkGlow, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🎤 Voice Post Selected (30s)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Ready to broadcast with neon waveform!", color = NeonCyan, fontSize = 11.sp)
                            }

                            // Clear button
                            IconButton(
                                onClick = { voicePostUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else if (isRecordingVoice) {
                        // RECORDING VOICE SCREEN PULSE WAVEFORM PREVIEW
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎤 RECORDING VOICE POST...", color = NeonMagenta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(8) { index ->
                                    val height = (10..40).random()
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(height.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(NeonCyan)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("0:${voiceDurationSeconds.toString().padStart(2, '0')} / 0:30", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { isRecordingVoice = false; voicePostUri = Uri.parse("voice_post_${System.currentTimeMillis()}.mp3") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("STOP & SAVE", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to select photo / video", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FB-style Quick Add bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add to your post", color = Color.White, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        IconButton(onClick = {
                            isVideoMode = false
                            showMediaSheet = true
                        }) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.Green)
                        }
                        IconButton(onClick = {
                            isVideoMode = true
                            showMediaSheet = true
                        }) {
                            Icon(Icons.Default.Videocam, null, tint = Color.Red)
                        }
                        // FEATURE 2: Quick Voice Post Button
                        IconButton(onClick = {
                            isRecordingVoice = true
                            selectedImageUri = null
                            selectedVideoUri = null
                            voicePostUri = null
                        }) {
                            Icon(Icons.Default.Mic, null, tint = NeonPinkGlow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FEATURE 3: PRIVACY SELECTOR
                Text("POST PRIVACY / VISIBILITY:", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                val privacyOptions = listOf(
                    Triple("Public 🌍", "public", Icons.Default.Public),
                    Triple("Friends 👥", "friends", Icons.Default.People),
                    Triple("Followers 🔔", "followers", Icons.Default.Notifications),
                    Triple("Only Me 🔒", "only_me", Icons.Default.Lock),
                    Triple("Custom ⚙️", "custom", Icons.Default.Settings)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(privacyOptions) { (label, key, icon) ->
                        val isSelected = key == selectedPrivacy
                        Surface(
                            color = if (isSelected) NeonMagenta else NeonDarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else NeonCardBorder),
                            modifier = Modifier
                                .clickable {
                                    selectedPrivacy = key
                                    if (key == "custom") {
                                        Toast.makeText(context, "Custom audience selected! 👥✓", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // POST INTERACTION CONTROLS
                Text("POST SETTINGS:", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(NeonDarkSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, NeonCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Allow Comments 💬", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = commentsEnabled,
                            onCheckedChange = { commentsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple)
                        )
                    }
                    HorizontalDivider(color = NeonCardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Allow Emoji Reactions 🔥", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = reactionsEnabled,
                            onCheckedChange = { reactionsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple)
                        )
                    }
                    HorizontalDivider(color = NeonCardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Allow Media Downloads ⬇️", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = downloadAllowed,
                            onCheckedChange = { downloadAllowed = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LOCATION STICKER PICKER
                Text("LOCATION STICKER (Lanka Spot 📍):", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(locationsList) { loc ->
                        val isSelected = loc == selectedLocation
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLocation = loc },
                            label = { Text(loc, fontSize = 11.sp, color = if (isSelected) Color.White else NeonTextMuted) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, containerColor = NeonDarkSurface)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // MOOD STICKER PICKER
                Text("MOOD / ACTIVITY:", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(moodsList) { mood ->
                        val isSelected = mood == selectedMood
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = mood },
                            label = { Text(mood, fontSize = 11.sp, color = if (isSelected) Color.White else NeonTextMuted) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonMagenta, containerColor = NeonDarkSurface)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("CAPTION / DIARY ENTRY", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("What's on your mind? If private mode is chosen, Gemini AI will write a helpful motivational encouragement reply!", color = NeonTextSubtle) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeonDarkSurface,
                        unfocusedContainerColor = NeonDarkSurface,
                        focusedBorderColor = NeonMagenta,
                        unfocusedBorderColor = NeonCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("CATEGORY", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, color = if (isSelected) Color.White else NeonTextMuted) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta,
                                containerColor = NeonDarkSurface
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal BottomSheet for media options (Camera & Gallery picker)
    if (showMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMediaSheet = false },
            containerColor = NeonDarkSurface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isVideoMode) "Video එකක් දාන්න" else "Photo එකක් දාන්න",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = {
                        Text(
                            text = if (isVideoMode) "📹 Camera එකෙන් Video ගන්න" else "📷 Camera එකෙන් Photo ගන්න",
                            color = Color.White
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        showMediaSheet = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                ListItem(
                    headlineContent = { Text("🖼️ Gallery එකෙන් තෝරන්න", color = Color.White) },
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        showMediaSheet = false
                        val request = PickVisualMediaRequest(
                            if (isVideoMode) ActivityResultContracts.PickVisualMedia.VideoOnly else ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                        if (isVideoMode) {
                            videoGalleryLauncher.launch(request)
                        } else {
                            photoGalleryLauncher.launch(request)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

fun uploadCompletePost(
    context: Context,
    uid: String,
    text: String,
    category: String,
    location: String,
    mood: String,
    privacy: String,
    aiReply: String,
    imageUri: Uri?,
    videoUri: Uri?,
    voiceUri: Uri?,
    onComplete: (String, String) -> Unit
) {
    val db = FirebaseDatabase.getInstance().reference
    val postId = db.child("posts").push().key ?: "post_${System.currentTimeMillis()}"

    val finalCaption = if (text.isBlank()) {
        "Exploring $location • $mood"
    } else {
        "$text\n📍 $location • $mood"
    }

    val type = when {
        imageUri != null -> "photo"
        videoUri != null -> "video"
        voiceUri != null -> "voice"
        else -> "text"
    }

    val saveLocalAndRtdb = { mediaUrl: String ->
        val map = mapOf(
            "postId" to postId,
            "ownerId" to uid,
            "text" to finalCaption,
            "mediaUrl" to mediaUrl,
            "type" to type,
            "timestamp" to System.currentTimeMillis(),
            "likes" to 0,
            "privacy" to privacy,
            "locationName" to location
        )

        if (privacy == "private") {
            // Save to private secret diary node
            val privateMap = map + mapOf("aiReply" to aiReply)
            db.child("posts_private/$uid/$postId").setValue(privateMap)
        } else {
            // Save to public database node
            db.child("posts/$postId").setValue(map)
            db.child("users/$uid/public_profile/posts/$postId").setValue(true)
        }

        // Lanka Spot mapping - save coordinates (mock coordinates Centered on Sri Lanka)
        if (location.isNotEmpty() && (imageUri != null || videoUri != null)) {
            val (x, y) = when (location) {
                "Pelmadulla, Sri Lanka" -> Pair(0.52f, 0.65f)
                "Colombo, Sri Lanka" -> Pair(0.35f, 0.58f)
                "Galle, Sri Lanka" -> Pair(0.42f, 0.88f)
                "Kandy, Sri Lanka" -> Pair(0.54f, 0.48f)
                else -> Pair(0.5f, 0.5f)
            }
            val spotMap = mapOf(
                "placeName" to location,
                "photoUrl" to mediaUrl,
                "userId" to uid,
                "xPercent" to x,
                "yPercent" to y
            )
            db.child("spots/$postId").setValue(spotMap)
        }

        onComplete(finalCaption, mediaUrl)
    }

    try {
        val storage = FirebaseStorage.getInstance()
        if (imageUri != null) {
            val ref = storage.getReference("posts/${uid}/${postId}.jpg")
            ref.putFile(imageUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        saveLocalAndRtdb(url.toString())
                    }.addOnFailureListener {
                        saveLocalAndRtdb(imageUri.toString())
                    }
                }
                .addOnFailureListener {
                    saveLocalAndRtdb(imageUri.toString())
                }
        } else if (videoUri != null) {
            val ref = storage.getReference("posts/${uid}/${postId}.mp4")
            ref.putFile(videoUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        saveLocalAndRtdb(url.toString())
                    }.addOnFailureListener {
                        saveLocalAndRtdb(videoUri.toString())
                    }
                }
                .addOnFailureListener {
                    saveLocalAndRtdb(videoUri.toString())
                }
        } else if (voiceUri != null) {
            // Voice simulated uploads
            saveLocalAndRtdb(voiceUri.toString())
        } else {
            saveLocalAndRtdb("")
        }
    } catch (e: Exception) {
        val mediaUrl = imageUri?.toString() ?: videoUri?.toString() ?: voiceUri?.toString() ?: ""
        saveLocalAndRtdb(mediaUrl)
    }
}
