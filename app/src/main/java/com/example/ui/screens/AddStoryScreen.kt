package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonDarkSurface
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.NeonPureBlack
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.editor.CameraHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    currentUid: String,
    onNavigate: (String) -> Unit,
    onMediaSelected: (Uri?, String) -> Unit // uri, type ("photo"/"video"/"text")
) {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(true) }

    var tempFile by remember { mutableStateOf<File?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri, "photo")
            onNavigate("story_editor")
        } else {
            onNavigate("home")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempUri != null) {
            onMediaSelected(tempUri, "photo")
            onNavigate("story_editor")
        } else {
            onNavigate("home")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            try {
                val (file, uri) = CameraHelper.createImageFile(context, false, currentUid)
                tempFile = file
                tempUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error starting camera: ${e.message}", Toast.LENGTH_SHORT).show()
                onNavigate("home")
            }
        } else {
            Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            onNavigate("home")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Story", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
            )
        },
        containerColor = NeonPureBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Share what's on your mind! ✨",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Pick a photo or take a new one to apply beautiful neon stickers, custom frames, locations, and text styles.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleGlow),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Select Media Source", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { 
                        showSheet = false 
                        onNavigate("home")
                    },
                    containerColor = NeonDarkSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Add to Your Story 🌿",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ListItem(
                            headlineContent = { Text("📷 Take Photo - Use Camera", color = Color.White) },
                            leadingContent = { 
                                Icon(
                                    imageVector = Icons.Default.CameraAlt, 
                                    contentDescription = "Camera", 
                                    tint = NeonPinkGlow 
                                ) 
                            },
                            modifier = Modifier
                                .clickable {
                                    showSheet = false
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                                .background(Color.Transparent)
                        )

                        ListItem(
                            headlineContent = { Text("🖼️ Choose from Gallery", color = Color.White) },
                            leadingContent = { 
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary, 
                                    contentDescription = "Gallery", 
                                    tint = NeonPinkGlow 
                                ) 
                            },
                            modifier = Modifier
                                .clickable {
                                    showSheet = false
                                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .background(Color.Transparent)
                        )

                        ListItem(
                            headlineContent = { Text("✍️ Text-Only Story", color = Color.White) },
                            leadingContent = { 
                                Icon(
                                    imageVector = Icons.Default.ColorLens, 
                                    contentDescription = "Text Only", 
                                    tint = NeonPinkGlow 
                                ) 
                            },
                            modifier = Modifier
                                .clickable {
                                    showSheet = false
                                    onMediaSelected(null, "text")
                                    onNavigate("story_editor")
                                }
                                .background(Color.Transparent)
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
