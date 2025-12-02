# 🔧 Fix Log - Chart & Camera Stream Issues

## 📅 Date: December 1, 2025

## ❌ Vấn đề đã phát hiện:

### 1. **Chart Screen không gửi Firebase Token**
**Log lỗi:**
```
INFO:__main__:[AUTH] Received id_token: None
WARNING:__main__:[AUTH] Missing Google ID token
401 UNAUTHORIZED
```

**Nguyên nhân:** 
- `ChartViewModel` không gửi Authorization header
- Token bị expire nhưng không refresh

### 2. **Home Screen không hiển thị camera stream**
**Triệu chứng:**
- Đã kết nối được với Raspberry Pi
- Home screen trống, không hiển thị camera
- Không có error message

**Nguyên nhân:**
- Dùng IP local `http://{rasp_ip}:8000` không accessible
- Logic check `raspIp == null || raspDeviceId == null` quá strict
- Thiếu logging để debug

---

## ✅ Giải pháp đã áp dụng:

### 1. **Fix ChartViewModel.kt**

#### Thay đổi:
```kotlin
// ❌ Trước (extends ViewModel):
class ChartViewModel @Inject constructor(): ViewModel()

// ✅ Sau (extends AndroidViewModel):
class ChartViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application)
```

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
                    val prefs = getApplication<Application>()
                        .getSharedPreferences("auth", Context.MODE_PRIVATE)
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

#### Update `loadImages()`:
```kotlin
fun loadImages() {
    viewModelScope.launch {
        isLoading.value = true
        errorMessage.value = ""

        // ✅ Get fresh token
        val freshToken = getFreshToken()
        if (freshToken == null) {
            errorMessage.value = "Lỗi xác thực. Vui lòng đăng nhập lại"
            isLoading.value = false
            return@launch
        }

        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BACKEND_URL/api/shrimp-images")
                    .get()
                    .addHeader("User-Agent", "Android-Camera-App")
                    .addHeader("Authorization", freshToken)  // ✅ Add token
                    .build()
                // ...
            }
        }
    }
}
```

#### Imports mới:
```kotlin
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
```

---

### 2. **Fix GalleryViewModel.kt**

#### Thay đổi tương tự ChartViewModel:
- Extends `AndroidViewModel` thay vì `ViewModel`
- Thêm `getFreshToken()`
- Update `loadImages()` không cần context parameter
- Update `deleteImage()` dùng fresh token

#### Fix duplicate variable:
```kotlin
// ❌ Trước:
val request = Request.Builder()...
val request = requestBuilder.build()  // Error!

// ✅ Sau:
val request = Request.Builder()
    .url(...)
    .delete()
    .addHeader("Authorization", freshToken)
    .build()  // Một biến duy nhất
```

---

### 3. **Fix GalleryScreen.kt**

#### Remove context parameter:
```kotlin
// ❌ Trước:
LaunchedEffect(Unit) {
    viewModel.loadImages(context)
}

// ✅ Sau:
LaunchedEffect(Unit) {
    viewModel.loadImages()
}
```

---

### 4. **Fix HomeScreen.kt**

#### Đổi từ Local IP sang Ngrok URL:
```kotlin
// ❌ Trước (unreliable):
val streamUrl = if (raspIp != null) "http://$raspIp:8000/blynk_feed" else null

// ✅ Sau (reliable):
val streamUrl = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev/blynk_feed"
```

#### Thêm debug logging:
```kotlin
android.util.Log.d("HomeScreen", "RaspIP: $raspIp, DeviceID: $raspDeviceId")
android.util.Log.d("HomeScreen", "StreamURL: $streamUrl")
android.util.Log.d("HomeScreen", "Starting camera stream...")
android.util.Log.d("HomeScreen", "Connecting to: $streamUrl")
android.util.Log.d("HomeScreen", "Response code: ${response.code}")
android.util.Log.e("HomeScreen", "Connection error", e)
```

#### Fix condition check:
```kotlin
// ❌ Trước (quá strict):
when {
    raspIp == null || raspDeviceId == null -> {
        // Show "not connected"
    }
}

// ✅ Sau (chỉ check deviceId):
when {
    raspDeviceId == null -> {
        // Show "not connected"
    }
}
```

#### Fix LaunchedEffect trigger:
```kotlin
// ❌ Trước (trigger bằng IP có thể null):
LaunchedEffect(raspIp) {
    if (raspIp != null && streamUrl != null) {
        // ...
    }
}

// ✅ Sau (trigger bằng deviceId):
LaunchedEffect(raspDeviceId) {
    if (raspDeviceId != null) {
        // ...
    }
}
```

---

### 5. **Fix ProfileScreen.kt - Add await() import**

#### Thêm import:
```kotlin
import kotlinx.coroutines.tasks.await
```

#### Giữ nguyên function getFreshToken():
```kotlin
suspend fun getFreshToken(): String? {
    return withContext(Dispatchers.IO) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val result = user.getIdToken(true).await()  // ✅ .await() now works
                val token = result.token
                // ...
            }
        }
    }
}
```

---

### 6. **Fix build.gradle.kts**

#### Thêm dependency:
```kotlin
// Coroutines support for Firebase/Play Services (provides .await())
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
```

---

## 🎯 Kết quả mong đợi:

### Chart Screen:
```
✅ Token được refresh tự động
✅ API call thành công với fresh token
✅ Backend log: [AUTH] Received id_token: eyJhb...
✅ Backend log: [AUTH] Decoded email: hodung15032003@gmail.com
✅ Response: 200 OK
✅ Dữ liệu chart hiển thị đúng
```

### Home Screen:
```
✅ Sau khi bind device, vào Home screen
✅ Camera stream tự động load từ ngrok URL
✅ Log: "Starting camera stream..."
✅ Log: "Connecting to: https://...ngrok-free.dev/blynk_feed"
✅ Log: "Response code: 200"
✅ Camera frames hiển thị realtime
✅ Nút chụp ảnh hoạt động
```

---

## 📝 Checklist Testing:

### Chart Screen:
- [ ] Vào Chart screen
- [ ] Không thấy lỗi 401 trong log
- [ ] Dữ liệu chart hiển thị
- [ ] FAB refresh hoạt động

### Gallery Screen:
- [ ] Vào Gallery screen
- [ ] Không thấy lỗi 401 trong log
- [ ] Danh sách ảnh hiển thị
- [ ] FAB refresh hoạt động

### Home Screen:
- [ ] Bind device trong Profile
- [ ] Vào Home screen
- [ ] Camera stream hiển thị ngay
- [ ] Không cần quét lại
- [ ] Nút camera hoạt động
- [ ] Xem log Logcat filter "HomeScreen"

---

## 🔍 Debug Commands:

### Xem log Android:
```bash
adb logcat | grep -E "HomeScreen|ChartViewModel|GalleryViewModel"
```

### Xem log Backend:
```bash
# Trên Raspberry Pi
tail -f nohup.out | grep -E "AUTH|BIND|devices"
```

### Test ngrok URL:
```bash
curl -I https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev/blynk_feed
# Should return: 200 OK
```

---

## 📚 Files Changed:

1. ✅ `app/src/main/java/com/dung/myapplication/mainUI/chart/ChartViewModel.kt`
2. ✅ `app/src/main/java/com/dung/myapplication/mainUI/gallery/GalleryViewModel.kt`
3. ✅ `app/src/main/java/com/dung/myapplication/mainUI/gallery/GalleryScreen.kt`
4. ✅ `app/src/main/java/com/dung/myapplication/mainUI/home/HomeScreen.kt`
5. ✅ `app/src/main/java/com/dung/myapplication/mainUI/profile/ProfileScreen.kt`
6. ✅ `app/build.gradle.kts`

---

## 🚀 Next Steps:

1. **Sync Gradle** để download dependency mới
2. **Build app**: `./gradlew assembleDebug`
3. **Install**: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. **Test** theo checklist trên
5. **Monitor logs** để verify

---

## 💡 Notes:

- **Ngrok URL**: Ổn định hơn local IP vì accessible từ bất kỳ đâu
- **Token auto-refresh**: Tất cả ViewModels giờ đều refresh token trước khi gọi API
- **AndroidViewModel**: Cần thiết để access Application context cho SharedPreferences
- **Debug logs**: Giúp track flow và phát hiện vấn đề nhanh hơn

---

✅ **All issues fixed and ready for testing!** 🎉

