package com.example.data.model

data class UserPresence(
    val uid: String = "",
    val state: String = "OFFLINE", // "ONLINE", "OFFLINE"
    val lastSeen: Long = 0L,
    val onlineStatusEnabled: Boolean = true
)
