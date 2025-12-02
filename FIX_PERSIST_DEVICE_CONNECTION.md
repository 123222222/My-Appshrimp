# ✅ FIX: Giữ trạng thái kết nối thiết bị qua sessions

## Vấn đề ban đầu

**Vấn đề 1**: Admin kết nối với thiết bị → Logout → Login lại → **Mất trạng thái kết nối**

**Vấn đề 2**: Login lại admin → Home báo "Chưa kết nối" → Vào Profile → Quay lại Home → Mới hiển thị "Đã kết nối"

### Nguyên nhân:
1. Backend lưu device binding: ✅
2. App lưu IP trong SharedPreferences: ✅  
3. Nhưng khi **logout**, SharedPreferences bị xóa: ❌
4. Khi **login lại**:
   - ProfileScreen check backend và lấy IP → Lưu vào SharedPreferences ✅
   - HomeScreen chỉ đọc SharedPreferences → Không có data → Báo "Chưa kết nối" ❌
5. Vào Profile → SharedPreferences được update → Quay lại Home → Có data → "Đã kết nối" ✅

## Giải pháp đã triển khai

### Backend lưu cả IP address

**File**: `backend/app_complete.py`

#### 1. Cập nhật format lưu trữ

**Trước đây** (format cũ):
```json
{
  "raspberrypi-001": "hodung15032003@gmail.com"
}
```

**Bây giờ** (format mới):
```json
{
  "raspberrypi-001": {
    "email": "hodung15032003@gmail.com",
    "ip": "192.168.1.100",
    "last_updated": 1733054400
  }
}
```

#### 2. API `/api/devices/bind` - Lưu IP

```python
@app.route('/api/devices/bind', methods=['POST'])
def bind_device():
    data = request.json
    device_id = data.get('device_id')
    device_ip = data.get('device_ip')  # ← Nhận IP từ app
    
    # Lưu cả email và IP
    permitted_devices[device_id] = {
        'email': email,
        'ip': device_ip,
        'last_updated': time.time()
    }
    save_permitted_devices(permitted_devices)
```

#### 3. API `/api/devices/my-device` - Trả về IP

```python
@app.route('/api/devices/my-device', methods=['GET'])
def get_my_device():
    # Tìm device của user
    for device_id, binding_info in permitted_devices.items():
        if binding_info.get('email') == email:
            return jsonify({
                "device_id": device_id,
                "device_ip": binding_info.get('ip'),  # ← Trả về IP
                "bound": True
            })
```

#### 4. Backward compatible

Code handle cả **format cũ** (string) và **format mới** (dict):

```python
# Handle both formats
owner_email = binding_info if isinstance(binding_info, str) else binding_info.get('email')
```

### App nhận và lưu IP từ Backend

**File**: `app/.../ProfileScreen.kt`

#### 1. Gửi IP khi bind

```kotlin
fun bindDevice(ip: String, deviceId: String) {
    val jsonBody = JSONObject().apply {
        put("device_id", deviceId)
        put("device_ip", ip)  // ← Gửi IP cho backend
    }.toString()
    
    // POST to /api/devices/bind
}
```

#### 2. Nhận IP từ backend khi check

```kotlin
LaunchedEffect(Unit) {
    // GET /api/devices/my-device
    val jsonResponse = JSONObject(response.body?.string())
    val deviceId = jsonResponse.optString("device_id")
    val deviceIpFromBackend = jsonResponse.optString("device_ip", null)  // ← Nhận IP
    
    // Ưu tiên IP từ backend
    val finalIp = deviceIpFromBackend ?: savedIp
    
    if (deviceId != null && finalIp != null) {
        // Lưu lại vào SharedPreferences
        prefs.edit()
            .putString("rasp_ip", finalIp)
            .putString("rasp_device_id", deviceId)
            .apply()
        
        boundDevice = "$deviceId ($finalIp)"  // ← Hiển thị "Đã kết nối"
    }
}
```

## Workflow mới

### Lần đầu bind device:
1. Admin quét WiFi → Tìm device với IP `192.168.1.100`
2. Nhấn bind → App gửi `device_id` + `device_ip` lên backend
3. Backend lưu vào JSON:
   ```json
   {
     "raspberrypi-001": {
       "email": "admin@gmail.com",
       "ip": "192.168.1.100"
     }
   }
   ```
4. App lưu vào SharedPreferences
5. ✅ Hiển thị "Đã kết nối"

### Logout và login lại:
1. SharedPreferences bị xóa (mất IP local)
2. App gọi `/api/devices/my-device`
3. Backend trả về:
   ```json
   {
     "device_id": "raspberrypi-001",
     "device_ip": "192.168.1.100"  ← Có IP!
   }
   ```
4. App nhận IP từ backend và lưu lại vào SharedPreferences
5. ✅ Hiển thị "Đã kết nối" (không cần quét lại!)

### Login bằng tài khoản khác:
1. User khác login
2. App check backend → Không có device binding
3. ⚠️ Hiển thị "Chưa kết nối"

## Testing

### Test 1: Bind và logout/login
```bash
# 1. Xóa data cũ
cd backend
del permitted_devices.json
echo {} > permitted_devices.json

# 2. Restart backend
python app_complete.py

# 3. App: Clear data và rebuild
gradlew clean build
```

**Steps**:
1. Login admin
2. Quét và bind device → ✅ "Đã kết nối"
3. Logout
4. Login lại admin
5. ✅ Verify: Vẫn thấy "Đã kết nối" (không cần quét lại)

### Test 2: Login tài khoản khác
1. Logout khỏi admin
2. Login user khác
3. ✅ Verify: Thấy "Chưa kết nối"

### Test 3: Cập nhật IP
1. Admin đã bind device
2. IP Raspberry Pi thay đổi (ví dụ: router assign IP mới)
3. Admin quét lại và bind
4. ✅ Verify: Backend cập nhật IP mới

## File changes

### Backend:
- ✅ `app_complete.py` - Updated endpoints
  - `bind_device()` - Save IP
  - `get_my_device()` - Return IP
  - `unbind_device()` - Handle dict format
  - `load_permitted_devices()` - Handle JSON errors

### App:
- ✅ `ProfileScreen.kt`
  - `bindDevice()` - Send IP to backend
  - `LaunchedEffect` - Get IP from backend
  - Save IP to SharedPreferences

### Data:
- ✅ `permitted_devices.json` - New format with IP

## Lợi ích

✅ **Persist qua sessions**: Admin logout/login vẫn giữ kết nối
✅ **Không cần cùng WiFi**: Dùng IP đã lưu, không cần quét lại
✅ **Backward compatible**: Vẫn hoạt động với format cũ
✅ **Update IP**: Có thể cập nhật IP mới khi cần
✅ **Multi-user**: Mỗi user có device riêng

## Lưu ý

1. **IP có thể thay đổi**: Nếu Raspberry Pi restart và router assign IP mới, admin cần quét và bind lại
2. **Static IP khuyến nghị**: Set static IP cho Raspberry Pi để tránh IP thay đổi
3. **Ngrok tốt hơn**: Dùng Ngrok URL thay vì IP local để không bị ảnh hưởng bởi network changes

## Next steps

### Cải tiến tương lai:
1. **Auto-detect IP change**: Ping IP cũ, nếu không connect được thì tự động prompt scan lại
2. **Multiple devices**: Admin có thể bind nhiều devices
3. **Device status**: Hiển thị online/offline status
4. **Ngrok integration**: Lưu Ngrok URL thay vì IP local

---

✅ **Hoàn thành!** Giờ admin logout/login vẫn giữ trạng thái kết nối thiết bị!

