# Hướng dẫn Fix: Lỗi Device Binding và JSON Parsing

## Vấn đề

### 1. Mới vào app đã hiển thị "Đã kết nối"
- Backend đã lưu device binding từ lần trước

### 2. Nút "Hủy kết nối" không hoạt động
- ✅ **ĐÃ FIX** trong ProfileScreen.kt

### 3. Lỗi khi bind device: "Expecting value" 
```
ERROR:__main__:[BIND] Error: Expecting value: line 1 column 1 (char 0)
```
- File `permitted_devices.json` bị rỗng hoặc format sai
- ✅ **ĐÃ FIX** trong app_complete.py

## Nguyên nhân

1. Backend đã lưu device binding trong `permitted_devices.json`
2. File JSON có thể bị corrupt hoặc rỗng
3. Backend không handle JSON error

## Giải pháp đã triển khai

### Fix 1: Backend - Handle JSON error
File: `backend/app_complete.py`

```python
def load_permitted_devices():
    if os.path.exists(PERMITTED_DEVICES_PATH):
        try:
            with open(PERMITTED_DEVICES_PATH, 'r') as f:
                content = f.read().strip()
                if not content:
                    return {}  # File rỗng
                return json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"[DEVICES] Error loading: {e}")
            # Backup file lỗi
            backup_path = f"{PERMITTED_DEVICES_PATH}.corrupt.{int(time.time())}"
            # ... backup code ...
            return {}
    return {}
```

### Fix 2: ProfileScreen - Unbind với backendDeviceId
File: `app/.../ProfileScreen.kt`

```kotlin
fun unbindDevice() {
    // Ưu tiên dùng backendDeviceId, fallback to SharedPreferences
    val deviceId = backendDeviceId ?: prefs.getString("rasp_device_id", null)
    
    if (deviceId != null) {
        // Call API với deviceId
        // Clear both states: boundDevice và backendDeviceId
    }
}
```

### Fix 3: Tạo file JSON đúng format
File: `backend/permitted_devices.json` đã được tạo với nội dung:
```json
{}
```

## Cách fix ngay (Quick Fix)

### Bước 1: Stop backend
```bash
# Nhấn Ctrl+C trong terminal đang chạy backend
```

### Bước 2: Xóa và tạo lại file JSON
```bash
cd D:\MyAppshrimp\backend

# Xóa file cũ (nếu có)
del permitted_devices.json

# File mới đã được tạo với nội dung: {}
```

### Bước 3: Restart backend
```bash
python app_complete.py
```

### Bước 4: Clear app data và test
1. Uninstall app cũ hoặc Clear Data
2. Rebuild và install app mới
3. Login admin → Profile
4. Verify: Thấy "Quét thiết bị"

## Testing Flow

### Test 1: Clear binding và quét lại
1. Xóa `permitted_devices.json`
2. Restart backend
3. Clear app data
4. Login admin → Profile
5. ✅ Thấy "Quét thiết bị"
6. Quét → Bind → ✅ Thấy "Đã kết nối"

### Test 2: Unbind khi thiếu IP
1. Device đã bound trên backend
2. Clear app data (không có IP local)
3. Login → Profile
4. ✅ Thấy warning "thiếu thông tin kết nối"
5. Nhấn "Hủy kết nối" → ✅ Thành công
6. ✅ Chuyển sang "Quét thiết bị"

---

✅ **Tất cả đã fix xong!**

