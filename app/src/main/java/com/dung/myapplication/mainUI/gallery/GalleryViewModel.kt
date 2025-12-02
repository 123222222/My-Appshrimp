package com.dung.myapplication.mainUI.gallery

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dung.myapplication.models.Config
import com.dung.myapplication.models.ShrimpImage
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val imageList = mutableStateListOf<ShrimpImage>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf("")

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Use Config for backend URL
    private val BACKEND_URL = Config.BACKEND_URL

    init {
        //loadImages()
    }

    private suspend fun getFreshToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val result = Tasks.await(user.getIdToken(true))
                    val token = result.token
                    if (token != null) {
                        // Save to SharedPreferences
                        val prefs = getApplication<Application>().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putString("idToken", token).apply()
                    }
                    token
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun loadImages() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = ""

            // Check if device is bound first
            val prefs = getApplication<Application>().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
            val raspDeviceId = prefs.getString("rasp_device_id", null)

            if (raspDeviceId == null) {
                errorMessage.value = "Chưa kết nối thiết bị. Vui lòng vào trang Hồ sơ để kết nối."
                isLoading.value = false
                return@launch
            }

            // Get fresh token
            val freshToken = getFreshToken()
            if (freshToken == null) {
                errorMessage.value = "Lỗi xác thực. Vui lòng đăng nhập lại"
                isLoading.value = false
                return@launch
            }

            // Verify device is still bound with backend
            val isDeviceBound = withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("$BACKEND_URL/api/devices/my-device")
                        .get()
                        .addHeader("Authorization", freshToken)
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val jsonResponse = org.json.JSONObject(response.body?.string() ?: "{}")
                            val bound = jsonResponse.optBoolean("bound", false)
                            val deviceId = jsonResponse.optString("device_id", "")
                            bound && deviceId == raspDeviceId
                        } else {
                            false
                        }
                    }
                } catch (e: Exception) {
                    false
                }
            }

            if (!isDeviceBound) {
                errorMessage.value = "Thiết bị chưa được kết nối. Vui lòng vào trang Hồ sơ để kết nối."
                isLoading.value = false
                // Clear device info
                prefs.edit()
                    .remove("rasp_ip")
                    .remove("rasp_device_id")
                    .apply()
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("$BACKEND_URL/api/shrimp-images")
                        .get()
                        .addHeader("User-Agent", "Android-Camera-App")
                        .addHeader("Authorization", freshToken)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                errorMessage.value = "Server error: ${response.code}"
                                isLoading.value = false
                            }
                            return@withContext
                        }

                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val images = json.decodeFromString<List<ShrimpImage>>(responseBody)
                            withContext(Dispatchers.Main) {
                                imageList.clear()
                                imageList.addAll(images)
                                isLoading.value = false
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage.value = "Error: ${e.message}"
                        isLoading.value = false
                    }
                }
            }
        }
    }

    fun deleteImage(imageId: String) {
        viewModelScope.launch {
            // Get fresh token
            val freshToken = getFreshToken()
            if (freshToken == null) {
                errorMessage.value = "Lỗi xác thực. Vui lòng đăng nhập lại"
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("$BACKEND_URL/api/shrimp-images/$imageId")
                        .delete()
                        .addHeader("User-Agent", "Android-Camera-App")
                        .addHeader("Authorization", freshToken)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                imageList.removeAll { it.id == imageId }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage.value = "Delete failed: ${e.message}"
                    }
                }
            }
        }
    }
}
