package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.config.AppConfig
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream

import android.net.Uri
import com.example.data.model.CloudStory
import com.example.data.model.CloudHighlight
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

/**
 * AppRepository: Generic Cloud Abstraction Layer
 * Encapsulates all backend database and storage calls under generic FriendHub Cloud methods.
 * Migrated to Firestore for better reliability and regional compatibility.
 */
object AppRepository {

    private var dbInstance: FirebaseFirestore? = null

    val firestore: FirebaseFirestore
        get() {
            return dbInstance ?: FirebaseFirestore.getInstance("ai-studio-android-friendhu-530105a2-b1f7-47c1-a824-306ef15e5050")
        }

    fun init(databaseId: String) {
        dbInstance = FirebaseFirestore.getInstance(databaseId)
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    /**
     * Fetch public profile for a user from FriendHub Cloud
     */
    fun getUserProfile(uid: String, onResult: (Map<String, Any>?) -> Unit) {
        firestore.collection("users").document(uid).collection("public_profile").document("info")
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.data)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    /**
     * Save/Update public profile to FriendHub Cloud
     */
    fun updateUserProfile(uid: String, profileData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(uid).collection("public_profile").document("info")
            .set(profileData)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Save Photo Style (Frames, Stickers, Filters)
     */
    fun savePhotoStyle(uid: String, styleData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(uid).collection("public_profile").document("photoStyle")
            .set(styleData)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Save a new post to FriendHub Cloud
     */
    fun savePost(postId: String, postData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        firestore.collection("posts").document(postId)
            .set(postData)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Toggle post like
     */
    fun togglePostLike(postId: String, uid: String, isLiked: Boolean, onComplete: (Boolean) -> Unit) {
        val ref = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val likes = snapshot.get("likes") as? MutableMap<String, Boolean> ?: mutableMapOf()
            if (isLiked) {
                likes[uid] = true
            } else {
                likes.remove(uid)
            }
            transaction.update(ref, "likes", likes)
        }.addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    /**
     * Send friend request
     */
    fun sendFriendRequest(targetUid: String, fromUid: String, onComplete: (Boolean) -> Unit) {
        val requestMap = mapOf(
            "fromUid" to fromUid,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("friend_requests").document(targetUid).collection("received").document(fromUid)
            .set(requestMap)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Delete post by Admin or Owner
     */
    fun deletePost(postId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("posts").document(postId)
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Ban / Delete User
     */
    fun deleteUser(uid: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(uid)
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Delete Marketplace Item
     */
    fun deleteMarketplaceItem(itemId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("marketplace").document(itemId)
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Update App Config node on FriendHub Cloud
     */
    fun saveAppConfig(key: String, value: Any, onComplete: (Boolean) -> Unit) {
        firestore.collection("app_config").document(key)
            .set(mapOf("value" to value))
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Check if two users are mutual friends in FriendHub Cloud
     */
    fun checkMutualFriendship(uid1: String, uid2: String, onResult: (Boolean) -> Unit) {
        firestore.collection("friends").document(uid1).collection("list").document(uid2)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    firestore.collection("friends").document(uid2).collection("list").document(uid1)
                        .get()
                        .addOnSuccessListener { snap2 ->
                            onResult(snap2.exists())
                        }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Safe helper for server error messages
     */
    fun getGenericErrorMessage(rawError: String?): String {
        return if (rawError.isNullOrBlank()) {
            "Server connection error, please try again."
        } else {
            "FriendHub Cloud error: Please check network and try again."
        }
    }

    /**
     * Upload Story Image Bitmap to Storage
     */
    fun uploadStoryBitmap(uid: String, storyId: String, bitmap: Bitmap, onComplete: (String?) -> Unit) {
        try {
            val storage = FirebaseStorage.getInstance()
            val ref = storage.getReference("stories/$uid/$storyId.jpg")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val data = baos.toByteArray()
            ref.putBytes(data)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        onComplete(uri.toString())
                    }.addOnFailureListener { e ->
                        Log.e("AppRepository", "Download URL failed: ${e.message}")
                        onComplete(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AppRepository", "Upload failed: ${e.message}")
                    onComplete(null)
                }
        } catch (e: Exception) {
            Log.e("AppRepository", "Storage Exception: ${e.message}")
            onComplete(null)
        }
    }

    /**
     * Upload Story Video to Storage
     */
    fun uploadStoryVideo(uid: String, storyId: String, videoUri: Uri, onComplete: (String?) -> Unit) {
        try {
            val storage = FirebaseStorage.getInstance()
            val ref = storage.getReference("stories/$uid/$storyId.mp4")
            ref.putFile(videoUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        onComplete(uri.toString())
                    }.addOnFailureListener { e ->
                        Log.e("AppRepository", "Video Download URL failed: ${e.message}")
                        onComplete(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AppRepository", "Video Upload failed: ${e.message}")
                    onComplete(null)
                }
        } catch (e: Exception) {
            Log.e("AppRepository", "Video Storage Exception: ${e.message}")
            onComplete(null)
        }
    }

    /**
     * Save Story Metadata in DB
     */
    fun saveCloudStory(story: CloudStory, onComplete: (Boolean) -> Unit) {
        val map = mutableMapOf(
            "storyId" to story.storyId,
            "ownerId" to story.ownerId,
            "ownerName" to story.ownerName,
            "ownerAvatar" to story.ownerAvatar,
            "mediaUrl" to story.mediaUrl,
            "type" to story.type,
            "timestamp" to story.timestamp,
            "expiresAt" to story.expiresAt,
            "textOverlay" to story.textOverlay,
            "backgroundColor" to story.backgroundColor
        )
        firestore.collection("stories").document(story.storyId)
            .set(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Link in user public profile
                    firestore.collection("users").document(story.ownerId).collection("public_profile").document("stories")
                        .update(story.storyId, true)
                        .addOnFailureListener {
                            // If document doesn't exist, create it
                            firestore.collection("users").document(story.ownerId).collection("public_profile").document("stories")
                                .set(mapOf(story.storyId to true))
                        }
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }

    /**
     * Listen to active (non-expired) Stories of all users
     */
    fun listenToActiveStories(onStoriesChanged: (List<CloudStory>) -> Unit) {
        val now = System.currentTimeMillis()
        firestore.collection("stories")
            .whereGreaterThan("expiresAt", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AppRepository", "Listen stories failed: ${error.message}")
                    return@addSnapshotListener
                }
                
                val storiesList = mutableListOf<CloudStory>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val storyId = doc.getString("storyId") ?: doc.id
                        val ownerId = doc.getString("ownerId") ?: ""
                        val ownerName = doc.getString("ownerName") ?: ""
                        val ownerAvatar = doc.getString("ownerAvatar") ?: ""
                        val mediaUrl = doc.getString("mediaUrl") ?: ""
                        val type = doc.getString("type") ?: "photo"
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        val expiresAt = doc.getLong("expiresAt") ?: (timestamp + 24 * 3600000L)
                        val textOverlay = doc.getString("textOverlay") ?: ""
                        val backgroundColor = doc.getString("backgroundColor") ?: "#000000"

                        // Extract viewers
                        val viewersMap = mutableMapOf<String, Long>()
                        (doc.get("viewers") as? Map<*, *>)?.forEach { (k, v) ->
                            if (k is String && v is Number) {
                                viewersMap[k] = v.toLong()
                            }
                        }

                        // Extract reactions
                        val reactionsMap = mutableMapOf<String, String>()
                        (doc.get("reactions") as? Map<*, *>)?.forEach { (k, v) ->
                            if (k is String && v is String) {
                                reactionsMap[k] = v
                            }
                        }

                        storiesList.add(
                            CloudStory(
                                storyId = storyId,
                                ownerId = ownerId,
                                ownerName = ownerName,
                                ownerAvatar = ownerAvatar,
                                mediaUrl = mediaUrl,
                                type = type,
                                timestamp = timestamp,
                                expiresAt = expiresAt,
                                textOverlay = textOverlay,
                                backgroundColor = backgroundColor,
                                viewers = viewersMap,
                                reactions = reactionsMap
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onStoriesChanged(storiesList.sortedBy { it.timestamp })
            }
    }

    /**
     * Add Emoji Reaction to Story
     */
    fun addStoryReaction(storyOwnerId: String, storyId: String, reactorUid: String, emoji: String, onComplete: (Boolean) -> Unit) {
        val storyRef = firestore.collection("stories").document(storyId)
        storyRef.update("reactions.$reactorUid", emoji)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val rMap = mapOf("emoji" to emoji, "timestamp" to System.currentTimeMillis())
                    firestore.collection("story_reactions").document(storyId).collection("users").document(reactorUid)
                        .set(rMap)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }

    /**
     * Add Viewer to Story
     */
    fun addStoryViewer(storyOwnerId: String, storyId: String, viewerUid: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("stories").document(storyId).update("viewers.$viewerUid", System.currentTimeMillis())
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Save text reply on Story
     */
    fun addStoryReply(storyOwnerId: String, storyId: String, replyText: String, senderName: String, onComplete: (Boolean) -> Unit) {
        val replyId = "reply_${System.currentTimeMillis()}"
        val replyMap = mapOf(
            "replyId" to replyId,
            "replyText" to replyText,
            "senderName" to senderName,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("stories").document(storyId).collection("replies").document(replyId)
            .set(replyMap)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Delete Story
     */
    fun deleteStory(storyOwnerId: String, storyId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("stories").document(storyId)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firestore.collection("users").document(storyOwnerId).collection("public_profile").document("stories")
                        .update(storyId, com.google.firebase.firestore.FieldValue.delete())
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }

    /**
     * Highlights Integration (Permanent Stories grouped under custom name)
     */
    fun createHighlight(uid: String, title: String, storyId: String, coverUrl: String, onComplete: (Boolean) -> Unit) {
        val highlightId = "hl_${System.currentTimeMillis()}"
        val hlMap = mapOf(
            "highlightId" to highlightId,
            "title" to title,
            "coverUrl" to coverUrl,
            "ownerId" to uid,
            "stories" to mapOf(storyId to true)
        )
        firestore.collection("users").document(uid).collection("highlights").document(highlightId)
            .set(hlMap)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun addStoryToHighlight(uid: String, highlightId: String, storyId: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(uid).collection("highlights").document(highlightId)
            .update("stories.$storyId", true)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun fetchUserHighlights(uid: String, onResult: (List<CloudHighlight>) -> Unit) {
        firestore.collection("users").document(uid).collection("highlights")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<CloudHighlight>()
                snapshot.documents.forEach { doc ->
                    try {
                        val highlightId = doc.getString("highlightId") ?: doc.id
                        val title = doc.getString("title") ?: ""
                        val coverUrl = doc.getString("coverUrl") ?: ""
                        val ownerId = doc.getString("ownerId") ?: ""
                        val storiesMap = mutableMapOf<String, Boolean>()
                        (doc.get("stories") as? Map<*, *>)?.forEach { (k, v) ->
                            if (k is String && v is Boolean) {
                                storiesMap[k] = v
                            }
                        }
                        list.add(
                            CloudHighlight(
                                highlightId = highlightId,
                                title = title,
                                coverUrl = coverUrl,
                                ownerId = ownerId,
                                stories = storiesMap
                            )
                        )
                    } catch (e: Exception) {}
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
