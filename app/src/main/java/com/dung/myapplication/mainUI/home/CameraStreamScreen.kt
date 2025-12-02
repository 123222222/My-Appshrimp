package com.dung.myapplication.mainUI.home

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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.utils.ShrimpApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraStreamScreen(
    streamUrl: String = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev/blynk_feed",
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var currentFrame by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var detectedImageUrl by remember { mutableStateOf<String?>(null) }
    var detectionCount by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val apiService = remember { ShrimpApiService(context) }

    LaunchedEffect(streamUrl) {
        try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(streamUrl)
                    .addHeader("User-Agent", "Android-Camera-App")
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Server error: ${'$'}{response.code}"
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

                        // Đọc MJPEG stream
                        val buffer = ByteArray(1024 * 1024) // 1MB buffer
                        var frameStart = -1
                        var bytesRead = 0

                        withContext(Dispatchers.Main) {
                            isLoading = false
                        }

                        while (isActive) {
                            val read = try { inputStream.read(buffer, bytesRead, buffer.size - bytesRead) } catch (e: Exception) { -1 }
                            if (read == -1) break

                            bytesRead += read

                            // Tìm JPEG start marker (0xFFD8)
                            if (frameStart == -1) {
                                for (i in 0 until bytesRead - 1) {
                                    if (buffer[i] == 0xFF.toByte() && buffer[i + 1] == 0xD8.toByte()) {
                                        frameStart = i
                                        break
                                    }
                                }
                            }

                            // Tìm JPEG end marker (0xFFD9)
                            if (frameStart != -1) {
                                for (i in frameStart + 2 until bytesRead - 1) {
                                    if (buffer[i] == 0xFF.toByte() && buffer[i + 1] == 0xD9.toByte()) {
                                        val frameLength = i - frameStart + 2
                                        val frameData = buffer.copyOfRange(frameStart, frameStart + frameLength)

                                        // Decode bitmap
                                        val bitmap = try { BitmapFactory.decodeByteArray(frameData, 0, frameLength) } catch (e: Exception) { null }
                                        if (bitmap != null) {
                                            withContext(Dispatchers.Main) {
                                                currentFrame = bitmap
                                            }
                                        }

                                        // Reset buffer
                                        System.arraycopy(buffer, frameStart + frameLength, buffer, 0, bytesRead - frameStart - frameLength)
                                        bytesRead -= (frameStart + frameLength)
                                        frameStart = -1
                                        break
                                    }
                                }
                            }

                            // Reset nếu buffer đầy
                            if (bytesRead >= buffer.size - 1024) {
                                bytesRead = 0
                                frameStart = -1
                            }

                            delay(10) // Giảm CPU usage
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Connection error: ${'$'}{e.message}"
                        isLoading = false
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi ngoài: ${'$'}{e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            MyTopBar(
                title = "Camera Stream",
                onBack = onBackClick
            )
        },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
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
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage.isNotEmpty() -> {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                detectedImageUrl != null -> {
                    // Hiển thị ảnh đã detect với bounding boxes
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = detectedImageUrl,
                            contentDescription = "Detection Result",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Badge hiển thị số tôm phát hiện
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
                    Text("Waiting for stream...")
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
                                val result = apiService.processImage(bitmap, streamUrl)
                                result.onSuccess { response ->
                                    // Lưu URL ảnh đã detect và số lượng tôm
                                    detectedImageUrl = response.cloudinaryUrl
                                    detectionCount = response.detections.size
                                    processingMessage = "Hoàn tất!"

                                    // Tắt processing indicator
                                    delay(1000)
                                    isProcessing = false
                                    processingMessage = ""

                                    // Hiển thị ảnh kết quả trong 5 giây
                                    delay(5000)

                                    // Quay lại stream
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
