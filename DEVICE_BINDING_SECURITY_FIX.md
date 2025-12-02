# Device Binding Security Fix

## Tóm tắt vấn đề
Trước đây, hệ thống có các lỗ hổng bảo mật:
1. **User không phải admin** vẫn có thể quét và kết nối với Raspberry Pi
2. **Không cần kết nối thiết bị** vẫn truy cập được camera, chart, gallery
3. **Không kiểm tra device binding** với backend khi truy cập các tính năng

## Giải pháp đã triển khai

### 1. ProfileScreen - Chỉ Admin mới được quét thiết bị
**File**: `app/src/main/java/com/dung/myapplication/mainUI/profile/ProfileScreen.kt`

**Thay đổi**:
- Thêm constant `ADMIN_EMAIL = "hodung15032003@gmail.com"` (phải khớp với backend)
- Thêm state `isAdmin` để kiểm tra user hiện tại có phải admin không
- Kiểm tra `currentEmail == ADMIN_EMAIL` trong LaunchedEffect
- Hiển thị message cảnh báo nếu user không phải admin
- Chỉ hiển thị nút "Quét thiết bị" nếu `isAdmin == true`
- **MỚI**: Thêm chức năng quản lý email được phép truy cập:
  - Hiển thị danh sách email đã được cấp quyền
  - Thêm email mới vào danh sách
  - Xóa email khỏi danh sách (không thể xóa admin email)
  - Dialog nhập email với validation

**Kết quả**: 
- User thường sẽ thấy thông báo:
```
⚠️ Chỉ Admin mới có thể quét và kết nối thiết bị
Vui lòng liên hệ Admin để được cấp quyền truy cập thiết bị.
```
- Admin sẽ thấy thêm card "Quản lý quyền truy cập" với:
  - Danh sách email được phép
  - Nút "Thêm Email"
  - Nút xóa cho mỗi email (trừ admin)

### 2. HomeScreen - Kiểm tra device binding trước khi stream camera
**File**: `app/src/main/java/com/dung/myapplication/mainUI/home/HomeScreen.kt`

**Thay đổi**:
- Trong `LaunchedEffect(raspDeviceId)`, kiểm tra device có thực sự được bind với backend không
- Gọi API `/api/devices/my-device` để verify
- Nếu device không bound, hiển thị error và xóa device info từ SharedPreferences
- Chỉ cho phép stream camera nếu device được verify thành công

**Kết quả**: 
- Không thể fake device binding bằng cách thêm vào SharedPreferences
- Mọi request đều được verify với backend

### 3. ChartViewModel - Kiểm tra device binding trước khi load dữ liệu
**File**: `app/src/main/java/com/dung/myapplication/mainUI/chart/ChartViewModel.kt`

**Thay đổi**:
- Trong `loadImages()`, kiểm tra `rasp_device_id` có tồn tại không
- Verify với backend qua API `/api/devices/my-device`
- Nếu không bound, hiển thị error: "Chưa kết nối thiết bị. Vui lòng vào trang Hồ sơ để kết nối."
- Xóa device info khỏi SharedPreferences nếu verify thất bại

**Kết quả**: Chart chỉ hiển thị khi có device binding hợp lệ

### 4. GalleryViewModel - Kiểm tra device binding trước khi load gallery
**File**: `app/src/main/java/com/dung/myapplication/mainUI/gallery/GalleryViewModel.kt`

**Thay đổi**:
- Tương tự ChartViewModel
- Kiểm tra device binding trước khi load images
- Verify với backend
- Hiển thị error message nếu không có device binding

**Kết quả**: Gallery chỉ hiển thị khi có device binding hợp lệ

### 5. MotorControlScreen - Kiểm tra device binding
**File**: `app/src/main/java/com/dung/myapplication/mainUI/control/MotorControlScreen.kt`

**Thay đổi**:
- Kiểm tra `rasp_device_id` từ SharedPreferences
- Nếu null, hiển thị message:
```
⚠️ Chưa kết nối thiết bị
Vui lòng vào trang Hồ sơ để quét và kết nối thiết bị Raspberry Pi
trước khi sử dụng chức năng này.
```
- Hiển thị nút "Đến trang Hồ sơ" để user có thể kết nối device

**Kết quả**: Control screen chỉ hoạt động khi có device binding

## Quy trình sử dụng mới

### Đối với Admin:
1. Đăng nhập bằng email `hodung15032003@gmail.com`
2. Vào trang **Hồ sơ**
3. **Quản lý quyền truy cập**:
   - Xem danh sách email được phép truy cập
   - Nhấn "+ Thêm Email" để cấp quyền cho user mới
   - Nhập email và nhấn "Thêm"
   - Có thể xóa email (trừ admin email) bằng nút xóa
4. **Kết nối thiết bị**:
   - Nhấn **"Quét thiết bị"** để tìm Raspberry Pi (cần cùng WiFi)
   - Chọn device để kết nối
5. Sau khi kết nối thành công, có thể sử dụng:
   - **Home**: Xem camera stream và chụp ảnh phát hiện tôm
   - **Chart**: Xem thống kê các lần phát hiện
   - **Gallery**: Xem thư viện ảnh đã chụp
   - **Control**: Điều khiển động cơ (đang phát triển)

### Đối với User thường:
1. Đăng nhập bằng email thường
2. Vào trang **Hồ sơ**
3. Sẽ thấy thông báo: "Chỉ Admin mới có thể quét và kết nối thiết bị"
4. **KHÔNG THỂ** sử dụng các tính năng:
   - Home: Hiển thị "Chưa kết nối thiết bị"
   - Chart: Hiển thị "Chưa kết nối thiết bị"
   - Gallery: Hiển thị "Chưa kết nối thiết bị"
   - Control: Hiển thị "Chưa kết nối thiết bị"

## Bảo mật Backend

Backend đã có sẵn các endpoint để quản lý device binding và email permissions:

### Device Management Endpoints

#### `/api/devices/bind` (POST)
- Bind device với email của user
- Kiểm tra device có bị bind bởi user khác không
- Lưu mapping `device_id -> email` vào `permitted_devices.json`

#### `/api/devices/my-device` (GET)
- Trả về thông tin device mà user đã bind
- Response: `{"bound": true/false, "device_id": "..."}`

#### `/api/devices/check` (POST)
- Kiểm tra user có quyền truy cập device không
- Verify `device_id` có khớp với email của user không

### Email Permission Management Endpoints (Admin Only)

#### `/api/admin/add-email` (POST)
- Thêm email mới vào danh sách được phép truy cập
- Chỉ admin mới có quyền
- Kiểm tra email đã tồn tại chưa
- Request body: `{"email": "user@example.com"}`
- Response: `{"success": true/false, "message": "..."}`

#### `/api/admin/list-emails` (GET)
- Lấy danh sách tất cả email được phép truy cập
- Chỉ admin mới có quyền
- Response: `{"success": true, "emails": ["email1", "email2"], "admin_email": "admin@..."}`

#### `/api/admin/remove-email` (POST)
- Xóa email khỏi danh sách được phép
- Chỉ admin mới có quyền
- Không được xóa email admin
- Request body: `{"email": "user@example.com"}`
- Response: `{"success": true/false, "message": "..."}`

## Testing

### Test Case 1: Admin login và bind device
1. Login với `hodung15032003@gmail.com`
2. Vào Profile → Quét thiết bị
3. Chọn device để kết nối
4. Verify: Có thể truy cập Home, Chart, Gallery, Control

### Test Case 2: Non-admin login
1. Login với email khác (VD: `user@gmail.com`)
2. Vào Profile
3. Verify: Thấy message "Chỉ Admin mới có thể quét và kết nối thiết bị"
4. Vào Home → Verify: "Chưa kết nối thiết bị"
5. Vào Chart → Verify: "Chưa kết nối thiết bị"
6. Vào Gallery → Verify: "Chưa kết nối thiết bị"
7. Vào Control → Verify: "Chưa kết nối thiết bị"

### Test Case 3: Fake device binding
1. Thử manually add `rasp_device_id` vào SharedPreferences
2. Vào Home/Chart/Gallery
3. Verify: Backend verify thất bại → Hiển thị error và xóa device info

### Test Case 4: Email permission management (Admin only)
1. Login với admin email `hodung15032003@gmail.com`
2. Vào Profile → Card "Quản lý quyền truy cập"
3. Verify: Thấy danh sách email hiện tại (ít nhất có admin email)
4. Nhấn "+ Thêm Email"
5. Nhập email mới (VD: `newuser@gmail.com`)
6. Nhấn "Thêm"
7. Verify: Email được thêm vào danh sách
8. Thử thêm email đã tồn tại → Verify: Thấy error "Email already permitted"
9. Nhấn nút xóa email vừa thêm
10. Verify: Email bị xóa khỏi danh sách
11. Thử xóa admin email → Verify: Không có nút xóa cho admin email

### Test Case 5: Non-admin cannot manage emails
1. Login với non-admin email
2. Vào Profile
3. Verify: Không thấy card "Quản lý quyền truy cập"

## Lưu ý quan trọng

1. **ADMIN_EMAIL phải khớp giữa app và backend**
   - App: `ProfileScreen.kt` line ~65: `val ADMIN_EMAIL = "hodung15032003@gmail.com"`
   - Backend: `app_complete.py` line 140: `ADMIN_EMAIL = os.getenv('ADMIN_EMAIL', 'hodung15032003@gmail.com')`

2. **Device binding được verify ở mọi request quan trọng**
   - Camera streaming
   - Load chart data
   - Load gallery images
   - Capture và detect images
   - Motor control (when implemented)

3. **SharedPreferences chỉ là cache**
   - Mọi operation đều verify với backend
   - Không tin tưởng client-side data

4. **UDP Discovery chỉ dùng cho lần đầu bind**
   - Sau khi bind, dùng Ngrok URL để remote access
   - Không cần cùng WiFi để sử dụng app (trừ lúc bind)

## File liên quan

### Android App
- `app/src/main/java/com/dung/myapplication/mainUI/profile/ProfileScreen.kt`
- `app/src/main/java/com/dung/myapplication/mainUI/home/HomeScreen.kt`
- `app/src/main/java/com/dung/myapplication/mainUI/chart/ChartViewModel.kt`
- `app/src/main/java/com/dung/myapplication/mainUI/gallery/GalleryViewModel.kt`
- `app/src/main/java/com/dung/myapplication/mainUI/control/MotorControlScreen.kt`
- `app/src/main/java/com/dung/myapplication/models/Config.kt`

### Backend
- `backend/app_complete.py` (device binding endpoints)
- `backend/permitted_devices.json` (device -> email mapping)
- `backend/permitted_emails.json` (allowed emails)

## Hướng dẫn thay đổi Admin Email

Nếu muốn thay đổi admin email:

1. **Backend**: Sửa file `backend/.env`
```bash
ADMIN_EMAIL=your_admin_email@gmail.com
```

2. **Android App**: Sửa file `ProfileScreen.kt`
```kotlin
val ADMIN_EMAIL = "your_admin_email@gmail.com"
```

3. Restart backend và rebuild app

