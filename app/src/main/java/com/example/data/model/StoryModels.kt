package com.example.data.model

data class CloudStory(
    val storyId: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerAvatar: String = "",
    val mediaUrl: String = "",
    val type: String = "photo", // "photo" or "video"
    val timestamp: Long = 0,
    val expiresAt: Long = 0,
    val textOverlay: String = "",
    val backgroundColor: String = "#000000",
    val viewers: Map<String, Long> = emptyMap(), // viewerUid -> timestamp
    val reactions: Map<String, String> = emptyMap(), // reactorUid -> emoji
    val privacy: String = "Public" // "Public", "Friends", "Followers", "Only Me"
)

data class PlacedSticker(
    val id: String,
    val type: String, // "emoji", "frame", "location", "mention"
    val content: String,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 1.0f,
    var rotation: Float = 0f
)

data class PlacedText(
    val id: String,
    val text: String,
    val style: String, // "Normal", "Bold", "Dream", "Cursive", "Neon Purple"
    val colorHex: String = "#FFFFFF",
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 1.0f
)

data class CloudHighlight(
    val highlightId: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val ownerId: String = "",
    val stories: Map<String, Boolean> = emptyMap() // storyId -> true
)
