package com.example.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.CloudStory
import com.example.data.model.PlacedSticker
import com.example.data.model.PlacedText
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryEditorScreen(
    currentUid: String,
    currentUsername: String,
    currentAvatar: String,
    sourceUri: Uri?,
    mediaType: String, // "photo" or "text"
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFilterId by remember { mutableStateOf("filter_original") }
    var selectedFrameId by remember { mutableStateOf("frame_none") }

    val placedStickers = remember { mutableStateListOf<PlacedSticker>() }
    val placedTexts = remember { mutableStateListOf<PlacedText>() }

    var selectedStickerId by remember { mutableStateOf<String?>(null) }
    var selectedTextId by remember { mutableStateOf<String?>(null) }

    // Text Dialog States
    var showTextDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var textStyleSelected by remember { mutableStateOf("Normal") }
    var textColorSelected by remember { mutableStateOf("#FFFFFF") }

    // Mention Dialog States
    var showMentionDialog by remember { mutableStateOf(false) }
    var mentionInput by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }

    // Color Matrix definition for compose preview
    val colorMatrix = remember(selectedFilterId) {
        when (selectedFilterId) {
            "filter_bright" -> ColorMatrix(floatArrayOf(
                1.2f, 0f, 0f, 0f, 10f,
                0f, 1.2f, 0f, 0f, 10f,
                0f, 0f, 1.2f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ))
            "filter_dark" -> ColorMatrix(floatArrayOf(
                0.8f, 0f, 0f, 0f, -20f,
                0f, 0.8f, 0f, 0f, -20f,
                0f, 0f, 0.9f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            ))
            "filter_bw" -> ColorMatrix().apply { setToSaturation(0f) }
            "filter_sunset" -> ColorMatrix(floatArrayOf(
                1.3f, 0f, 0f, 0f, 20f,
                0.1f, 1.1f, 0f, 0f, 10f,
                0f, 0f, 0.8f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            ))
            "filter_nature" -> ColorMatrix(floatArrayOf(
                0.9f, 0f, 0f, 0f, -10f,
                0f, 1.3f, 0f, 0f, 15f,
                0f, 0.2f, 1.1f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ))
            else -> ColorMatrix() // Original
        }
    }

    // Load original bitmap
    LaunchedEffect(sourceUri, mediaType) {
        if (mediaType == "photo" && sourceUri != null) {
            withContext(Dispatchers.IO) {
                originalBitmap = ImageComposer.getBitmapFromUri(context, sourceUri)
            }
        } else {
            // Create a blank purple-black canvas for text-only story
            val b = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(b)
            canvas.drawColor(android.graphics.Color.parseColor("#0C081A"))
            originalBitmap = b
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Story Editor", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("add_story") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showTextDialog = true }) {
                        Icon(imageVector = Icons.Default.TextFields, contentDescription = "Add Text", tint = Color.White)
                    }
                    IconButton(onClick = { showMentionDialog = true }) {
                        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = "Mention Friend", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
            )
        },
        containerColor = NeonPureBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. STORY WORKSPACE (Height: 60% of screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (originalBitmap != null) {
                    // Photo Preview
                    Image(
                        bitmap = originalBitmap!!.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(colorMatrix)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0C081A), Color(0xFF230C33))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonPurpleGlow)
                    }
                }

                // Neon Frame overlay in preview
                if (selectedFrameId != "frame_none") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .border(
                                width = 4.dp,
                                brush = Brush.linearGradient(
                                    colors = when (selectedFrameId) {
                                        "frame_purple_neon" -> listOf(Color(0xFFFF2A9D), Color(0xFF9E00FF))
                                        "frame_blue_gradient" -> listOf(Color(0xFF00D1FF), Color(0xFF2A5BD7))
                                        "frame_gold_crown" -> listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                        "frame_rainbow" -> listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta)
                                        else -> listOf(Color(0xFF800000), Color(0xFFFFD700))
                                    }
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    )
                }

                // Render Placed Stickers
                placedStickers.forEachIndexed { index, sticker ->
                    val isSelected = selectedStickerId == sticker.id
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(sticker.offsetX.roundToInt(), sticker.offsetY.roundToInt()) }
                            .pointerInput(sticker.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    placedStickers[index] = sticker.copy(
                                        offsetX = sticker.offsetX + dragAmount.x,
                                        offsetY = sticker.offsetY + dragAmount.y
                                    )
                                    selectedStickerId = sticker.id
                                    selectedTextId = null
                                }
                            }
                            .clickable {
                                selectedStickerId = sticker.id
                                selectedTextId = null
                            }
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) NeonPinkGlow else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (sticker.content == "location_pelmadulla") "📍 Pelmadulla" else sticker.content,
                            fontSize = (32 * sticker.scale).sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Render Placed Texts
                placedTexts.forEachIndexed { index, txt ->
                    val isSelected = selectedTextId == txt.id
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(txt.offsetX.roundToInt(), txt.offsetY.roundToInt()) }
                            .pointerInput(txt.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    placedTexts[index] = txt.copy(
                                        offsetX = txt.offsetX + dragAmount.x,
                                        offsetY = txt.offsetY + dragAmount.y
                                    )
                                    selectedTextId = txt.id
                                    selectedStickerId = null
                                }
                            }
                            .clickable {
                                selectedTextId = txt.id
                                selectedStickerId = null
                            }
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) NeonPinkGlow else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(6.dp)
                    ) {
                        Text(
                            text = txt.text,
                            fontSize = (24 * txt.scale).sp,
                            color = Color(android.graphics.Color.parseColor(txt.colorHex)),
                            fontWeight = if (txt.style == "Bold") FontWeight.Bold else FontWeight.Normal,
                            fontFamily = when (txt.style) {
                                "Cursive" -> FontFamily.Cursive
                                else -> FontFamily.Default
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. STYLING TOOLBARS (Filters, Frames, Stickers)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = NeonDarkSurface),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selector: Filters
                    Text("Live Filters", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ImageComposer.FILTERS_LIST) { filter ->
                            val isSelected = selectedFilterId == filter.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedFilterId = filter.id }
                                    .background(
                                        if (isSelected) NeonPurpleGlow.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.FilterBAndW, contentDescription = filter.name, tint = Color.White)
                                }
                                Text(filter.name, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    // Selector: Frames
                    Text("Neon Frame Overlays", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val storyFrames = listOf(
                        Pair("frame_none", "None ❌"),
                        Pair("frame_purple_neon", "Neon Purple 💜"),
                        Pair("frame_blue_gradient", "Cyan Sky 💙"),
                        Pair("frame_gold_crown", "Royal Gold 👑"),
                        Pair("frame_rainbow", "Pride 🌈"),
                        Pair("frame_sri_lanka", "Lanka Pride 🇱🇰")
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(storyFrames) { frame ->
                            val isSelected = selectedFrameId == frame.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) NeonPurpleGlow else Color(0xFF161224))
                                    .clickable { selectedFrameId = frame.first }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(frame.second, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Selector: Quick Stickers / Emojis / Features
                    Text("Interactive Stickers & Location Pin", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val staticStickers = listOf(
                        "❤️", "🔥", "👑", "⭐", "🌿", "✈️", "📷", "💯", "🎮", "🇱🇰", "location_pelmadulla"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(staticStickers) { stick ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1933))
                                    .clickable {
                                        val newSticker = PlacedSticker(
                                            id = "sticker_${System.currentTimeMillis()}",
                                            type = if (stick == "location_pelmadulla") "location" else "emoji",
                                            content = stick,
                                            offsetX = 0f,
                                            offsetY = 0f,
                                            scale = 1.2f
                                        )
                                        placedStickers.add(newSticker)
                                        Toast
                                            .makeText(context, "Added Sticker! Drag to move.", Toast.LENGTH_SHORT)
                                            .show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (stick == "location_pelmadulla") "📍" else stick,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }

                    // Bottom Row: Scale control & Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Trash tool if a sticker/text is selected
                        if (selectedStickerId != null || selectedTextId != null) {
                            IconButton(
                                onClick = {
                                    if (selectedStickerId != null) {
                                        placedStickers.removeAll { it.id == selectedStickerId }
                                        selectedStickerId = null
                                    } else if (selectedTextId != null) {
                                        placedTexts.removeAll { it.id == selectedTextId }
                                        selectedTextId = null
                                    }
                                },
                                modifier = Modifier.background(Color.Red.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Selected", tint = Color.Red)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }

                        if (isUploading) {
                            CircularProgressIndicator(color = NeonPinkGlow, modifier = Modifier.size(24.dp))
                        } else {
                            Button(
                                onClick = {
                                    if (originalBitmap == null) {
                                        Toast.makeText(context, "Error: Image not loaded correctly.", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    isUploading = true
                                    coroutineScope.launch {
                                        try {
                                            val storyId = "story_${System.currentTimeMillis()}"
                                            
                                            // 1. Compose full-resolution bitmap
                                            val compositeBitmap = withContext(Dispatchers.IO) {
                                                try {
                                                    ImageComposer.mergeStoryWithStyle(
                                                        context = context,
                                                        originalBitmap = originalBitmap!!,
                                                        stickers = placedStickers.toList(),
                                                        texts = placedTexts.toList(),
                                                        filterId = selectedFilterId,
                                                        selectedFrameId = selectedFrameId
                                                    )
                                                } catch (e: Exception) {
                                                    Log.e("StoryEditor", "Merge failed: ${e.message}")
                                                    null
                                                }
                                            }

                                            if (compositeBitmap == null) {
                                                isUploading = false
                                                Toast.makeText(context, "Failed to process image.", Toast.LENGTH_SHORT).show()
                                                return@launch
                                            }

                                            // 2. Upload composite bitmap
                                            AppRepository.uploadStoryBitmap(
                                                uid = currentUid,
                                                storyId = storyId,
                                                bitmap = compositeBitmap,
                                                onComplete = { mediaUrl ->
                                                    if (mediaUrl != null) {
                                                        // 3. Save Story metadata
                                                        val now = System.currentTimeMillis()
                                                        val cloudStory = CloudStory(
                                                            storyId = storyId,
                                                            ownerId = currentUid,
                                                            ownerName = currentUsername,
                                                            ownerAvatar = currentAvatar,
                                                            mediaUrl = mediaUrl,
                                                            type = "photo",
                                                            timestamp = now,
                                                            expiresAt = now + 24 * 3600000L,
                                                            textOverlay = if (placedTexts.isNotEmpty()) placedTexts.first().text else ""
                                                        )
                                                        AppRepository.saveCloudStory(cloudStory) { success ->
                                                            isUploading = false
                                                            if (success) {
                                                                Toast.makeText(context, "Story Published! ✓", Toast.LENGTH_SHORT).show()
                                                                onNavigate("home")
                                                            } else {
                                                                Toast.makeText(context, "Cloud Database error. Please try again.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    } else {
                                                        isUploading = false
                                                        Toast.makeText(context, "Upload failed. Check your internet connection.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            )
                                        } catch (e: Exception) {
                                            isUploading = false
                                            Log.e("StoryEditor", "Publishing error: ${e.message}")
                                            Toast.makeText(context, "An unexpected error occurred.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPinkGlow),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Publish", modifier = Modifier.padding(end = 8.dp))
                                Text("Publish Story", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Size Booster for selected item
                        if (selectedStickerId != null || selectedTextId != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        selectedStickerId?.let { id ->
                                            val index = placedStickers.indexOfFirst { it.id == id }
                                            if (index != -1) {
                                                val s = placedStickers[index]
                                                placedStickers[index] = s.copy(scale = (s.scale - 0.2f).coerceAtLeast(0.4f))
                                            }
                                        }
                                        selectedTextId?.let { id ->
                                            val index = placedTexts.indexOfFirst { it.id == id }
                                            if (index != -1) {
                                                val t = placedTexts[index]
                                                placedTexts[index] = t.copy(scale = (t.scale - 0.2f).coerceAtLeast(0.4f))
                                            }
                                        }
                                    },
                                    modifier = Modifier.background(Color.DarkGray, CircleShape)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Shrink", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        selectedStickerId?.let { id ->
                                            val index = placedStickers.indexOfFirst { it.id == id }
                                            if (index != -1) {
                                                val s = placedStickers[index]
                                                placedStickers[index] = s.copy(scale = s.scale + 0.2f)
                                            }
                                        }
                                        selectedTextId?.let { id ->
                                            val index = placedTexts.indexOfFirst { it.id == id }
                                            if (index != -1) {
                                                val t = placedTexts[index]
                                                placedTexts[index] = t.copy(scale = t.scale + 0.2f)
                                            }
                                        }
                                    },
                                    modifier = Modifier.background(Color.DarkGray, CircleShape)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Grow", tint = Color.White)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                }
            }
        }
    }

    // Dynamic Style Text Input Dialog
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("Add Styled Text ✍️", color = Color.White) },
            containerColor = NeonDarkSurface,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Enter text") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonPurpleGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Font Style", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val fontStyles = listOf("Normal", "Bold", "Dream", "Cursive")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fontStyles.forEach { style ->
                            val active = textStyleSelected == style
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NeonPurpleGlow else Color.DarkGray)
                                    .clickable { textStyleSelected = style }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(style, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Text("Text Color", color = NeonPinkGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val colors = listOf(
                        Pair("#FFFFFF", "White"),
                        Pair("#FF00FF", "Pink"),
                        Pair("#8B5CF6", "Purple"),
                        Pair("#00FFFF", "Cyan"),
                        Pair("#FFFF00", "Yellow"),
                        Pair("#00FF00", "Green")
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colors) { pair ->
                            val active = textColorSelected == pair.first
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(pair.first)))
                                    .border(
                                        width = if (active) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { textColorSelected = pair.first }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            // Apply Dream mode separator mapping if selected
                            val processedText = if (textStyleSelected == "Dream") {
                                textInput.split(" ").filter { it.isNotBlank() }.joinToString(" • ")
                            } else {
                                textInput
                            }

                            val newText = PlacedText(
                                id = "text_${System.currentTimeMillis()}",
                                text = processedText,
                                style = textStyleSelected,
                                colorHex = textColorSelected,
                                offsetX = 0f,
                                offsetY = 0f,
                                scale = 1.0f
                            )
                            placedTexts.add(newText)
                            textInput = ""
                        }
                        showTextDialog = false
                    }
                ) {
                    Text("Add", color = NeonPinkGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Mention tagged dialog
    if (showMentionDialog) {
        AlertDialog(
            onDismissRequest = { showMentionDialog = false },
            title = { Text("Mention Friend 👥", color = Color.White) },
            containerColor = NeonDarkSurface,
            text = {
                OutlinedTextField(
                    value = mentionInput,
                    onValueChange = { mentionInput = it },
                    label = { Text("Username handle (e.g. sarah)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonPurpleGlow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (mentionInput.isNotBlank()) {
                            val handle = if (mentionInput.startsWith("@")) mentionInput else "@$mentionInput"
                            val newSticker = PlacedSticker(
                                id = "sticker_${System.currentTimeMillis()}",
                                type = "mention",
                                content = handle,
                                offsetX = 0f,
                                offsetY = 0f,
                                scale = 1.0f
                            )
                            placedStickers.add(newSticker)
                            mentionInput = ""
                        }
                        showMentionDialog = false
                    }
                ) {
                    Text("Tag", color = NeonPinkGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMentionDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
