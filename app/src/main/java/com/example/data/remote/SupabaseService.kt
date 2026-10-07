package com.example.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseService {
    private val client = OkHttpClient()

    suspend fun fetchTableData(
        url: String,
        anonKey: String,
        tableName: String
    ): String? = withContext(Dispatchers.IO) {
        if (url.isBlank() || anonKey.isBlank()) return@withContext null
        try {
            val endpoint = "$url/rest/v1/$tableName?select=*"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return@withContext response.body?.string()
            } else {
                Log.e("SupabaseService", "Error ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Network exception: ${e.localizedMessage}")
        }
        return@withContext null
    }

    suspend fun insertRow(
        url: String,
        anonKey: String,
        tableName: String,
        jsonBody: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (url.isBlank() || anonKey.isBlank()) return@withContext false
        try {
            val endpoint = "$url/rest/v1/$tableName"
            val mediaType = "application/json".toMediaType()
            val body = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Insert exception: ${e.localizedMessage}")
        }
        return@withContext false
    }
}
