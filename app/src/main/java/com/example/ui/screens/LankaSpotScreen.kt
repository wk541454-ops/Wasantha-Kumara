package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class SpotItem(
    val id: String,
    val placeName: String,
    val xPercent: Float, // Map X coordinate
    val yPercent: Float, // Map Y coordinate
    val photoUrl: String,
    val userName: String,
    val userHandle: String,
    val userAvatar: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LankaSpotScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val rtdbRef = FirebaseDatabase.getInstance().reference

    var spotsList by remember { mutableStateOf<List<SpotItem>>(emptyList()) }
    var selectedSpot by remember { mutableStateOf<SpotItem?>(null) }
    var isQuerying by remember { mutableStateOf(false) }

    // Map Spot seed data
    val defaultSpots = listOf(
        SpotItem(
            id = "spot_pelmadulla",
            placeName = "Pelmadulla Viewpoint",
            xPercent = 0.52f,
            yPercent = 0.65f,
            photoUrl = "https://images.unsplash.com/photo-1544197423-1531-c6227db76b6e?auto=format&fit=crop&w=400&q=80",
            userName = "Wasantha Kumara",
            userHandle = "Wasantha_K",
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
            description = "Beautiful mountains and lush green landscape in Pelmadulla, Sri Lanka! 🏔️✨"
        ),
        SpotItem(
            id = "spot_colombo",
            placeName = "Colombo Lotus Tower",
            xPercent = 0.35f,
            yPercent = 0.58f,
            photoUrl = "https://images.unsplash.com/photo-1588646953014-85cb44e25828?auto=format&fit=crop&w=400&q=80",
            userName = "Nishadi Perera",
            userHandle = "nish_p",
            userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
            description = "Amazing views of the Colombo skyline from the Lotus Tower! 🗼💜"
        ),
        SpotItem(
            id = "spot_galle",
            placeName = "Galle Dutch Fort",
            xPercent = 0.42f,
            yPercent = 0.88f,
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80",
            userName = "Sophia Anderson",
            userHandle = "sophiaa",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
            description = "Historical lighthouse and beautiful sunset stroll around Galle Dutch Fort! 🌅🌊"
        ),
        SpotItem(
            id = "spot_kandy",
            placeName = "Kandy Lake Stroll",
            xPercent = 0.54f,
            yPercent = 0.48f,
            photoUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=400&q=80",
            userName = "Amara Silva",
            userHandle = "amara_s",
            userAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=150&q=80",
            description = "Lakeside views and peaceful climate in the sacred city of Kandy! 🍃🌸"
        )
    )

    // Load active spots from RTDB / FriendHub Cloud
    LaunchedEffect(Unit) {
        isQuerying = true
        rtdbRef.child("spots").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dbSpots = mutableListOf<SpotItem>()
                for (spotSnap in snapshot.children) {
                    val placeName = spotSnap.child("placeName").value as? String ?: "Cool Spot"
                    val photoUrl = spotSnap.child("photoUrl").value as? String ?: ""
                    val x = (spotSnap.child("xPercent").value as? Double ?: 0.5).toFloat()
                    val y = (spotSnap.child("yPercent").value as? Double ?: 0.5).toFloat()
                    val userId = spotSnap.child("userId").value as? String ?: "user_1"

                    dbSpots.add(
                        SpotItem(
                            id = spotSnap.key ?: "spot",
                            placeName = placeName,
                            xPercent = x,
                            yPercent = y,
                            photoUrl = photoUrl,
                            userName = "Local Spot Poster",
                            userHandle = "spot_lover",
                            userAvatar = "",
                            description = "Discovered an amazing new Lanka Spot! Come visit! 📍"
                        )
                    )
                }

                // Append custom spots with DB spots
                spotsList = defaultSpots + dbSpots
                isQuerying = false
            }

            override fun onCancelled(error: DatabaseError) {
                spotsList = defaultSpots
                isQuerying = false
            }
        })
    }

    // Infinite pulse transition for spot markers
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lanka Spot • map of cool places", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Intro Text
                Text(
                    text = "Lanka Spot Explorer 🇱🇰",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "Tap on the glowing radar nodes to discover trending spots posted by friends nearby.",
                    color = NeonTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp).align(Alignment.Start)
                )

                // STYLIZED MAP BOX
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NeonDarkSurface)
                        .border(1.dp, NeonCardBorder, RoundedCornerShape(24.dp))
                ) {
                    // Draw Interactive Map Coordinates of Sri Lanka
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val w = size.width
                        val h = size.height

                        // Draw Sri Lanka Silhouette outline path
                        val path = Path().apply {
                            moveTo(w * 0.45f, h * 0.15f) // Jaffna top
                            quadraticTo(w * 0.52f, h * 0.28f, w * 0.58f, h * 0.38f) // Trincomalee east coast
                            quadraticTo(w * 0.65f, h * 0.55f, w * 0.58f, h * 0.72f) // Batticaloa / Arugam Bay
                            quadraticTo(w * 0.52f, h * 0.88f, w * 0.45f, h * 0.90f) // Matara south
                            quadraticTo(w * 0.35f, h * 0.85f, w * 0.35f, h * 0.68f) // Colombo / West coast
                            quadraticTo(w * 0.32f, h * 0.42f, w * 0.40f, h * 0.25f) // Mannar / Kalpitiya
                            close()
                        }

                        // Draw beautiful glowing map grid filling
                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF8A2BE2).copy(alpha = 0.25f), Color.Transparent),
                                center = Offset(w * 0.48f, h * 0.55f),
                                radius = w * 0.35f
                            )
                        )

                        // Draw Map border line
                        drawPath(
                            path = path,
                            color = NeonMagenta,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw map grids
                        for (i in 1..8) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, h * (i / 9f)),
                                end = Offset(w, h * (i / 9f))
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(w * (i / 9f), 0f),
                                end = Offset(w * (i / 9f), h)
                            )
                        }
                    }

                    // Render Glowing radar nodes for spots on Map!
                    spotsList.forEach { spot ->
                        Box(
                            modifier = Modifier
                                .absoluteOffset(
                                    x = (spot.xPercent * 280).dp,
                                    y = (spot.yPercent * 300).dp
                                )
                                .size(24.dp)
                                .clickable { selectedSpot = spot },
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulse circle
                            Box(
                                modifier = Modifier
                                    .size(pulseRadius.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedSpot?.id == spot.id) NeonCyan.copy(alpha = 0.4f)
                                        else Color(0xFFFF007F).copy(alpha = 0.3f)
                                    )
                            )

                            // Inner core node
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedSpot?.id == spot.id) NeonCyan else Color(0xFFFF007F))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SPOT DETAILS OVERLAY COMPONENT
                AnimatedVisibility(
                    visible = selectedSpot != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val spot = selectedSpot
                    if (spot != null) {
                        Surface(
                            color = NeonDarkSurface,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, NeonCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Close button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = spot.placeName,
                                        color = NeonCyan,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { selectedSpot = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Spot image
                                AsyncImage(
                                    model = spot.photoUrl,
                                    contentDescription = spot.placeName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, NeonCardBorder, RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Description
                                Text(
                                    text = spot.description,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Poster Profile card
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                ) {
                                    AsyncImage(
                                        model = spot.userAvatar.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80" },
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, NeonCyan, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = spot.userName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${spot.userHandle}",
                                            color = NeonTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
