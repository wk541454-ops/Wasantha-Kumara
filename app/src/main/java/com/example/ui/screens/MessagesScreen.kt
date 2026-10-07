package com.example.ui.screens

import android.widget.Toast
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.isPureEmoji
import com.example.data.model.DirectChatMessage
import com.example.data.model.MessageItem
import com.example.data.repository.PresenceManager
import com.example.ui.components.ConversationItemRow
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.components.NeonBackground
import com.example.ui.components.OnlineGreenRing
import com.example.ui.theme.*
import com.example.utils.PresenceUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigate: (String) -> Unit
) {
    var activeChatUser by remember { mutableStateOf<MessageItem?>(null) }
    val currentUid = try { FirebaseAuth.getInstance().currentUser?.uid ?: "user_1" } catch (e: Exception) { "user_1" }

    val conversationsList = remember {
        mutableStateListOf(
            MessageItem("1", "Sarah James", "", "Hey Sophia! Loved your latest architecture post ✨", "10:14 AM", 2, true),
            MessageItem("2", "Niko Vance", "", "Are you attending the live session tonight?", "9:42 AM", 1, true),
            MessageItem("3", "Rudraksh Sharma", "", "Sent you the marketplace offer details.", "Yesterday", 0, false),
            MessageItem("4", "Franklin Sr", "", "Thanks for sharing the FH code sample!", "Oct 3", 0, false)
        )
    }

    if (activeChatUser != null) {
        ActiveChatScreen(
            chatUser = activeChatUser!!,
            onBack = { activeChatUser = null },
            onNavigate = onNavigate
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Messages & Chats", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate("home") }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigate("messenger_settings") }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Messenger Settings", tint = NeonCyan)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
                )
            },
            bottomBar = { CustomBottomNavBar(selectedRoute = "messages", onNavigate = onNavigate) },
            containerColor = NeonPureBlack
        ) { innerPadding ->
            NeonBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversationsList, key = { it.id }) { item ->
                        ConversationItemRow(
                            item = item,
                            currentUid = currentUid,
                            onChatClick = { activeChatUser = item },
                            onProfileClick = { onNavigate("profile") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveChatScreen(
    chatUser: MessageItem,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val colors = LocalFriendHubColors.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var textInput by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showCommandPanel by remember { mutableStateOf(false) }
    
    // Track played animations locally
    val playedAnimations = remember { mutableStateMapOf<String, Boolean>() }
    
    // Auth & Channel IDs
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    val currentUid = auth?.currentUser?.uid ?: "user_1"
    val conversationId = remember(chatUser.id, currentUid) {
        if (currentUid < chatUser.id) "${currentUid}_${chatUser.id}" else "${chatUser.id}_${currentUid}"
    }
    
    val firestore = remember {
        val dbId = context.getString(com.example.R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val chatMessages = remember { mutableStateListOf<DirectChatMessage>() }
    var isOtherTyping by remember { mutableStateOf(false) }
    val globalFloatingLikes = remember { mutableStateListOf<com.example.ui.components.FloatingLike>() }

    // Dialog state for Voice / Video Call
    var showCallDialog by remember { mutableStateOf<String?>(null) } // "voice" or "video"

    val sendMessage = { content: String, effect: String? ->
        if (content.isNotBlank()) {
            val msgId = "msg_${System.currentTimeMillis()}"
            val newMsgMap = mutableMapOf(
                "id" to msgId,
                "senderId" to currentUid,
                "text" to content.trim(),
                "timestamp" to "Just now",
                "timestampLong" to System.currentTimeMillis(),
                "seen" to false
            )
            if (effect != null) {
                newMsgMap["effectType"] = effect
            }
            
            // Immediate feedback
            val sentText = content.trim()
            textInput = ""
            showCommandPanel = false
            showEmojiPicker = false
            
            firestore.collection("chats").document(conversationId).collection("messages").document(msgId)
                .set(newMsgMap)
                .addOnSuccessListener {
                    // Success
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to send: ${it.message}", Toast.LENGTH_SHORT).show()
                    textInput = sentText
                }
        }
    }

    // 1. Real-time typing status publisher
    LaunchedEffect(textInput) {
        try {
            val typingRef = firestore.collection("chats").document(conversationId).collection("typing").document(currentUid)
            if (textInput.isNotEmpty()) {
                typingRef.set(mapOf("active" to true, "timestamp" to FieldValue.serverTimestamp()))
                delay(3000L)
                typingRef.set(mapOf("active" to false, "timestamp" to FieldValue.serverTimestamp()))
            } else {
                typingRef.set(mapOf("active" to false, "timestamp" to FieldValue.serverTimestamp()))
            }
        } catch (_: Exception) {}
    }

    // 2. Real-time typing status listener
    DisposableEffect(conversationId) {
        val otherTypingRef = firestore.collection("chats").document(conversationId).collection("typing").document(chatUser.id)
        val registration = otherTypingRef.addSnapshotListener { snapshot, error ->
            if (snapshot != null && snapshot.exists()) {
                isOtherTyping = snapshot.getBoolean("active") ?: false
            } else {
                isOtherTyping = false
            }
        }
        
        onDispose {
            registration.remove()
        }
    }

    // 3. Real-time message listener
    DisposableEffect(conversationId) {
        val messagesRef = firestore.collection("chats").document(conversationId).collection("messages")
        val registration = messagesRef.orderBy("timestampLong", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessagesScreen", "Listen messages failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = mutableListOf<DirectChatMessage>()
                    var incomingReactionTriggered = false
                    var triggeredEmoji = "👍"
                    
                    snapshot.documents.forEach { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val senderId = doc.getString("senderId") ?: ""
                            val text = doc.getString("text") ?: ""
                            val timestamp = doc.getString("timestamp") ?: ""
                            val timestampLong = doc.getLong("timestampLong") ?: 0L
                            val seen = doc.getBoolean("seen") ?: false
                            val effectType = doc.getString("effectType")
                            
                            val reactionsMap = mutableMapOf<String, String>()
                            (doc.get("reactions") as? Map<*, *>)?.forEach { (k, v) ->
                                if (k is String && v is String) {
                                    reactionsMap[k] = v
                                }
                            }
                            
                            val isFromMe = senderId == currentUid
                            val msg = DirectChatMessage(
                                id = id,
                                senderId = senderId,
                                text = text,
                                timestamp = timestamp,
                                isFromMe = isFromMe,
                                seen = seen,
                                reactions = reactionsMap,
                                timestampLong = timestampLong,
                                effectType = effectType
                            )
                            list.add(msg)
                            
                            // Real-time animation trigger on new reaction
                            val previousMsg = chatMessages.find { it.id == id }
                            if (previousMsg != null && previousMsg.reactions != msg.reactions) {
                                val newReact = msg.reactions.values.lastOrNull()
                                if (newReact != null) {
                                    triggeredEmoji = newReact
                                    incomingReactionTriggered = true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    chatMessages.clear()
                    chatMessages.addAll(list)
                    
                    if (incomingReactionTriggered) {
                        com.example.ui.components.spawnFloatingLikesBurst(
                            globalFloatingLikes,
                            count = 14,
                            primaryEmoji = triggeredEmoji
                        )
                    }
                }
            }
        
        onDispose {
            registration.remove()
        }
    }

    // Presence state observer
    var isChatUserOnline by remember { mutableStateOf(chatUser.isOnline) }
    var chatUserLastSeenTs by remember { mutableLongStateOf(0L) }
    var isMutualFriend by remember { mutableStateOf(false) }

    LaunchedEffect(chatUser.id, currentUid) {
        com.example.data.repository.AppRepository.checkMutualFriendship(currentUid, chatUser.id) { result ->
            isMutualFriend = result
        }
    }


    // Auto Seen status updater
    LaunchedEffect(chatMessages.size) {
        try {
            chatMessages.forEach { msg ->
                if (msg.senderId != currentUid && !msg.seen) {
                    firestore.collection("chats").document(conversationId).collection("messages").document(msg.id)
                        .update("seen", true)
                }
            }
        } catch (_: Exception) {}
    }
    DisposableEffect(chatUser.id, currentUid, isMutualFriend) {
        val listener = if (isMutualFriend) {
            PresenceManager.observeUserPresence(viewerUid = currentUid, targetUid = chatUser.id, isMutualFriend = true, isBlocked = false) { isOnline, lastSeen ->
                isChatUserOnline = isOnline
                chatUserLastSeenTs = lastSeen
            }
        } else null
        onDispose { PresenceManager.removePresenceListener(listener) }
    }

    fun makeCall(type: String) { showCallDialog = type }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("profile") }
                    ) {
                        OnlineGreenRing(isOnline = isChatUserOnline, modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.primary)) {
                                Text(text = chatUser.senderName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = chatUser.senderName, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = if (isOtherTyping) "typing..." else if (isChatUserOnline) "Online" else PresenceUtils.formatLastSeen(chatUserLastSeenTs),
                                color = if (isOtherTyping) colors.secondary else if (isChatUserOnline) colors.onlineGreen else colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { makeCall("voice") }) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Voice Call", tint = colors.secondary)
                    }
                    IconButton(onClick = { makeCall("video") }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = colors.tertiary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NeonBackground(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        var showMsgMenu by remember { mutableStateOf(false) }
                        val reactionEmojis = listOf("❤️", "😂", "👍", "🔥", "😮", "😢", "👏", "🎉")

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart) {
                            // Message Emoji Effect Layer
                            var triggerEffect by remember { mutableStateOf<Long?>(null) }
                            if (msg.text.isPureEmoji()) {
                                LaunchedEffect(msg.id) {
                                    if (playedAnimations[msg.id] != true) {
                                        delay(300)
                                        triggerEffect = System.currentTimeMillis()
                                        playedAnimations[msg.id] = true
                                    }
                                }
                                com.example.ui.components.MessageEmojiEffect(
                                    emoji = msg.text,
                                    trigger = triggerEffect,
                                    effectType = msg.effectType,
                                    modifier = Modifier.align(if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart)
                                )
                            }

                            Column(horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start) {
                                // Floating reactions picker
                                AnimatedVisibility(visible = showMsgMenu, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                                    Card(colors = CardDefaults.cardColors(containerColor = colors.cardBackground), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, colors.cardBorder), modifier = Modifier.padding(bottom = 4.dp)) {
                                        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            reactionEmojis.forEach { emoji ->
                                                Text(text = emoji, fontSize = 20.sp, modifier = Modifier.clickable { 
                                                    showMsgMenu = false
                                                    firestore.collection("chats").document(conversationId).collection("messages").document(msg.id)
                                                        .update("reactions.$currentUid", emoji)
                                                }.padding(4.dp))
                                            }
                                        }
                                    }
                                }

                                // Message Bubble
                                Surface(
                                    color = if (msg.isFromMe) colors.primary else colors.cardBackground,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.widthIn(max = 280.dp).combinedClickable(
                                        onClick = { showMsgMenu = false; if (msg.text.isPureEmoji()) triggerEffect = System.currentTimeMillis() },
                                        onLongClick = { showMsgMenu = true }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                        Text(text = msg.text, color = if (msg.isFromMe) Color.White else colors.textPrimary, fontSize = 14.sp)
                                        Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = msg.timestamp, color = (if (msg.isFromMe) Color.White else colors.textSecondary).copy(alpha = 0.7f), fontSize = 10.sp)
                                            if (msg.isFromMe) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(imageVector = if (msg.seen) Icons.Default.DoneAll else Icons.Default.Check, contentDescription = null, tint = if (msg.seen) colors.secondary else Color.White.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }

                                // Reactions indicator
                                if (msg.reactions.isNotEmpty()) {
                                    Row(modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        msg.reactions.forEach { (uid, reaction) ->
                                            Box(modifier = Modifier.background(colors.cardSelected, CircleShape).border(0.5.dp, colors.cardBorder, CircleShape).clickable { 
                                                if (uid == currentUid) {
                                                    firestore.collection("chats").document(conversationId).collection("messages").document(msg.id)
                                                        .update("reactions.$currentUid", FieldValue.delete())
                                                }
                                            }.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Text(text = reaction, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isOtherTyping) {
                    Text(text = "${chatUser.senderName} is typing...", color = colors.secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                }

                // Input Bar
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showEmojiPicker = !showEmojiPicker; showCommandPanel = false }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = if (showEmojiPicker) Icons.Default.Keyboard else Icons.Default.EmojiEmotions, contentDescription = "Emoji Picker", tint = colors.secondary, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { showCommandPanel = !showCommandPanel; showEmojiPicker = false }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = if (showCommandPanel) Icons.Default.Close else Icons.Default.AddCircle, contentDescription = "Menu Options", tint = colors.secondary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type message...", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardBackground,
                            unfocusedContainerColor = colors.cardBackground,
                            focusedBorderColor = colors.secondary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { sendMessage(textInput, null) }, modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.secondary)) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = colors.background)
                    }
                }

                // Sub-panels
                AnimatedVisibility(visible = showCommandPanel, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
                    CommandPanel(onOptionSelected = { option -> showCommandPanel = false; textInput = "[Attached $option] $textInput" })
                }
                AnimatedVisibility(visible = showEmojiPicker, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
                    com.example.ui.components.AnimatedEmojiPicker(onEmojiSelected = { emoji, effect -> if (emoji.isPureEmoji()) sendMessage(emoji, effect) else textInput += emoji }, onDismiss = { showEmojiPicker = false })
                }
            }
        }

        if (globalFloatingLikes.isNotEmpty()) {
            com.example.ui.components.FloatingLikesOverlay(likes = globalFloatingLikes)
        }

        showCallDialog?.let { type ->
            CallScreen(
                channelName = conversationId,
                isVideo = type == "video",
                onEndCall = { showCallDialog = null }
            )
        }
    }
}
@Composable
fun CommandPanel(
    onOptionSelected: (String) -> Unit
) {
    val options = listOf(
        Triple("Photo", Icons.Default.PhotoCamera, NeonPinkGlow),
        Triple("Video", Icons.Default.Videocam, NeonPurpleGlow),
        Triple("Gallery", Icons.Default.PhotoLibrary, NeonBlue),
        Triple("Camera", Icons.Default.CameraAlt, NeonCyan),
        Triple("File", Icons.Default.AttachFile, Color.White),
        Triple("Location", Icons.Default.LocationOn, NeonCyan),
        Triple("Contact", Icons.Default.Person, NeonMagenta),
        Triple("Voice Note", Icons.Default.Mic, NeonPinkGlow),
        Triple("GIF", Icons.Default.Gif, NeonPurple),
        Triple("Sticker", Icons.Default.Face, NeonBlue),
        Triple("Poll", Icons.Default.Poll, NeonCyan),
        Triple("Event", Icons.Default.Event, NeonMagenta),
        Triple("Music", Icons.Default.MusicNote, NeonPinkGlow),
        Triple("Share Profile", Icons.Default.AccountBox, NeonBlue),
        Triple("Share Post", Icons.Default.Share, NeonCyan),
        Triple("Share Story", Icons.Default.AutoAwesome, NeonMagenta),
        Triple("Marketplace", Icons.Default.ShoppingBag, NeonPinkGlow)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .background(NeonDarkSurface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, NeonCardBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "FRIENDHUB COMMAND & ATTACHMENT OPTIONS",
            color = NeonTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(options.size) { idx ->
                val (label, icon, color) = options[idx]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF151225), RoundedCornerShape(14.dp))
                        .border(0.5.dp, NeonCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onOptionSelected(label) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                }
            }
        }
    }
}
