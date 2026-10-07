package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.CommentItem
import com.example.data.model.PostItem
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    post: PostItem,
    onDismiss: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    var newCommentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firestore = remember {
        val dbId = context.getString(com.example.R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUid = auth.currentUser?.uid ?: "user_1"
    var currentUsername by remember { mutableStateOf("You") }

    // Fetch current username
    LaunchedEffect(currentUid) {
        try {
            com.example.data.repository.AppRepository.getUserProfile(currentUid) { profile ->
                if (profile != null) {
                    currentUsername = (profile["fullName"] as? String) ?: (profile["name"] as? String) ?: "You"
                }
            }
        } catch (_: Exception) {}
    }

    val comments = remember { mutableStateListOf<CommentItem>() }

    // 1. Real-time comment listener from Firestore
    DisposableEffect(post.id) {
        val commentsRef = firestore.collection("posts").document(post.id).collection("comments")
        val registration = commentsRef.orderBy("timestampLong", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = mutableListOf<CommentItem>()
                    if (snapshot.isEmpty) {
                        // Fallback to initial dummy comments if collection is completely empty
                        list.add(CommentItem("1", post.id, "niko", "", "Stunning architecture! ✨ Love the clean lines.", "15m ago", 12))
                        list.add(CommentItem("2", post.id, "sarahjms", "", "Which camera lens was this taken with?", "40m ago", 5))
                    } else {
                        snapshot.documents.forEach { doc ->
                            try {
                                val id = doc.id
                                val author = doc.getString("authorName") ?: "User"
                                val text = doc.getString("text") ?: ""
                                val timestamp = doc.getString("timestamp") ?: "Just now"
                                val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                list.add(CommentItem(id, post.id, author, "", text, timestamp, likesCount))
                            } catch (_: Exception) {}
                        }
                    }
                    comments.clear()
                    comments.addAll(list)
                }
            }

        onDispose {
            registration.remove()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NeonDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Comments (${comments.size + post.commentsCount - 4})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comments, key = { it.id }) { comment ->
                    val index = comments.indexOf(comment)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonPurple)
                                .clickable {
                                    onDismiss()
                                    onProfileClick()
                                }
                        ) {
                            Text(text = comment.authorName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = comment.authorName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.clickable {
                                        onDismiss()
                                        onProfileClick()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = comment.timestamp, color = NeonTextSubtle, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = comment.text, color = NeonTextMuted, fontSize = 13.sp)
                        }

                        // Comment Like Button
                        com.example.ui.components.FriendHubLikeButton(
                            isLiked = comment.isLiked,
                            onLikeToggle = { liked ->
                                val newCount = if (liked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
                                comments[index] = comment.copy(isLiked = liked, likesCount = newCount)
                            },
                            onReactionSelect = { emoji ->
                                val newCount = comment.likesCount + 1
                                comments[index] = comment.copy(isLiked = true, likesCount = newCount)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add comment input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...", color = NeonTextSubtle) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeonCardSurface,
                        unfocusedContainerColor = NeonCardSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = NeonCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            val commentId = "comment_${System.currentTimeMillis()}"
                            val commentData = mapOf(
                                "id" to commentId,
                                "authorName" to currentUsername,
                                "text" to newCommentText.trim(),
                                "timestamp" to "Just now",
                                "timestampLong" to System.currentTimeMillis(),
                                "likesCount" to 0
                            )
                            firestore.collection("posts").document(post.id).collection("comments").document(commentId)
                                .set(commentData)
                                .addOnSuccessListener {
                                    newCommentText = ""
                                }
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}
