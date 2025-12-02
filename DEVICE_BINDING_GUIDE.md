# Hướng dẫn kết nối thiết bị Raspberry Pi với App

## 🎯 Tổng quan

**HỆ THỐNG MỚI - DEVICE BINDING ĐỘC LẬP:**

Mỗi user được cấp quyền có thể:
- ✅ Tự do quét và kết nối thiết bị riêng của mình
- ✅ Nhiều user có thể kết nối cùng 1 thiết bị vật lý
- ✅ Hủy kết nối của mình mà không ảnh hưởng tới user khác
- ✅ Truy cập Home, Camera Stream, Gallery, Chart

**Admin** chỉ việc:
- Cấp quyền truy cập cho user mới qua email
- Không cần quản lý device binding cho từng user

## 🔐 Quy trình cấp quyền

### Admin cấp quyền cho user mới

1. **Admin đăng nhập** vào app
2. Vào màn hình **Profile**
3. Trong phần **"Quản lý quyền truy cập"**, nhấn **"Thêm tài khoản"**
4. Nhập email Google của user mới
5. Nhấn **"Thêm"**

✅ User mới giờ có thể đăng nhập và sử dụng hệ thống!

## 📱 Quy trình user mới sử dụng

### 1. Đăng nhập vào App
- User mới sử dụng tài khoản Google đã được admin cấp quyền để đăng nhập
- Nếu email chưa được cấp quyền → Hiển thị thông báo: **"Tài khoản chưa được cấp phép. Liên hệ Admin để được cấp quyền."**

### 2. Quét thiết bị Raspberry Pi
1. Vào màn hình **Profile**
2. Nhấn nút **"Quét thiết bị"**
3. App sẽ quét mạng WiFi để tìm Raspberry Pi (qua UDP broadcast)
4. Danh sách thiết bị hiển thị: **IP + Device ID**

### 3. Kết nối thiết bị
1. Chọn thiết bị từ danh sách
2. App sẽ:
   - Lưu device info vào SharedPreferences (local)
   - Gửi request bind lên backend với Firebase ID Token
   - Backend lưu: `user_email -> {device_id, ip}`
3. Thông báo: **"Device bound successfully"**

### 4. Sử dụng Camera
1. Về màn hình **Home**
2. Camera stream tự động hiển thị
3. Nhấn nút **📷 Camera** để chụp ảnh phát hiện tôm
4. Ảnh được xử lý và hiển thị kết quả

### 5. Hủy kết nối (nếu cần)
1. Vào màn hình **Profile**
2. Nhấn **"Hủy kết nối"**
3. Device của user này bị unbind
4. ✅ **Các user khác KHÔNG bị ảnh hưởng!**

## 🔄 Backend APIs

### 1. Check Authentication Status
```http
POST /api/auth/check
Headers:
  Authorization: <Firebase ID Token>

Response:
{
  "token_valid": true,
  "decoded_email": "user@gmail.com",
  "email_permitted": true,
  "is_admin": false,
  "has_device_bound": true,
  "user_devices": [
    {
      "device_id": "raspberrypi-001",
      "ip": "192.168.1.100"
    }
  ]
}
```

### 2. Bind Device (User's Own Binding)
```http
POST /api/devices/bind
Headers:
  Authorization: <Firebase ID Token>
Body:
{
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}

Response:
{
  "success": true,
  "message": "Device bound successfully",
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

### 3. Get My Device
```http
GET /api/devices/my-device
Headers:
  Authorization: <Firebase ID Token>

Response:
{
  "success": true,
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100",
  "bound": true
}
```

### 4. Unbind Device (User's Own Device)
```http
POST /api/devices/unbind
Headers:
  Authorization: <Firebase ID Token>
Body:
{
  "device_id": "raspberrypi-001"
}

Response:
{
  "success": true,
  "message": "Device unbound successfully"
}
```

### 5. Admin: List Permitted Emails
```http
GET /api/admin/list-emails
Headers:
  Authorization: <Admin Firebase ID Token>

Response:
{
  "success": true,
  "emails": [
    "admin@gmail.com",
    "user1@gmail.com",
    "user2@gmail.com"
  ]
}
```

### 6. Admin: Add Permitted Email
```http
POST /api/admin/add-email
Headers:
  Authorization: <Admin Firebase ID Token>
Body:
{
  "email": "newuser@gmail.com"
}

Response:
{
  "success": true,
  "message": "Email added successfully"
}
```

### 7. Admin: Remove Permitted Email
```http
POST /api/admin/remove-email
Headers:
  Authorization: <Admin Firebase ID Token>
Body:
{
  "email": "user@gmail.com"
}

Response:
{
  "success": true,
  "message": "Email removed successfully"
}
```

## 🔧 Cơ chế hoạt động

### Device Discovery (UDP Broadcast)
```
App (Android)                    Raspberry Pi
     │                                │
     │  UDP Broadcast                 │
     │  "DISCOVER_RASP"               │
     │  Port: 50000                   │
     ├────────────────────────────────>
     │                                │
     │         UDP Reply              │
     │  "RASP_HERE:device_id"         │
     <────────────────────────────────┤
     │                                │
     │  Extract: IP + Device ID       │
     └────────────────────────────────┘
```

### Independent Device Binding

**Old System (Shared):**
```json
// permitted_devices.json
{
  "raspberrypi-001": "admin@gmail.com"
}
// ❌ Problem: Only admin can use this device
```

**New System (Independent):**
```json
// permitted_devices.json
{
  "admin@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1701234567
  },
  "user1@gmail.com": {
    "device_id": "raspberrypi-001",  // Same device!
    "ip": "192.168.1.100",
    "last_updated": 1701234568
  },
  "user2@gmail.com": {
    "device_id": "raspberrypi-002",  // Different device
    "ip": "192.168.1.101",
    "last_updated": 1701234569
  }
}
```

✅ **Benefits:**
- Each user has their own device binding
- Multiple users can use the same physical device
- Unbinding doesn't affect other users
- Users can switch devices freely

### Camera Stream Access
1. User binds device → Saves `device_id` + `ip` locally
2. Home screen loads → Connects to `http://{ip}:8000/blynk_feed`
3. MJPEG stream displays in real-time
4. User clicks capture → Sends frame to backend for detection

### Security
- ✅ All APIs require Firebase ID Token
- ✅ Token verified with Firebase Admin SDK
- ✅ Email checked against `permitted_emails.json`
- ✅ Each user can only unbind their own device
- ✅ Admin-only endpoints protected with admin email check

## 📁 File Storage

### Backend Files

#### `permitted_emails.json`
```json
[
  "admin@gmail.com",
  "user1@gmail.com",
  "user2@gmail.com"
]
```

#### `permitted_devices.json` (New Format)
```json
{
  "admin@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1701234567
  },
  "user1@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1701234568
  }
}
```

### Android App (SharedPreferences "auth")
```kotlin
val prefs = context.getSharedPreferences("auth", MODE_PRIVATE)

// Stored data:
prefs.getString("rasp_device_id", null)  // Device ID
prefs.getString("rasp_ip", null)         // Device IP
prefs.getString("idToken", null)         // Firebase ID Token
```

## 🐛 Troubleshooting

### "Tài khoản chưa được cấp phép"
**Nguyên nhân:** Email chưa được admin thêm vào `permitted_emails.json`

**Giải pháp:**
1. Liên hệ admin
2. Admin vào Profile → Thêm email của bạn
3. Đăng xuất và đăng nhập lại

### Không tìm thấy thiết bị khi quét
**Nguyên nhân:** 
- App và Raspberry Pi không cùng mạng WiFi
- Firewall block UDP port 50000

**Giải pháp:**
1. Kiểm tra kết nối WiFi
2. Ping Raspberry Pi: `ping {raspberry_pi_ip}`
3. Kiểm tra Raspberry Pi đang chạy backend

### "You don't have permission to unbind this device"
**Nguyên nhân:** Đang cố unbind device của user khác

**Giải pháp:** Chỉ có thể unbind device của chính mình

### Kết nối camera bị lỗi
**Giải pháp:**
1. Kiểm tra backend đang chạy: `ps aux | grep python`
2. Test stream trực tiếp: `http://{ip}:8000/blynk_feed`
3. Kiểm tra camera được kết nối: `v4l2-ctl --list-devices`

## 💡 Best Practices

1. **Admin:** Chỉ cấp quyền cho user tin tưởng
2. **User:** Unbind device khi không sử dụng nữa
3. **Backup:** Admin nên backup `permitted_emails.json` và `permitted_devices.json`
4. **Security:** Không chia sẻ Firebase credentials

## ✅ Flow Summary

```
Admin:
  1. Add user email to permitted list
  2. User can now login

User:
  1. Login with Google (permitted email)
  2. Scan for devices
  3. Bind device (independent binding)
  4. Use Home/Camera/Gallery/Chart
  5. Unbind when done (doesn't affect others)
```

### 1. Đăng nhập vào App
- Sử dụng tài khoản Google để đăng nhập
- Sau khi đăng nhập thành công, token xác thực sẽ được lưu tự động

### 2. Quét thiết bị Raspberry Pi
1. Vào màn hình **Hồ sơ (Profile)**
2. Nhấn nút **"Quét thiết bị"**
3. App sẽ quét mạng WiFi hiện tại để tìm Raspberry Pi
4. Danh sách thiết bị tìm được sẽ hiển thị (IP + Device ID)

### 3. Kết nối thiết bị
1. Chọn thiết bị muốn kết nối từ danh sách
2. App sẽ:
   - Lưu thông tin thiết bị vào local storage
   - Gửi yêu cầu "bind" lên backend server
   - Backend lưu mapping: `device_id -> email_user`
3. Sau khi kết nối thành công, bạn sẽ thấy thông tin thiết bị đã kết nối

### 4. Sử dụng Camera
1. Quay về màn hình **Home**
2. Camera stream sẽ tự động hiển thị (không cần quét lại)
3. Nhấn nút **Camera** để chụp ảnh và phát hiện tôm
4. Ảnh được xử lý sẽ hiển thị với bounding boxes và số lượng tôm

### 5. Hủy kết nối (nếu cần)
1. Vào màn hình **Hồ sơ**
2. Nhấn nút **"Hủy kết nối"**
3. Thiết bị sẽ được giải phóng và có thể kết nối với tài khoản khác

## Backend APIs

### 1. Bind Device
```
POST /api/devices/bind
Headers:
  - Authorization: <Firebase ID Token>
Body:
  {
    "device_id": "raspberrypi-001"
  }
Response:
  {
    "success": true,
    "message": "Device bound successfully",
    "device_id": "raspberrypi-001"
  }
```

### 2. Check Permission
```
POST /api/devices/check
Headers:
  - Authorization: <Firebase ID Token>
Body:
  {
    "device_id": "raspberrypi-001"
  }
Response:
  {
    "success": true,
    "message": "Access granted",
    "device_id": "raspberrypi-001"
  }
```

### 3. Get My Device
```
GET /api/devices/my-device
Headers:
  - Authorization: <Firebase ID Token>
Response:
  {
    "success": true,
    "device_id": "raspberrypi-001",
    "bound": true
  }
```

### 4. Unbind Device
```
POST /api/devices/unbind
Headers:
  - Authorization: <Firebase ID Token>
Body:
  {
    "device_id": "raspberrypi-001"
  }
Response:
  {
    "success": true,
    "message": "Device unbound successfully"
  }
```

## Cơ chế hoạt động

### Device Discovery (UDP Broadcast)
1. App gửi broadcast message "DISCOVER_RASP" qua UDP port 50000
2. Raspberry Pi nhận được và reply với Device ID
3. App nhận IP và Device ID từ response

### Device Binding
1. User chọn device từ danh sách scan được
2. App lưu thông tin device vào SharedPreferences:
   - `rasp_ip`: IP của Raspberry Pi
   - `rasp_device_id`: Device ID của Raspberry Pi
3. App gửi request bind lên backend với Firebase ID Token
4. Backend verify token và lưu mapping: `device_id -> user_email`
5. File `permitted_devices.json` lưu mapping này

### Camera Stream
1. Khi vào Home screen, app kiểm tra xem đã có device_id chưa
2. Nếu có, tự động kết nối tới `http://{rasp_ip}:8000/blynk_feed`
3. Stream camera hiển thị realtime qua MJPEG protocol
4. User nhấn nút chụp để gửi frame lên backend phát hiện tôm

### Security
- Mỗi device chỉ bind được với 1 email tại 1 thời điểm
- Nếu device đã bind cho user khác, request bind sẽ bị reject
- Chỉ owner mới có thể unbind device
- Tất cả API đều yêu cầu Firebase authentication

## File Storage

### Backend
- `permitted_devices.json`: Lưu mapping device_id -> email
```json
{
  "raspberrypi-001": "user@gmail.com",
  "raspberrypi-002": "admin@gmail.com"
}
```

### Android App (SharedPreferences "auth")
- `rasp_ip`: IP của Raspberry Pi đã kết nối
- `rasp_device_id`: Device ID của Raspberry Pi
- `idToken`: Firebase ID Token để xác thực

## Troubleshooting

### Không tìm thấy thiết bị
- Kiểm tra app và Raspberry Pi cùng mạng WiFi
- Kiểm tra Firewall không block UDP port 50000
- Kiểm tra Raspberry Pi đã chạy server chưa

### Kết nối camera bị lỗi
- Kiểm tra camera đã được bật trên Raspberry Pi
- Kiểm tra port 8000 có accessible không
- Thử kết nối trực tiếp qua browser: `http://{rasp_ip}:8000/blynk_feed`

### Không thể bind device
- Kiểm tra device đã bind cho user khác chưa
- Kiểm tra Firebase authentication token còn hợp lệ không
- Xem log backend để debug

## Lưu ý
- Chỉ kết nối khi đang ở cùng mạng WiFi với Raspberry Pi
- Sau khi bind, device chỉ có thể truy cập bởi tài khoản đó
- Để đổi chủ sở hữu, phải unbind device trước
- Camera stream chỉ hoạt động khi đã bind device

