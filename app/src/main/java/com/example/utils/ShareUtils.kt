package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.PostItem

object ShareUtils {
    private const val PROJECT_ID = "gen-lang-client-0417982200"

    fun sharePost(ctx: Context, postId: String, caption: String) {
        val url = "https://$PROJECT_ID.web.app/post/$postId"
        val txt = "$caption\n\nShared from FriendHub 💜\n$url"
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, txt)
        }
        ctx.startActivity(Intent.createChooser(i, "Share via"))
    }

    fun sharePost(ctx: Context, post: PostItem) {
        sharePost(ctx, post.id, post.caption)
    }

    fun shareToWhatsApp(ctx: Context, caption: String, postId: String) {
        try {
            val url = "https://$PROJECT_ID.web.app/post/$postId"
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, "$caption\n$url")
            }
            ctx.startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(ctx, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyLink(ctx: Context, postId: String) {
        val url = "https://$PROJECT_ID.web.app/post/$postId"
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("FH Link", url))
        Toast.makeText(ctx, "Link copied! 🔗", Toast.LENGTH_SHORT).show()
    }

    fun shareToFacebook(ctx: Context, postId: String) {
        try {
            val url = "https://$PROJECT_ID.web.app/post/$postId"
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sharer/sharer.php?u=$url"))
            ctx.startActivity(i)
        } catch (e: Exception) {
            sharePost(ctx, postId, "")
        }
    }
}
