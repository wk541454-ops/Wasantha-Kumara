package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
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
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class LiveComment(val user: String, val text: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamingScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var heartCount by remember { mutableIntStateOf(1420) }
    var chatMessage by remember { mutableStateOf("") }
    val commentsList = remember {
        mutableStateListOf(
            LiveComment("sarahjms", "Welcome everyone to the FH Live session!! 🔥", NeonMagenta),
            LiveComment("niko", "Sublime architectural setup!", NeonCyan),
            LiveComment("rudraksh", "Which software did you use for the rendering?", NeonOrange),
            LiveComment("franklin_", "Love the neon background aesthetic ✨", NeonPinkGlow)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            heartCount += (2..8).random()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Red)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("● LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("3.4K Viewers", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = NeonPureBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Stream Video Surface Background
            Image(
                painter = painterResource(id = R.drawable.post_architecture),
                contentDescription = "Live Video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Streamer Header Info
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(NeonMagenta)
                ) {
                    Text("SA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Sophia Anderson", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("@sophiaa", color = NeonCyan, fontSize = 10.sp)
                }
            }

            // Floating Chat & Interaction Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // Live Chat Messages
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(commentsList) { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.user, color = item.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = item.text, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Controls Input + Heart Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatMessage,
                        onValueChange = { chatMessage = it },
                        placeholder = { Text("Say something in live stream...", color = NeonTextSubtle, fontSize = 13.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (chatMessage.isNotBlank()) {
                                        commentsList.add(LiveComment("You", chatMessage, NeonCyan))
                                        chatMessage = ""
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = NeonCyan)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.7f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.7f),
                            focusedBorderColor = NeonPinkGlow,
                            unfocusedBorderColor = NeonCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Heart Floating Trigger
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(NeonPinkGlow, NeonMagenta))
                            )
                            .clickable {
                                heartCount++
                                Toast.makeText(context, "💖 Sent heart to stream!", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Heart", tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(text = "$heartCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
