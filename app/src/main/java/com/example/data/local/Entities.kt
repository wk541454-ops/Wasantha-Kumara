package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val imageUrl: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean,
    val caption: String,
    val timeAgo: String,
    val category: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val time: String,
    val type: String,
    val isRead: Boolean,
    val userAvatarUrl: String
)

@Entity(tableName = "marketplace")
data class MarketplaceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val price: Double,
    val sellerName: String,
    val sellerAvatarUrl: String,
    val category: String,
    val imageUrl: String,
    val description: String,
    val location: String,
    val timestamp: String,
    val isSaved: Boolean
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val username: String,
    val avatarUrl: String,
    val isCurrentUser: Boolean,
    val hasUnseenStory: Boolean
)

@Entity(tableName = "user_profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val coverPhotoUrl: String = "",
    val bio: String,
    val bioLine2: String = "Technology | Travel | Photography | Good Vibes",
    val location: String = "Pelmadulla, Sri Lanka",
    val role: String = "Self Learner",
    val joinedDate: String = "Joined 2025",
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val friendsCount: Int = 56,
    val isVerified: Boolean,
    val isOnline: Boolean = true
)
