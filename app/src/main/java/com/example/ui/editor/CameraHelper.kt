package com.example.ui.editor

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraHelper {

    private const val PROVIDER_AUTHORITY = "com.mycompany.friendhub.fileprovider"

    /**
     * Create a temporary file in local cache directory and return its content Uri via FileProvider
     */
    fun createTempImageUri(context: Context, prefix: String, uid: String): Uri? {
        return try {
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val tempFile = File.createTempFile(
                "${prefix}_${uid}_${System.currentTimeMillis()}",
                ".jpg",
                cacheDir
            )
            FileProvider.getUriForFile(
                context,
                PROVIDER_AUTHORITY,
                tempFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createImageFile(context: Context, isCover: Boolean, uid: String): Pair<File, Uri> {
        val fileName = if(isCover) "cover_${uid}${System.currentTimeMillis()}.jpg" else "profile${uid}_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        file.createNewFile()
        val uri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, file)
        return Pair(file, uri)
    }
}
