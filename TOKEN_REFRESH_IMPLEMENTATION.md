# 🔐 Firebase Token Auto Refresh - Implementation Guide

## 🎯 Vấn đề

Firebase ID Token có thời hạn **1 giờ**. Sau khi hết hạn, mọi API call sẽ bị reject với lỗi:
```
ERROR: [AUTH] Invalid token: Token expired
401 UNAUTHORIZED
```

## ✅ Giải pháp

Đã implement **Auto Token Refresh** trong tất cả các API calls:

### 1. ProfileScreen.kt

#### Thêm function `getFreshToken()`:
```kotlin
suspend fun getFreshToken(): String? {
    return withContext(Dispatchers.IO) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val result = user.getIdToken(true).await()  // Force refresh with true
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
```

#### Sử dụng trong các API calls:

**LaunchedEffect (Check binding):**
```kotlin
LaunchedEffect(Unit) {
    isCheckingBinding = true
    val freshToken = getFreshToken()  // ✅ Get fresh token
    if (freshToken != null) {
        // Use freshToken in API call
        .addHeader("Authorization", freshToken)
    }
}
```

**bindDevice():**
```kotlin
fun bindDevice(ip: String, deviceId: String) {
    scope.launch {
        val freshToken = getFreshToken()  // ✅ Get fresh token
        if (freshToken == null) {
            Toast.makeText(context, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
            return@launch
        }
        // Use freshToken
        .addHeader("Authorization", freshToken)
    }
}
```

**unbindDevice():**
```kotlin
fun unbindDevice() {
    scope.launch {
        val freshToken = getFreshToken()  // ✅ Get fresh token
        if (freshToken == null) {
            Toast.makeText(context, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
            return@launch
        }
        // Use freshToken
        .addHeader("Authorization", freshToken)
    }
}
```

### 2. ShrimpApiService.kt

#### Thêm function `getFreshToken()`:
```kotlin
private suspend fun getFreshToken(): String? {
    return withContext(Dispatchers.IO) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val result = Tasks.await(user.getIdToken(true))
                val token = result.token
                if (token != null) {
                    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
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
```

#### Sử dụng trong `processImage()`:
```kotlin
suspend fun processImage(bitmap: Bitmap, sourceUrl: String): Result<YoloProcessResponse> {
    return withContext(Dispatchers.IO) {
        try {
            // Get fresh token
            val idToken = getFreshToken()  // ✅ Always get fresh token
            if (idToken == null) {
                return@withContext Result.failure(
                    Exception("Authentication failed. Please login again.")
                )
            }
            
            // Use idToken in API call
            .addHeader("Authorization", idToken)
        }
    }
}
```

## 🔄 Cách hoạt động

### 1. **Force Refresh**
```kotlin
user.getIdToken(true)  // true = force refresh
```
- Tham số `true` bắt Firebase refresh token ngay cả khi chưa hết hạn
- Đảm bảo luôn có token mới nhất

### 2. **Tasks.await()**
```kotlin
import com.google.android.gms.tasks.Tasks

val result = Tasks.await(user.getIdToken(true))
```
- Convert Firebase Task thành coroutine
- Cho phép sử dụng suspend function

### 3. **Save to SharedPreferences**
```kotlin
prefs.edit().putString("idToken", token).apply()
```
- Lưu token mới vào local storage
- Các phần khác của app cũng có thể dùng

## 📊 Flow hoàn chỉnh

```
User mở app
    ↓
Gọi API (bind/unbind/detect)
    ↓
getFreshToken() được gọi
    ↓
Check FirebaseAuth.currentUser != null?
    ├── Yes → getIdToken(true) [Force refresh]
    │         ↓
    │     Nhận token mới từ Firebase
    │         ↓
    │     Save to SharedPreferences
    │         ↓
    │     Return token
    │         ↓
    │     Use token in API call
    │         ↓
    │     Backend verify token ✅
    │         ↓
    │     API success
    │
    └── No → Return null
             ↓
         Show "Vui lòng đăng nhập lại"
```

## ✅ Lợi ích

1. **Tự động**: Không cần user làm gì cả
2. **Transparent**: User không biết token đã được refresh
3. **Reliable**: Luôn có token hợp lệ khi gọi API
4. **Error handling**: Thông báo rõ ràng nếu không refresh được

## 🧪 Testing

### Test token refresh:
1. Đăng nhập vào app
2. Đợi > 1 giờ (hoặc xóa token trong SharedPreferences)
3. Thử bind device hoặc chụp ảnh
4. Token sẽ tự động refresh → API thành công

### Test error handling:
1. Đăng xuất khỏi Firebase
2. Thử bind device
3. Sẽ thấy toast: "Lỗi xác thực. Vui lòng đăng nhập lại"

## 📝 Checklist

- [x] ProfileScreen.kt - LaunchedEffect refresh token
- [x] ProfileScreen.kt - bindDevice() refresh token
- [x] ProfileScreen.kt - unbindDevice() refresh token
- [x] ShrimpApiService.kt - processImage() refresh token
- [x] Import Tasks.await()
- [x] Error handling với null token
- [x] Save refreshed token to SharedPreferences

## 🎯 Kết quả

**Trước khi fix:**
```
08:13:26 POST /api/devices/bind
ERROR: [AUTH] Invalid token: Token expired
401 UNAUTHORIZED
```

**Sau khi fix:**
```
08:15:30 POST /api/devices/bind
INFO: [AUTH] Received fresh token
INFO: [AUTH] Decoded email: hodung15032003@gmail.com
INFO: [BIND] Device raspberrypi-001 bound to hodung15032003@gmail.com
200 OK
```

✅ **Token luôn fresh, API luôn thành công!** 🎉

