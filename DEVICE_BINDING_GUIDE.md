# Hướng dẫn kết nối thiết bị Raspberry Pi với App

## Tổng quan
Hệ thống cho phép mỗi tài khoản Google kết nối với 1 thiết bị Raspberry Pi duy nhất. Sau khi kết nối, tài khoản có thể xem camera stream và chụp ảnh phát hiện tôm trực tiếp từ màn hình Home.

## Quy trình kết nối

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

