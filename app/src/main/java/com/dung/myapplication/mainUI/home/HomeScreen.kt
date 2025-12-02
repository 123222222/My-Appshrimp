package com.dung.myapplication.mainUI.home

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.models.Config
import com.dung.myapplication.utils.ShrimpApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHomeClick: () -> Unit = {},
    onChartClick: () -> Unit = {},
    onControlClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    var raspIp by remember { mutableStateOf(prefs.getString("rasp_ip", null)) }
    var raspDeviceId by remember { mutableStateOf(prefs.getString("rasp_device_id", null)) }
    val scope = rememberCoroutineScope()

    var currentFrame by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var detectedImageUrl by remember { mutableStateOf<String?>(null) }
    var detectionCount by remember { mutableStateOf(0) }

    val apiService = remember { ShrimpApiService(context) }

    // Use Config URL for remote access (không cần cùng mạng nữa)
    val streamUrl = Config.STREAM_URL

    android.util.Log.d("HomeScreen", "DeviceID: $raspDeviceId")
    android.util.Log.d("HomeScreen", "StreamURL: $streamUrl")

    // Check backend for device binding if local data is missing
    LaunchedEffect(Unit) {
        if (raspDeviceId == null || raspIp == null) {
            android.util.Log.d("HomeScreen", "No local device data, checking backend...")

            val freshToken = withContext(Dispatchers.IO) {
                try {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val result = com.google.android.gms.tasks.Tasks.await(user.getIdToken(true))
                        result.token
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            if (freshToken != null) {
                withContext(Dispatchers.IO) {
                    try {
                        val client = okhttp3.OkHttpClient()
                        val request = okhttp3.Request.Builder()
                            .url("${Config.BACKEND_URL}/api/devices/my-device")
                            .get()
                            .addHeader("Authorization", freshToken)
                            .build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val jsonResponse = org.json.JSONObject(response.body?.string() ?: "{}")
                            val bound = jsonResponse.optBoolean("bound", false)

                            android.util.Log.d("HomeScreen", "Backend check: bound=$bound")

                            if (bound) {
                                val deviceId = jsonResponse.optString("device_id")
                                val deviceIp = jsonResponse.optString("device_ip", null)

                                android.util.Log.d("HomeScreen", "Backend returned: deviceId=$deviceId, ip=$deviceIp")

                                // CHỈ cho phép nếu có CẢ device_id VÀ IP
                                // Backend endpoint /my-device CHỈ trả về device của CHÍNH user này
                                // Nên nếu bound=true thì user này là owner
                                if (deviceId != null && deviceIp != null) {
                                    // Save to SharedPreferences
                                    prefs.edit()
                                        .putString("rasp_ip", deviceIp)
                                        .putString("rasp_device_id", deviceId)
                                        .apply()

                                    // Update states
                                    withContext(Dispatchers.Main) {
                                        raspIp = deviceIp
                                        raspDeviceId = deviceId
                                        android.util.Log.d("HomeScreen", "Updated device info from backend - User is device owner")
                                    }
                                } else {
                                    android.util.Log.d("HomeScreen", "Device bound but missing IP")
                                }
                            } else {
                                android.util.Log.d("HomeScreen", "User has no device bound - cannot access camera")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error checking backend", e)
                    }
                }
            }
        } else {
            android.util.Log.d("HomeScreen", "Local device data exists: deviceId=$raspDeviceId, ip=$raspIp")
        }
    }

    // Auto load camera stream if device is bound
    LaunchedEffect(raspDeviceId, raspIp) {
        if (raspDeviceId != null && raspIp != null) {
            isLoading = true
            errorMessage = ""

            android.util.Log.d("HomeScreen", "Starting camera stream...")
            try {
                // Get fresh token for authentication
                val freshToken = withContext(Dispatchers.IO) {
                    try {
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val result = com.google.android.gms.tasks.Tasks.await(user.getIdToken(true))
                            result.token
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                if (freshToken == null) {
                    errorMessage = "Lỗi xác thực. Vui lòng đăng nhập lại"
                    isLoading = false
                    return@LaunchedEffect
                }

                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build()

                    val request = Request.Builder()
                        .url(streamUrl)
                        .addHeader("User-Agent", "Android-Camera-App")
                        .addHeader("Authorization", freshToken)  // Add Firebase token
                        .build()

                    android.util.Log.d("HomeScreen", "Connecting to: $streamUrl with auth token")
                    try {
                        client.newCall(request).execute().use { response ->
                            android.util.Log.d("HomeScreen", "Response code: ${response.code}")
                            if (!response.isSuccessful) {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Server error: ${response.code}"
                                    isLoading = false
                                }
                                return@withContext
                            }

                            val inputStream = response.body?.byteStream()
                            if (inputStream == null) {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "No stream data"
                                    isLoading = false
                                }
                                return@withContext
                            }

                            // Read MJPEG stream
                            val buffer = ByteArray(1024 * 1024) // 1MB buffer
                            var frameStart = -1
                            var bytesRead = 0

                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }

                            while (isActive) {
                                val read = try {
                                    inputStream.read(buffer, bytesRead, buffer.size - bytesRead)
                                } catch (_: Exception) {
                                    -1
                                }
                                if (read == -1) break

                                bytesRead += read

                                // Find JPEG start marker (0xFFD8)
                                if (frameStart == -1) {
                                    for (i in 0 until bytesRead - 1) {
                                        if (buffer[i] == 0xFF.toByte() && buffer[i + 1] == 0xD8.toByte()) {
                                            frameStart = i
                                            break
                                        }
                                    }
                                }

                                // Find JPEG end marker (0xFFD9)
                                if (frameStart != -1) {
                                    for (i in frameStart + 2 until bytesRead - 1) {
                                        if (buffer[i] == 0xFF.toByte() && buffer[i + 1] == 0xD9.toByte()) {
                                            val frameLength = i - frameStart + 2
                                            val frameData =
                                                buffer.copyOfRange(frameStart, frameStart + frameLength)

                                            // Decode bitmap
                                            val bitmap = try {
                                                BitmapFactory.decodeByteArray(frameData, 0, frameLength)
                                            } catch (_: Exception) {
                                                null
                                            }
                                            if (bitmap != null) {
                                                withContext(Dispatchers.Main) {
                                                    currentFrame = bitmap
                                                }
                                            }

                                            // Reset buffer
                                            System.arraycopy(
                                                buffer,
                                                frameStart + frameLength,
                                                buffer,
                                                0,
                                                bytesRead - frameStart - frameLength
                                            )
                                            bytesRead -= (frameStart + frameLength)
                                            frameStart = -1
                                            break
                                        }
                                    }
                                }

                                // Reset if buffer is full
                                if (bytesRead >= buffer.size - 1024) {
                                    bytesRead = 0
                                    frameStart = -1
                                }

                                delay(10) // Reduce CPU usage
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Connection error", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = "Connection error: ${e.message}"
                            isLoading = false
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Outer error", e)
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        } else {
            android.util.Log.d("HomeScreen", "No device bound, skipping camera stream")
        }
    }

    Scaffold(
        topBar = { MyTopBar("Camera Stream") },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onChartClick = onChartClick,
                onControlClick = onControlClick,
                onGalleryClick = onGalleryClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                raspDeviceId == null -> {
                    // Not bound to any device
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Chưa kết nối thiết bị",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vui lòng vào trang Hồ sơ để quét và kết nối thiết bị Raspberry Pi",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Button(onClick = onProfileClick) {
                            Text("Đến trang Hồ sơ")
                        }
                    }
                }
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage.isNotEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            // Retry connection
                            errorMessage = ""
                            isLoading = true
                            scope.launch {
                                delay(100)
                                isLoading = false
                            }
                        }) {
                            Text("Thử lại")
                        }
                    }
                }
                detectedImageUrl != null -> {
                    // Show detected image with bounding boxes
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = detectedImageUrl,
                            contentDescription = "Detection Result",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Badge showing detection count
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "🦐 Phát hiện $detectionCount tôm",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                currentFrame != null -> {
                    Image(
                        bitmap = currentFrame!!.asImageBitmap(),
                        contentDescription = "Camera Stream",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    Text("Đang chờ camera stream...")
                }
            }

            // Capture button
            if (currentFrame != null && !isProcessing && detectedImageUrl == null) {
                FloatingActionButton(
                    onClick = {
                        currentFrame?.let { bitmap ->
                            isProcessing = true
                            processingMessage = "Đang xử lý ảnh..."

                            scope.launch {
                                val result = apiService.processImage(bitmap, streamUrl ?: "")
                                result.onSuccess { response ->
                                    // Save detected image URL and detection count
                                    detectedImageUrl = response.cloudinaryUrl
                                    detectionCount = response.detections.size
                                    processingMessage = "Hoàn tất!"

                                    // Hide processing indicator
                                    delay(1000)
                                    isProcessing = false
                                    processingMessage = ""

                                    // Show result for 5 seconds
                                    delay(5000)

                                    // Back to stream
                                    detectedImageUrl = null
                                    detectionCount = 0
                                }.onFailure { error ->
                                    processingMessage = "Lỗi: ${error.message}"
                                    delay(3000)
                                    isProcessing = false
                                    processingMessage = ""
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color.White
                    )
                }
            }

            // Processing indicator
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = processingMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
