package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerSettingsScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUid = try { FirebaseAuth.getInstance().currentUser?.uid ?: "user_1" } catch (e: Exception) { "user_1" }

    // Helper to sync setting to Firebase users/{uid}/messengerSettings
    fun syncSettingToFirebase(key: String, value: Any) {
        try {
            val dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child("users/$currentUid/messengerSettings/$key").setValue(value)
        } catch (_: Exception) {}
    }

    // SECTION 1: Chat Settings
    var chatTheme by remember { mutableStateOf("Default") }
    var showMessagePreview by remember { mutableStateOf(true) }
    var enterKeySends by remember { mutableStateOf(true) }
    var showTypingIndicator by remember { mutableStateOf(true) }
    var showReadReceipts by remember { mutableStateOf(true) }
    var showOnlineStatus by remember { mutableStateOf(true) }

    // SECTION 2: Notifications
    var msgNotifications by remember { mutableStateOf(true) }
    var msgSound by remember { mutableStateOf(true) }
    var msgVibration by remember { mutableStateOf(true) }
    var groupNotifs by remember { mutableStateOf(true) }
    var callNotifs by remember { mutableStateOf(true) }
    var previewOnNotif by remember { mutableStateOf(true) }

    // SECTION 3: Privacy
    var whoCanMessage by remember { mutableStateOf("Everyone") }
    var allowMsgRequests by remember { mutableStateOf(true) }
    var disappearingMsgs by remember { mutableStateOf("OFF") }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showRestrictedDialog by remember { mutableStateOf(false) }

    // SECTION 4: Media & Data
    var autoDownloadImages by remember { mutableStateOf(true) }
    var autoDownloadVideos by remember { mutableStateOf(false) }
    var autoDownloadFiles by remember { mutableStateOf(false) }
    var wifiOnlyDownload by remember { mutableStateOf(true) }
    var dataSaverMode by remember { mutableStateOf(false) }

    // SECTION 5: Appearance
    var wallpaperStyle by remember { mutableStateOf("Default Neon") }
    var bubbleStyle by remember { mutableStateOf("Standard Neon") }
    var fontSizeStyle by remember { mutableStateOf("Normal") }

    // SECTION 6: Call Settings
    var incomingCallNotifs by remember { mutableStateOf(true) }
    var callSound by remember { mutableStateOf(true) }
    var callVibration by remember { mutableStateOf(true) }
    var callAvailability by remember { mutableStateOf(true) }
    var blockIncomingCalls by remember { mutableStateOf(false) }

    // SECTION 7: Chat Management Dialogs
    var showArchivedDialog by remember { mutableStateOf(false) }
    var showMutedDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf(false) }
    var showGroupChatsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // SECTION 8 & 9: Storage & Security
    var cacheSizeMb by remember { mutableFloatStateOf(14.2f) }
    var showSecurityInfoDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val blockedList = remember { mutableStateListOf("User_Spammer_99", "Fake_Account_01") }
    val restrictedList = remember { mutableStateListOf("User_Unknown_44") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Messenger Settings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Manage chat, privacy & media preferences",
                            color = NeonTextSubtle,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("messages") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to Messages", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
            )
        },
        containerColor = NeonPureBlack
    ) { innerPadding ->
        NeonBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // SECTION 1: CHAT SETTINGS
                MessengerSectionHeader(title = "CHAT SETTINGS", icon = Icons.Default.ChatBubbleOutline)
                MessengerCardContainer {
                    // Chat Theme
                    MessengerSelectorRow(
                        label = "Chat Theme",
                        currentValue = chatTheme,
                        options = listOf("Default", "Dark", "Light", "System"),
                        onSelected = {
                            chatTheme = it
                            syncSettingToFirebase("chatTheme", it)
                            Toast.makeText(context, "Chat Theme set to $it ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Show Message Preview",
                        checked = showMessagePreview,
                        onCheckedChange = {
                            showMessagePreview = it
                            syncSettingToFirebase("showMessagePreview", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Enter Key Sends Message",
                        checked = enterKeySends,
                        onCheckedChange = {
                            enterKeySends = it
                            syncSettingToFirebase("enterKeySends", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Typing Indicator",
                        checked = showTypingIndicator,
                        onCheckedChange = {
                            showTypingIndicator = it
                            syncSettingToFirebase("showTypingIndicator", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Read Receipts",
                        checked = showReadReceipts,
                        onCheckedChange = {
                            showReadReceipts = it
                            syncSettingToFirebase("showReadReceipts", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Online Status",
                        checked = showOnlineStatus,
                        onCheckedChange = {
                            showOnlineStatus = it
                            syncSettingToFirebase("showOnlineStatus", it)
                        }
                    )
                }

                // SECTION 2: NOTIFICATIONS
                MessengerSectionHeader(title = "NOTIFICATIONS", icon = Icons.Default.Notifications)
                MessengerCardContainer {
                    MessengerSwitchRow(
                        label = "Message Notifications",
                        checked = msgNotifications,
                        onCheckedChange = {
                            msgNotifications = it
                            syncSettingToFirebase("msgNotifications", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Message Sound",
                        checked = msgSound,
                        onCheckedChange = {
                            msgSound = it
                            syncSettingToFirebase("msgSound", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Vibration",
                        checked = msgVibration,
                        onCheckedChange = {
                            msgVibration = it
                            syncSettingToFirebase("msgVibration", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Group Message Notifications",
                        checked = groupNotifs,
                        onCheckedChange = {
                            groupNotifs = it
                            syncSettingToFirebase("groupNotifs", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Call Notifications",
                        checked = callNotifs,
                        onCheckedChange = {
                            callNotifs = it
                            syncSettingToFirebase("callNotifs", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Message Preview in Notification",
                        checked = previewOnNotif,
                        onCheckedChange = {
                            previewOnNotif = it
                            syncSettingToFirebase("previewOnNotif", it)
                        }
                    )
                }

                // SECTION 3: CHAT PRIVACY
                MessengerSectionHeader(title = "CHAT PRIVACY", icon = Icons.Default.Lock)
                MessengerCardContainer {
                    MessengerSelectorRow(
                        label = "Who can message me",
                        currentValue = whoCanMessage,
                        options = listOf("Everyone", "Friends", "Nobody"),
                        onSelected = {
                            whoCanMessage = it
                            syncSettingToFirebase("whoCanMessage", it)
                            Toast.makeText(context, "Messaging privilege updated: $it ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Allow Message Requests",
                        checked = allowMsgRequests,
                        onCheckedChange = {
                            allowMsgRequests = it
                            syncSettingToFirebase("allowMsgRequests", it)
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Blocked Users (${blockedList.size})",
                        actionText = "Manage",
                        onClick = { showBlockedDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Restricted Users (${restrictedList.size})",
                        actionText = "Manage",
                        onClick = { showRestrictedDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSelectorRow(
                        label = "Disappearing Messages",
                        currentValue = disappearingMsgs,
                        options = listOf("OFF", "24 hours", "7 days", "30 days"),
                        onSelected = {
                            disappearingMsgs = it
                            syncSettingToFirebase("disappearingMsgs", it)
                            Toast.makeText(context, "Disappearing messages set to $it ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // SECTION 4: MEDIA & DATA
                MessengerSectionHeader(title = "MEDIA & DATA", icon = Icons.Default.CloudDownload)
                MessengerCardContainer {
                    MessengerSwitchRow(
                        label = "Auto-download images",
                        checked = autoDownloadImages,
                        onCheckedChange = { autoDownloadImages = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Auto-download videos",
                        checked = autoDownloadVideos,
                        onCheckedChange = { autoDownloadVideos = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Auto-download files",
                        checked = autoDownloadFiles,
                        onCheckedChange = { autoDownloadFiles = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Wi-Fi Only for Downloads",
                        checked = wifiOnlyDownload,
                        onCheckedChange = { wifiOnlyDownload = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Data Saver Mode",
                        checked = dataSaverMode,
                        onCheckedChange = { dataSaverMode = it }
                    )
                }

                // SECTION 5: CHAT APPEARANCE
                MessengerSectionHeader(title = "CHAT APPEARANCE", icon = Icons.Default.Palette)
                MessengerCardContainer {
                    MessengerSelectorRow(
                        label = "Chat Wallpaper",
                        currentValue = wallpaperStyle,
                        options = listOf("Default Neon", "Midnight Purple", "Dark Emerald"),
                        onSelected = {
                            wallpaperStyle = it
                            Toast.makeText(context, "Wallpaper updated: $it ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSelectorRow(
                        label = "Message Bubble Style",
                        currentValue = bubbleStyle,
                        options = listOf("Standard Neon", "Rounded Pill", "Minimal Cyber"),
                        onSelected = {
                            bubbleStyle = it
                            Toast.makeText(context, "Bubble Style updated: $it ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSelectorRow(
                        label = "Font Size",
                        currentValue = fontSizeStyle,
                        options = listOf("Small", "Normal", "Large"),
                        onSelected = {
                            fontSizeStyle = it
                            Toast.makeText(context, "Font Size set to $it ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // SECTION 6: CALL SETTINGS
                MessengerSectionHeader(title = "CALL SETTINGS", icon = Icons.Default.PhoneInTalk)
                MessengerCardContainer {
                    MessengerSwitchRow(
                        label = "Incoming Call Notifications",
                        checked = incomingCallNotifs,
                        onCheckedChange = { incomingCallNotifs = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Call Ringtone Sound",
                        checked = callSound,
                        onCheckedChange = { callSound = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Call Vibration",
                        checked = callVibration,
                        onCheckedChange = { callVibration = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Online Availability for Calls",
                        checked = callAvailability,
                        onCheckedChange = { callAvailability = it }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerSwitchRow(
                        label = "Block All Incoming Calls",
                        checked = blockIncomingCalls,
                        onCheckedChange = { blockIncomingCalls = it }
                    )
                }

                // SECTION 7: CHAT MANAGEMENT
                MessengerSectionHeader(title = "CHAT MANAGEMENT", icon = Icons.Default.FolderSpecial)
                MessengerCardContainer {
                    MessengerActionRow(
                        label = "Archived Chats (2)",
                        actionText = "View",
                        onClick = { showArchivedDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Muted Chats (1)",
                        actionText = "View",
                        onClick = { showMutedDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Message Requests (3)",
                        actionText = "View",
                        onClick = { showRequestsDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Group Chats (4)",
                        actionText = "Manage",
                        onClick = { showGroupChatsDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Delete Active Conversation",
                        actionText = "Delete",
                        actionColor = Color.Red,
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }

                // SECTION 8: SECURITY
                MessengerSectionHeader(title = "SECURITY & PRIVACY", icon = Icons.Default.Security)
                MessengerCardContainer {
                    MessengerActionRow(
                        label = "End-to-End Chat Encryption Info",
                        actionText = "Info",
                        onClick = { showSecurityInfoDialog = true }
                    )
                    HorizontalDivider(color = NeonCardBorder)
                    MessengerActionRow(
                        label = "Report Suspicious Conversation",
                        actionText = "Report",
                        actionColor = Color(0xFFFFB800),
                        onClick = { showReportDialog = true }
                    )
                }

                // SECTION 9: STORAGE & CACHE
                MessengerSectionHeader(title = "STORAGE & CACHE", icon = Icons.Default.Storage)
                MessengerCardContainer {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Temporary Chat Cache", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Images: 8.5 MB | Videos: 4.2 MB | Files: 1.5 MB", color = NeonTextSubtle, fontSize = 11.sp)
                        }
                        Text("${"%.1f".format(cacheSizeMb)} MB", color = NeonCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = NeonCardBorder)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (cacheSizeMb > 0f) {
                                    cacheSizeMb = 0.0f
                                    Toast.makeText(context, "Temporary chat cache cleared! (14.2 MB freed) ✓", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Cache is already empty (0.0 MB)", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Clear Temporary Chat Cache", color = NeonPinkGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // DIALOGS

    // 1. Blocked Users Dialog
    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("Blocked Users", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (blockedList.isEmpty()) {
                        Text("No blocked users.", color = NeonTextMuted)
                    } else {
                        blockedList.forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user, color = Color.White, fontSize = 13.sp)
                                TextButton(onClick = {
                                    blockedList.remove(user)
                                    Toast.makeText(context, "$user unblocked ✓", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Unblock", color = NeonCyan)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBlockedDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                    Text("Close", color = Color.Black)
                }
            }
        )
    }

    // 2. Restricted Users Dialog
    if (showRestrictedDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictedDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("Restricted Users", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (restrictedList.isEmpty()) {
                        Text("No restricted users.", color = NeonTextMuted)
                    } else {
                        restrictedList.forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user, color = Color.White, fontSize = 13.sp)
                                TextButton(onClick = {
                                    restrictedList.remove(user)
                                    Toast.makeText(context, "$user restriction removed ✓", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Remove", color = NeonCyan)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRestrictedDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                    Text("Close", color = Color.Black)
                }
            }
        )
    }

    // 3. Archived Chats Dialog
    if (showArchivedDialog) {
        AlertDialog(
            onDismissRequest = { showArchivedDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("Archived Conversations", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Franklin Sr (Oct 3, 2026)", color = Color.White, fontSize = 13.sp)
                    Text("• Rudraksh Sharma (Yesterday)", color = Color.White, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showArchivedDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                    Text("Close", color = Color.Black)
                }
            }
        )
    }

    // 4. Muted Chats Dialog
    if (showMutedDialog) {
        AlertDialog(
            onDismissRequest = { showMutedDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("Muted Conversations", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• FriendHub Tech Developers Group (Muted for 8 hrs)", color = Color.White, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showMutedDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                    Text("Close", color = Color.Black)
                }
            }
        )
    }

    // 5. Delete Conversation Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("Delete Conversation?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete this active chat thread? All local messages will be removed. This action cannot be undone.",
                    color = NeonTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Conversation deleted successfully ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // 6. Security Info Dialog
    if (showSecurityInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityInfoDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("End-to-End Chat Encryption", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "FriendHub Messenger uses end-to-end encryption protocols for private 1-on-1 direct messages.\n\nYour messages, photos, and voice notes stay private between you and the recipient.",
                    color = NeonTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(onClick = { showSecurityInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                    Text("Got it", color = Color.Black)
                }
            }
        )
    }

    // 7. Report Conversation Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = NeonDarkSurface,
            title = { Text("Report Conversation", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Submit a report for review by FriendHub Moderation Admins if this chat contains spam or abusive content.",
                    color = NeonTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        Toast.makeText(context, "Conversation reported to Moderation Team ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800))
                ) {
                    Text("Submit Report", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun MessengerSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = NeonPinkGlow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun MessengerCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = NeonDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NeonCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun MessengerSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple)
        )
    }
}

@Composable
fun MessengerSelectorRow(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = currentValue, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(NeonDarkSurface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = {
                            expanded = false
                            onSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MessengerActionRow(
    label: String,
    actionText: String,
    actionColor: Color = NeonCyan,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(text = actionText, color = actionColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
