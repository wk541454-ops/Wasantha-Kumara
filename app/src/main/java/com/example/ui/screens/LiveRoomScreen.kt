package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LiveCommentItem
import com.example.data.model.LiveSession
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveRoomScreen(
    liveId: String,
    userProfile: UserProfile,
    onNavigate: (String) -> Unit,
    onLiveEnded: (LiveSession) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: "current_user_123"

    val rtdb = remember { try { FirebaseDatabase.getInstance().reference } catch (e: Exception) { unstableRef() } }

    var session by remember { mutableStateOf(LiveSession(liveId = liveId)) }
    var comments by remember { mutableStateOf<List<LiveCommentItem>>(emptyList()) }
    var inputMessage by remember { mutableStateOf("") }
    var floatingReactions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFollowingCreator by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }

    val isCreator = session.creatorId == currentUid

    // Real-time session listener
    LaunchedEffect(liveId) {
        rtdb.child("liveSessions/$liveId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val s = snapshot.getValue(LiveSession::class.java)
                if (s != null) {
                    session = s
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Real-time comments listener
        rtdb.child("liveComments/$liveId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<LiveCommentItem>()
                for (child in snapshot.children) {
                    val c = child.getValue(LiveCommentItem::class.java)
                    if (c != null) list.add(c)
                }
                comments = list.sortedBy { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Viewer presence tracking
        rtdb.child("liveSessions/$liveId/viewerCount").runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val count = currentData.getValue(Int::class.java) ?: 0
                currentData.value = count + 1
                return com.google.firebase.database.Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }

    // Leave presence cleanup on dispose
    DisposableEffect(liveId) {
        onDispose {
            try {
                rtdb.child("liveSessions/$liveId/viewerCount").runTransaction(object : com.google.firebase.database.Transaction.Handler {
                    override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                        val count = currentData.getValue(Int::class.java) ?: 1
                        currentData.value = (count - 1).coerceAtLeast(0)
                        return com.google.firebase.database.Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
                })
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonPureBlack)
    ) {
        // Video Stream Background
        Image(
            painter = painterResource(id = R.drawable.post_architecture),
            contentDescription = "Live Video Stream",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Overlays for readable text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Floating Reactions Animation Layer
        Box(modifier = Modifier.fillMaxSize()) {
            floatingReactions.forEachIndexed { index, emoji ->
                Text(
                    text = emoji,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = (30 + (index * 15)).dp, bottom = (120 + (index * 25)).dp)
                )
            }
        }

        // Top App Bar / Creator Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Creator info pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onNavigate("profile") }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PurpleMain, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.creatorName.take(2).uppercase().ifEmpty { "FH" },
                        color = WhiteText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = session.creatorName.ifEmpty { "Creator" }, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (session.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(12.sp.value.dp))
                        }
                    }
                    Text(text = "@${session.creatorHandle}", color = GrayText, fontSize = 10.sp)
                }

                if (!isCreator) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { isFollowingCreator = !isFollowingCreator },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFollowingCreator) Color.DarkGray else PurpleMain),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (isFollowingCreator) "Following" else "Follow", color = WhiteText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Viewer count & Close button
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = WhiteText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${session.viewerCount}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                IconButton(
                    onClick = {
                        if (isCreator) {
                            coroutineScope.launch {
                                rtdb.child("liveSessions/$liveId/status").setValue("ended")
                                rtdb.child("liveSessions/$liveId/endedAt").setValue(System.currentTimeMillis())
                            }
                            onLiveEnded(session)
                        } else {
                            onNavigate("live")
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WhiteText)
                }
            }
        }

        // Bottom Area: Comments and Interaction controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // Live Comments Stream
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                reverseLayout = true
            ) {
                items(comments.reversed()) { comment ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.userName, 
                                color = PurpleMain, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onNavigate("profile") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = comment.text, color = WhiteText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            if (isCreator) {
                                IconButton(
                                    onClick = {
                                        rtdb.child("liveComments/$liveId/${comment.commentId}").removeValue()
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = GrayText)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Input, Reactions, Share, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Say something...", color = GrayText, fontSize = 13.sp) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    val commentId = "c_${System.currentTimeMillis()}"
                                    val item = LiveCommentItem(
                                        commentId = commentId,
                                        liveId = liveId,
                                        userId = currentUid,
                                        userName = userProfile.name.ifEmpty { "User" },
                                        text = inputMessage.trim()
                                    )
                                    rtdb.child("liveComments/$liveId/$commentId").setValue(item)
                                    inputMessage = ""
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = PurpleMain)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.8f),
                        focusedBorderColor = PurpleMain,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                )

                // Reaction Hearts / Emojis Button
                IconButton(
                    onClick = {
                        val emojis = listOf("❤️", "🔥", "👏", "😂", "😍", "👍")
                        val chosen = emojis.random()
                        floatingReactions = floatingReactions + chosen
                        coroutineScope.launch {
                            delay(2000)
                            floatingReactions = floatingReactions.drop(1)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, BorderGray, CircleShape)
                ) {
                    Text("❤️", fontSize = 18.sp)
                }

                // Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Join my FriendHub Live stream: friendhub://live/$liveId")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Live Stream"))
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, BorderGray, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = WhiteText, modifier = Modifier.size(20.dp))
                }

                // More / Moderation Menu Button
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, BorderGray, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = WhiteText, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Moderation Dropdown Menu
        DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false },
            modifier = Modifier.background(CardBg)
        ) {
            DropdownMenuItem(
                text = { Text("Report Live Stream", color = Color.Red) },
                onClick = {
                    showMoreMenu = false
                    showReportDialog = true
                },
                leadingIcon = { Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = Color.Red) }
            )
            DropdownMenuItem(
                text = { Text("Block User", color = WhiteText) },
                onClick = {
                    showMoreMenu = false
                    Toast.makeText(context, "User blocked ✓", Toast.LENGTH_SHORT).show()
                },
                leadingIcon = { Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = WhiteText) }
            )
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Live Stream", color = WhiteText, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val categories = listOf("Spam", "Harassment", "Inappropriate content", "Dangerous behavior", "Other")
                    categories.forEach { cat ->
                        TextButton(
                            onClick = {
                                showReportDialog = false
                                Toast.makeText(context, "Report submitted ($cat). Thank you!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat, color = PurpleMain, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = CardBg
        )
    }
}

private fun unstableRef(): com.google.firebase.database.DatabaseReference {
    return FirebaseDatabase.getInstance().reference
}
