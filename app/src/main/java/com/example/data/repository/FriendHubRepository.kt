package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.SupabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FriendHubRepository(
    private val context: Context,
    val userPrefs: UserPreferencesRepository
) {
    private val db = FriendHubDatabase.getDatabase(context)
    private val postDao = db.postDao()
    private val notificationDao = db.notificationDao()
    private val marketplaceDao = db.marketplaceDao()
    private val storyDao = db.storyDao()
    private val profileDao = db.profileDao()
    private val supabaseService = SupabaseService()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Seed initial data into Room if empty
        scope.launch {
            seedInitialDataIfEmpty()
        }
    }

    val posts: Flow<List<PostItem>> = postDao.getAllPosts().map { list ->
        list.map {
            PostItem(
                id = it.id,
                authorName = it.authorName,
                authorHandle = it.authorHandle,
                authorAvatarUrl = it.authorAvatarUrl,
                imageUrl = it.imageUrl,
                likesCount = it.likesCount,
                commentsCount = it.commentsCount,
                isLiked = it.isLiked,
                isSaved = it.isSaved,
                caption = it.caption,
                timeAgo = it.timeAgo,
                category = it.category
            )
        }
    }

    val notifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications().map { list ->
        list.map {
            NotificationItem(
                id = it.id,
                title = it.title,
                description = it.description,
                time = it.time,
                type = it.type,
                isRead = it.isRead,
                userAvatarUrl = it.userAvatarUrl
            )
        }
    }

    val marketplaceItems: Flow<List<MarketplaceItem>> = marketplaceDao.getAllMarketplaceItems().map { list ->
        list.map {
            MarketplaceItem(
                id = it.id,
                title = it.title,
                price = it.price,
                sellerName = it.sellerName,
                sellerAvatarUrl = it.sellerAvatarUrl,
                category = it.category,
                imageUrl = it.imageUrl,
                description = it.description,
                location = it.location,
                timestamp = it.timestamp,
                isSaved = it.isSaved
            )
        }
    }

    val stories: Flow<List<StoryItem>> = storyDao.getAllStories().map { list ->
        list.map {
            StoryItem(
                id = it.id,
                username = it.username,
                avatarUrl = it.avatarUrl,
                isCurrentUser = it.isCurrentUser,
                hasUnseenStory = it.hasUnseenStory
            )
        }
    }

    val userProfile: Flow<UserProfile> = profileDao.getProfile().map {
        if (it != null) {
            UserProfile(
                id = it.id,
                name = it.name,
                handle = it.handle,
                avatarUrl = it.avatarUrl,
                coverPhotoUrl = it.coverPhotoUrl,
                bio = it.bio,
                bioLine2 = it.bioLine2,
                location = it.location,
                role = it.role,
                joinedDate = it.joinedDate,
                followersCount = it.followersCount,
                followingCount = it.followingCount,
                postsCount = it.postsCount,
                friendsCount = it.friendsCount,
                isVerified = it.isVerified,
                isOnline = it.isOnline
            )
        } else {
            UserProfile()
        }
    }

    suspend fun toggleLikePost(postId: String) {
        val currentPosts = postDao.getAllPosts().first()
        val post = currentPosts.find { it.id == postId } ?: return
        val newIsLiked = !post.isLiked
        val newCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        postDao.updateLike(postId, newIsLiked, newCount)
    }

    suspend fun addPost(caption: String, imageUrl: String, category: String = "My Feed") {
        val newPost = PostEntity(
            id = "post_${System.currentTimeMillis()}",
            authorName = "Sophia Anderson",
            authorHandle = "sophiaa",
            authorAvatarUrl = "",
            imageUrl = imageUrl.ifBlank { "android.resource://${context.packageName}/drawable/post_architecture" },
            likesCount = 1,
            commentsCount = 0,
            isLiked = true,
            isSaved = false,
            caption = caption,
            timeAgo = "Just now",
            category = category
        )
        postDao.insertPost(newPost)
    }

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    suspend fun addNotification(title: String, description: String, type: String = "system") {
        val notif = NotificationEntity(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            description = description,
            time = "Just now",
            type = type,
            isRead = false,
            userAvatarUrl = ""
        )
        notificationDao.insertNotification(notif)
    }

    suspend fun addMarketplaceItem(
        title: String,
        price: Double,
        category: String,
        description: String,
        imageUrl: String
    ) {
        val item = MarketplaceEntity(
            id = "item_${System.currentTimeMillis()}",
            title = title,
            price = price,
            sellerName = "Sophia Anderson",
            sellerAvatarUrl = "",
            category = category,
            imageUrl = imageUrl,
            description = description,
            location = "San Francisco, CA",
            timestamp = "Just now",
            isSaved = false
        )
        marketplaceDao.insertMarketplaceItem(item)

        // Notify user about marketplace item creation
        addNotification(
            title = "Item Listed!",
            description = "Your item '$title' is now live on Marketplace.",
            type = "marketplace"
        )
    }

    suspend fun updateFullProfile(
        name: String,
        handle: String,
        bio: String,
        bioLine2: String,
        location: String,
        role: String,
        avatarUrl: String = "",
        coverPhotoUrl: String = ""
    ) {
        val current = profileDao.getProfile().first() ?: ProfileEntity(
            id = "user_1",
            name = "Wasantha Kumara",
            handle = "Wasantha_K",
            avatarUrl = avatarUrl,
            coverPhotoUrl = coverPhotoUrl,
            bio = bio,
            bioLine2 = bioLine2,
            location = location,
            role = role,
            joinedDate = "Joined 2025",
            followersCount = 1200,
            followingCount = 320,
            postsCount = 248,
            friendsCount = 56,
            isVerified = true,
            isOnline = true
        )

        val updated = current.copy(
            name = name,
            handle = handle,
            bio = bio,
            bioLine2 = bioLine2,
            location = location,
            role = role,
            avatarUrl = if (avatarUrl.isNotEmpty()) avatarUrl else current.avatarUrl,
            coverPhotoUrl = if (coverPhotoUrl.isNotEmpty()) coverPhotoUrl else current.coverPhotoUrl
        )
        profileDao.insertProfile(updated)
    }

    suspend fun updateProfile(name: String, bio: String, handle: String) {
        updateFullProfile(
            name = name,
            handle = handle,
            bio = bio,
            bioLine2 = "Technology | Travel | Photography | Good Vibes",
            location = "Pelmadulla, Sri Lanka",
            role = "Self Learner"
        )
    }

    private suspend fun seedInitialDataIfEmpty() {
        // Seed Stories
        val existingStories = storyDao.getAllStories().first()
        if (existingStories.isEmpty()) {
            val defaultStories = listOf(
                StoryEntity("1", "Profile", "", isCurrentUser = true, hasUnseenStory = true),
                StoryEntity("2", "sarahjms", "", isCurrentUser = false, hasUnseenStory = true),
                StoryEntity("3", "niko", "", isCurrentUser = false, hasUnseenStory = true),
                StoryEntity("4", "rudraksh", "", isCurrentUser = false, hasUnseenStory = true),
                StoryEntity("5", "franklin_", "", isCurrentUser = false, hasUnseenStory = true),
                StoryEntity("6", "Sr.", "", isCurrentUser = false, hasUnseenStory = false)
            )
            storyDao.insertStories(defaultStories)
        }

        // Seed Posts
        val existingPosts = postDao.getAllPosts().first()
        if (existingPosts.isEmpty()) {
            val pkg = context.packageName
            val defaultPosts = listOf(
                PostEntity(
                    id = "1",
                    authorName = "Sophia Anderson",
                    authorHandle = "sophiaa",
                    authorAvatarUrl = "",
                    imageUrl = "android.resource://$pkg/drawable/post_architecture",
                    likesCount = 1250,
                    commentsCount = 78,
                    isLiked = true,
                    isSaved = false,
                    caption = "Clean lines & modern serenity in the city ✨",
                    timeAgo = "2 HOURS AGO",
                    category = "My Feed"
                ),
                PostEntity(
                    id = "2",
                    authorName = "Niko Vance",
                    authorHandle = "niko",
                    authorAvatarUrl = "",
                    imageUrl = "android.resource://$pkg/drawable/fh_logo",
                    likesCount = 3840,
                    commentsCount = 142,
                    isLiked = false,
                    isSaved = true,
                    caption = "Exploring neon horizons with the squad! 🚀 #FriendHub",
                    timeAgo = "4 HOURS AGO",
                    category = "Explore"
                )
            )
            postDao.insertPosts(defaultPosts)
        }

        // Seed Notifications
        val existingNotifs = notificationDao.getAllNotifications().first()
        if (existingNotifs.isEmpty()) {
            val defaultNotifs = listOf(
                NotificationEntity("1", "sarahjms liked your photo", "Liked 'Clean lines & modern serenity'", "10m ago", "like", false, ""),
                NotificationEntity("2", "niko commented on your post", "\"Stunning architecture! ✨\"", "25m ago", "comment", false, ""),
                NotificationEntity("3", "rudraksh started following you", "Tap to view profile", "1h ago", "follow", false, ""),
                NotificationEntity("4", "Cyberpunk Glasses listed!", "New item matches your saved search", "2h ago", "marketplace", true, ""),
                NotificationEntity("5", "franklin_ is LIVE now 🔴", "Join the interactive stream", "3h ago", "live", true, "")
            )
            notificationDao.insertNotifications(defaultNotifs)
        }

        // Seed Marketplace
        val existingMarket = marketplaceDao.getAllMarketplaceItems().first()
        if (existingMarket.isEmpty()) {
            val pkg = context.packageName
            val defaultItems = listOf(
                MarketplaceEntity(
                    id = "m1",
                    title = "Futuristic AR Glasses",
                    price = 249.00,
                    sellerName = "Alex Vance",
                    sellerAvatarUrl = "",
                    category = "Tech",
                    imageUrl = "android.resource://$pkg/drawable/fh_logo",
                    description = "Ultra high resolution HUD display with dual audio and gesture control.",
                    location = "San Francisco, CA",
                    timestamp = "1 hour ago",
                    isSaved = false
                ),
                MarketplaceEntity(
                    id = "m2",
                    title = "Neon Glow Headphones",
                    price = 189.99,
                    sellerName = "Elena Ray",
                    sellerAvatarUrl = "",
                    category = "Audio",
                    imageUrl = "android.resource://$pkg/drawable/fh_logo",
                    description = "Active noise cancelling with customizable RGB light rings.",
                    location = "Oakland, CA",
                    timestamp = "3 hours ago",
                    isSaved = true
                ),
                MarketplaceEntity(
                    id = "m3",
                    title = "Minimal Studio Desk",
                    price = 520.00,
                    sellerName = "Marco K",
                    sellerAvatarUrl = "",
                    category = "Furniture",
                    imageUrl = "android.resource://$pkg/drawable/post_architecture",
                    description = "Custom matte black walnut wood desk with built-in wireless charger.",
                    location = "San Jose, CA",
                    timestamp = "5 hours ago",
                    isSaved = false
                ),
                MarketplaceEntity(
                    id = "m4",
                    title = "FH Official Neon Hoodie",
                    price = 85.00,
                    sellerName = "FriendHub Merch",
                    sellerAvatarUrl = "",
                    category = "Fashion",
                    imageUrl = "android.resource://$pkg/drawable/fh_logo",
                    description = "Heavyweight 100% organic cotton hoodie with reflective embroidered logo.",
                    location = "San Francisco, CA",
                    timestamp = "1 day ago",
                    isSaved = true
                )
            )
            marketplaceDao.insertMarketplaceItems(defaultItems)
        }

        // Seed Profile
        val existingProfile = profileDao.getProfile().first()
        if (existingProfile == null) {
            val defaultProfile = ProfileEntity(
                id = "user_1",
                name = "Wasantha Kumara",
                handle = "Wasantha_K",
                avatarUrl = "",
                coverPhotoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
                bio = "Dream • Learn • Build • Grow 🌿",
                bioLine2 = "Technology | Travel | Photography | Good Vibes",
                location = "Pelmadulla, Sri Lanka",
                role = "Self Learner",
                joinedDate = "Joined 2025",
                followersCount = 1200,
                followingCount = 320,
                postsCount = 248,
                friendsCount = 56,
                isVerified = true,
                isOnline = true
            )
            profileDao.insertProfile(defaultProfile)
        }
    }
}
