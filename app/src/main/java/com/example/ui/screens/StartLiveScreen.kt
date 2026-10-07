package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.LiveSession
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartLiveScreen(
    userProfile: UserProfile,
    onNavigate: (String) -> Unit,
    onLiveStarted: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: "current_user_123"

    var liveTitle by remember { mutableStateOf("Chilling & building with FriendHub ✨") }
    var selectedCategory by remember { mutableStateOf("Tech & Dev") }
    var audienceVisibility by remember { mutableStateOf("Public") }
    var isMicMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasMicPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasMicPermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasMicPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-Live Broadcast Setup", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("live") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackBg)
            )
        },
        containerColor = BlackBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, BorderGray, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!isCameraOff && hasCameraPermission) {
                    Image(
                        painter = painterResource(id = R.drawable.post_architecture),
                        contentDescription = "Camera Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.VideocamOff, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Camera is off or permission missing", color = GrayText, fontSize = 13.sp)
                    }
                }

                // Controls overlay on preview
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isMicMuted = !isMicMuted }) {
                        Icon(
                            imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = if (isMicMuted) Color.Red else WhiteText
                        )
                    }
                    IconButton(onClick = { isCameraOff = !isCameraOff }) {
                        Icon(
                            imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Camera",
                            tint = if (isCameraOff) Color.Red else WhiteText
                        )
                    }
                }
            }

            // Live Title Input
            OutlinedTextField(
                value = liveTitle,
                onValueChange = { liveTitle = it },
                label = { Text("Live Broadcast Title", color = GrayText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleMain,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category & Visibility Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category", color = GrayText, fontSize = 13.sp)
                        Text(selectedCategory, color = PurpleMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = BorderGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audience Privacy", color = GrayText, fontSize = 13.sp)
                        Text(audienceVisibility, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Live Button
            Button(
                onClick = {
                    if (liveTitle.isBlank()) {
                        Toast.makeText(context, "Please enter a valid live title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!hasCameraPermission || !hasMicPermission) {
                        Toast.makeText(context, "Camera and Microphone permissions are required to go live", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            val liveId = "live_${System.currentTimeMillis()}"
                            val session = LiveSession(
                                liveId = liveId,
                                creatorId = currentUid,
                                creatorName = userProfile.name.ifEmpty { "Sophia Anderson" },
                                creatorHandle = userProfile.handle.ifEmpty { "sophiaa" },
                                creatorAvatarUrl = userProfile.avatarUrl,
                                title = liveTitle,
                                status = "live",
                                startedAt = System.currentTimeMillis(),
                                viewerCount = 1,
                                category = selectedCategory,
                                visibility = audienceVisibility,
                                isVerified = true
                            )

                            val rtdb = FirebaseDatabase.getInstance()
                            rtdb.reference.child("liveSessions/$liveId").setValue(session)

                            Toast.makeText(context, "Going Live! 🚀", Toast.LENGTH_SHORT).show()
                            onLiveStarted(liveId)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to start live: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.RadioButtonChecked, contentDescription = null, tint = WhiteText)
                Spacer(modifier = Modifier.width(10.dp))
                Text("START LIVE BROADCAST", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
