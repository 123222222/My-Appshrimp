package com.dung.myapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.dung.myapplication.models.Config
import com.dung.myapplication.models.YoloProcessResponse
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ShrimpApiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Use Config for backend URL
    private val BACKEND_URL = Config.BACKEND_URL

    private suspend fun getAuthHeaders(): Map<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = com.dung.myapplication.utils.UserSession.getCurrentUser(context)
                val loginType = com.dung.myapplication.utils.UserSession.getLoginType(context)

                if (loginType == com.dung.myapplication.utils.UserSession.LoginType.PHONE && currentUser?.phone != null) {
                    // Phone authentication
                    mapOf("X-Phone-Auth" to currentUser.phone)
                } else {
                    // Google authentication
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val result = Tasks.await(user.getIdToken(true))
                        val token = result.token
                        if (token != null) {
                            // Save to SharedPreferences
                            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                            prefs.edit().putString("idToken", token).apply()
                            mapOf("Authorization" to token)
                        } else {
                            emptyMap()
                        }
                    } else {
                        emptyMap()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }

    suspend fun processImage(bitmap: Bitmap, sourceUrl: String): Result<YoloProcessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get auth headers
                val authHeaders = getAuthHeaders()
                if (authHeaders.isEmpty()) {
                    return@withContext Result.failure(
                        Exception("Authentication failed. Please login again.")
                    )
                }

                // Convert bitmap to Base64
                val base64Image = bitmapToBase64(bitmap)

                // Create JSON request
                val jsonBody = """
                    {
                        "image": "$base64Image",
                        "source": "$sourceUrl"
                    }
                """.trimIndent()

                val requestBuilder = Request.Builder()
                    .url("$BACKEND_URL/api/detect-shrimp")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .addHeader("User-Agent", "Android-Camera-App")

                authHeaders.forEach { (key, value) ->
                    requestBuilder.addHeader(key, value)
                }

                val request = requestBuilder.build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Server error: ${response.code} - ${response.message}")
                        )
                    }

                    val responseBody = response.body?.string()
                        ?: return@withContext Result.failure(Exception("Empty response"))

                    val result = json.decodeFromString<YoloProcessResponse>(responseBody)
                    Result.success(result)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
