package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.TextureView
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.utils.AgoraManager
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas

@Composable
fun CallScreen(
    channelName: String,
    isVideo: Boolean,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(isVideo) }
    var remoteUidState by remember { mutableStateOf<Int?>(null) }
    var callStatus by remember { mutableStateOf("Connecting...") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

        if (recordAudioGranted && (!isVideo || cameraGranted)) {
            // Permissions granted, start Agora
            startAgoraCall(context, channelName, isVideo, onRemoteJoined = { uid ->
                remoteUidState = uid
                callStatus = "Connected"
            }, onRemoteOffline = {
                remoteUidState = null
                callStatus = "User offline"
            })
        } else {
            callStatus = "Permissions Denied"
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (isVideo) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
        permissionLauncher.launch(permissions)
    }

    DisposableEffect(Unit) {
        onDispose {
            AgoraManager.leaveChannel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
    ) {
        if (isVideoEnabled) {
            // Local & Remote Video feeds
            Box(modifier = Modifier.fillMaxSize()) {
                if (remoteUidState != null) {
                    // Remote Video takes full screen
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                val view = TextureView(ctx)
                                addView(view)
                                AgoraManager.getEngine()?.setupRemoteVideo(
                                    VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, remoteUidState!!)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Waiting for recipient...", color = Color.White, fontSize = 16.sp)
                    }
                }

                // Local Video takes picture-in-picture floating box
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 20.dp)
                        .size(120.dp, 160.dp)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                val view = TextureView(ctx)
                                addView(view)
                                AgoraManager.getEngine()?.setupLocalVideo(
                                    VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Voice Call Audio Mode Only UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF241D3B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Calling",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isVideo) "Video Call" else "Voice Call",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Room: $channelName",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = callStatus,
                    color = Color(0xFF00FFCC),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Call status indicator for Video Call
        if (isVideoEnabled) {
            Text(
                text = callStatus,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 20.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            )
        }

        // Call Control Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    AgoraManager.getEngine()?.muteLocalAudioStream(isMuted)
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = Color.White
                )
            }

            // End Call Button
            IconButton(
                onClick = {
                    AgoraManager.leaveChannel()
                    onEndCall()
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Red, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (isVideo) {
                // Toggle Video Button
                IconButton(
                    onClick = {
                        isVideoEnabled = !isVideoEnabled
                        AgoraManager.getEngine()?.enableLocalVideo(isVideoEnabled)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(if (!isVideoEnabled) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Toggle Video",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun startAgoraCall(
    context: Context,
    channelName: String,
    isVideo: Boolean,
    onRemoteJoined: (Int) -> Unit,
    onRemoteOffline: () -> Unit
) {
    AgoraManager.init(context)
    val rtcEngine = AgoraManager.getEngine() ?: return

    rtcEngine.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION)
    if (isVideo) {
        rtcEngine.enableVideo()
    } else {
        rtcEngine.disableVideo()
    }

    // Set custom handler for call status
    rtcEngine.addHandler(object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            onRemoteJoined(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            onRemoteOffline()
        }
    })

    // Join channel using secure locked Agora token
    rtcEngine.joinChannel(AgoraManager.getPrimaryToken(), channelName, null, 0)
}
