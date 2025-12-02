# 📱 ANDROID APP - DEVICE BINDING CHO TẤT CẢ USER

## ⚠️ LƯU Ý QUAN TRỌNG

**Device Binding KHÔNG PHẢI là tính năng chỉ dành cho admin!**

✅ **TẤT CẢ USER** (admin và user thường) đều có thể:
- Quét thiết bị (UDP Discovery)
- Kết nối thiết bị (Bind Device)
- Hủy kết nối thiết bị (Unbind Device)
- Truy cập Home, Chart, Gallery

❌ **CHỈ ADMIN** mới có thể:
- Thêm/xóa user (Email Management)

---

## Backend API - Device Binding

### 1. Bind Device
```
POST /api/devices/bind
Headers: Authorization: <FIREBASE_TOKEN>
Body: {
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

**Quyền:** `@requires_google_auth` → ✅ TẤT CẢ USER

### 2. Unbind Device
```
POST /api/devices/unbind
Headers: Authorization: <FIREBASE_TOKEN>
Body: {
  "device_id": "raspberrypi-001"
}
```

**Quyền:** `@requires_google_auth` → ✅ TẤT CẢ USER

### 3. Get My Device
```
GET /api/devices/my-device
Headers: Authorization: <FIREBASE_TOKEN>
```

**Quyền:** `@requires_google_auth` → ✅ TẤT CẢ USER

### 4. Check Device Permission
```
POST /api/devices/check
Headers: Authorization: <FIREBASE_TOKEN>
Body: {
  "device_id": "raspberrypi-001"
}
```

**Quyền:** `@requires_google_auth` → ✅ TẤT CẢ USER

---

## Android App Implementation

### ❌ SAI - Không làm thế này:

```kotlin
// ❌ ĐỪNG kiểm tra isAdmin cho device binding!
fun showDeviceScanner() {
    if (currentUser.isAdmin) {  // ❌ SAI!
        // Show device scanner
    } else {
        Toast.makeText(this, "Only admin can scan devices", Toast.LENGTH_SHORT).show()
    }
}
```

### ✅ ĐÚNG - Làm thế này:

```kotlin
// ✅ ĐÚNG - Tất cả user đều có thể quét thiết bị
fun showDeviceScanner() {
    // Không cần kiểm tra isAdmin
    // Chỉ cần kiểm tra user đã đăng nhập chưa
    if (isUserLoggedIn()) {
        startDeviceDiscovery()
    } else {
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
    }
}

fun bindDevice(deviceId: String, deviceIp: String) {
    // Tất cả user đều có thể bind
    val request = BindDeviceRequest(deviceId, deviceIp)
    apiService.bindDevice(firebaseToken, request)
        .enqueue(object : Callback<BindDeviceResponse> {
            override fun onResponse(call: Call<BindDeviceResponse>, response: Response<BindDeviceResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Device bound successfully", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            }
            override fun onFailure(call: Call<BindDeviceResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to bind device", Toast.LENGTH_SHORT).show()
            }
        })
}
```

---

## UI/UX Guidelines

### Navigation Menu

```
📱 Main Menu
├─ 🏠 Home              ✅ Tất cả user
├─ 📊 Chart             ✅ Tất cả user
├─ 🖼️ Gallery           ✅ Tất cả user
├─ 📱 Device Settings   ✅ Tất cả user
│   ├─ Scan Device      ✅ Tất cả user
│   ├─ Unbind Device    ✅ Tất cả user
│   └─ Device Info      ✅ Tất cả user
├─ ⚙️ Settings          ✅ Tất cả user
└─ 👥 User Management   ⭐ Chỉ admin
    ├─ Add User         ⭐ Chỉ admin
    └─ Remove User      ⭐ Chỉ admin
```

### Conditional UI

```kotlin
// ViewModel hoặc Activity
class MainViewModel : ViewModel() {
    
    val isAdmin: Boolean
        get() = FirebaseAuth.getInstance().currentUser?.email == ADMIN_EMAIL
    
    // Menu items
    fun getMenuItems(): List<MenuItem> {
        val items = mutableListOf(
            MenuItem("Home", R.drawable.ic_home),
            MenuItem("Chart", R.drawable.ic_chart),
            MenuItem("Gallery", R.drawable.ic_gallery),
            MenuItem("Device Settings", R.drawable.ic_device),  // ✅ Tất cả user
        )
        
        // ⭐ CHỈ thêm User Management cho admin
        if (isAdmin) {
            items.add(MenuItem("User Management", R.drawable.ic_users))
        }
        
        return items
    }
}
```

---

## Luồng Hoạt Động

### User Thường - First Time Login

1. **Đăng nhập** với Google (email đã được admin add)
2. **Quét thiết bị** → Show device scanner screen
3. **Tìm thấy device** → "raspberrypi-001" at 192.168.1.100
4. **Nhấn Connect** → Call API `/api/devices/bind`
5. **Backend kiểm tra:**
   - ✅ Token hợp lệ
   - ✅ Email trong permitted_emails.json
   - ✅ Device chưa bind với ai khác
   - ✅ Bind thành công!
6. **Navigate to Home** → User có thể xem camera, detect, etc.

### User Thường - Subsequent Logins

1. **Đăng nhập** với Google
2. **Check device binding** → Call API `/api/devices/my-device`
3. **Response:** 
   ```json
   {
     "success": true,
     "device_id": "raspberrypi-001",
     "device_ip": "192.168.1.100",
     "bound": true
   }
   ```
4. **Navigate to Home** → Automatically connect to device

### Admin - Same Flow

Admin cũng đi qua luồng tương tự, chỉ khác là có thêm menu "User Management".

---

## API Response Examples

### Bind Device - Success

```json
{
  "success": true,
  "message": "Device bound successfully",
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

### Bind Device - Already Bound to Another User

```json
{
  "success": false,
  "message": "Device already bound to another account: other@example.com"
}
```

**→ User thường KHÔNG thể chiếm device của người khác!**

### Unbind Device - Success

```json
{
  "success": true,
  "message": "Device unbound successfully"
}
```

### Unbind Device - Not Owner

```json
{
  "success": false,
  "message": "You don't have permission to unbind this device"
}
```

**→ User chỉ có thể unbind device của chính mình!**

---

## Security & Permissions

### ✅ Điều Backend Đảm Bảo

1. **Authentication:** Tất cả device APIs yêu cầu Firebase token hợp lệ
2. **Authorization:** Email phải có trong `permitted_emails.json`
3. **Ownership:** 
   - User chỉ có thể bind device chưa được bind
   - User chỉ có thể unbind device của chính mình
   - User không thể chiếm device của người khác
4. **Device Isolation:** Mỗi device chỉ có thể bind với 1 user tại 1 thời điểm

### ❌ Điều Android App KHÔNG NÊN Làm

1. ❌ Kiểm tra `isAdmin` trước khi cho quét device
2. ❌ Ẩn nút "Scan Device" với user thường
3. ❌ Hiển thị message "Only admin can bind device"
4. ❌ Disable device settings cho user thường

### ✅ Điều Android App NÊN Làm

1. ✅ Cho phép TẤT CẢ user quét và bind device
2. ✅ Chỉ ẩn menu "User Management" với non-admin
3. ✅ Hiển thị device settings cho tất cả user
4. ✅ Trust backend để handle permissions

---

## Testing Checklist

### Test với User Thường

- [ ] Đăng nhập với email non-admin
- [ ] Có thể thấy nút "Scan Device"
- [ ] Có thể quét và tìm thấy device
- [ ] Có thể bind device thành công
- [ ] Có thể truy cập Home/Chart/Gallery
- [ ] Có thể unbind device
- [ ] **KHÔNG** thấy menu "User Management"

### Test với Admin

- [ ] Đăng nhập với admin email
- [ ] Có thể thấy nút "Scan Device"
- [ ] Có thể quét và bind device
- [ ] Có thể truy cập Home/Chart/Gallery
- [ ] Có thể unbind device
- [ ] **CÓ** thấy menu "User Management"
- [ ] Có thể thêm/xóa user

### Test Device Ownership

- [ ] User A bind device → Success
- [ ] User B thử bind cùng device → Fail (already bound)
- [ ] User B thử unbind device của User A → Fail (not owner)
- [ ] User A unbind device → Success
- [ ] User B bind device → Success (giờ available)

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Hardcoded Admin Check
```kotlin
// ❌ SAI
if (email == "hodung15032003@gmail.com") {
    showDeviceScanner()
}
```

### ✅ Correct:
```kotlin
// ✅ ĐÚNG
if (isUserLoggedIn()) {  // Bất kỳ user nào đã login
    showDeviceScanner()
}
```

### ❌ Mistake 2: UI Restriction
```kotlin
// ❌ SAI
binding.btnScanDevice.visibility = if (isAdmin) View.VISIBLE else View.GONE
```

### ✅ Correct:
```kotlin
// ✅ ĐÚNG
binding.btnScanDevice.visibility = View.VISIBLE  // Luôn visible cho mọi user
```

### ❌ Mistake 3: Misleading Error Message
```kotlin
// ❌ SAI
Toast.makeText(this, "Only admin can scan devices", Toast.LENGTH_SHORT).show()
```

### ✅ Correct:
```kotlin
// ✅ ĐÚNG
Toast.makeText(this, "Please login to scan devices", Toast.LENGTH_SHORT).show()
```

---

## Summary

| Feature | Admin | User Thường | Backend Check |
|---------|:-----:|:-----------:|---------------|
| **Scan Device** | ✅ | ✅ | `@requires_google_auth` |
| **Bind Device** | ✅ | ✅ | `@requires_google_auth` |
| **Unbind Device** | ✅ | ✅ | `@requires_google_auth` + ownership |
| **Home/Chart/Gallery** | ✅ | ✅ | `@requires_google_auth` |
| **User Management** | ✅ | ❌ | `email == ADMIN_EMAIL` |

---

## Kết Luận

🎯 **Device Binding là tính năng cho TẤT CẢ USER, không phải chỉ admin!**

- ✅ Backend đã đúng (dùng `@requires_google_auth`)
- ✅ Chỉ cần sửa Android app để không check `isAdmin` cho device features
- ✅ Chỉ ẩn "User Management" menu với non-admin users

**Android app cần sửa:**
1. Remove admin check khỏi device scanner
2. Show device settings cho tất cả user
3. Chỉ ẩn User Management menu

