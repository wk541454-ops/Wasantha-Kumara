package com.example.data.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

object PresenceManager : DefaultLifecycleObserver {
    private var currentUid: String? = null
    private var isOnlinePrivacyEnabled: Boolean = true

    private var dbInstance: FirebaseFirestore? = null

    val firestore: FirebaseFirestore
        get() {
            return dbInstance ?: FirebaseFirestore.getInstance("ai-studio-android-friendhu-530105a2-b1f7-47c1-a824-306ef15e5050")
        }

    fun init(databaseId: String) {
        dbInstance = FirebaseFirestore.getInstance(databaseId)
    }

    fun startPresenceTracking(uid: String, onlinePrivacyOn: Boolean = true) {
        currentUid = uid
        isOnlinePrivacyEnabled = onlinePrivacyOn
        updatePresence(true)
    }

    fun setOnlineStatusPrivacy(uid: String, enabled: Boolean) {
        isOnlinePrivacyEnabled = enabled
        firestore.collection("presence").document(uid)
            .update("onlineStatusEnabled", enabled)
    }

    fun setAppForegroundState(isForeground: Boolean) {
        updatePresence(isForeground)
    }

    private fun updatePresence(isOnline: Boolean) {
        val uid = currentUid ?: return
        val map = mapOf(
            "state" to if (isOnline) "ONLINE" else "OFFLINE",
            "lastSeen" to FieldValue.serverTimestamp(),
            "onlineStatusEnabled" to isOnlinePrivacyEnabled
        )
        firestore.collection("presence").document(uid)
            .set(map)
    }

    override fun onStart(owner: LifecycleOwner) {
        setAppForegroundState(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        setAppForegroundState(false)
    }

    /**
     * Observe presence of target user for a viewer.
     */
    fun observeUserPresence(
        viewerUid: String,
        targetUid: String,
        isMutualFriend: Boolean,
        isBlocked: Boolean,
        onUpdate: (isOnline: Boolean, lastSeen: Long) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration? {
        // Simple friend/privacy check logic (can be refined)
        if (!isMutualFriend || isBlocked || viewerUid == targetUid) {
            onUpdate(false, 0L)
            return null
        }

        return firestore.collection("presence").document(targetUid)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val state = snapshot.getString("state") ?: "OFFLINE"
                    val lastSeen = snapshot.getTimestamp("lastSeen")?.toDate()?.time ?: 0L
                    val targetPrivacyEnabled = snapshot.getBoolean("onlineStatusEnabled") ?: true

                    if (!targetPrivacyEnabled || !isOnlinePrivacyEnabled) {
                        onUpdate(false, 0L)
                    } else {
                        onUpdate(state == "ONLINE", lastSeen)
                    }
                } else {
                    onUpdate(false, 0L)
                }
            }
    }

    fun removePresenceListener(registration: com.google.firebase.firestore.ListenerRegistration?) {
        registration?.remove()
    }
}
