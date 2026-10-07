package com.example.data.model

data class UserProfile(
    val id: String = "user_1",
    val name: String = "Wasantha Kumara",
    val handle: String = "Wasantha_K",
    val avatarUrl: String = "",
    val coverPhotoUrl: String = "",
    val bio: String = "Dream • Learn • Build • Grow 🌿",
    val bioLine2: String = "Technology | Travel | Photography | Good Vibes",
    val location: String = "Pelmadulla, Sri Lanka",
    val role: String = "Self Learner",
    val joinedDate: String = "Joined 2025",
    val followersCount: Int = 1200,
    val followingCount: Int = 320,
    val postsCount: Int = 248,
    val friendsCount: Int = 56,
    val isVerified: Boolean = true,
    val isOnline: Boolean = true,
    val website: String = "https://friendhub.app/wasantha",
    val birthday: String = "May 14",
    val showBirthday: Boolean = true,
    val education: String = "Bachelor of Science in Computer Science",
    val workplace: String = "FriendHub Engineering",
    val profession: String = "Software Engineer & Creator",
    val skills: String = "Kotlin, Jetpack Compose, Firebase, UI/UX, Cloud",
    val interests: String = "Mobile Dev, AI, Open Source, Photography",
    val languages: String = "English, Sinhala",
    val hobbies: String = "Hiking, Music Production, Reading",
    val aboutMe: String = "Passionate about creating modern software experiences and connecting people worldwide.",
    val phone: String = "+94 77 123 4567",
    val email: String = "wasantha@friendhub.app",
    val favoriteThings: String = "Coffee ☕, Clean Code 💻, Nature 🌄",
    val socialLinks: String = "github.com/wasanthak, twitter.com/wasantha_k",
    val featuredContent: String = "FriendHub Launch Highlights 🚀",
    val sectionVisibility: Map<String, String> = mapOf(
        "About Me" to "Public",
        "Work" to "Public",
        "Education" to "Friends",
        "Skills" to "Public",
        "Interests" to "Public",
        "Hobbies" to "Public",
        "Languages" to "Public",
        "Website" to "Public",
        "Contact" to "Only Me"
    )
)

data class StoryItem(
    val id: String,
    val username: String,
    val avatarUrl: String = "",
    val isCurrentUser: Boolean = false,
    val hasUnseenStory: Boolean = true
)

data class PostItem(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String = "",
    val imageUrl: String = "",
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val caption: String,
    val timeAgo: String,
    val category: String = "My Feed"
)

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val type: String, // "like", "comment", "follow", "marketplace", "live"
    val isRead: Boolean = false,
    val userAvatarUrl: String = ""
)

data class MarketplaceItem(
    val id: String,
    val title: String,
    val price: Double,
    val sellerName: String,
    val sellerAvatarUrl: String = "",
    val category: String,
    val imageUrl: String = "",
    val description: String,
    val location: String = "San Francisco, CA",
    val timestamp: String = "2 hours ago",
    val isSaved: Boolean = false
)

data class CommentItem(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class MessageItem(
    val id: String,
    val senderName: String,
    val senderAvatarUrl: String = "",
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

data class DirectChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val seen: Boolean = false,
    val reactions: Map<String, String> = emptyMap(),
    val timestampLong: Long = 0L,
    val effectType: String? = null
)
