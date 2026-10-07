package com.example.ui.editor

import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import com.example.data.model.PlacedSticker
import com.example.data.model.PlacedText

data class FilterItem(
    val id: String,
    val name: String,
    val description: String = ""
)

object ImageComposer {

    val FILTERS_LIST = listOf(
        FilterItem("filter_original", "Original", "Default photo colors"),
        FilterItem("filter_bright", "Bright", "High contrast and clarity"),
        FilterItem("filter_dark", "Dark Vibe", "Deep moody neon shadows"),
        FilterItem("filter_bw", "B & W", "Monochrome black and white"),
        FilterItem("filter_sunset", "Sunset Warm", "Warm golden-hour amber glow"),
        FilterItem("filter_nature", "Nature Boost", "Vibrant green & turquoise boost")
    )

    fun applyFilterToBitmap(original: Bitmap, filterId: String): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (filterId) {
            "filter_bright" -> {
                val cm = ColorMatrix(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 10f,
                    0f, 1.2f, 0f, 0f, 10f,
                    0f, 0f, 1.2f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "filter_dark" -> {
                val cm = ColorMatrix(floatArrayOf(
                    0.8f, 0f, 0f, 0f, -20f,
                    0f, 0.8f, 0f, 0f, -20f,
                    0f, 0f, 0.9f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "filter_bw" -> {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "filter_sunset" -> {
                val cm = ColorMatrix(floatArrayOf(
                    1.3f, 0f, 0f, 0f, 20f,
                    0.1f, 1.1f, 0f, 0f, 10f,
                    0f, 0f, 0.8f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "filter_nature" -> {
                val cm = ColorMatrix(floatArrayOf(
                    0.9f, 0f, 0f, 0f, -10f,
                    0f, 1.3f, 0f, 0f, 15f,
                    0f, 0.2f, 1.1f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    /**
     * Composites original photo with filter, circular crop, frame gradient, and sticker emoji badge
     */
    fun mergePhotoWithStyleIds(
        context: Context,
        originalBitmap: Bitmap,
        frameId: String,
        stickerId: String,
        filterId: String
    ): Bitmap {
        // 1. Scale/crop original image to a square bitmap (512x512 for optimal performance & quality)
        val size = 512
        val squareBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val squareCanvas = Canvas(squareBitmap)
        val origMin = Math.min(originalBitmap.width, originalBitmap.height)
        val rectSrc = Rect(
            (originalBitmap.width - origMin) / 2,
            (originalBitmap.height - origMin) / 2,
            (originalBitmap.width + origMin) / 2,
            (originalBitmap.height + origMin) / 2
        )
        val rectDest = Rect(0, 0, size, size)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        squareCanvas.drawBitmap(originalBitmap, rectSrc, rectDest, paint)

        // 2. Apply filter
        val filteredBitmap = applyFilterToBitmap(squareBitmap, filterId)

        // 3. Create transparent output bitmap
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 4. Draw filtered image clipped to a circle
        paint.reset()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(filteredBitmap, 0f, 0f, paint)

        // Clear transfer mode for drawing the frame and stickers on top
        paint.xfermode = null

        // 5. Draw Frame
        drawFrameOnCanvas(canvas, frameId, size.toFloat())

        // 6. Draw Sticker
        drawStickerOnCanvas(canvas, stickerId, size.toFloat())

        return result
    }

    private fun drawFrameOnCanvas(canvas: Canvas, frameId: String, size: Float) {
        if (frameId == "frame_none") return

        val strokeWidth = size * 0.05f
        val radius = (size - strokeWidth) / 2f
        val cx = size / 2f
        val cy = size / 2f

        val colorsMap = mapOf(
            "frame_purple_neon" to intArrayOf(0xFFD946EF.toInt(), 0xFF8B5CF6.toInt(), 0xFFD946EF.toInt()),
            "frame_blue_gradient" to intArrayOf(0xFF00D1FF.toInt(), 0xFF2A5BD7.toInt(), 0xFF00D1FF.toInt()),
            "frame_gold_crown" to intArrayOf(0xFFFFD700.toInt(), 0xFFFFA500.toInt(), 0xFFFFD700.toInt()),
            "frame_rainbow" to intArrayOf(0xFFFF0000.toInt(), 0xFFFF7F00.toInt(), 0xFFFFD700.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt(), 0xFF4B0082.toInt(), 0xFF9400D3.toInt(), 0xFFFF0000.toInt()),
            "frame_verified_black" to intArrayOf(0xFF000000.toInt(), 0xFF1D9BF0.toInt(), 0xFF000000.toInt()),
            "frame_cyberpunk" to intArrayOf(0xFFFF007F.toInt(), 0xFF00FFFF.toInt(), 0xFFFF007F.toInt()),
            "frame_golden_ring" to intArrayOf(0xFFFFDF00.toInt(), 0xFFD4AF37.toInt(), 0xFFFFDF00.toInt()),
            "frame_fire_rim" to intArrayOf(0xFFFF4500.toInt(), 0xFFFF8C00.toInt(), 0xFFFF0000.toInt(), 0xFFFF4500.toInt()),
            "frame_emerald_glow" to intArrayOf(0xFF00FF7F.toInt(), 0xFF10B981.toInt(), 0xFF00FF7F.toInt()),
            "frame_pink_rose" to intArrayOf(0xFFFF69B4.toInt(), 0xFFFF1493.toInt(), 0xFFFF69B4.toInt()),
            "frame_neon_cyan" to intArrayOf(0xFF00FFFF.toInt(), 0xFF0088FF.toInt(), 0xFF00FFFF.toInt()),
            "frame_sunset_orange" to intArrayOf(0xFFFF5722.toInt(), 0xFFFF9800.toInt(), 0xFFFF5722.toInt()),
            "frame_diamond_ring" to intArrayOf(0xFFE0F7FA.toInt(), 0xFF80DEEA.toInt(), 0xFF00E5FF.toInt(), 0xFFE0F7FA.toInt()),
            "frame_white_minimal" to intArrayOf(0xFFFFFFFF.toInt(), 0xFFCCCCCC.toInt(), 0xFFFFFFFF.toInt()),
            "frame_futuristic_hud" to intArrayOf(0xFF00F0FF.toInt(), 0xFF7000FF.toInt(), 0xFF00F0FF.toInt()),
            "frame_sri_lanka" to intArrayOf(0xFF800000.toInt(), 0xFFFFD700.toInt(), 0xFF008080.toInt(), 0xFFFF8C00.toInt(), 0xFF800000.toInt()),
            "frame_heart_glow" to intArrayOf(0xFFFF1493.toInt(), 0xFFFF69B4.toInt(), 0xFFFF1493.toInt()),
            "frame_star_cluster" to intArrayOf(0xFFFFD700.toInt(), 0xFF1E90FF.toInt(), 0xFFFFD700.toInt()),
            "frame_royal_purple" to intArrayOf(0xFF4B0082.toInt(), 0xFF9370DB.toInt(), 0xFF4B0082.toInt())
        )

        val colors = colorsMap[frameId] ?: intArrayOf(0xFF8B5CF6.toInt(), 0xFFD946EF.toInt(), 0xFF8B5CF6.toInt())

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
            shader = SweepGradient(cx, cy, colors, null)
        }

        // Draw outer glow shadow
        val glowPaint = Paint(paint).apply {
            this.strokeWidth = strokeWidth * 1.4f
            alpha = 100
        }
        canvas.drawCircle(cx, cy, radius, glowPaint)

        // Draw main frame ring
        canvas.drawCircle(cx, cy, radius, paint)

        // Draw specific accents
        when (frameId) {
            "frame_gold_crown" -> {
                val crownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFD700.toInt()
                    style = Paint.Style.FILL
                }
                val path = Path().apply {
                    val topY = size * 0.04f
                    val botY = size * 0.16f
                    moveTo(cx - 20f, botY)
                    lineTo(cx - 30f, topY + 10f)
                    lineTo(cx - 10f, topY + 20f)
                    lineTo(cx, topY)
                    lineTo(cx + 10f, topY + 20f)
                    lineTo(cx + 30f, topY + 10f)
                    lineTo(cx + 20f, botY)
                    close()
                }
                canvas.drawPath(path, crownPaint)
            }
            "frame_verified_black" -> {
                val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF1D9BF0.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(size * 0.82f, size * 0.82f, size * 0.12f, bluePaint)
                
                val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    this.strokeWidth = size * 0.02f
                    strokeCap = Paint.Cap.ROUND
                }
                val checkPath = Path().apply {
                    moveTo(size * 0.77f, size * 0.82f)
                    lineTo(size * 0.80f, size * 0.85f)
                    lineTo(size * 0.87f, size * 0.78f)
                }
                canvas.drawPath(checkPath, checkPaint)
            }
            "frame_sri_lanka" -> {
                val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFD700.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(size * 0.85f, size * 0.15f, size * 0.06f, goldPaint)
            }
        }
    }

    private fun drawStickerOnCanvas(canvas: Canvas, stickerId: String, size: Float) {
        if (stickerId == "sticker_none") return

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.14f
            textAlign = Paint.Align.CENTER
        }

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        when (stickerId) {
            "sticker_verified" -> {
                badgePaint.color = 0xFF1D9BF0.toInt()
                canvas.drawCircle(size * 0.82f, size * 0.82f, size * 0.12f, badgePaint)
                
                textPaint.textSize = size * 0.12f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("☑️", size * 0.82f, size * 0.82f + yOffset, textPaint)
            }
            "sticker_crown" -> {
                badgePaint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(size * 0.5f, size * 0.12f, size * 0.12f, badgePaint)
                
                textPaint.textSize = size * 0.13f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("👑", size * 0.5f, size * 0.12f + yOffset, textPaint)
            }
            "sticker_heart" -> {
                badgePaint.color = 0xFFFF1493.toInt()
                canvas.drawCircle(size * 0.82f, size * 0.82f, size * 0.12f, badgePaint)
                
                textPaint.textSize = size * 0.13f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("❤️", size * 0.82f, size * 0.82f + yOffset, textPaint)
            }
            "sticker_fire" -> {
                badgePaint.color = 0xFFFF4500.toInt()
                canvas.drawCircle(size * 0.82f, size * 0.18f, size * 0.12f, badgePaint)
                
                textPaint.textSize = size * 0.13f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("🔥", size * 0.82f, size * 0.18f + yOffset, textPaint)
            }
            "sticker_star" -> {
                badgePaint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(size * 0.18f, size * 0.18f, size * 0.12f, badgePaint)
                
                textPaint.textSize = size * 0.13f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("⭐", size * 0.18f, size * 0.18f + yOffset, textPaint)
            }
            "sticker_sl_flag" -> {
                badgePaint.color = 0xFF008080.toInt()
                canvas.drawCircle(size * 0.82f, size * 0.82f, size * 0.13f, badgePaint)
                
                textPaint.textSize = size * 0.14f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("🇱🇰", size * 0.82f, size * 0.82f + yOffset, textPaint)
            }
            "sticker_founder" -> {
                badgePaint.color = 0xFF8B5CF6.toInt()
                canvas.drawCircle(size * 0.5f, size * 0.86f, size * 0.13f, badgePaint)
                
                textPaint.textSize = size * 0.14f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("🏆", size * 0.5f, size * 0.86f + yOffset, textPaint)
            }
            "sticker_traveler" -> {
                badgePaint.color = 0xFF00D1FF.toInt()
                canvas.drawCircle(size * 0.18f, size * 0.82f, size * 0.12f, badgePaint)
                
                textPaint.textSize = size * 0.13f
                val fontMetrics = textPaint.fontMetrics
                val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
                canvas.drawText("✈️", size * 0.18f, size * 0.82f + yOffset, textPaint)
            }
        }
    }

    fun mergePhotoWithStyle(
        context: Context,
        originalBitmap: Bitmap,
        frameResId: Int? = null,
        stickerResId: Int? = null
    ): Bitmap {
        val result = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        frameResId?.let { res ->
            try {
                val frame = BitmapFactory.decodeResource(context.resources, res)
                if (frame != null) {
                    val scaledFrame = Bitmap.createScaledBitmap(frame, originalBitmap.width, originalBitmap.height, true)
                    canvas.drawBitmap(scaledFrame, 0f, 0f, null)
                }
            } catch (e: Exception) {}
        }

        stickerResId?.let { res ->
            try {
                val sticker = BitmapFactory.decodeResource(context.resources, res)
                if (sticker != null) {
                    val size = originalBitmap.width / 4
                    val scaledSticker = Bitmap.createScaledBitmap(sticker, size, size, true)
                    canvas.drawBitmap(
                        scaledSticker,
                        (originalBitmap.width - size - 20).toFloat(),
                        (originalBitmap.height - size - 20).toFloat(),
                        null
                    )
                }
            } catch (e: Exception) {}
        }
        return result
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val cacheDir = File(context.cacheDir, "profile_pics")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "styled_profile_${System.currentTimeMillis()}.jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()
        return Uri.fromFile(file)
    }

    /**
     * Composites a full 720x1280 Story Image with custom filters, frames, dragged stickers, and styled text overlays
     */
    fun mergeStoryWithStyle(
        context: Context,
        originalBitmap: Bitmap,
        stickers: List<PlacedSticker>,
        texts: List<PlacedText>,
        filterId: String,
        selectedFrameId: String = "frame_none"
    ): Bitmap {
        val width = 720
        val height = 1280
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.BLACK)

        // Fit background image center crop
        val origWidth = originalBitmap.width
        val origHeight = originalBitmap.height
        val scale = Math.max(width.toFloat() / origWidth, height.toFloat() / origHeight)
        val fillWidth = origWidth * scale
        val fillHeight = origHeight * scale
        val left = (width - fillWidth) / 2f
        val top = (height - fillHeight) / 2f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val destRect = RectF(left, top, left + fillWidth, top + fillHeight)

        // Apply filter first
        val filteredBitmap = applyFilterToBitmap(originalBitmap, filterId)
        canvas.drawBitmap(filteredBitmap, null, destRect, paint)

        // Draw Story Frame Overlay
        if (!selectedFrameId.isNullOrEmpty() && selectedFrameId != "frame_none") {
            drawStoryFrameOnCanvas(canvas, selectedFrameId, width.toFloat(), height.toFloat())
        }

        // Draw Stickers
        stickers.forEach { sticker ->
            drawPlacedStickerOnCanvas(canvas, sticker, width.toFloat(), height.toFloat())
        }

        // Draw Texts
        texts.forEach { txt ->
            drawPlacedTextOnCanvas(canvas, txt, width.toFloat(), height.toFloat())
        }

        return result
    }

    private fun drawStoryFrameOnCanvas(canvas: Canvas, frameId: String, width: Float, height: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 24f
        }
        val rect = RectF(12f, 12f, width - 12f, height - 12f)

        when (frameId) {
            "frame_purple_neon" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, height, 0xFFD946EF.toInt(), 0xFF8B5CF6.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRoundRect(rect, 40f, 40f, paint)
            }
            "frame_blue_gradient" -> {
                paint.shader = LinearGradient(0f, 0f, width, height, 0xFF00D1FF.toInt(), 0xFF2A5BD7.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRoundRect(rect, 40f, 40f, paint)
            }
            "frame_gold_crown" -> {
                paint.shader = LinearGradient(0f, 0f, width, 0f, 0xFFFFD700.toInt(), 0xFFFFA500.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRoundRect(rect, 40f, 40f, paint)
            }
            "frame_rainbow" -> {
                paint.shader = SweepGradient(width / 2f, height / 2f, intArrayOf(
                    0xFFFF0000.toInt(), 0xFFFFD700.toInt(), 0xFF00FF00.toInt(),
                    0xFF00FFFF.toInt(), 0xFF9400D3.toInt(), 0xFFFF0000.toInt()
                ), null)
                canvas.drawRoundRect(rect, 40f, 40f, paint)
            }
            "frame_sri_lanka" -> {
                paint.color = 0xFF800000.toInt()
                canvas.drawRoundRect(rect, 40f, 40f, paint)
            }
        }
    }

    private fun drawPlacedStickerOnCanvas(canvas: Canvas, sticker: PlacedSticker, screenWidth: Float, screenHeight: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 70f * sticker.scale
            textAlign = Paint.Align.CENTER
        }
        canvas.save()
        val cx = sticker.offsetX + screenWidth / 2f
        val cy = sticker.offsetY + screenHeight / 2f
        canvas.translate(cx, cy)
        canvas.rotate(sticker.rotation)

        val fontMetrics = textPaint.fontMetrics
        val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom

        val textToDraw = when (sticker.content) {
            "location_pelmadulla" -> "📍 Pelmadulla"
            else -> sticker.content
        }
        canvas.drawText(textToDraw, 0f, yOffset, textPaint)
        canvas.restore()
    }

    private fun drawPlacedTextOnCanvas(canvas: Canvas, txt: PlacedText, screenWidth: Float, screenHeight: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 50f * txt.scale
            textAlign = Paint.Align.CENTER
            color = try { Color.parseColor(txt.colorHex) } catch(e: Exception) { Color.WHITE }
            typeface = when (txt.style) {
                "Bold" -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                "Cursive" -> Typeface.create("sans-serif-condensed", Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
        }
        canvas.save()
        val cx = txt.offsetX + screenWidth / 2f
        val cy = txt.offsetY + screenHeight / 2f
        canvas.translate(cx, cy)

        val fontMetrics = textPaint.fontMetrics
        val yOffset = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
        canvas.drawText(txt.text, 0f, yOffset, textPaint)
        canvas.restore()
    }
}
