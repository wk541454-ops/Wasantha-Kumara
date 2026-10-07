package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageItem
import com.example.data.repository.PresenceManager
import com.example.ui.theme.*
import com.example.utils.PresenceUtils

@Composable
fun ConversationItemRow(
    item: MessageItem,
    currentUid: String,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var isUserOnline by remember { mutableStateOf(item.isOnline) }
    var lastSeenTs by remember { mutableLongStateOf(0L) }

    val colors = LocalFriendHubColors.current

    DisposableEffect(item.id, currentUid) {
        val registration = PresenceManager.observeUserPresence(
            viewerUid = currentUid,
            targetUid = item.id,
            isMutualFriend = true,
            isBlocked = false
        ) { isOnline, lastSeen ->
            isUserOnline = isOnline
            lastSeenTs = lastSeen
        }

        onDispose {
            PresenceManager.removePresenceListener(registration)
        }
    }

    Surface(
        color = colors.cardBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                OnlineGreenRing(
                    isOnline = isUserOnline,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable { onProfileClick() }
                    ) {
                        Text(text = item.senderName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.senderName, color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (isUserOnline) "Online" else PresenceUtils.formatLastSeen(lastSeenTs), color = if (isUserOnline) colors.onlineGreen else colors.textSecondary, fontSize = 11.sp, fontWeight = if (isUserOnline) FontWeight.Bold else FontWeight.Normal)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.lastMessage, color = colors.textSecondary, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    if (item.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(18.dp).clip(CircleShape).background(colors.tertiary)
                        ) {
                            Text(text = "${item.unreadCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
