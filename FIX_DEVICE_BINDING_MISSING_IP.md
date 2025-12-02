# Fix: Device Binding Issue - Missing IP Address

## Vấn đề

Khi user đăng nhập, app kiểm tra với backend và thấy device đã được bind, nhưng:
- Không có IP address trong SharedPreferences (vì không cùng WiFi)
- Home/Chart/Gallery báo "Chưa kết nối thiết bị" 
- Nút "Hủy kết nối" không hoạt động

## Nguyên nhân

1. Backend lưu device binding trong `permitted_devices.json`
2. Khi check với endpoint `/api/devices/my-device`, backend trả về `bound: true`
3. Nhưng SharedPreferences không có `rasp_ip` (chỉ có IP khi quét UDP)
4. App logic cũ hiển thị "Đã kết nối" chỉ dựa vào backend response
5. Các screen khác check `rasp_device_id` nhưng không có IP để connect

## Giải pháp đã triển khai

### 1. ProfileScreen - Logic mới

```kotlin
// Thêm state để track device_id từ backend
var backendDeviceId by remember { mutableStateOf<String?>(null) }

// Trong LaunchedEffect
if (bound) {
    val deviceId = jsonResponse.optString("device_id")
    backendDeviceId = deviceId // Save backend device_id
    
    val savedIp = prefs.getString("rasp_ip", null)
    val savedDeviceId = prefs.getString("rasp_device_id", null)
    
    // Chỉ hiển thị "Đã kết nối" nếu có CẢ device_id VÀ IP
    if (savedDeviceId == deviceId && savedIp != null) {
        boundDevice = "$deviceId ($savedIp)"
    } else {
        // Device đã bind trên backend nhưng thiếu IP
        boundDevice = null
        // Clear invalid data
        prefs.edit()
            .remove("rasp_ip")
            .remove("rasp_device_id")
            .apply()
    }
}
```

### 2. UI States

App có 3 trạng thái device:

#### State 1: Fully Connected (boundDevice != null)
```
✅ Đã kết nối
Device: rasp_001 (192.168.1.100)
[Hủy kết nối]
```
- Có device_id VÀ IP
- Có thể sử dụng Home/Chart/Gallery

#### State 2: Bound but Missing IP (backendDeviceId != null && boundDevice == null)
```
⚠️ Thiết bị đã được bind nhưng thiếu thông tin kết nối
Device ID: rasp_001
Vui lòng hủy kết nối và quét lại thiết bị để lấy địa chỉ IP mới.
[Hủy kết nối]
```
- Device đã bind trên backend
- Nhưng không có IP local (không cùng WiFi)
- Phải unbind rồi quét lại khi cùng WiFi

#### State 3: Not Connected (boundDevice == null && backendDeviceId == null)
```
Quét mạng WiFi để tìm thiết bị Raspberry Pi
[Quét thiết bị]
```
- Chưa bind device nào
- Admin có thể quét và bind

### 3. Unbind Function

```kotlin
fun unbindDevice() {
    // Call backend /api/devices/unbind
    // Clear SharedPreferences
    // Clear boundDevice AND backendDeviceId
    boundDevice = null
    backendDeviceId = null
}
```

## Workflow mới

### Khi không cùng WiFi:

1. User đăng nhập → App check backend
2. Backend trả về `bound: true, device_id: "rasp_001"`
3. App check SharedPreferences → Không có `rasp_ip`
4. App hiển thị: **State 2 - Bound but Missing IP**
5. User nhấn "Hủy kết nối"
6. Backend xóa binding trong `permitted_devices.json`
7. App clear SharedPreferences và states
8. User chuyển sang **State 3 - Not Connected**

### Khi cùng WiFi:

1. User (admin) vào Profile
2. Nhấn "Quét thiết bị"
3. Tìm thấy device với IP
4. Nhấn bind → Lưu cả device_id VÀ IP
5. Backend lưu binding
6. App hiển thị: **State 1 - Fully Connected**
7. Có thể sử dụng Home/Chart/Gallery

## Testing

### Test Case 1: User bind device khi cùng WiFi
1. Login admin
2. Quét thiết bị → Tìm thấy `rasp_001 (192.168.1.100)`
3. Bind → Thành công
4. Verify: Thấy "✅ Đã kết nối" với IP
5. Vào Home → Camera stream hoạt động

### Test Case 2: User bind device rồi thoát khỏi WiFi
1. Đã bind device (State 1)
2. Thoát khỏi WiFi của Raspberry Pi
3. Logout và login lại
4. Verify: Thấy "⚠️ Thiết bị đã được bind nhưng thiếu thông tin kết nối"
5. Vào Home → Báo "Chưa kết nối thiết bị"
6. Nhấn "Hủy kết nối" → Thành công
7. Verify: Chuyển sang state "Quét thiết bị"

### Test Case 3: User không cùng WiFi từ đầu
1. Admin đã bind device từ máy khác
2. User mới login lần đầu
3. Backend trả về device đã bound
4. Nhưng không có IP local
5. Verify: Thấy warning "thiếu thông tin kết nối"
6. Nhấn "Hủy kết nối" → Admin phải bind lại

## Files đã sửa

1. ✅ `ProfileScreen.kt`
   - Thêm `backendDeviceId` state
   - Logic check device binding mới
   - UI cho 3 states
   - Unbind clear cả 2 states

## Lưu ý quan trọng

### ⚠️ Giới hạn hiện tại

1. **Chỉ bind được khi cùng WiFi**: UDP discovery chỉ hoạt động trong LAN
2. **Mất IP khi logout**: SharedPreferences không persist IP qua sessions (có thể fix bằng cách lưu IP vào backend)
3. **Một device một user**: Mỗi device chỉ bind với 1 email tại 1 thời điểm

### 💡 Cải tiến tương lai

1. **Backend lưu IP**: Backend lưu cả IP trong `permitted_devices.json`:
   ```json
   {
     "rasp_001": {
       "owner": "admin@gmail.com",
       "ip": "192.168.1.100",
       "last_seen": "2024-12-01T10:30:00"
     }
   }
   ```

2. **API trả về IP**: Endpoint `/api/devices/my-device` trả về cả IP
   ```json
   {
     "bound": true,
     "device_id": "rasp_001",
     "ip": "192.168.1.100"
   }
   ```

3. **Ngrok URL**: Dùng Ngrok URL thay vì IP local để không cần cùng WiFi

## Summary

✅ **Đã sửa**: 
- App không còn hiển thị "Đã kết nối" khi thiếu IP
- Unbind button hoạt động chính xác
- Home/Chart/Gallery yêu cầu cả device_id VÀ IP

✅ **UX tốt hơn**:
- Warning rõ ràng khi thiếu IP
- Hướng dẫn user phải unbind và quét lại
- 3 states phân biệt rõ ràng

✅ **Security**:
- Vẫn verify với backend
- Không trust client-side data
- Admin only cho bind/unbind

