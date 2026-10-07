package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.config.AppConfig
import com.example.data.model.PostItem
import com.example.data.model.UserProfile
import com.example.ui.components.CommentSheet
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.components.FloatingLike
import com.example.ui.components.FloatingLikesOverlay
import com.example.ui.components.FriendHubGlowingNeonLikeIcon
import com.example.ui.components.spawnFloatingLikesBurst
import com.example.ui.theme.*
import com.example.data.repository.AppRepository
import com.example.utils.ShareUtils
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    userPosts: List<PostItem>,
    onUpdateProfile: (String, String, String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    val currentUid = auth?.currentUser?.uid ?: "current_user_123"

    // Dynamic State for Cover & Profile Photos
    var coverUrl by remember {
        mutableStateOf(
            userProfile.coverPhotoUrl.ifEmpty {
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80"
            }
        )
    }
    var profilePicUrl by remember { mutableStateOf(userProfile.avatarUrl) }

    // Dynamic State for Profile Info
    var currentName by remember { mutableStateOf(userProfile.name.ifEmpty { "Wasantha Kumara" }) }
    var currentHandle by remember { mutableStateOf(userProfile.handle.ifEmpty { "Wasantha_K" }) }
    var currentBio by remember { mutableStateOf(userProfile.bio.ifEmpty { "Dream • Learn • Build • Grow 🌿" }) }
    var currentBioLine2 by remember { mutableStateOf(userProfile.bioLine2.ifEmpty { "Technology | Travel | Photography | Good Vibes" }) }
    var currentLocation by remember { mutableStateOf(userProfile.location.ifEmpty { "Pelmadulla, Sri Lanka" }) }
    var currentRole by remember { mutableStateOf(userProfile.role.ifEmpty { "Self Learner" }) }

    // Interactive States
    val profileFloatingLikes = remember { mutableStateListOf<FloatingLike>() }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Posts, 1: Photos, 2: Videos
    var activeCommentPost by remember { mutableStateOf<PostItem?>(null) }
    var selectedPhotoForViewer by remember { mutableStateOf<String?>(null) }
    var postToShare by remember { mutableStateOf<PostItem?>(null) }

    // Dialogs & Sheets
    var showEditModal by remember { mutableStateOf(false) }
    var showPhotoOptionsSheet by remember { mutableStateOf(false) }
    var isCoverSelected by remember { mutableStateOf(false) }
    var showThreeDotsSheet by remember { mutableStateOf(false) }
    var activeUserListType by remember { mutableStateOf<String?>(null) }
    var activeHighlightDetail by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showCreateHighlightDialog by remember { mutableStateOf(false) }

    // Highlights State
    val highlightsList = remember {
        mutableStateListOf<Map<String, Any>>()
    }

    // Real-time synchronization loader
    LaunchedEffect(Unit) {
        try {
            AppRepository.getUserProfile(currentUid) { data ->
                if (data != null) {
                    val name = data["fullName"] as? String ?: data["name"] as? String
                    if (!name.isNullOrBlank()) currentName = name
                    val handle = data["handle"] as? String
                    if (!handle.isNullOrBlank()) currentHandle = handle
                    val bio = data["bio"] as? String
                    if (!bio.isNullOrBlank()) currentBio = bio
                    val bio2 = data["bioLine2"] as? String
                    if (!bio2.isNullOrBlank()) currentBioLine2 = bio2
                    val loc = data["location"] as? String
                    if (!loc.isNullOrBlank()) currentLocation = loc
                    val role = data["role"] as? String
                    if (!role.isNullOrBlank()) currentRole = role
                    val pPic = data["profilePicUrl"] as? String ?: data["avatarUrl"] as? String
                    if (!pPic.isNullOrBlank()) profilePicUrl = pPic
                    val cPic = data["coverPhotoUrl"] as? String
                    if (!cPic.isNullOrBlank()) coverUrl = cPic
                }
            }
        } catch (e: Exception) {}
    }

    // Photo Picker
    val colors = LocalFriendHubColors.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
            val fileType = if (isCoverSelected) "cover" else "avatar"
            val ref = storage.getReference("users/$currentUid/$fileType.jpg")
            
            Toast.makeText(context, "Uploading photo...", Toast.LENGTH_SHORT).show()
            
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        val publicUrl = downloadUri.toString()
                        if (isCoverSelected) {
                            coverUrl = publicUrl
                            try {
                                AppRepository.getUserProfile(currentUid) { oldData ->
                                    val newData = (oldData ?: emptyMap()).toMutableMap()
                                    newData["coverPhotoUrl"] = publicUrl
                                    AppRepository.updateUserProfile(currentUid, newData) {}
                                }
                            } catch (e: Exception) {}
                            Toast.makeText(context, "Cover Photo Updated! ✓", Toast.LENGTH_SHORT).show()
                        } else {
                            profilePicUrl = publicUrl
                            try {
                                AppRepository.getUserProfile(currentUid) { oldData ->
                                    val newData = (oldData ?: emptyMap()).toMutableMap()
                                    newData["profilePicUrl"] = publicUrl
                                    newData["avatarUrl"] = publicUrl
                                    AppRepository.updateUserProfile(currentUid, newData) {}
                                }
                            } catch (e: Exception) {}
                            Toast.makeText(context, "Profile Photo Updated! ✓", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            // Clean Top Bar in BlackBg
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigate("home") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "@$currentHandle",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { onNavigate("search") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { showThreeDotsSheet = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        bottomBar = {
            CustomBottomNavBar(selectedRoute = "profile", onNavigate = onNavigate)
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. COVER PHOTO & PROFILE AVATAR
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    // Cover Image with dark gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                isCoverSelected = true
                                showPhotoOptionsSheet = true
                            }
                    ) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = "Cover Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Smooth gradient into BlackBg
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            colors.background.copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )

                        // Camera edit badge on cover photo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .background(colors.cardSelected.copy(alpha = 0.85f), CircleShape)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .clickable {
                                    isCoverSelected = true
                                    showPhotoOptionsSheet = true
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Edit Cover",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Overlapping Avatar with Purple Neon Halo Border
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 20.dp)
                            .size(96.dp)
                    ) {
                        // Radiant purple aura
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(
                                    elevation = 14.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFFD946EF),
                                    spotColor = PurpleMain
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            PurpleMain.copy(alpha = 0.45f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Inner Avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BlackBg)
                                .border(
                                    width = 3.dp,
                                    brush = Brush.linearGradient(
                                        listOf(PurpleMain, PinkMain, Color(0xFF7C3AED))
                                    ),
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .clickable {
                                    isCoverSelected = false
                                    showPhotoOptionsSheet = true
                                }
                        ) {
                            if (profilePicUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profilePicUrl,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(PurpleMain, Color(0xFF7C3AED)))
                                        )
                                ) {
                                    Text(
                                        text = currentName.take(2).uppercase(),
                                        color = WhiteText,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Online Green Dot with Black border
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BlackBg)
                                .padding(2.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    }

                    // Online Status Chip next to avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 126.dp, bottom = 8.dp)
                            .background(PillBg, RoundedCornerShape(20.dp))
                            .border(1.dp, BorderGray, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Online • Creator",
                                color = WhiteText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. NAME & HANDLE SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentName,
                            color = WhiteText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = PurpleMain,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "@$currentHandle",
                        color = GrayText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // 3. BIO CARD SECTION (CardBg #1A1A1A with BorderGray #2A2A2A)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderGray, BorderGray)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentBio,
                            color = WhiteText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (currentBioLine2.isNotBlank()) {
                            Text(
                                text = currentBioLine2,
                                color = GrayText,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Metadata Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileBadgeChip(icon = Icons.Default.LocationOn, text = currentLocation)
                            ProfileBadgeChip(icon = Icons.Default.School, text = currentRole)
                            ProfileBadgeChip(icon = Icons.Default.CalendarToday, text = userProfile.joinedDate.ifEmpty { "Joined 2025" })
                        }
                    }
                }
            }

            // 4. STATS CARD (Posts, Followers, Following, Friends)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderGray, BorderGray)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(
                            count = "${userPosts.size.coerceAtLeast(248)}",
                            label = "Posts"
                        ) { activeTab = 0 }

                        ProfileStatItem(
                            count = "1.2K",
                            label = "Followers"
                        ) { activeUserListType = "Followers" }

                        ProfileStatItem(
                            count = "${userProfile.followingCount.coerceAtLeast(320)}",
                            label = "Following"
                        ) { activeUserListType = "Following" }

                        ProfileStatItem(
                            count = "${userProfile.friendsCount.coerceAtLeast(56)}",
                            label = "Friends"
                        ) { activeUserListType = "Friends" }
                    }
                }
            }

            // 5. ACTION BUTTONS ROW (Edit Profile, Share, Settings)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit Profile (Purple Gradient Pill)
                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(listOf(PurpleMain, Color(0xFF7C3AED)))
                            )
                            .clickable { showEditModal = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = WhiteText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Profile",
                                color = WhiteText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Share Profile (Dark Pill)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PillBg)
                            .border(1.dp, BorderGray, RoundedCornerShape(24.dp))
                            .clickable {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out @$currentHandle on FriendHub: https://gen-lang-client-0417982200.web.app/u/$currentHandle")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = WhiteText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share",
                                color = WhiteText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Settings Icon Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PillBg)
                            .border(1.dp, BorderGray, CircleShape)
                            .clickable { onNavigate("menu") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PurpleMain,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 6. STORY HIGHLIGHTS ROW
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "HIGHLIGHTS",
                        color = PurpleMain,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Highlights Items
                        items(highlightsList) { highlight ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { activeHighlightDetail = highlight }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(66.dp)
                                        .background(
                                            brush = Brush.linearGradient(listOf(PurpleMain, PinkMain)),
                                            shape = CircleShape
                                        )
                                        .padding(2.dp)
                                        .background(BlackBg, CircleShape)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                ) {
                                    AsyncImage(
                                        model = highlight["img"].toString(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = highlight["title"].toString(),
                                    color = WhiteText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Add Highlight (+)
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showCreateHighlightDialog = true }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(66.dp)
                                        .background(PillBg, CircleShape)
                                        .border(1.5.dp, BorderGray, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Highlight",
                                        tint = WhiteText,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("New", color = GrayText, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 7. CONTENT FILTER TABS (Posts, Photos, Videos)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tabs = listOf("Posts", "Photos", "Videos")
                    tabs.forEachIndexed { index, tabName ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PurpleMain else PillBg)
                                .border(
                                    1.dp,
                                    if (isSelected) PurpleMain else BorderGray,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { activeTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabName,
                                color = if (isSelected) WhiteText else GrayText,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 8. TAB CONTENTS
            when (activeTab) {
                0 -> {
                    // POSTS TAB (Feed Cards matching HomeScreen exact screenshot 1 design)
                    val postsToShow = userPosts

                    items(postsToShow, key = { it.id }) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Column {
                                // 1. Post Header (Avatar, Name, Time, Privacy, Three-Dot Option Menu)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circle Avatar with initials
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary),
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
                                            fontSize = 14.sp
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
                                }

                                // 2. Description / Caption ABOVE photo/video (with expand/collapse)
                                var isExpanded by remember { mutableStateOf(false) }
                                val canExpand = post.caption.length > 80
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isExpanded || !canExpand) post.caption else post.caption.take(80) + "...",
                                        color = WhiteText,
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

                                // 3. Post Image
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
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        AsyncImage(
                                            model = post.imageUrl,
                                            contentDescription = post.caption,
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(R.drawable.post_architecture),
                                            error = painterResource(R.drawable.post_architecture),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // 4. Content & Actions below Image
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Glowing 3D Neon Like + count
                                        var isProfilePostLiked by remember(post.id) { mutableStateOf(post.isLiked) }
                                        var profileLikesCount by remember(post.id) { mutableIntStateOf(post.likesCount) }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                isProfilePostLiked = !isProfilePostLiked
                                                profileLikesCount += if (isProfilePostLiked) 1 else -1
                                                spawnFloatingLikesBurst(profileFloatingLikes, count = 28, primaryEmoji = "👍")
                                                Toast.makeText(context, if (isProfilePostLiked) "Liked! 💙" else "Unliked", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            FriendHubGlowingNeonLikeIcon(
                                                isLiked = isProfilePostLiked,
                                                sizeDp = 38.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "%,d".format(profileLikesCount),
                                                color = if (isProfilePostLiked) Color(0xFF38BDF8) else WhiteText,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // ">_ COMMAND" Pill
                                        Box(
                                            modifier = Modifier
                                                .background(PillBg, RoundedCornerShape(20.dp))
                                                .border(1.dp, BorderGray, RoundedCornerShape(20.dp))
                                                .clickable { onNavigate("command") }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = ">_ COMMAND",
                                                color = GrayText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Standard recognizable Comment Button with icon
                                        IconButton(
                                            onClick = { activeCommentPost = post },
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

                                        // Share Arrow Circle
                                        IconButton(
                                            onClick = { postToShare = post },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(PillBg, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = WhiteText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Liked by line
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = WhiteText)) {
                                                append("Liked by ${post.authorName.ifEmpty { currentName }} ")
                                            }
                                            withStyle(SpanStyle(color = GrayText)) {
                                                append("and ${"%,d".format((post.likesCount - 1).coerceAtLeast(0))} others")
                                            }
                                        },
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "View all ${post.commentsCount} comments",
                                        color = GrayText,
                                        fontSize = 13.sp,
                                        modifier = Modifier.clickable { activeCommentPost = post }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // PHOTOS TAB (3-column square grid)
                    val photoUrls = listOf(
                        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80",
                        "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=400&q=80",
                        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=400&q=80",
                        "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=400&q=80",
                        "https://images.unsplash.com/photo-1511884642898-4c92249e20b6?auto=format&fit=crop&w=400&q=80",
                        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=400&q=80"
                    )

                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            items(photoUrls) { url ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPhotoForViewer = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // VIDEOS TAB
                    val videosList = listOf(
                        Pair("Neon City Walk Vlog 🌃", "12.4K views • 0:45"),
                        Pair("Sri Lanka Mountain Drone Footage 🌄", "8.9K views • 1:20"),
                        Pair("Coding FriendHub Jetpack Compose App 💻", "25.1K views • 2:15")
                    )

                    items(videosList) { (title, meta) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { Toast.makeText(context, "Playing '$title' Reel", Toast.LENGTH_SHORT).show() },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderGray, BorderGray)))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(listOf(PurpleMain, Color(0xFF7C3AED)))
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = WhiteText,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(meta, color = GrayText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------- DIALOGS & BOTTOM SHEETS -------------------

    // 1. EDIT PROFILE DIALOG
    if (showEditModal) {
        var editName by remember { mutableStateOf(currentName) }
        var editHandle by remember { mutableStateOf(currentHandle) }
        var editBio by remember { mutableStateOf(currentBio) }
        var editBio2 by remember { mutableStateOf(currentBioLine2) }
        var editLoc by remember { mutableStateOf(currentLocation) }
        var editRole by remember { mutableStateOf(currentRole) }

        AlertDialog(
            onDismissRequest = { showEditModal = false },
            title = {
                Text(
                    text = "Edit Profile",
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = PurpleMain,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editHandle,
                        onValueChange = { editHandle = it },
                        label = { Text("Handle (@username)", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = PurpleMain,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = GrayText) },
                        singleLine = false,
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = PurpleMain,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBio2,
                        onValueChange = { editBio2 = it },
                        label = { Text("Interests / Sub Bio", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = PurpleMain,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editLoc,
                        onValueChange = { editLoc = it },
                        label = { Text("Location", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = PurpleMain,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editRole,
                        onValueChange = { editRole = it },
                        label = { Text("Role / Interest", color = GrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = PurpleMain,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentName = editName
                        currentHandle = editHandle
                        currentBio = editBio
                        currentBioLine2 = editBio2
                        currentLocation = editLoc
                        currentRole = editRole

                        try {
                            val map = mapOf(
                                "fullName" to editName,
                                "name" to editName,
                                "handle" to editHandle,
                                "bio" to editBio,
                                "bioLine2" to editBio2,
                                "location" to editLoc,
                                "role" to editRole
                            )
                            AppRepository.getUserProfile(currentUid) { oldData ->
                                val newData = (oldData ?: emptyMap()).toMutableMap()
                                newData.putAll(map)
                                AppRepository.updateUserProfile(currentUid, newData) {}
                            }
                        } catch (e: Exception) {}

                        onUpdateProfile(editName, editBio, editHandle)
                        showEditModal = false
                        Toast.makeText(context, "Profile updated! ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
                ) {
                    Text("Save", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditModal = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = CardBg
        )
    }

    // 2. PHOTO OPTIONS BOTTOM SHEET (Camera / Gallery)
    if (showPhotoOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoOptionsSheet = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = if (isCoverSelected) "Change Cover Photo" else "Change Profile Photo",
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileSheetRow(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Choose from Gallery",
                    tint = PurpleMain
                ) {
                    showPhotoOptionsSheet = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }

                ProfileSheetRow(
                    icon = Icons.Default.Visibility,
                    title = "View Photo Full Size",
                    tint = WhiteText
                ) {
                    showPhotoOptionsSheet = false
                    selectedPhotoForViewer = if (isCoverSelected) coverUrl else profilePicUrl
                }

                ProfileSheetRow(
                    icon = Icons.Default.Delete,
                    title = "Remove Photo",
                    tint = Color(0xFFFF4B4B)
                ) {
                    showPhotoOptionsSheet = false
                    if (isCoverSelected) {
                        coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80"
                        try {
                            AppRepository.getUserProfile(currentUid) { oldData ->
                                val newData = (oldData ?: emptyMap()).toMutableMap()
                                newData["coverPhotoUrl"] = ""
                                AppRepository.updateUserProfile(currentUid, newData) {}
                            }
                        } catch (e: Exception) {}
                    } else {
                        profilePicUrl = ""
                        try {
                            AppRepository.getUserProfile(currentUid) { oldData ->
                                val newData = (oldData ?: emptyMap()).toMutableMap()
                                newData["profilePicUrl"] = ""
                                newData["avatarUrl"] = ""
                                AppRepository.updateUserProfile(currentUid, newData) {}
                            }
                        } catch (e: Exception) {}
                    }
                    Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // 3. THREE DOTS MENU SHEET
    if (showThreeDotsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThreeDotsSheet = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text("Profile Options", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ProfileSheetRow(icon = Icons.Outlined.ContentCopy, title = "Copy Profile Link", tint = WhiteText) {
                    showThreeDotsSheet = false
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Profile Link", "https://gen-lang-client-0417982200.web.app/u/$currentHandle"))
                    Toast.makeText(context, "Profile link copied to clipboard! ✓", Toast.LENGTH_SHORT).show()
                }

                ProfileSheetRow(icon = Icons.Outlined.Share, title = "Share Profile via Apps", tint = PurpleMain) {
                    showThreeDotsSheet = false
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out @$currentHandle on FriendHub: https://gen-lang-client-0417982200.web.app/u/$currentHandle")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
                }

                ProfileSheetRow(icon = Icons.Outlined.Settings, title = "Settings & Privacy", tint = WhiteText) {
                    showThreeDotsSheet = false
                    onNavigate("menu")
                }

                ProfileSheetRow(icon = Icons.Outlined.Report, title = "Report an Issue", tint = Color(0xFFFF8C42)) {
                    showThreeDotsSheet = false
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
                            putExtra(Intent.EXTRA_SUBJECT, "Report User Profile: @$currentHandle")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Report sent to ${AppConfig.CONTACT_EMAIL}", Toast.LENGTH_LONG).show()
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // 4. USER LIST BOTTOM SHEET (Followers / Following / Friends)
    if (activeUserListType != null) {
        ModalBottomSheet(
            onDismissRequest = { activeUserListType = null },
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("$activeUserListType", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                val sampleUserList = listOf(
                    Pair("Sarah James", "@sarahjms"),
                    Pair("Niko Vance", "@niko"),
                    Pair("Rudraksh Sharma", "@rudraksh"),
                    Pair("Franklin Sr", "@franklin_")
                )

                sampleUserList.forEach { (name, handle) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(PurpleMain, Color(0xFF7C3AED)))
                                )
                        ) {
                            Text(name.take(2).uppercase(), color = WhiteText, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(handle, color = GrayText, fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .background(PillBg, RoundedCornerShape(16.dp))
                                .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                                .clickable {
                                    Toast.makeText(context, "Connected with $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Connect", fontSize = 12.sp, color = WhiteText, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // 5. HIGHLIGHT DETAIL SHEET
    if (activeHighlightDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { activeHighlightDetail = null },
            containerColor = BlackBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeHighlightDetail!!["title"].toString(),
                    color = WhiteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = activeHighlightDetail!!["img"].toString(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { activeHighlightDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 6. CREATE HIGHLIGHT DIALOG
    if (showCreateHighlightDialog) {
        var newHighlightTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateHighlightDialog = false },
            title = { Text("New Highlight", color = WhiteText, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newHighlightTitle,
                    onValueChange = { newHighlightTitle = it },
                    placeholder = { Text("e.g. Vacation 🏖️", color = GrayText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        focusedBorderColor = PurpleMain,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHighlightTitle.isNotBlank()) {
                            highlightsList.add(
                                mapOf(
                                    "id" to "${System.currentTimeMillis()}",
                                    "title" to newHighlightTitle,
                                    "img" to "https://images.unsplash.com/photo-1511884642898-4c92249e20b6?auto=format&fit=crop&w=400&q=80"
                                )
                            )
                            showCreateHighlightDialog = false
                            Toast.makeText(context, "Highlight '$newHighlightTitle' added! ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
                ) {
                    Text("Create", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateHighlightDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = CardBg
        )
    }

    // 7. PHOTO VIEWER DIALOG
    if (selectedPhotoForViewer != null) {
        AlertDialog(
            onDismissRequest = { selectedPhotoForViewer = null },
            title = { Text("Photo Preview", color = WhiteText, fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = selectedPhotoForViewer!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPhotoForViewer = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
                ) {
                    Text("Close", color = WhiteText)
                }
            },
            containerColor = CardBg
        )
    }

    // 8. SHARE BOTTOM SHEET
    postToShare?.let { post ->
        ModalBottomSheet(
            onDismissRequest = { postToShare = null },
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Share Post",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareOptionItem(label = "WhatsApp", color = Color(0xFF25D366), iconRes = R.drawable.ic_whatsapp) {
                        ShareUtils.shareToWhatsApp(context, post.caption, post.id)
                        postToShare = null
                    }
                    ShareOptionItem(label = "Facebook", color = Color(0xFF1877F2), iconRes = R.drawable.ic_facebook) {
                        ShareUtils.shareToFacebook(context, post.id)
                        postToShare = null
                    }
                    ShareOptionItem(label = "Copy Link", color = GrayText, iconRes = R.drawable.ic_link) {
                        ShareUtils.copyLink(context, post.id)
                        postToShare = null
                    }
                    ShareOptionItem(label = "More", color = BlueFeed, iconRes = R.drawable.ic_more) {
                        ShareUtils.sharePost(context, post.id, post.caption)
                        postToShare = null
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // 9. COMMENTS SHEET
    if (activeCommentPost != null) {
        CommentSheet(
            post = activeCommentPost!!,
            onDismiss = { activeCommentPost = null }
        )
    }

    if (profileFloatingLikes.isNotEmpty()) {
        FloatingLikesOverlay(likes = profileFloatingLikes)
    }
}
}

// ------------------- HELPER COMPOSABLES -------------------

@Composable
fun ProfileBadgeChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(PillBg, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = GrayText, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun ProfileStatItem(count: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Text(text = count, color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = GrayText, fontSize = 12.sp)
    }
}

@Composable
fun ProfileSheetRow(icon: ImageVector, title: String, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = WhiteText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ShareOptionItem(label: String, color: Color, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = GrayText, fontSize = 11.sp)
    }
}
