package com.dung.myapplication.mainUI.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.models.Config
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onHomeClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onChartClick: () -> Unit = {},
    onControlClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    users: List<com.dung.myapplication.models.User> = emptyList(),
    context: Context
) {
    var scanning by remember { mutableStateOf(false) }
    val foundDevices = remember { mutableStateListOf<Pair<String, String>>() } // Pair<IP, device_id>
    var boundDevice by remember { mutableStateOf<String?>(null) }
    var isCheckingBinding by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }
    var backendDeviceId by remember { mutableStateOf<String?>(null) } // Device bound on backend
    var isEmailPermitted by remember { mutableStateOf<Boolean?>(null) } // null = checking, true = permitted, false = not permitted
    var permissionCheckError by remember { mutableStateOf<String?>(null) }

    // Email permission management states
    val permittedEmails = remember { mutableStateListOf<String>() }
    var isLoadingEmails by remember { mutableStateOf(false) }
    var showAddEmailDialog by remember { mutableStateOf(false) }
    var newEmailInput by remember { mutableStateOf("") }
    var emailActionInProgress by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val currentEmail = firebaseUser?.email ?: ""
    val client = OkHttpClient()

    // Use Config for backend URL
    val backendUrl = Config.BACKEND_URL

    // Admin email (should match backend ADMIN_EMAIL)
    val ADMIN_EMAIL = "hodung15032003@gmail.com"

    // Function to get fresh token
    suspend fun getFreshToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val result = user.getIdToken(true).await()
                    val token = result.token
                    if (token != null) {
                        // Save to SharedPreferences
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

    // Kiểm tra admin và xem user đã bind device nào chưa
    LaunchedEffect(Unit) {
        isCheckingBinding = true

        // Check if user is admin
        isAdmin = (currentEmail == ADMIN_EMAIL)

        // First, check if email is permitted
        val freshToken = getFreshToken()
        if (freshToken != null) {
            try {
                // Check authentication status
                withContext(Dispatchers.IO) {
                    val authCheckRequest = Request.Builder()
                        .url("$backendUrl/api/auth/check")
                        .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                        .addHeader("Authorization", freshToken)
                        .build()
                    val authResponse = client.newCall(authCheckRequest).execute()
                    if (authResponse.isSuccessful) {
                        val authJson = JSONObject(authResponse.body?.string() ?: "{}")
                        val emailPermitted = authJson.optBoolean("email_permitted", false)
                        withContext(Dispatchers.Main) {
                            isEmailPermitted = emailPermitted
                        }

                        if (!emailPermitted) {
                            // Email not permitted, don't check device binding
                            withContext(Dispatchers.Main) {
                                permissionCheckError = "Tài khoản chưa được cấp quyền"
                            }
                            isCheckingBinding = false
                            return@withContext
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isEmailPermitted = false
                            permissionCheckError = "Không thể xác thực tài khoản"
                        }
                        isCheckingBinding = false
                        return@withContext
                    }
                }

                // If email is permitted, check device binding
                withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("$backendUrl/api/devices/my-device")
                        .get()
                        .addHeader("Authorization", freshToken)
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                        val bound = jsonResponse.optBoolean("bound", false)
                        if (bound) {
                            val deviceId = jsonResponse.optString("device_id")
                            val deviceIpFromBackend = jsonResponse.optString("device_ip", null)
                            backendDeviceId = deviceId // Save backend device_id

                            // Lấy IP từ SharedPreferences
                            val savedIp = prefs.getString("rasp_ip", null)
                            val savedDeviceId = prefs.getString("rasp_device_id", null)

                            // Ưu tiên IP từ backend, nếu không có thì dùng local
                            val finalIp = deviceIpFromBackend ?: savedIp

                            // Chỉ hiển thị "Đã kết nối" nếu có cả device_id VÀ IP
                            if (deviceId != null && finalIp != null) {
                                // Lưu IP vào SharedPreferences nếu có từ backend
                                if (deviceIpFromBackend != null) {
                                    prefs.edit()
                                        .putString("rasp_ip", deviceIpFromBackend)
                                        .putString("rasp_device_id", deviceId)
                                        .apply()
                                }

                                withContext(Dispatchers.Main) {
                                    boundDevice = "$deviceId ($finalIp)"
                                }
                            } else {
                                // Device đã bind trên backend nhưng không có IP
                                withContext(Dispatchers.Main) {
                                    boundDevice = null
                                }
                                // Clear invalid data
                                prefs.edit()
                                    .remove("rasp_ip")
                                    .remove("rasp_device_id")
                                    .apply()
                            }
                        } else {
                            backendDeviceId = null
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isCheckingBinding = false
    }

    // Load permitted emails if admin
    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            isLoadingEmails = true
            try {
                val freshToken = getFreshToken()
                if (freshToken != null) {
                    withContext(Dispatchers.IO) {
                        val request = Request.Builder()
                            .url("$backendUrl/api/admin/list-emails")
                            .get()
                            .addHeader("Authorization", freshToken)
                            .build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                            val emailsArray = jsonResponse.optJSONArray("emails")
                            withContext(Dispatchers.Main) {
                                permittedEmails.clear()
                                if (emailsArray != null) {
                                    for (i in 0 until emailsArray.length()) {
                                        permittedEmails.add(emailsArray.getString(i))
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoadingEmails = false
        }
    }

    fun loadPermittedEmails() {
        scope.launch {
            isLoadingEmails = true
            try {
                val freshToken = getFreshToken()
                if (freshToken == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi xác thực", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("$backendUrl/api/admin/list-emails")
                        .get()
                        .addHeader("Authorization", freshToken)
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                        val emailsArray = jsonResponse.optJSONArray("emails")
                        withContext(Dispatchers.Main) {
                            permittedEmails.clear()
                            if (emailsArray != null) {
                                for (i in 0 until emailsArray.length()) {
                                    permittedEmails.add(emailsArray.getString(i))
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Lỗi tải danh sách email", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            isLoadingEmails = false
        }
    }

    fun addPermittedEmail(email: String) {
        scope.launch {
            emailActionInProgress = true
            try {
                val freshToken = getFreshToken()
                if (freshToken == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi xác thực", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val jsonBody = JSONObject().apply {
                        put("email", email)
                    }.toString()

                    val request = Request.Builder()
                        .url("$backendUrl/api/admin/add-email")
                        .post(jsonBody.toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", freshToken)
                        .build()
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "{}"
                    val jsonResponse = JSONObject(responseBody)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Thêm email thành công", Toast.LENGTH_SHORT).show()
                            loadPermittedEmails()
                            newEmailInput = ""
                            showAddEmailDialog = false
                        } else {
                            val message = jsonResponse.optString("message", "Lỗi không xác định")
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            emailActionInProgress = false
        }
    }

    fun removePermittedEmail(email: String) {
        scope.launch {
            emailActionInProgress = true
            try {
                val freshToken = getFreshToken()
                if (freshToken == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi xác thực", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val jsonBody = JSONObject().apply {
                        put("email", email)
                    }.toString()

                    val request = Request.Builder()
                        .url("$backendUrl/api/admin/remove-email")
                        .post(jsonBody.toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", freshToken)
                        .build()
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "{}"
                    val jsonResponse = JSONObject(responseBody)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Xóa email thành công", Toast.LENGTH_SHORT).show()
                            loadPermittedEmails()
                        } else {
                            val message = jsonResponse.optString("message", "Lỗi không xác định")
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            emailActionInProgress = false
        }
    }

    fun scanDevices() {
        scope.launch {
            scanning = true
            foundDevices.clear()
            try {
                withContext(Dispatchers.IO) {
                    val socket = DatagramSocket()
                    socket.broadcast = true
                    socket.soTimeout = 2000

                    // Use Config for UDP discovery
                    val message = Config.UDP_DISCOVERY_MESSAGE.toByteArray()
                    val packet = DatagramPacket(
                        message,
                        message.size,
                        java.net.InetAddress.getByName("255.255.255.255"),
                        Config.UDP_DISCOVERY_PORT
                    )
                    socket.send(packet)
                    val buffer = ByteArray(1024)
                    val start = System.currentTimeMillis()
                    while (System.currentTimeMillis() - start < 2000) {
                        try {
                            val response = DatagramPacket(buffer, buffer.size)
                            socket.receive(response)
                            val respStr = String(response.data, 0, response.length).trim()
                            val ip = response.address.hostAddress
                            if (ip != null) {
                                withContext(Dispatchers.Main) {
                                    foundDevices.add(ip to respStr)
                                }
                            }
                        } catch (e: SocketTimeoutException) {
                            break
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    socket.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi quét: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            scanning = false
        }
    }

    fun bindDevice(ip: String, deviceId: String) {
        scope.launch {
            try {
                // Get fresh token first
                val freshToken = getFreshToken()
                if (freshToken == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    // Lưu vào SharedPreferences
                    prefs.edit()
                        .putString("rasp_ip", ip)
                        .putString("rasp_device_id", deviceId)
                        .apply()

                    // Gửi lên backend để bind (bao gồm cả IP)
                    val jsonBody = JSONObject().apply {
                        put("device_id", deviceId)
                        put("device_ip", ip)  // Gửi IP để backend lưu
                    }.toString()

                    val request = Request.Builder()
                        .url("$backendUrl/api/devices/bind")
                        .post(jsonBody.toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", freshToken)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "{}"
                    val jsonResponse = JSONObject(responseBody)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            boundDevice = "$deviceId ($ip)"
                            backendDeviceId = deviceId
                            foundDevices.clear()
                            Toast.makeText(
                                context,
                                "Kết nối thiết bị thành công!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val message = jsonResponse.optString("message", "Lỗi không xác định")

                            // Parse detailed error for device already bound
                            val errorMsg = if (message.contains("already bound to another account", ignoreCase = true)) {
                                // Extract owner email from message
                                val ownerEmail = message.substringAfter("account: ", "unknown")
                                "❌ Thiết bị này đã được kết nối bởi:\n$ownerEmail\n\nVui lòng yêu cầu $ownerEmail hủy kết nối hoặc chọn thiết bị khác."
                            } else {
                                "Lỗi: $message"
                            }

                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun unbindDevice() {
        scope.launch {
            try {
                // Get fresh token first
                val freshToken = getFreshToken()
                if (freshToken == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    // Ưu tiên dùng backendDeviceId, nếu không có thì lấy từ SharedPreferences
                    val deviceId = backendDeviceId ?: prefs.getString("rasp_device_id", null)

                    if (deviceId != null) {
                        val jsonBody = JSONObject().apply {
                            put("device_id", deviceId)
                        }.toString()

                        val request = Request.Builder()
                            .url("$backendUrl/api/devices/unbind")
                            .post(jsonBody.toRequestBody("application/json".toMediaType()))
                            .addHeader("Authorization", freshToken)
                            .build()

                        val response = client.newCall(request).execute()
                        val responseBody = response.body?.string() ?: "{}"

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                // Xóa khỏi SharedPreferences
                                prefs.edit()
                                    .remove("rasp_ip")
                                    .remove("rasp_device_id")
                                    .apply()
                                boundDevice = null
                                backendDeviceId = null
                                Toast.makeText(
                                    context,
                                    "Đã hủy kết nối thiết bị",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val jsonResponse = JSONObject(responseBody)
                                val message = jsonResponse.optString("message", "Lỗi không xác định")
                                Toast.makeText(
                                    context,
                                    "Lỗi: $message",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Không tìm thấy thông tin thiết bị",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = { MyTopBar("Hồ sơ") },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onGalleryClick = onGalleryClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick,
                onChartClick = onChartClick,
                onControlClick = onControlClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Thông tin tài khoản",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Email: $currentEmail",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Device Management Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quản lý thiết bị",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isCheckingBinding) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (isEmailPermitted == false) {
                        // Email chưa được cấp quyền
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "❌ Tài khoản chưa được cấp quyền",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Tài khoản của bạn chưa có quyền truy cập hệ thống. Vui lòng liên hệ Admin để được cấp quyền.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Email Admin: $ADMIN_EMAIL",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else if (isEmailPermitted == true && boundDevice != null) {
                        // Đã kết nối thiết bị (có cả device_id và IP)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Đã kết nối",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = boundDevice!!,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { unbindDevice() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Hủy kết nối")
                        }
                    } else if (isEmailPermitted == true && backendDeviceId != null && boundDevice == null) {
                        // Device đã bind trên backend nhưng thiếu IP local
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "⚠️ Thiết bị đã được bind nhưng thiếu thông tin kết nối",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Device ID: $backendDeviceId",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Vui lòng hủy kết nối và quét lại thiết bị để lấy địa chỉ IP mới.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Button(
                            onClick = { unbindDevice() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Hủy kết nối")
                        }
                    } else if (isEmailPermitted == true) {
                        // Chưa kết nối - CHỈ user được cấp quyền mới có thể quét và kết nối
                        Text(
                            text = "Quét mạng WiFi để tìm thiết bị Raspberry Pi",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = { scanDevices() },
                            enabled = !scanning,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (scanning) "Đang quét..." else "Quét thiết bị")
                        }

                        if (foundDevices.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                text = "Thiết bị tìm thấy:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(foundDevices) { (ip, deviceId) ->
                                    OutlinedButton(
                                        onClick = { bindDevice(ip, deviceId) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = "Device: $deviceId",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "IP: $ip",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Email Permission Management Card (Only for Admin)
            if (isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quản lý quyền truy cập",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddEmailDialog = true },
                                enabled = !emailActionInProgress
                            ) {
                                Text("+ Thêm Email")
                            }
                        }

                        if (isLoadingEmails) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else if (permittedEmails.isEmpty()) {
                            Text(
                                text = "Chưa có email nào được cấp quyền",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Email được phép truy cập:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(permittedEmails) { email ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (email == ADMIN_EMAIL)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        tonalElevation = 2.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = email,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (email == ADMIN_EMAIL)
                                                        FontWeight.Bold
                                                    else
                                                        FontWeight.Normal
                                                )
                                                if (email == ADMIN_EMAIL) {
                                                    Text(
                                                        text = "Admin",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            if (email != ADMIN_EMAIL) {
                                                IconButton(
                                                    onClick = {
                                                        removePermittedEmail(email)
                                                    },
                                                    enabled = !emailActionInProgress
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Xóa",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Email Dialog
    if (showAddEmailDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!emailActionInProgress) {
                    showAddEmailDialog = false
                    newEmailInput = ""
                }
            },
            title = { Text("Thêm Email") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Nhập email của người dùng muốn cấp quyền truy cập:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = { newEmailInput = it },
                        label = { Text("Email") },
                        placeholder = { Text("example@gmail.com") },
                        singleLine = true,
                        enabled = !emailActionInProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEmailInput.isNotBlank()) {
                            addPermittedEmail(newEmailInput.trim())
                        }
                    },
                    enabled = !emailActionInProgress && newEmailInput.isNotBlank()
                ) {
                    if (emailActionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Thêm")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddEmailDialog = false
                        newEmailInput = ""
                    },
                    enabled = !emailActionInProgress
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}
