package com.example.ui.editor

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoStyleEditorScreen(
    imageUri: Uri? = null,
    originalBitmap: Bitmap? = null,
    initialFrameId: String = "frame_purple_neon",
    initialStickerId: String = "sticker_verified",
    initialFilterId: String = "filter_original",
    onSaveBitmap: ((Bitmap) -> Unit)? = null,
    onApplyAndSave: ((Uri?, String, String, String) -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: "user_123"

    var selectedFrameId by remember { mutableStateOf(initialFrameId) }
    var selectedStickerId by remember { mutableStateOf(initialStickerId) }
    var selectedFilterId by remember { mutableStateOf(initialFilterId) }
    var activeCategoryTab by remember { mutableIntStateOf(0) } // 0: Frames, 1: Stickers, 2: Filters, 3: Bio Templates

    var isSaving by remember { mutableStateOf(false) }

    val bioTemplates = listOf(
        "Dream • Learn • Build • Grow 🌿",
        "Technology | Travel | Photography | Good Vibes",
        "Pelmadulla, Sri Lanka 📍",
        "Creator • Developer • Innovator 🚀"
    )

    // Calculate dynamic ColorFilter matrix for live preview
    val liveColorFilter = remember(selectedFilterId) {
        val matrixArray = when (selectedFilterId) {
            "filter_bright" -> floatArrayOf(
                1.2f, 0f, 0f, 0f, 10f,
                0f, 1.2f, 0f, 0f, 10f,
                0f, 0f, 1.2f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
            "filter_dark" -> floatArrayOf(
                0.8f, 0f, 0f, 0f, -20f,
                0f, 0.8f, 0f, 0f, -20f,
                0f, 0f, 0.9f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            "filter_sunset" -> floatArrayOf(
                1.3f, 0f, 0f, 0f, 20f,
                0.1f, 1.1f, 0f, 0f, 10f,
                0f, 0f, 0.8f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            "filter_nature" -> floatArrayOf(
                0.9f, 0f, 0f, 0f, -10f,
                0f, 1.3f, 0f, 0f, 15f,
                0f, 0.2f, 1.1f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
            "filter_bw" -> null // Handled with built-in saturation
            else -> null
        }

        if (selectedFilterId == "filter_bw") {
            val cm = ColorMatrix().apply { setToSaturation(0f) }
            ColorFilter.colorMatrix(cm)
        } else if (matrixArray != null) {
            ColorFilter.colorMatrix(ColorMatrix(matrixArray))
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Style Editor (FB Frames)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // PREVIEW - CIRCLE AVATAR WITH GRADIENT BORDER
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(
                            4.dp,
                            Brush.linearGradient(listOf(Color(0xFF8A2BE2), Color(0xFF00D1FF))),
                            CircleShape
                        )
                        .background(NeonDarkSurface)
                ) {
                    if (originalBitmap != null) {
                        Image(
                            bitmap = originalBitmap.asImageBitmap(),
                            contentDescription = "Original Photo",
                            contentScale = ContentScale.Crop,
                            colorFilter = liveColorFilter,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            colorFilter = liveColorFilter,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(NeonPurple)
                        ) {
                            Text("FH", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Live Canvas Overlay for Frame & Sticker
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        FrameOverlay.drawFrame(selectedFrameId, this, size)
                        StickerOverlay.drawSticker(selectedStickerId, this, size)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CATEGORY TABS
                TabRow(
                    selectedTabIndex = activeCategoryTab,
                    containerColor = NeonDarkSurface,
                    contentColor = NeonCyan,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = activeCategoryTab == 0,
                        onClick = { activeCategoryTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Frames", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeCategoryTab == 1,
                        onClick = { activeCategoryTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stickers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeCategoryTab == 2,
                        onClick = { activeCategoryTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Filter, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeCategoryTab == 3,
                        onClick = { activeCategoryTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bios", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CONTENT BASED ON ACTIVE TAB
                when (activeCategoryTab) {
                    0 -> {
                        // FRAMES ROW
                        Text("Frames (20 Facebook-Style Neon & Pride Frames)", color = Color.Gray, modifier = Modifier.align(Alignment.Start), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(FrameOverlay.FRAMES_LIST) { frame ->
                                val isSelected = frame.id == selectedFrameId
                                Surface(
                                    color = if (isSelected) NeonDarkSurface else NeonPureBlack,
                                    shape = CircleShape,
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.Cyan else NeonCardBorder),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { selectedFrameId = frame.id }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                            FrameOverlay.drawFrame(frame.id, this, size)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // STICKERS ROW (Heart, Fire, Crown, Star)
                        Text("Stickers & Badge Overlays", color = Color.Gray, modifier = Modifier.align(Alignment.Start), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(StickerOverlay.STICKERS_LIST) { sticker ->
                                val isSelected = sticker.id == selectedStickerId
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonDarkSurface else Color.DarkGray)
                                        .border(BorderStroke(if (isSelected) 2.dp else 0.dp, Color.Cyan), CircleShape)
                                        .clickable { selectedStickerId = sticker.id }
                                ) {
                                    Text(sticker.iconEmoji, fontSize = 26.sp)
                                }
                            }
                        }
                    }

                    2 -> {
                        // FILTERS ROW
                        Text("Filters - Instagram & Sunset Styles", color = Color.Gray, modifier = Modifier.align(Alignment.Start), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ImageComposer.FILTERS_LIST) { filter ->
                                val isSelected = filter.id == selectedFilterId
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonDarkSurface else Color(0xFF161616)),
                                    border = BorderStroke(if (isSelected) 1.5.dp else 0.dp, Color.Cyan),
                                    modifier = Modifier
                                        .size(height = 68.dp, width = 100.dp)
                                        .clickable { selectedFilterId = filter.id }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(filter.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(filter.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // BIO TEMPLATES ROW
                        Text("Bio Quick Templates", color = Color.Gray, modifier = Modifier.align(Alignment.Start), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(bioTemplates) { template ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable {
                                            try {
                                                FirebaseDatabase.getInstance().reference
                                                    .child("users/$currentUid/public_profile/bio")
                                                    .setValue(template)
                                            } catch (e: Exception) {}
                                            Toast.makeText(context, "Bio Template set to: $template", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Text(template, color = Color.White, modifier = Modifier.padding(10.dp), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // APPLY & SAVE BUTTON
                Button(
                    onClick = {
                        isSaving = true
                        try {
                            // 1. Get source bitmap
                            val srcBitmap = originalBitmap ?: imageUri?.let { ImageComposer.getBitmapFromUri(context, it) }
                            if (srcBitmap != null) {
                                // 2. Compose styled circular image
                                val styledBitmap = ImageComposer.mergePhotoWithStyleIds(
                                    context = context,
                                    originalBitmap = srcBitmap,
                                    frameId = selectedFrameId,
                                    stickerId = selectedStickerId,
                                    filterId = selectedFilterId
                                )
                                // 3. Save composited styled image to cache
                                val savedUri = ImageComposer.saveBitmapToCache(context, styledBitmap)

                                // 4. Save metadata to RTDB
                                val rtdb = FirebaseDatabase.getInstance().reference
                                val styleMap = mapOf(
                                    "profilePicUrl" to savedUri.toString(),
                                    "frameId" to selectedFrameId,
                                    "stickerId" to selectedStickerId,
                                    "filterId" to selectedFilterId,
                                    "updatedAt" to System.currentTimeMillis()
                                )
                                rtdb.child("users/$currentUid/public_profile/photoStyle").setValue(styleMap)
                                rtdb.child("users/$currentUid/public_profile/profilePicUrl").setValue(savedUri.toString())

                                onApplyAndSave?.invoke(savedUri, selectedFrameId, selectedStickerId, selectedFilterId)
                                onSaveBitmap?.invoke(styledBitmap)
                            } else {
                                onApplyAndSave?.invoke(imageUri, selectedFrameId, selectedStickerId, selectedFilterId)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onApplyAndSave?.invoke(imageUri, selectedFrameId, selectedStickerId, selectedFilterId)
                        } finally {
                            isSaving = false
                            Toast.makeText(context, "Frame & Sticker Applied Live! ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DD75B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Apply & Save - Profile එකට දාන්න", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
