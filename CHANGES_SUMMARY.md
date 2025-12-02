# Tóm tắt các thay đổi - Device Binding System

## 📋 Tổng quan
Đã cập nhật hệ thống để cho phép mỗi tài khoản Google kết nối với 1 thiết bị Raspberry Pi duy nhất. Sau khi kết nối, camera stream tự động hiển thị ở Home screen mà không cần quét lại.

## 🔧 Thay đổi Backend (Python Flask)

### File: `backend/app_complete.py`

#### 1. Thêm Logging Setup
```python
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
```
- Thêm logging để debug authentication và device binding

#### 2. Cập nhật Device Management System
- **Thay đổi cấu trúc dữ liệu**: Từ `{email: [device_ids]}` → `{device_id: email_owner}`
- **Lý do**: Đảm bảo 1 device chỉ bind với 1 tài khoản

#### 3. API Endpoints mới/cập nhật:

**a) `/api/devices/bind` (POST)**
- Bind thiết bị với tài khoản hiện tại
- Kiểm tra nếu device đã bind cho user khác → từ chối
- Nếu đã bind cho chính user → trả về success
- Lưu vào `permitted_devices.json`

**b) `/api/devices/check` (POST)**
- Kiểm tra quyền truy cập device
- Verify user có phải owner không

**c) `/api/devices/my-device` (GET)**
- Lấy thông tin device đã bind với user hiện tại
- Trả về device_id hoặc null nếu chưa bind

**d) `/api/devices/unbind` (POST)**
- Hủy bind device
- Chỉ owner mới được phép unbind

**e) `/api/devices/access-token` (POST)** - Deprecated
- Giữ lại để tương thích ngược
- Khuyến nghị dùng `/api/devices/bind` thay thế

#### 4. File lưu trữ
- `permitted_devices.json`: Lưu mapping `{device_id: email_owner}`
```json
{
  "raspberrypi-001": "user@gmail.com"
}
```

## 📱 Thay đổi Frontend (Kotlin Android)

### File: `app/src/main/java/com/dung/myapplication/mainUI/profile/ProfileScreen.kt`

#### Viết lại hoàn toàn ProfileScreen với các tính năng:

1. **Kiểm tra device binding khi load**
   - Gọi API `/api/devices/my-device` để check xem đã bind chưa
   - Hiển thị trạng thái kết nối

2. **Quét thiết bị (UDP Broadcast)**
   - Nút "Quét thiết bị" để tìm Raspberry Pi trong LAN
   - Gửi broadcast "DISCOVER_RASP" qua UDP port 50000
   - Hiển thị danh sách thiết bị tìm được (IP + Device ID)

3. **Kết nối thiết bị**
   - User chọn device từ danh sách
   - Lưu vào SharedPreferences: `rasp_ip`, `rasp_device_id`
   - Gọi API `/api/devices/bind` để bind với backend
   - Hiển thị toast thông báo kết quả

4. **Hủy kết nối**
   - Nút "Hủy kết nối" để unbind device
   - Gọi API `/api/devices/unbind`
   - Xóa thông tin khỏi SharedPreferences

5. **UI/UX cải tiến**
   - Card hiển thị thông tin tài khoản
   - Card quản lý thiết bị với icon và màu sắc
   - Loading indicator khi quét
   - Status badge cho trạng thái kết nối

### File: `app/src/main/java/com/dung/myapplication/mainUI/home/HomeScreen.kt`

#### Viết lại hoàn toàn HomeScreen với logic mới:

1. **Kiểm tra binding status**
   - Đọc `rasp_ip` và `rasp_device_id` từ SharedPreferences
   - Nếu chưa có → hiển thị thông báo "Chưa kết nối thiết bị"
   - Nếu có → tự động load camera stream

2. **Tự động load camera stream**
   - LaunchedEffect tự động kết nối khi có device info
   - Đọc MJPEG stream từ `http://{rasp_ip}:8000/blynk_feed`
   - Parse JPEG frames và hiển thị realtime
   - Xử lý lỗi và hiển thị thông báo

3. **Chụp ảnh và phát hiện**
   - Nút FAB (Floating Action Button) với icon camera
   - Khi nhấn: capture frame hiện tại → gửi lên backend
   - Hiển thị ảnh kết quả với bounding boxes
   - Badge hiển thị số lượng tôm phát hiện
   - Tự động quay về stream sau 5 giây

4. **Xử lý trạng thái**
   - Loading: hiển thị CircularProgressIndicator
   - Error: hiển thị message + nút "Thử lại"
   - Processing: overlay với progress indicator
   - Success: hiển thị detected image

5. **UI Components**
   - Fullscreen camera view
   - FAB button ở bottom center
   - Processing overlay với text progress
   - Detection result badge

## 🔄 Flow hoạt động mới

### 1. Lần đầu sử dụng:
```
User đăng nhập 
  → Vào Profile
  → Quét thiết bị
  → Chọn device để kết nối
  → Backend lưu binding
  → Về Home → Camera tự động hiển thị
```

### 2. Lần sau:
```
User đăng nhập
  → Vào Home
  → Camera tự động load (không cần quét)
  → Nhấn nút camera để chụp
  → Xem kết quả phát hiện
```

## 🔐 Security Flow

### Authentication
```
Android App → Firebase Auth → ID Token
  → Gửi trong header "Authorization"
  → Backend verify token qua Firebase Admin SDK
  → Extract email từ token
```

### Device Binding
```
User A bind device_001
  → Backend lưu: { "device_001": "userA@gmail.com" }
User B cố bind device_001
  → Backend check owner
  → Reject vì device đã thuộc User A
```

## 📊 Data Flow

### Device Discovery
```
App → UDP Broadcast "DISCOVER_RASP"
  → Raspberry Pi reply với Device ID
  → App nhận IP + Device ID
  → Hiển thị danh sách
```

### Camera Streaming
```
App → HTTP GET http://{ip}:8000/blynk_feed
  → Server → MJPEG stream
  → App parse JPEG frames
  → Display bitmap realtime
```

### Image Detection
```
App capture frame
  → Convert to Base64
  → POST /api/detect-shrimp với idToken
  → Backend verify token
  → Run YOLO detection
  → Upload to Cloudinary
  → Save to MongoDB
  → Return result với bounding boxes
```

## 📝 Các file quan trọng

### Backend
- `app_complete.py`: Main server file
- `permitted_devices.json`: Device binding storage
- `firebase-admin.json`: Firebase credentials
- `.env`: Environment variables

### Android
- `ProfileScreen.kt`: Device scanning & binding UI
- `HomeScreen.kt`: Camera stream & capture UI
- `ShrimpApiService.kt`: API client
- SharedPreferences "auth": Local storage (ip, device_id, idToken)

## ✅ Testing Checklist

### Backend
- [ ] Server khởi động thành công
- [ ] UDP responder hoạt động (port 50000)
- [ ] Firebase authentication hoạt động
- [ ] API `/api/devices/bind` hoạt động đúng
- [ ] API `/api/devices/my-device` trả về đúng data
- [ ] API `/api/devices/unbind` hoạt động
- [ ] Camera stream `/blynk_feed` hoạt động
- [ ] API `/api/detect-shrimp` xử lý ảnh đúng

### Android App
- [ ] Login với Google thành công
- [ ] Quét thiết bị tìm thấy Raspberry Pi
- [ ] Bind device thành công
- [ ] Thông tin device hiển thị đúng ở Profile
- [ ] Home screen tự động load camera
- [ ] Camera stream hiển thị smooth
- [ ] Nút chụp ảnh hoạt động
- [ ] Kết quả detection hiển thị đúng
- [ ] Unbind device hoạt động
- [ ] Sau unbind, Home screen hiển thị "Chưa kết nối"

## 🚀 Deployment

### Backend (Raspberry Pi)
```bash
cd backend
python3 app_complete.py
```
Server sẽ:
- Chạy Flask server trên port 8000
- Khởi động UDP responder trên port 50000
- Load camera và AI model

### Android App
1. Build APK: `./gradlew assembleDebug`
2. Install: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Đảm bảo app và Raspberry Pi cùng mạng WiFi

## 🐛 Known Issues & Solutions

### Issue 1: UDP broadcast không hoạt động
**Nguyên nhân**: Firewall block port 50000
**Giải pháp**: Mở port UDP 50000 trên Raspberry Pi

### Issue 2: Camera stream lag
**Nguyên nhân**: Network bandwidth thấp
**Giải pháp**: Giảm resolution camera hoặc FPS

### Issue 3: Token expired
**Nguyên nhân**: Firebase ID token hết hạn (1 giờ)
**Giải pháp**: App tự động refresh token khi cần

### Issue 4: Device đã bind cho user khác
**Nguyên nhân**: Device chưa được unbind
**Giải pháp**: User cũ phải unbind hoặc admin xóa khỏi `permitted_devices.json`

## 📚 Documentation
- `DEVICE_BINDING_GUIDE.md`: Hướng dẫn chi tiết
- `ARCHITECTURE.md`: Kiến trúc hệ thống
- `BACKEND_API_DOCS.md`: API documentation

## 🎯 Next Steps (Future Improvements)
1. Thêm multiple camera support
2. Push notification khi phát hiện tôm
3. Historical data analytics
4. Device health monitoring
5. Cloud-based device management
6. QR code pairing thay vì UDP broadcast

