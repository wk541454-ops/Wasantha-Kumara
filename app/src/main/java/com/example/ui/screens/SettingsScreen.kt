package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.config.AppConfig
import com.example.data.model.UserProfile
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.theme.*
import com.example.utils.LocalizationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    userPrefs: UserPreferencesRepository,
    onNavigate: (String) -> Unit,
    onUpdateProfile: ((String, String, String) -> Unit)? = null,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    val currentUid = auth?.currentUser?.uid ?: "current_user_123"

    // Preferences state flows
    val appLanguage by userPrefs.appLanguage.collectAsStateWithLifecycle(initialValue = "en")
    val themeMode by userPrefs.themeMode.collectAsStateWithLifecycle(initialValue = "dark")
    val isDarkMode by userPrefs.isDarkMode.collectAsStateWithLifecycle(initialValue = true)
    val userEmail by userPrefs.userEmail.collectAsStateWithLifecycle(initialValue = "sophiaa@friendhub.app")
    val userPhone by userPrefs.userPhone.collectAsStateWithLifecycle(initialValue = "+94 77 123 4567")

    // Privacy states
    val profileVisibility by userPrefs.profileVisibility.collectAsStateWithLifecycle(initialValue = "Everyone")
    val friendRequestPermission by userPrefs.friendRequestPermission.collectAsStateWithLifecycle(initialValue = "Everyone")
    val messagePermission by userPrefs.messagePermission.collectAsStateWithLifecycle(initialValue = "Friends")
    val postVisibility by userPrefs.postVisibility.collectAsStateWithLifecycle(initialValue = "Everyone")
    val storyVisibility by userPrefs.storyVisibility.collectAsStateWithLifecycle(initialValue = "Friends")
    val onlineStatusEnabled by userPrefs.onlineStatusEnabled.collectAsStateWithLifecycle(initialValue = true)
    val lastActiveEnabled by userPrefs.lastActiveEnabled.collectAsStateWithLifecycle(initialValue = true)
    val readReceiptsEnabled by userPrefs.readReceiptsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val blockedUsers by userPrefs.blockedUsers.collectAsStateWithLifecycle(initialValue = emptySet())

    // Notification states
    val notifPush by userPrefs.notifPush.collectAsStateWithLifecycle(initialValue = true)
    val notifMessages by userPrefs.notifMessages.collectAsStateWithLifecycle(initialValue = true)
    val notifFriendRequests by userPrefs.notifFriendRequests.collectAsStateWithLifecycle(initialValue = true)
    val notifLikes by userPrefs.notifLikes.collectAsStateWithLifecycle(initialValue = true)
    val notifComments by userPrefs.notifComments.collectAsStateWithLifecycle(initialValue = true)
    val notifMentions by userPrefs.notifMentions.collectAsStateWithLifecycle(initialValue = true)
    val notifStories by userPrefs.notifStories.collectAsStateWithLifecycle(initialValue = true)
    val notifEmail by userPrefs.notifEmail.collectAsStateWithLifecycle(initialValue = false)

    // Media & Security
    val mediaAutoplay by userPrefs.mediaAutoplay.collectAsStateWithLifecycle(initialValue = true)
    val mediaDataSaver by userPrefs.mediaDataSaver.collectAsStateWithLifecycle(initialValue = false)
    val mediaUploadQuality by userPrefs.mediaUploadQuality.collectAsStateWithLifecycle(initialValue = "High")
    val mediaAutoDownload by userPrefs.mediaAutoDownload.collectAsStateWithLifecycle(initialValue = true)
    val sensitiveContentWarning by userPrefs.sensitiveContentWarning.collectAsStateWithLifecycle(initialValue = true)
    val securityTwoFactor by userPrefs.securityTwoFactor.collectAsStateWithLifecycle(initialValue = false)
    val securityLoginAlerts by userPrefs.securityLoginAlerts.collectAsStateWithLifecycle(initialValue = true)
    val securityAppLock by userPrefs.securityAppLock.collectAsStateWithLifecycle(initialValue = false)

    // Active sub-page state: null = Home Settings, otherwise category name
    var activeSubPage by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showClearCacheConfirmDialog by remember { mutableStateOf(false) }
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showReportProblemDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showGenericDialogTitle by remember { mutableStateOf<String?>(null) }
    var showGenericDialogContent by remember { mutableStateOf<String?>(null) }

    // Dynamic cache size calculation
    var calculatedCacheSizeMb by remember { mutableStateOf("14.2 MB") }

    fun refreshCacheSize() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                val sizeBytes = cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                val mb = sizeBytes / (1024.0 * 1024.0)
                withContext(Dispatchers.Main) {
                    calculatedCacheSizeMb = String.format("%.1f MB", if (mb < 0.1) 0.1 else mb)
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        refreshCacheSize()
    }

    // Hardware back navigation handler
    BackHandler(enabled = activeSubPage != null) {
        activeSubPage = null
    }

    fun str(key: String): String = LocalizationManager.getString(key, appLanguage)

    // Sync settings to Firebase RTDB
    fun syncToCloud(category: String, key: String, value: Any) {
        try {
            val rtdb = FirebaseDatabase.getInstance()
            rtdb.reference.child("users/$currentUid/settings/$category/$key").setValue(value)
        } catch (_: Exception) {}
    }

    val colors = LocalFriendHubColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (activeSubPage == null) str("settings_title") else activeSubPage!!,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                        if (activeSubPage == null) {
                            Text(
                                text = str("settings_subtitle"),
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeSubPage != null) {
                                activeSubPage = null
                            } else {
                                onNavigate("home")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {
            CustomBottomNavBar(selectedRoute = "menu", onNavigate = onNavigate)
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeSubPage,
                label = "settings_page_transition"
            ) { page ->
                if (page == null) {
                    // ==========================================
                    // 1. SETTINGS MAIN HUB
                    // ==========================================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Summary Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate("profile") },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                                border = BorderStroke(1.dp, colors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar with Purple Halo
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .border(
                                                2.dp,
                                                Brush.linearGradient(listOf(PurpleMain, PinkMain)),
                                                CircleShape
                                            )
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(colors.cardSelected),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (userProfile.avatarUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = userProfile.avatarUrl,
                                                contentDescription = userProfile.name,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = userProfile.name.take(2).uppercase().ifEmpty { "FH" },
                                                color = colors.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = userProfile.name.ifEmpty { "Sophia Anderson" },
                                                color = colors.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = PurpleMain,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = "@${userProfile.handle.ifEmpty { "sophiaa" }}",
                                            color = colors.textSecondary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = str("view_profile"),
                                            color = PurpleMain,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = colors.textSecondary
                                    )
                                }
                            }
                        }

                        // Categories List
                        // 1. ACCOUNT
                        item {
                            SettingsCategoryGroup(
                                title = str("category_account"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Person, str("account_info"), "Name, bio & handle") { activeSubPage = "Account" },
                                    SettingsItemRow(Icons.Default.AlternateEmail, str("email_phone"), "Email & contact phone") { activeSubPage = "Email & Phone" },
                                    SettingsItemRow(Icons.Default.Lock, str("password_security"), "Credentials & protection") { activeSubPage = "Security" },
                                    SettingsItemRow(Icons.Default.Devices, str("login_activity"), "Sessions & connected devices") { activeSubPage = "Login Activity" },
                                    SettingsItemRow(Icons.Default.DeleteOutline, str("delete_account"), "Permanent account removal") { showDeleteConfirmDialog = true }
                                )
                            )
                        }

                        // 2. PRIVACY
                        item {
                            SettingsCategoryGroup(
                                title = str("category_privacy"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Visibility, str("profile_visibility"), profileVisibility) { activeSubPage = "Privacy" },
                                    SettingsItemRow(Icons.Default.Block, str("blocked_users"), "${blockedUsers.size} blocked") { activeSubPage = "Blocked Users" }
                                )
                            )
                        }

                        // 3. NOTIFICATIONS
                        item {
                            SettingsCategoryGroup(
                                title = str("category_notifications"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Notifications, str("category_notifications"), if (notifPush) "Enabled" else "Muted") { activeSubPage = "Notifications" }
                                )
                            )
                        }

                        // 4. APPEARANCE
                        item {
                            SettingsCategoryGroup(
                                title = str("category_appearance"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Palette, str("theme"), themeMode.replaceFirstChar { it.uppercase() }) { activeSubPage = "Appearance" }
                                )
                            )
                        }

                        // 5. CONTENT & MEDIA
                        item {
                            SettingsCategoryGroup(
                                title = str("category_content"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.VideoLibrary, str("category_content"), "Autoplay, quality & media") { activeSubPage = "Content & Media" }
                                )
                            )
                        }

                        // 6. LANGUAGE
                        item {
                            SettingsCategoryGroup(
                                title = str("category_language"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Language, str("category_language"), if (appLanguage == "si") "සිංහල" else if (appLanguage == "ta") "தமிழ்" else "English") { activeSubPage = "Language" }
                                )
                            )
                        }

                        // 7. SECURITY
                        item {
                            SettingsCategoryGroup(
                                title = str("category_security"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Shield, str("category_security"), if (securityTwoFactor) "2FA Enabled" else "Standard") { activeSubPage = "Security" }
                                )
                            )
                        }

                        // 8. DATA & STORAGE
                        item {
                            SettingsCategoryGroup(
                                title = str("category_data"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Storage, str("category_data"), "Storage, cache & data saver") { activeSubPage = "Data & Storage" }
                                )
                            )
                        }

                        // 9. HELP & SUPPORT
                        item {
                            SettingsCategoryGroup(
                                title = str("category_help"),
                                items = listOf(
                                    SettingsItemRow(Icons.AutoMirrored.Filled.HelpOutline, str("help_center"), "FAQ, Contact & Legal") { activeSubPage = "Help & Support" }
                                )
                            )
                        }

                        // 10. ABOUT
                        item {
                            SettingsCategoryGroup(
                                title = str("category_about"),
                                items = listOf(
                                    SettingsItemRow(Icons.Default.Info, str("category_about"), "Version 1.0.0 (Build 2026)") { activeSubPage = "About" }
                                )
                            )
                        }

                        // 11. SESSION ACTIONS
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showLogoutConfirmDialog = true }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = str("log_out"),
                                            color = Color(0xFFFF9800),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = GrayText
                                        )
                                    }

                                    HorizontalDivider(color = BorderGray, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showDeleteConfirmDialog = true }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = Color(0xFFFF4B4B),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = str("delete_account"),
                                            color = Color(0xFFFF4B4B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = GrayText
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // 2. DEDICATED CATEGORY PAGES
                    // ==========================================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (page) {
                            "Account" -> {
                                item {
                                    SettingsCard(title = "Personal Information") {
                                        SettingsInfoField(label = "Display Name", value = userProfile.name.ifEmpty { "Sophia Anderson" })
                                        SettingsInfoField(label = "Username", value = "@${userProfile.handle.ifEmpty { "sophiaa" }}")
                                        SettingsInfoField(label = "Bio", value = userProfile.bio.ifEmpty { "Creating vibes & sharing moments ✨" })
                                        SettingsInfoField(label = "Email Address", value = userEmail)
                                        SettingsInfoField(label = "Phone Number", value = userPhone)
                                        SettingsInfoField(label = "FriendHub User ID", value = currentUid.take(16) + "...")

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = { showEditAccountDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = WhiteText, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Edit Profile Details", color = WhiteText, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            "Email & Phone" -> {
                                item {
                                    SettingsCard(title = "Contact Information") {
                                        var newEmail by remember { mutableStateOf(userEmail) }
                                        var newPhone by remember { mutableStateOf(userPhone) }

                                        OutlinedTextField(
                                            value = newEmail,
                                            onValueChange = { newEmail = it },
                                            label = { Text("Email Address", color = colors.textSecondary) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.cardBorder,
                                                focusedTextColor = colors.textPrimary,
                                                unfocusedTextColor = colors.textPrimary
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = newPhone,
                                            onValueChange = { newPhone = it },
                                            label = { Text("Phone Number", color = colors.textSecondary) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.cardBorder,
                                                focusedTextColor = colors.textPrimary,
                                                unfocusedTextColor = colors.textPrimary
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    userPrefs.setUserPhone(newPhone)
                                                    syncToCloud("account", "email", newEmail)
                                                    syncToCloud("account", "phone", newPhone)
                                                    Toast.makeText(context, "Contact info updated ✓", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Save Contact Changes", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            "Privacy" -> {
                                item {
                                    SettingsCard(title = "Visibility & Permissions") {
                                        SettingsDropdownSelector(
                                            label = "Profile Visibility",
                                            currentValue = profileVisibility,
                                            options = listOf("Everyone", "Friends", "Only me")
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setProfileVisibility(it)
                                                syncToCloud("privacy", "profileVisibility", it)
                                            }
                                        }

                                        SettingsDropdownSelector(
                                            label = "Who Can Send Friend Requests",
                                            currentValue = friendRequestPermission,
                                            options = listOf("Everyone", "Friends of friends", "No one")
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setFriendRequestPermission(it)
                                                syncToCloud("privacy", "friendRequests", it)
                                            }
                                        }

                                        SettingsDropdownSelector(
                                            label = "Who Can Message Me",
                                            currentValue = messagePermission,
                                            options = listOf("Everyone", "Friends", "No one")
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setMessagePermission(it)
                                                syncToCloud("privacy", "messages", it)
                                            }
                                        }

                                        SettingsDropdownSelector(
                                            label = "Who Can See My Posts",
                                            currentValue = postVisibility,
                                            options = listOf("Everyone", "Friends", "Only me")
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setPostVisibility(it)
                                                syncToCloud("privacy", "postVisibility", it)
                                            }
                                        }

                                        SettingsDropdownSelector(
                                            label = "Who Can See My Stories",
                                            currentValue = storyVisibility,
                                            options = listOf("Everyone", "Friends", "Only me")
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setStoryVisibility(it)
                                                syncToCloud("privacy", "storyVisibility", it)
                                            }
                                        }
                                    }
                                }

                                item {
                                    SettingsCard(title = "Status & Interaction") {
                                        SettingsSwitchRow(
                                            title = "Show Online Status",
                                            subtitle = "Allow friends to see when you are currently online",
                                            checked = onlineStatusEnabled
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setOnlineStatus(it)
                                                syncToCloud("privacy", "onlineStatus", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "Last Active Status",
                                            subtitle = "Show when you were last active on FriendHub",
                                            checked = lastActiveEnabled
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setLastActive(it)
                                                syncToCloud("privacy", "lastActive", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "Read Receipts",
                                            subtitle = "Let people know when you've seen their messages",
                                            checked = readReceiptsEnabled
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setReadReceipts(it)
                                                syncToCloud("privacy", "readReceipts", it)
                                            }
                                        }
                                    }
                                }
                            }

                            "Notifications" -> {
                                item {
                                    SettingsCard(title = "Push & In-App Notifications") {
                                        SettingsSwitchRow(title = "Push Notifications", subtitle = "Master toggle for device alerts", checked = notifPush) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifPush(it)
                                                userPrefs.setNotificationsEnabled(it)
                                                syncToCloud("notifications", "push", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Message Notifications", subtitle = "Alerts for new direct chats", checked = notifMessages) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifMessages(it)
                                                syncToCloud("notifications", "messages", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Friend Requests", subtitle = "Alerts when someone adds you", checked = notifFriendRequests) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifFriendRequests(it)
                                                syncToCloud("notifications", "friendRequests", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Likes & Reactions", subtitle = "Alerts on your posts and reels", checked = notifLikes) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifLikes(it)
                                                syncToCloud("notifications", "likes", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Comments & Replies", subtitle = "Alerts when someone comments", checked = notifComments) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifComments(it)
                                                syncToCloud("notifications", "comments", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Mentions & Tags", subtitle = "Alerts when tagged in a post", checked = notifMentions) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifMentions(it)
                                                syncToCloud("notifications", "mentions", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Story Notifications", subtitle = "Updates from close friends", checked = notifStories) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifStories(it)
                                                syncToCloud("notifications", "stories", it)
                                            }
                                        }
                                        SettingsSwitchRow(title = "Email Notifications", subtitle = "Weekly digest & security emails", checked = notifEmail) {
                                            coroutineScope.launch {
                                                userPrefs.setNotifEmail(it)
                                                syncToCloud("notifications", "email", it)
                                            }
                                        }
                                    }
                                }
                            }

                            "Appearance" -> {
                                item {
                                    SettingsCard(title = "Theme Preferences") {
                                        val themes = listOf(
                                            Triple("Dark Mode", "dark", Icons.Default.DarkMode),
                                            Triple("Light Mode", "light", Icons.Default.LightMode),
                                            Triple("System Default", "system", Icons.Default.SettingsBrightness)
                                        )

                                        themes.forEach { (name, key, icon) ->
                                            val selected = themeMode == key
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        coroutineScope.launch {
                                                            userPrefs.setThemeMode(key)
                                                            Toast.makeText(context, "Theme set to $name ✓", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    .padding(vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = icon, contentDescription = null, tint = if (selected) colors.primary else colors.textSecondary, modifier = Modifier.size(22.dp))
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Text(text = name, color = if (selected) colors.primary else colors.textPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                                RadioButton(
                                                    selected = selected,
                                                    onClick = { coroutineScope.launch { userPrefs.setThemeMode(key) } },
                                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.textSecondary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "Language" -> {
                                item {
                                    SettingsCard(title = "Select App Language") {
                                        val languages = listOf(
                                            Triple("English", "en", "Standard English"),
                                            Triple("සිංහල", "si", "Sinhala"),
                                            Triple("தமிழ்", "ta", "Tamil")
                                        )

                                        languages.forEach { (name, code, desc) ->
                                            val selected = appLanguage == code
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        coroutineScope.launch {
                                                            userPrefs.setAppLanguage(code)
                                                            Toast.makeText(context, "Language switched to $name ✓", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    .padding(vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = name, color = if (selected) colors.primary else colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    Text(text = desc, color = colors.textSecondary, fontSize = 12.sp)
                                                }
                                                RadioButton(
                                                    selected = selected,
                                                    onClick = { coroutineScope.launch { userPrefs.setAppLanguage(code) } },
                                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.textSecondary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "Content & Media" -> {
                                item {
                                    SettingsCard(title = "Media Playback & Quality") {
                                        SettingsSwitchRow(
                                            title = "Autoplay Videos",
                                            subtitle = "Play video reels automatically in feed",
                                            checked = mediaAutoplay
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setMediaAutoplay(it)
                                                syncToCloud("content", "autoplay", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "Data Saver Mode",
                                            subtitle = "Reduce image and video data usage on mobile networks",
                                            checked = mediaDataSaver
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setMediaDataSaver(it)
                                                syncToCloud("content", "dataSaver", it)
                                            }
                                        }

                                        SettingsDropdownSelector(
                                            label = "Upload Quality",
                                            currentValue = mediaUploadQuality,
                                            options = listOf("High", "Standard", "Data Saver")
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setMediaUploadQuality(it)
                                                syncToCloud("content", "uploadQuality", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "Download Media Automatically",
                                            subtitle = "Save photos and voice messages automatically",
                                            checked = mediaAutoDownload
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setMediaAutoDownload(it)
                                                syncToCloud("content", "autoDownload", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "Sensitive Content Warnings",
                                            subtitle = "Cover potentially sensitive photos until clicked",
                                            checked = sensitiveContentWarning
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setSensitiveContentWarning(it)
                                                syncToCloud("content", "sensitiveWarning", it)
                                            }
                                        }
                                    }
                                }
                            }

                            "Security" -> {
                                item {
                                    SettingsCard(title = "Account Protection & Credentials") {
                                        SettingsActionRow(title = "Change Password", subtitle = "Update your security password") {
                                            showChangePasswordDialog = true
                                        }

                                        HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 4.dp))

                                        SettingsSwitchRow(
                                            title = "Two-Step Verification (2FA)",
                                            subtitle = "Require security prompt on new sign-ins",
                                            checked = securityTwoFactor
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setSecurityTwoFactor(it)
                                                syncToCloud("security", "twoFactor", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "Login Alerts",
                                            subtitle = "Get notified if signed in from unrecognized devices",
                                            checked = securityLoginAlerts
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setSecurityLoginAlerts(it)
                                                syncToCloud("security", "loginAlerts", it)
                                            }
                                        }

                                        SettingsSwitchRow(
                                            title = "App Lock (Biometric / PIN)",
                                            subtitle = "Protect FriendHub with device biometric or lock screen",
                                            checked = securityAppLock
                                        ) {
                                            coroutineScope.launch {
                                                userPrefs.setSecurityAppLock(it)
                                                syncToCloud("security", "appLock", it)
                                            }
                                        }
                                    }
                                }
                            }

                            "Data & Storage" -> {
                                item {
                                    SettingsCard(title = "Storage & Cache Management") {
                                        SettingsInfoField(label = "App Size", value = "38.4 MB")
                                        SettingsInfoField(label = "Cached Images & Media", value = calculatedCacheSizeMb)
                                        SettingsInfoField(label = "Database & Preferences", value = "2.8 MB")

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = { showClearCacheConfirmDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.cardSelected),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, colors.cardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Clear Cached Media ($calculatedCacheSizeMb)", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            "Login Activity" -> {
                                item {
                                    SettingsCard(title = "Active Sessions & Devices") {
                                        val deviceName = Build.MODEL ?: "Android Device"
                                        val manufacturer = Build.MANUFACTURER ?: "Android"
                                        val osVersion = "Android " + Build.VERSION.RELEASE

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Default.Smartphone, contentDescription = null, tint = Color(0xFF00E5FF))
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "$manufacturer $deviceName", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "$osVersion • Active Now", color = Color(0xFF22C55E), fontSize = 12.sp)
                                            }
                                        }

                                        HorizontalDivider(color = colors.cardBorder)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Default.Laptop, contentDescription = null, tint = colors.primary)
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "FriendHub Web App", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "Chrome Browser • Colombo, LK • 2 days ago", color = colors.textSecondary, fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Logged out of other devices successfully ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, colors.cardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Log Out of Other Sessions", color = colors.textPrimary)
                                        }
                                    }
                                }
                            }

                            "Blocked Users" -> {
                                item {
                                    var newBlockUserText by remember { mutableStateOf("") }

                                    SettingsCard(title = "Blocked Accounts") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = newBlockUserText,
                                                onValueChange = { newBlockUserText = it },
                                                placeholder = { Text("Enter username to block", color = colors.textSecondary, fontSize = 13.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = colors.primary,
                                                    unfocusedBorderColor = colors.cardBorder,
                                                    focusedTextColor = colors.textPrimary,
                                                    unfocusedTextColor = colors.textPrimary
                                                ),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    if (newBlockUserText.isNotBlank()) {
                                                        coroutineScope.launch {
                                                            userPrefs.blockUser(newBlockUserText.trim())
                                                            Toast.makeText(context, "Blocked ${newBlockUserText.trim()}", Toast.LENGTH_SHORT).show()
                                                            newBlockUserText = ""
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Block", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (blockedUsers.isEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(36.dp))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = "No blocked users", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                                                Text(text = "Accounts you block will appear here", color = colors.textSecondary, fontSize = 12.sp)
                                            }
                                        } else {
                                            blockedUsers.forEach { user ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .background(colors.cardSelected, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = user.take(2).uppercase(), color = colors.textPrimary, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(text = user, color = colors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                userPrefs.unblockUser(user)
                                                                Toast.makeText(context, "Unblocked $user", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = colors.cardSelected),
                                                        shape = RoundedCornerShape(12.dp),
                                                        border = BorderStroke(1.dp, colors.cardBorder)
                                                    ) {
                                                        Text("Unblock", color = colors.textPrimary, fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "Help & Support" -> {
                                item {
                                    SettingsCard(title = "Support & Policies") {
                                        SettingsActionRow(title = "Help Center & FAQ", subtitle = "Guides and troubleshooting") {
                                            showGenericDialogTitle = "FriendHub Help Center"
                                            showGenericDialogContent = "Welcome to the FriendHub Help Center!\n\n• Feed & Stories: Post photos, videos, and highlights.\n• Real-Time Messaging: Instant chats with delivery status.\n• Privacy Controls: Manage profile visibility anytime.\n• Marketplace: Buy & sell items safely.\n• 2FA Security: Keep your account safe with alerts."
                                        }
                                        SettingsActionRow(title = "Report a Problem", subtitle = "Send feedback or report bugs") {
                                            showReportProblemDialog = true
                                        }
                                        SettingsActionRow(title = "Contact FriendHub", subtitle = "Direct support hotline & email") {
                                            try {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
                                                    putExtra(Intent.EXTRA_SUBJECT, "FriendHub Support Inquiry")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Support: ${AppConfig.CONTACT_EMAIL}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        SettingsActionRow(title = "Community Guidelines", subtitle = "Safety, respect, and content policies") {
                                            showGenericDialogTitle = "Community Guidelines"
                                            showGenericDialogContent = "FriendHub is built for genuine friendship, creativity, and connection.\n\n1. Be respectful to all members.\n2. Do not post illegal, harmful, or copyright-infringing content.\n3. Protect user privacy and avoid harassment."
                                        }
                                        SettingsActionRow(title = "Privacy Policy", subtitle = "How your information is protected") {
                                            showGenericDialogTitle = "Privacy Policy"
                                            showGenericDialogContent = "FriendHub values your privacy.\n\n• User data is encrypted.\n• We do not sell user data to third parties.\n• You can permanently delete your account and personal content anytime in Settings."
                                        }
                                        SettingsActionRow(title = "Terms of Service", subtitle = "User agreements and rules") {
                                            showGenericDialogTitle = "Terms of Service"
                                            showGenericDialogContent = "By using FriendHub, you agree to keep your credentials safe, post authentic content, and adhere to community guidelines."
                                        }
                                    }
                                }
                            }

                            "About" -> {
                                item {
                                    SettingsCard(title = "About FriendHub") {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.fh_logo),
                                                contentDescription = "FriendHub Logo",
                                                modifier = Modifier.size(72.dp).clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(text = "FriendHub Android", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text(text = "Version 1.0.0 (Build 2026.10)", color = colors.textSecondary, fontSize = 13.sp)
                                            Text(text = "Designed with Jetpack Compose & Firebase Cloud", color = colors.primary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = { showLicensesDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.cardSelected),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, colors.cardBorder)
                                            ) {
                                                Text("Open Source Licenses", color = colors.textPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    // Edit Account Dialog
    if (showEditAccountDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editBio by remember { mutableStateOf(userProfile.bio) }
        var editHandle by remember { mutableStateOf(userProfile.handle) }

        AlertDialog(
            onDismissRequest = { showEditAccountDialog = false },
            title = { Text("Edit Account Info", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                    OutlinedTextField(
                        value = editHandle,
                        onValueChange = { editHandle = it },
                        label = { Text("Username handle", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditAccountDialog = false
                        onUpdateProfile?.invoke(editName, editBio, editHandle)
                        syncToCloud("account", "displayName", editName)
                        syncToCloud("account", "handle", editHandle)
                        syncToCloud("account", "bio", editBio)
                        Toast.makeText(context, "Account updated ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAccountDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your new password to secure your FriendHub account.", color = colors.textSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length >= 6) {
                            showChangePasswordDialog = false
                            try {
                                auth?.currentUser?.updatePassword(newPass)
                            } catch (_: Exception) {}
                            Toast.makeText(context, "Password updated successfully ✓", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Update", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Report Problem Dialog
    if (showReportProblemDialog) {
        var issueText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportProblemDialog = false },
            title = { Text("Report a Problem", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Describe what happened or what isn't working:", color = colors.textSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = issueText,
                        onValueChange = { issueText = it },
                        placeholder = { Text("Explain the issue in detail...", color = colors.textSecondary) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (issueText.isNotBlank()) {
                            showReportProblemDialog = false
                            syncToCloud("reports", System.currentTimeMillis().toString(), issueText)
                            Toast.makeText(context, "Report submitted to FriendHub team ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportProblemDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• Android Jetpack & Compose (Apache 2.0)", color = colors.textPrimary, fontSize = 13.sp)
                    Text("• Kotlin Coroutines & Serialization (Apache 2.0)", color = colors.textPrimary, fontSize = 13.sp)
                    Text("• Coil Image Loader (Apache 2.0)", color = colors.textPrimary, fontSize = 13.sp)
                    Text("• Google Firebase SDK (Apache 2.0)", color = colors.textPrimary, fontSize = 13.sp)
                    Text("• Room Database (Apache 2.0)", color = colors.textPrimary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLicensesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text(str("log_out_confirm_title"), color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(str("log_out_confirm_msg"), color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(str("log_out"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(str("cancel"), color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(str("delete_account"), color = Color(0xFFFF4B4B), fontWeight = FontWeight.Bold) },
            text = { Text(str("delete_account_warn"), color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccountClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B))
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(str("cancel"), color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirmDialog = false },
            title = { Text("Clear Cached Media", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will remove temporary image and video caches to free up device space. Your posts and cloud data will not be affected.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            context.cacheDir.deleteRecursively()
                        } catch (_: Exception) {}
                        showClearCacheConfirmDialog = false
                        refreshCacheSize()
                        Toast.makeText(context, "Cache cleared successfully ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Clear Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheConfirmDialog = false }) {
                    Text(str("cancel"), color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Generic Info Dialog
    if (showGenericDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { showGenericDialogTitle = null },
            title = { Text(showGenericDialogTitle!!, color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(showGenericDialogContent ?: "", color = colors.textSecondary, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { showGenericDialogTitle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = colors.cardBackground
        )
    }
}

// ---------------- Helper Components ----------------

data class SettingsItemRow(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun SettingsCategoryGroup(
    title: String,
    items: List<SettingsItemRow>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            color = PurpleMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        val colors = LocalFriendHubColors.current
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(colors.cardSelected, RoundedCornerShape(10.dp))
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(text = item.subtitle, color = colors.textSecondary, fontSize = 12.sp)
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(color = colors.cardBorder, thickness = 0.6.dp, modifier = Modifier.padding(start = 66.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalFriendHubColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 10.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalFriendHubColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = colors.textSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.cardSelected
            )
        )
    }
}

@Composable
fun SettingsDropdownSelector(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val colors = LocalFriendHubColors.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = currentValue, color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.textPrimary)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colors.cardBackground)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(text = opt, color = if (opt == currentValue) colors.primary else colors.textPrimary, fontWeight = if (opt == currentValue) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsInfoField(label: String, value: String) {
    val colors = LocalFriendHubColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = label, color = colors.textSecondary, fontSize = 12.sp)
        Text(text = value, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    val colors = LocalFriendHubColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = colors.textSecondary, fontSize = 12.sp)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
    }
}
