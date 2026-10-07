package com.example.data.model

data class LiveSession(
    val liveId: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val creatorHandle: String = "",
    val creatorAvatarUrl: String = "",
    val title: String = "",
    val status: String = "live", // "live", "ended"
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = 0L,
    val viewerCount: Int = 0,
    val category: String = "General",
    val thumbnailUrl: String = "",
    val visibility: String = "Public", // "Public", "Followers", "Friends"
    val commentsEnabled: Boolean = true,
    val reactionsEnabled: Boolean = true,
    val participantLimit: Int = 5,
    val currentParticipants: List<String> = emptyList(),
    val streamProvider: String = "WebRTC/RTMP",
    val streamChannelId: String = "",
    val moderationState: String = "normal",
    val peakViewers: Int = 0,
    val totalViewers: Int = 0,
    val totalComments: Int = 0,
    val totalReactions: Int = 0,
    val newFollowersGained: Int = 0,
    val isVerified: Boolean = true
)

data class LiveCommentItem(
    val commentId: String = "",
    val liveId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorHex: String = "#FFFFFF"
)

data class LiveReactionItem(
    val reactionId: String = "",
    val liveId: String = "",
    val userId: String = "",
    val emoji: String = "❤️",
    val timestamp: Long = System.currentTimeMillis()
)
