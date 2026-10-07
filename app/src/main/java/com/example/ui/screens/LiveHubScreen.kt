package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.LiveSession
import com.example.data.model.UserProfile
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHubScreen(
    userProfile: UserProfile,
    userPrefs: UserPreferencesRepository,
    onNavigate: (String) -> Unit,
    onStartLiveClick: () -> Unit,
    onOpenLiveViewer: (String) -> Unit
) {
    val context = LocalContext.current
    val rtdb = remember { try { FirebaseDatabase.getInstance().reference } catch (e: Exception) { null } }

    var liveSessions by remember {
        mutableStateOf(
            listOf(
                LiveSession(
                    liveId = "live_sample_1",
                    creatorId = "user_sophia",
                    creatorName = "Sophia Anderson",
                    creatorHandle = "sophiaa",
                    title = "Building Jetpack Compose Android Architecture LIVE 🚀",
                    viewerCount = 1420,
                    category = "Tech & Dev",
                    thumbnailUrl = "",
                    isVerified = true
                ),
                LiveSession(
                    liveId = "live_sample_2",
                    creatorId = "user_alex",
                    creatorName = "Alex Rivera",
                    creatorHandle = "alexr",
                    title = "Acoustic Chill Session & Q&A 🎸",
                    viewerCount = 890,
                    category = "Music & Art",
                    thumbnailUrl = "",
                    isVerified = true
                ),
                LiveSession(
                    liveId = "live_sample_3",
                    creatorId = "user_elena",
                    creatorName = "Elena Rostova",
                    creatorHandle = "elena_dev",
                    title = "Full Stack AI App Development Masterclass ✨",
                    viewerCount = 2150,
                    category = "Tech & Dev",
                    thumbnailUrl = "",
                    isVerified = true
                )
            )
        )
    }
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    val categories = listOf("All Live", "Following", "Friends", "Popular", "Music & Art", "Tech & Dev")

    LaunchedEffect(Unit) {
        rtdb?.child("liveSessions")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<LiveSession>()
                for (child in snapshot.children) {
                    try {
                        val session = child.getValue(LiveSession::class.java)
                        if (session != null && session.status == "live") {
                            list.add(session)
                        }
                    } catch (_: Exception) {}
                }
                if (list.isNotEmpty()) {
                    liveSessions = list
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "FriendHub LIVE", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("● LIVE", color = WhiteText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(text = "Watch. Connect. Share.", color = GrayText, fontSize = 12.sp)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (userProfile.followersCount >= 1000) {
                                onStartLiveClick()
                            } else {
                                Toast.makeText(context, "Live requires 1,000 followers. Current: ${userProfile.followersCount}", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = WhiteText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Go Live", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackBg)
            )
        },
        bottomBar = {
            CustomBottomNavBar(selectedRoute = "live", onNavigate = onNavigate)
        },
        containerColor = BlackBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (userProfile.followersCount < 1000) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, PurpleMain.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Live Broadcast Unlocked at 1,000 Followers", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You currently have ${userProfile.followersCount} followers. Grow your audience by sharing engaging posts and stories to unlock broadcasting!",
                                color = GrayText,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { (userProfile.followersCount / 1000f).coerceIn(0f, 1f) },
                                color = PurpleMain,
                                trackColor = PillBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        val selected = selectedCategoryTab == index
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategoryTab = index },
                            label = { Text(cat, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleMain,
                                selectedLabelColor = WhiteText,
                                containerColor = CardBg,
                                labelColor = GrayText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = if (selected) PurpleMain else BorderGray
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LIVE NOW (${liveSessions.size})",
                        color = WhiteText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Explore all",
                        color = PurpleMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(liveSessions) { session ->
                LiveStreamCard(
                    session = session,
                    onClick = { onOpenLiveViewer(session.liveId) }
                )
            }
        }
    }
}

@Composable
fun LiveStreamCard(
    session: LiveSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.post_architecture),
                contentDescription = session.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = WhiteText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${session.viewerCount}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = session.title,
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PurpleMain, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = session.creatorName.take(2).uppercase(),
                            color = WhiteText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = session.creatorName,
                        color = WhiteText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    if (session.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${session.category}",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
