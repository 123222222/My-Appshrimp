# FIX: DEVICE BINDING & PERMISSION ISSUES

## Ngày: 2 December 2025

## Các Vấn Đề Đã Sửa

### 1. ❌ Device đã bind với admin → User mới không thể bind

**Vấn đề:** 
- Admin đã bind device `raspberrypi-001`
- User mới thêm vào cố gắng bind cùng device
- Lỗi: "device already bound to another account: hodung15032003@gmail.com"

**Giải pháp:**
- ✅ Cải thiện thông báo lỗi để hiển thị rõ ai đang sở hữu device
- ✅ Hướng dẫn user yêu cầu owner hủy kết nối trước
- ✅ Backend đã đúng - chỉ 1 device/1 user tại 1 thời điểm

**Code thay đổi:** `ProfileScreen.kt` - line ~470
```kotlin
val errorMsg = if (message.contains("already bound to another account", ignoreCase = true)) {
    val ownerEmail = message.substringAfter("account: ", "unknown")
    "❌ Thiết bị này đã được kết nối bởi:\n$ownerEmail\n\nVui lòng yêu cầu $ownerEmail hủy kết nối hoặc chọn thiết bị khác."
} else {
    "Lỗi: $message"
}
```

---

### 2. ❌ User mới chưa bind nhưng vẫn thấy camera

**Vấn đề:**
- User mới đăng nhập (chưa bind device)
- Home screen vẫn hiển thị camera của user trước
- Nguyên nhân: SharedPreferences còn lưu device data của user cũ

**Giải pháp:**
- ✅ Clear device data khi user khác đăng nhập
- ✅ Track `last_login_email` trong SharedPreferences
- ✅ Auto-clear `rasp_ip` và `rasp_device_id` nếu email khác

**Code thay đổi:** `MainActivity.kt`
```kotlin
val currentEmail = currentUser?.email
val savedEmail = prefs.getString("last_login_email", null)

if (currentEmail != null && currentEmail != savedEmail) {
    // Different user - clear device binding data
    prefs.edit()
        .remove("rasp_ip")
        .remove("rasp_device_id")
        .putString("last_login_email", currentEmail)
        .apply()
}
```

---

### 3. ❌ User chưa được cấp phép vẫn thấy UI quét thiết bị

**Vấn đề:**
- User với email không có trong `permitted_emails.json`
- Vẫn thấy nút "Quét thiết bị" và có thể quét
- Không có thông báo rõ ràng về quyền truy cập

**Giải pháp:**
- ✅ Gọi API `/api/auth/check` để kiểm tra permission TRƯỚC khi hiển thị UI
- ✅ Hiển thị message rõ ràng nếu chưa được cấp quyền
- ✅ Ẩn toàn bộ UI device scanning cho user chưa có quyền

**Code thay đổi:** `ProfileScreen.kt`

**Thêm state:**
```kotlin
var isEmailPermitted by remember { mutableStateOf<Boolean?>(null) }
var permissionCheckError by remember { mutableStateOf<String?>(null) }
```

**Kiểm tra permission:**
```kotlin
// First, check if email is permitted
val authCheckRequest = Request.Builder()
    .url("$backendUrl/api/auth/check")
    .post(okhttp3.RequestBody.create(null, ByteArray(0)))
    .addHeader("Authorization", freshToken)
    .build()
val authResponse = client.newCall(authCheckRequest).execute()
if (authResponse.isSuccessful) {
    val authJson = JSONObject(authResponse.body?.string() ?: "{}")
    val emailPermitted = authJson.optBoolean("email_permitted", false)
    isEmailPermitted = emailPermitted
    
    if (!emailPermitted) {
        permissionCheckError = "Tài khoản chưa được cấp quyền"
        return@withContext
    }
}
```

**UI hiển thị:**
```kotlin
} else if (isEmailPermitted == false) {
    // Email chưa được cấp quyền
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "❌ Tài khoản chưa được cấp quyền",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Tài khoản của bạn chưa có quyền truy cập hệ thống. Vui lòng liên hệ Admin để được cấp quyền.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Email Admin: $ADMIN_EMAIL",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

## Luồng Hoạt Động Mới

### User Được Cấp Quyền - Lần Đầu Đăng Nhập

```
1. Đăng nhập với email đã được admin add
2. MainActivity check → Email khác → Clear device data
3. ProfileScreen → Check /api/auth/check
   ✅ email_permitted = true
4. ProfileScreen → Check /api/devices/my-device
   → bound = false (chưa bind)
5. Hiển thị UI "Quét thiết bị"
6. User quét → Tìm thấy device
7. User click Connect → Gọi /api/devices/bind
   ✅ Thành công → Lưu device data
8. Home screen → Hiển thị camera
```

### User Được Cấp Quyền - Đăng Nhập Lần 2

```
1. Đăng nhập với cùng email
2. MainActivity check → Email giống → Không clear data
3. ProfileScreen → Check permission → ✅ Permitted
4. ProfileScreen → Check device binding
   → bound = true, có device_id và IP
5. Hiển thị "Đã kết nối: raspberrypi-001"
6. Home screen → Auto load camera từ saved data
```

### User CHƯA Được Cấp Quyền

```
1. Đăng nhập với email chưa được add
2. MainActivity → Clear device data (nếu user khác)
3. ProfileScreen → Check /api/auth/check
   ❌ email_permitted = false
4. Hiển thị message:
   "❌ Tài khoản chưa được cấp quyền
    Vui lòng liên hệ Admin: hodung15032003@gmail.com"
5. KHÔNG hiển thị nút "Quét thiết bị"
6. Home screen → Không có device data → Hiển thị "Chưa kết nối"
```

### User Thử Bind Device Đã Có Owner

```
1. User A đã bind raspberrypi-001
2. User B (đã được cấp quyền) đăng nhập
3. User B quét và tìm thấy raspberrypi-001
4. User B click Connect
5. Backend check → Device đã bind với User A
6. Trả về: "Device already bound to another account: userA@gmail.com"
7. App hiển thị:
   "❌ Thiết bị này đã được kết nối bởi:
    userA@gmail.com
    
    Vui lòng yêu cầu userA@gmail.com hủy kết nối
    hoặc chọn thiết bị khác."
```

---

## Files Đã Thay Đổi

### 1. `MainActivity.kt`
- ✅ Thêm logic clear device data khi user khác đăng nhập
- ✅ Track last_login_email để phát hiện user change

### 2. `ProfileScreen.kt`
- ✅ Thêm state `isEmailPermitted` và `permissionCheckError`
- ✅ Gọi `/api/auth/check` trước khi check device binding
- ✅ Hiển thị UI khác nhau cho permitted vs non-permitted users
- ✅ Cải thiện error message khi device đã được bind

### 3. `backend/app_complete.py`
- ✅ Đã có sẵn endpoint `/api/auth/check` (debug endpoint)
- ✅ Backend logic đúng - không cần sửa

---

## Testing Checklist

### Test Case 1: User Chưa Được Cấp Quyền
- [ ] Đăng nhập với email không có trong permitted_emails.json
- [ ] Vào Profile screen
- [ ] Thấy message "❌ Tài khoản chưa được cấp quyền"
- [ ] KHÔNG thấy nút "Quét thiết bị"
- [ ] Vào Home screen → Hiển thị "Chưa kết nối"

### Test Case 2: User Được Cấp Quyền - Chưa Bind
- [ ] Admin add email vào permitted list
- [ ] User đăng nhập
- [ ] Vào Profile screen
- [ ] Thấy nút "Quét thiết bị"
- [ ] Quét và tìm thấy device
- [ ] Click Connect → Thành công
- [ ] Vào Home → Thấy camera

### Test Case 3: Device Đã Có Owner
- [ ] User A đã bind device
- [ ] User B (permitted) đăng nhập
- [ ] User B quét và thấy device
- [ ] User B click Connect
- [ ] Thấy message: "Thiết bị đã được kết nối bởi: userA@gmail.com"
- [ ] Message hướng dẫn liên hệ owner

### Test Case 4: User Change
- [ ] User A đăng nhập và bind device
- [ ] Đăng xuất
- [ ] User B đăng nhập
- [ ] Profile → KHÔNG thấy device của User A
- [ ] Home → KHÔNG thấy camera của User A
- [ ] SharedPreferences đã được clear

### Test Case 5: Same User Re-login
- [ ] User A đăng nhập và bind device
- [ ] Đăng xuất
- [ ] User A đăng nhập lại
- [ ] Profile → Vẫn thấy "Đã kết nối"
- [ ] Home → Auto load camera
- [ ] Device data được preserve

---

## Backend API Flow

### 1. Check Permission
```
POST /api/auth/check
Headers: Authorization: <FIREBASE_TOKEN>

Response (Permitted):
{
  "token_valid": true,
  "decoded_email": "user@example.com",
  "email_permitted": true,
  "is_admin": false,
  "has_device_bound": false
}

Response (Not Permitted):
{
  "token_valid": true,
  "decoded_email": "user@example.com",
  "email_permitted": false
}
```

### 2. Check Device Binding
```
GET /api/devices/my-device
Headers: Authorization: <FIREBASE_TOKEN>

Response (Has Device):
{
  "success": true,
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100",
  "bound": true
}

Response (No Device):
{
  "success": true,
  "device_id": null,
  "device_ip": null,
  "bound": false
}
```

### 3. Bind Device
```
POST /api/devices/bind
Headers: Authorization: <FIREBASE_TOKEN>
Body: {
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}

Response (Success):
{
  "success": true,
  "message": "Device bound successfully"
}

Response (Already Bound):
{
  "success": false,
  "message": "Device already bound to another account: owner@example.com"
}
```

---

## Security Notes

1. ✅ **Email Permission Check**: Endpoint `/api/auth/check` không cần authentication để debug, nhưng chỉ trả về info nếu có valid token
2. ✅ **Device Ownership**: Backend enforce 1 device = 1 user, không thể chiếm device của người khác
3. ✅ **Data Isolation**: User chỉ thấy device của chính mình qua `/api/devices/my-device`
4. ✅ **Auto Clear**: Device data tự động clear khi user khác đăng nhập

---

## Summary

🎯 **3 Vấn Đề Đã Fix:**
1. ✅ User chưa cấp quyền → Hiển thị message rõ ràng, không cho quét
2. ✅ Device đã có owner → Hiển thị ai đang sở hữu, hướng dẫn rõ ràng
3. ✅ User change → Auto clear device data cũ

🎉 **Kết Quả:**
- User experience tốt hơn với messages rõ ràng
- Security tốt hơn với permission check chặt chẽ
- Data isolation giữa các users

