package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.example.ui.theme.LocalFriendHubColors
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.model.PostItem
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FriendHubRepository
import com.example.ui.components.CommentSheet
import com.example.ui.components.SupabaseConfigDialog
import com.example.ui.screens.*
import com.example.ui.editor.StoryEditorScreen
import com.example.ui.theme.FriendHubTheme
import com.example.ui.theme.NeonPureBlack
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.example.utils.NetworkMonitor
import com.example.utils.NetworkStatus
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var repository: FriendHubRepository
    private lateinit var userPrefs: UserPreferencesRepository
    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Programmatically with User Project Credentials
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("gen-lang-client-0417982200")
                    .setApplicationId("1:893700779160:android:410db0b84bb23190b9b4ed")
                    .setApiKey("AIzaSyDft6GCleUC8VDuOVC2MBfeiJwQAgKw6cE")
                    .setStorageBucket("gen-lang-client-0417982200.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                
                // Enable Firestore Persistence (standard cache)
                val dbId = getString(R.string.firestore_database_id)
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                FirebaseFirestore.getInstance(dbId).firestoreSettings = settings
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase init error: ${e.localizedMessage}")
        }

        userPrefs = UserPreferencesRepository(applicationContext)
        repository = FriendHubRepository(applicationContext, userPrefs)
        networkMonitor = NetworkMonitor(applicationContext)
        com.example.utils.AgoraManager.init(applicationContext)

        val dbId = getString(R.string.firestore_database_id)
        com.example.data.repository.AppRepository.init(dbId)
        com.example.data.repository.PresenceManager.init(dbId)

        setContent {
            val networkStatus by networkMonitor.status.collectAsStateWithLifecycle(initialValue = NetworkStatus.Available)
            val themeMode by userPrefs.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val systemInDarkTheme = isSystemInDarkTheme()
            val isDarkMode = remember(themeMode, systemInDarkTheme) {
                when (themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> systemInDarkTheme
                }
            }

            val notificationsEnabled by userPrefs.notificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
            val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
            val isAdmin by userPrefs.isAdmin.collectAsStateWithLifecycle(initialValue = false)
            val supabaseUrl by userPrefs.supabaseUrl.collectAsStateWithLifecycle(initialValue = "")
            val supabaseKey by userPrefs.supabaseKey.collectAsStateWithLifecycle(initialValue = "")

            val posts by repository.posts.collectAsStateWithLifecycle(initialValue = emptyList())
            val notifications by repository.notifications.collectAsStateWithLifecycle(initialValue = emptyList())
            val marketplaceItems by repository.marketplaceItems.collectAsStateWithLifecycle(initialValue = emptyList())
            val stories by repository.stories.collectAsStateWithLifecycle(initialValue = emptyList())
            val userProfile by repository.userProfile.collectAsStateWithLifecycle(
                initialValue = com.example.data.model.UserProfile()
            )

            var currentScreen by remember { mutableStateOf("splash") }
            var activeCommentPost by remember { mutableStateOf<PostItem?>(null) }
            var showSupabaseConfig by remember { mutableStateOf(false) }

            var activeCloudStories by remember { mutableStateOf<List<com.example.data.model.CloudStory>>(emptyList()) }
            val globalFloatingLikes = remember { mutableStateListOf<com.example.ui.components.FloatingLike>() }
            var storySelectedMediaUri by remember { mutableStateOf<android.net.Uri?>(null) }
            var storySelectedMediaType by remember { mutableStateOf("photo") }
            var storyViewerInitialUserId by remember { mutableStateOf("") }
            var activeLiveId by remember { mutableStateOf("") }
            var endedLiveSession by remember { mutableStateOf(com.example.data.model.LiveSession()) }

            LaunchedEffect(isLoggedIn) {
                if (currentScreen != "splash") {
                    val firebaseUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
                    val userAuth = isLoggedIn || firebaseUser != null
                    currentScreen = if (userAuth) (if (isAdmin) "admin" else "home") else "login"
                }
            }

            LaunchedEffect(Unit) {
                try {
                    val uid = try { FirebaseAuth.getInstance().currentUser?.uid ?: "user_1" } catch (e: Exception) { "user_1" }
                    androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.addObserver(com.example.data.repository.PresenceManager)
                    com.example.data.repository.PresenceManager.startPresenceTracking(uid)
                    
                    com.example.data.repository.AppRepository.listenToActiveStories { list ->
                        activeCloudStories = list
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Presence init error: ${e.message}")
                }
            }

            // Handle back button navigation
            if (currentScreen != "home" && currentScreen != "splash" && currentScreen != "login") {
                BackHandler {
                    if (currentScreen == "create_account_flow") {
                        currentScreen = "login"
                    } else if (currentScreen == "messenger_settings") {
                        currentScreen = "messages"
                    } else {
                        currentScreen = "home"
                    }
                }
            }

            FriendHubTheme(darkTheme = isDarkMode) {
                Box(
                    modifier = Modifier.fillMaxSize().background(LocalFriendHubColors.current.background)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Connectivity Banner
                        AnimatedVisibility(
                            visible = networkStatus == NetworkStatus.Unavailable,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Surface(
                                color = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Waiting for connection...",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Main Content
                        Box(modifier = Modifier.weight(1f)) {
                            when (currentScreen) {
                                "splash" -> {
                            SplashScreen(
                                onSplashFinished = {
                                    val firebaseUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
                                    val userAuth = isLoggedIn || firebaseUser != null
                                    currentScreen = if (userAuth) (if (isAdmin) "admin" else "home") else "login"
                                }
                            )
                        }

                        "login" -> {
                            LoginScreen(
                                onLoginSuccess = { email ->
                                    lifecycleScope.launch {
                                        val auth = FirebaseAuth.getInstance()
                                        val uid = auth.currentUser?.uid
                                        if (uid != null) {
                                            com.example.data.repository.AppRepository.getUserProfile(uid) { profile ->
                                                lifecycleScope.launch {
                                                    userPrefs.setIsAdmin(false)
                                                    userPrefs.setLoggedIn(true, email)
                                                    if (profile == null) {
                                                        currentScreen = "create_account_flow"
                                                    } else {
                                                        currentScreen = "home"
                                                    }
                                                }
                                            }
                                        } else {
                                            userPrefs.setIsAdmin(false)
                                            userPrefs.setLoggedIn(true, email)
                                            currentScreen = "home"
                                        }
                                    }
                                },
                                onAdminLoginSuccess = { email ->
                                    lifecycleScope.launch {
                                        userPrefs.setIsAdmin(true)
                                        userPrefs.setLoggedIn(true, email)
                                        currentScreen = "admin"
                                    }
                                },
                                onCreateAccountClick = {
                                    currentScreen = "create_account_flow"
                                }
                            )
                        }

                        "create_account_flow" -> {
                            com.example.ui.screens.createaccount.CreateAccountFlowContainer(
                                onRegistrationComplete = { email ->
                                    lifecycleScope.launch {
                                        userPrefs.setLoggedIn(true, email)
                                        currentScreen = "home"
                                    }
                                },
                                onBackToLogin = {
                                    currentScreen = "login"
                                }
                            )
                        }

                        "home" -> {
                            Box(Modifier.fillMaxSize()) {
                                HomeScreen(
                                    posts = posts,
                                    stories = stories,
                                    activeCloudStories = activeCloudStories,
                                    floatingLikes = globalFloatingLikes,
                                    unreadNotificationCount = notifications.count { !it.isRead },
                                    onSearchClick = { currentScreen = "search" },
                                    onNotificationClick = { currentScreen = "notifications" },
                                    onSettingsClick = { currentScreen = "settings" },
                                    onCreateClick = { currentScreen = "create_post" },
                                    onLiveTabClick = { currentScreen = "live" },
                                    onMarketplaceTabClick = { currentScreen = "marketplace" },
                                    onNavigate = { route -> currentScreen = route },
                                    onToggleLike = { postId ->
                                        lifecycleScope.launch { repository.toggleLikePost(postId) }
                                    },
                                    onOpenComments = { post -> activeCommentPost = post },
                                    onSharePost = { post ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Check out this post on FriendHub: ${post.caption}")
                                            type = "text/plain"
                                        }
                                        startActivity(Intent.createChooser(sendIntent, "Share Post"))
                                    },
                                    onAddStoryClick = {
                                        currentScreen = "add_story"
                                    },
                                    onViewStoryClick = { userId ->
                                        storyViewerInitialUserId = userId
                                        currentScreen = "story_viewer"
                                    }
                                )
                                if (globalFloatingLikes.isNotEmpty()) {
                                    Box(Modifier.fillMaxSize().zIndex(10f)) {
                                        com.example.ui.components.FloatingLikesOverlay(likes = globalFloatingLikes)
                                    }
                                }
                            }
                        }

                        "menu" -> {
                            MenuScreen(
                                userProfile = userProfile,
                                isDarkMode = isDarkMode,
                                notificationsEnabled = notificationsEnabled,
                                isAdmin = isAdmin,
                                onToggleDarkMode = { enabled ->
                                    lifecycleScope.launch { userPrefs.setDarkMode(enabled) }
                                },
                                onToggleNotifications = { enabled ->
                                    lifecycleScope.launch { userPrefs.setNotificationsEnabled(enabled) }
                                },
                                onEditProfileClick = { currentScreen = "profile" },
                                onLogoutClick = {
                                    lifecycleScope.launch {
                                        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                                        userPrefs.setLoggedIn(false)
                                        currentScreen = "login"
                                    }
                                },
                                onDeleteAccountClick = {
                                    lifecycleScope.launch {
                                        try { FirebaseAuth.getInstance().currentUser?.delete() } catch (e: Exception) {}
                                        userPrefs.setLoggedIn(false)
                                        currentScreen = "login"
                                    }
                                },
                                onConfigureSupabaseClick = { showSupabaseConfig = true },
                                onAdminPanelClick = { currentScreen = "admin" },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "settings" -> {
                            SettingsScreen(
                                userProfile = userProfile,
                                userPrefs = userPrefs,
                                onNavigate = { route -> currentScreen = route },
                                onUpdateProfile = { name, bio, handle ->
                                    lifecycleScope.launch { repository.updateProfile(name, bio, handle) }
                                },
                                onLogoutClick = {
                                    lifecycleScope.launch {
                                        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                                        userPrefs.setLoggedIn(false)
                                        currentScreen = "login"
                                    }
                                },
                                onDeleteAccountClick = {
                                    lifecycleScope.launch {
                                        try {
                                            val auth = FirebaseAuth.getInstance()
                                            val uid = auth.currentUser?.uid
                                            if (uid != null) {
                                                try {
                                                    com.google.firebase.database.FirebaseDatabase.getInstance()
                                                        .reference.child("users/$uid").removeValue()
                                                } catch (_: Exception) {}
                                            }
                                            auth.currentUser?.delete()
                                        } catch (e: Exception) {}
                                        userPrefs.setLoggedIn(false)
                                        currentScreen = "login"
                                    }
                                }
                            )
                        }

                        "admin" -> {
                            AdminPanelScreen(
                                posts = posts,
                                marketplaceItems = marketplaceItems,
                                onDeletePost = { id -> },
                                onLogout = {
                                    lifecycleScope.launch {
                                        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                                        userPrefs.setLoggedIn(false)
                                        currentScreen = "login"
                                    }
                                },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "notifications" -> {
                            NotificationsScreen(
                                notifications = notifications,
                                onMarkAsRead = { id ->
                                    lifecycleScope.launch { repository.markNotificationAsRead(id) }
                                },
                                onMarkAllAsRead = {
                                    lifecycleScope.launch { repository.markAllNotificationsAsRead() }
                                },
                                onDeleteNotification = { id ->
                                    lifecycleScope.launch { repository.deleteNotification(id) }
                                },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "marketplace" -> {
                            MarketplaceScreen(
                                itemsList = marketplaceItems,
                                onAddItem = { title, price, category, desc, img ->
                                    lifecycleScope.launch {
                                        repository.addMarketplaceItem(title, price, category, desc, img)
                                    }
                                },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "profile" -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                userPosts = posts.filter { it.authorName == userProfile.name },
                                onUpdateProfile = { name, bio, handle ->
                                    lifecycleScope.launch { repository.updateProfile(name, bio, handle) }
                                },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "search" -> {
                            SearchScreen(onNavigate = { route -> currentScreen = route })
                        }

                        "live" -> {
                            LiveHubScreen(
                                userProfile = userProfile,
                                userPrefs = userPrefs,
                                onNavigate = { route -> currentScreen = route },
                                onStartLiveClick = { currentScreen = "start_live" },
                                onOpenLiveViewer = { liveId ->
                                    activeLiveId = liveId
                                    currentScreen = "live_room"
                                }
                            )
                        }

                        "start_live" -> {
                            StartLiveScreen(
                                userProfile = userProfile,
                                onNavigate = { route -> currentScreen = route },
                                onLiveStarted = { liveId ->
                                    activeLiveId = liveId
                                    currentScreen = "live_room"
                                }
                            )
                        }

                        "live_room" -> {
                            LiveRoomScreen(
                                liveId = activeLiveId,
                                userProfile = userProfile,
                                onNavigate = { route -> currentScreen = route },
                                onLiveEnded = { session ->
                                    endedLiveSession = session
                                    currentScreen = "live_ended"
                                }
                            )
                        }

                        "live_ended" -> {
                            LiveEndedScreen(
                                session = endedLiveSession,
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "create_post" -> {
                            CreatePostScreen(
                                onCreatePost = { caption, imgUrl, category ->
                                    lifecycleScope.launch { repository.addPost(caption, imgUrl, category) }
                                },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "mood_match" -> {
                            MoodMatchScreen(onNavigate = { route -> currentScreen = route })
                        }

                        "lanka_spot" -> {
                            LankaSpotScreen(onNavigate = { route -> currentScreen = route })
                        }

                        "video" -> {
                            VideoReelsScreen(
                                posts = posts,
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "messages" -> {
                            MessagesScreen(onNavigate = { route -> currentScreen = route })
                        }

                        "messenger_settings" -> {
                            MessengerSettingsScreen(onNavigate = { route -> currentScreen = route })
                        }

                        "command" -> {
                            CommandCenterScreen(
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "add_story" -> {
                            val curUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: "current_user_123"
                            AddStoryScreen(
                                currentUid = curUid,
                                onNavigate = { route -> currentScreen = route },
                                onMediaSelected = { uri, type ->
                                    storySelectedMediaUri = uri
                                    storySelectedMediaType = type
                                }
                            )
                        }

                        "story_editor" -> {
                            val curUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: "current_user_123"
                            StoryEditorScreen(
                                currentUid = curUid,
                                currentUsername = userProfile.name,
                                currentAvatar = userProfile.avatarUrl,
                                sourceUri = storySelectedMediaUri,
                                mediaType = storySelectedMediaType,
                                onNavigate = { route -> currentScreen = route }
                            )
                        }

                        "story_viewer" -> {
                            val curUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: "current_user_123"
                            StoryViewerScreen(
                                activeStories = activeCloudStories,
                                initialUserId = storyViewerInitialUserId,
                                currentUid = curUid,
                                currentUserProfile = userProfile,
                                onNavigate = { route -> currentScreen = route },
                                onAddStoryToHighlight = { story ->
                                    val hlUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: "current_user_123"
                                    com.example.data.repository.AppRepository.createHighlight(
                                        uid = hlUid,
                                        title = "Featured 🌟",
                                        storyId = story.storyId,
                                        coverUrl = story.mediaUrl
                                    ) { success ->
                                        if (success) {
                                            Toast.makeText(this@MainActivity, "Added to Highlights! ✓", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Comments Modal Sheet
                    if (activeCommentPost != null) {
                        CommentSheet(
                            post = activeCommentPost!!,
                            onDismiss = { activeCommentPost = null }
                        )
                    }

                    // Supabase Config Dialog
                    if (showSupabaseConfig) {
                        SupabaseConfigDialog(
                            initialUrl = supabaseUrl,
                            initialKey = supabaseKey,
                            onDismiss = { showSupabaseConfig = false },
                            onSave = { url, key ->
                                lifecycleScope.launch {
                                    userPrefs.saveSupabaseCredentials(url, key)
                                }
                            }
                        )
                    }
                        }
                    }
                }
            }
        }
    }
}
