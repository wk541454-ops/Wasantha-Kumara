package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun generateMotivationalReply(userPostText: String): String = withContext(Dispatchers.IO) {
        val apiKey = "PLACEHOLDER_KEY" // BuildConfig.GEMINI_API_KEY disabled due to build issues
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null" || apiKey == "PLACEHOLDER_KEY") {
            return@withContext "Stay strong! You are doing amazing. Keep dreaming, learning, and building! 🌿✨"
        }

        try {
            // Build Gemini Request JSON
            val requestJson = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "The following is a user's private diary entry. Write a very brief, empathetic, encouraging, and motivational reply (max 2 sentences, 40 words, friendly tone): \"$userPostText\"")
                            })
                        })
                    })
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Keep shining! Your journey is unique and wonderful. 🌟"
                }

                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val text = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                return@withContext text.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Trust the process. Believe in yourself and keep pushing forward! 💪✨"
        }
    }
}
