package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {

    suspend fun downloadMediaToGallery(
        context: Context,
        mediaUrl: String,
        isVideo: Boolean = false,
        onStatus: (String) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            onStatus("Downloading...")
            Toast.makeText(context, "Downloading media to Gallery... ⬇️", Toast.LENGTH_SHORT).show()
        }

        try {
            if (mediaUrl.isBlank()) {
                withContext(Dispatchers.Main) {
                    onStatus("Download failed")
                    Toast.makeText(context, "Invalid media URL.", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val filename = "FriendHub_${System.currentTimeMillis()}" + if (isVideo) ".mp4" else ".jpg"
            val mimeType = if (isVideo) "video/mp4" else "image/jpeg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        if (isVideo) Environment.DIRECTORY_MOVIES + "/FriendHub" else Environment.DIRECTORY_PICTURES + "/FriendHub"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }

                val resolver = context.contentResolver
                val itemUri = resolver.insert(collection, contentValues)

                if (itemUri != null) {
                    val url = URL(mediaUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    resolver.openOutputStream(itemUri)?.use { outputStream ->
                        connection.inputStream.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)

                    withContext(Dispatchers.Main) {
                        onStatus("Download complete")
                        Toast.makeText(context, "Download complete! Saved to Gallery ✓ 🖼️", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onStatus("Download failed")
                        Toast.makeText(context, "Failed to access MediaStore.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Fallback for older Android versions
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(
                        if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
                    ),
                    "FriendHub"
                )
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, filename)
                val url = URL(mediaUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                FileOutputStream(file).use { outputStream ->
                    connection.inputStream.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                withContext(Dispatchers.Main) {
                    onStatus("Download complete")
                    Toast.makeText(context, "Download complete! Saved to ${file.absolutePath} ✓", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onStatus("Download failed")
                Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
