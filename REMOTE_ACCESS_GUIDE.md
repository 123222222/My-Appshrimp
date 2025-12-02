# 📡 Hướng dẫn sử dụng hệ thống Remote Access qua Ngrok

## 🎯 Mục tiêu

Sau khi kết nối thiết bị Raspberry Pi lần đầu (cần cùng mạng WiFi), user có thể:
- ✅ Truy cập từ bất kỳ mạng nào (4G, WiFi khác, v.v.)
- ✅ Xem camera stream realtime qua Ngrok
- ✅ Chụp ảnh và detect tôm
- ✅ Xem gallery, chart
- ✅ Chỉ cần đăng nhập Google

## 🔄 Flow hoạt động

### 1️⃣ Lần đầu tiên (Setup - Cần cùng mạng WiFi)

```
User                    App                     Raspberry Pi           Backend (Ngrok)
  │                      │                            │                        │
  │── Đăng nhập Google ──→│                            │                        │
  │                      │── Firebase Token ──────────┼───────────────────────→│
  │                      │                            │                        │
  │── Vào Profile ───────→│                            │                        │
  │                      │                            │                        │
  │── Nhấn "Quét thiết bị"│                            │                        │
  │                      │── UDP Broadcast ──────────→│                        │
  │                      │   "DISCOVER_RASP"          │                        │
  │                      │                            │                        │
  │                      │←─ UDP Response ────────────│                        │
  │                      │   "raspberrypi-001"        │                        │
  │                      │   (192.168.1.100)          │                        │
  │                      │                            │                        │
  │← Hiển thị list device─│                            │                        │
  │                      │                            │                        │
  │── Click để kết nối ──→│                            │                        │
  │                      │                            │                        │
  │                      │── Lưu local: ──────────────┼────────────────────────┤
  │                      │   rasp_ip: 192.168.1.100   │                        │
  │                      │   rasp_device_id: rasp-001 │                        │
  │                      │                            │                        │
  │                      │── POST /api/devices/bind ──┼───────────────────────→│
  │                      │   + Firebase Token         │                        │
  │                      │   + device_id              │                        │
  │                      │                            │                        │
  │                      │←─ 200 OK ──────────────────┼────────────────────────┤
  │                      │   Device bound!            │                        │
  │                      │                            │                        │
  │← "Kết nối thành công"│                            │                        │
```

**Lưu trữ:**
- **App (SharedPreferences)**: 
  - `rasp_ip`: `192.168.1.100` (chỉ để tham khảo, không dùng nữa)
  - `rasp_device_id`: `raspberrypi-001` (quan trọng!)
  - `idToken`: Firebase token

- **Backend (permitted_devices.json)**:
  ```json
  {
    "raspberrypi-001": "hodung15032003@gmail.com"
  }
  ```

### 2️⃣ Lần sau (Remote Access - Không cần cùng mạng)

```
User (Mạng 4G)          App                     Backend (Ngrok)
  │                      │                            │
  │── Đăng nhập Google ──→│                            │
  │                      │── Refresh Firebase Token ──→│
  │                      │                            │
  │                      │← Verify token + email ─────┤
  │                      │                            │
  │── Vào Home ──────────→│                            │
  │                      │                            │
  │                      │── Check device_id exists? ─┤
  │                      │   ✅ Yes: raspberrypi-001  │
  │                      │                            │
  │                      │── GET https://xxx.ngrok-free.dev/blynk_feed
  │                      │                            │
  │                      │←─ MJPEG Stream ────────────┤
  │← Camera hiển thị ────┤   (Realtime video)         │
  │                      │                            │
  │── Nhấn nút chụp ─────→│                            │
  │                      │                            │
  │                      │── POST /api/detect-shrimp ─→│
  │                      │   + Base64 image           │
  │                      │   + Firebase Token         │
  │                      │                            │
  │                      │                            │── Verify token
  │                      │                            │── Check device permission
  │                      │                            │── Run YOLO detection
  │                      │                            │── Upload Cloudinary
  │                      │                            │── Save MongoDB
  │                      │                            │
  │                      │←─ Detection result ─────────┤
  │← Hiển thị ảnh + bbox ┤   (Image URL + detections) │
```

## 🔐 Security Flow

### Authentication & Authorization

```python
# Backend: app_complete.py

@app.route('/api/detect-shrimp', methods=['POST'])
@requires_google_auth  # Decorator kiểm tra token
def detect_shrimp():
    # 1. Verify Firebase ID Token
    id_token = request.headers.get('Authorization')
    decoded = firebase_auth.verify_id_token(id_token)
    email = decoded.get('email')
    
    # 2. Check email in permitted list
    if email not in load_permitted_emails():
        return 403  # Forbidden
    
    # 3. Process image
    # ...
```

### Device Binding

```python
# permitted_devices.json
{
    "raspberrypi-001": "hodung15032003@gmail.com",
    "raspberrypi-002": "user2@gmail.com"
}

# Mỗi device chỉ bind với 1 email
# Mỗi email có thể bind nhiều devices (nếu muốn)
```

## 🌐 Ngrok Configuration

### Start Ngrok on Raspberry Pi

```bash
# backend/start_server.sh

# 1. Start Flask server
python3 app_complete.py &

# 2. Start Ngrok
ngrok http 8000
```

### Ngrok URL

```
https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev
├── /blynk_feed          # Camera stream (MJPEG)
├── /api/detect-shrimp   # Image detection
├── /api/shrimp-images   # Gallery
└── /health              # Health check
```

**Đặc điểm:**
- ✅ Accessible từ Internet
- ✅ HTTPS tự động
- ✅ Không cần port forwarding
- ✅ Không cần static IP
- ⚠️ URL thay đổi mỗi lần restart (dùng paid plan để fix)

## 📱 Android App Configuration

### HomeScreen.kt

```kotlin
// Dùng Ngrok URL thay vì local IP
val streamUrl = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev/blynk_feed"

// Không cần check raspIp nữa, chỉ cần check raspDeviceId
LaunchedEffect(raspDeviceId) {
    if (raspDeviceId != null) {
        // Load stream từ Ngrok
        connectToStream(streamUrl)
    }
}
```

### ShrimpApiService.kt

```kotlin
// Backend URL dùng Ngrok
private val BACKEND_URL = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev"

suspend fun processImage(bitmap: Bitmap): Result<...> {
    // Auto refresh Firebase token
    val freshToken = getFreshToken()
    
    // POST to Ngrok URL
    val request = Request.Builder()
        .url("$BACKEND_URL/api/detect-shrimp")
        .addHeader("Authorization", freshToken)
        .post(...)
        .build()
}
```

## 🔧 Troubleshooting

### Issue 1: Ngrok URL expired
**Triệu chứng**: Camera không load, API error 404

**Giải pháp**:
1. Restart ngrok trên Raspberry Pi
2. Copy URL mới từ ngrok terminal
3. Update trong code:
   ```kotlin
   // HomeScreen.kt & ShrimpApiService.kt
   val NGROK_URL = "https://NEW-URL.ngrok-free.dev"
   ```
4. Rebuild app

**Giải pháp lâu dài**: Dùng Ngrok paid plan → Static domain

### Issue 2: "Missing Google ID token"
**Triệu chứng**: API calls bị 401 Unauthorized

**Giải pháp**:
- Token đã hết hạn (>1 giờ)
- App tự động refresh token trước mỗi API call
- Nếu vẫn lỗi → Đăng xuất/đăng nhập lại

### Issue 3: "Device not bound"
**Triệu chứng**: Home screen hiển thị "Chưa kết nối thiết bị"

**Giải pháp**:
1. Vào Profile screen
2. Kiểm tra device binding status
3. Nếu chưa bind → Quét và kết nối lại (cần cùng WiFi)

### Issue 4: Camera lag hoặc không smooth
**Nguyên nhân**: Network latency qua Ngrok

**Giải pháp**:
- Giảm quality stream trong backend:
  ```python
  cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 60])
  ```
- Giảm resolution camera:
  ```python
  camera.set(cv2.CAP_PROP_FRAME_WIDTH, 480)
  camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 360)
  ```

## 📊 Data Flow Summary

```
┌─────────────────────┐
│   Android App       │
│  (Any Network)      │
└──────────┬──────────┘
           │
           │ HTTPS
           │ (Ngrok Tunnel)
           ▼
┌─────────────────────┐
│   Ngrok Server      │
│   (Cloud)           │
└──────────┬──────────┘
           │
           │ HTTP (Local)
           ▼
┌─────────────────────┐
│  Raspberry Pi       │
│  Flask Server       │
│  + Camera           │
│  + YOLO Model       │
│  (Home Network)     │
└─────────────────────┘
```

**Lợi ích:**
- ✅ User không cần biết IP của Raspberry Pi
- ✅ Không cần cấu hình router/firewall
- ✅ Truy cập từ bất kỳ đâu trên thế giới
- ✅ HTTPS bảo mật
- ✅ Chỉ người có tài khoản Google được phép mới truy cập

## 🎯 Checklist Setup

### Lần đầu (Admin setup)

- [ ] Raspberry Pi kết nối WiFi
- [ ] Cài đặt backend (`python3 app_complete.py`)
- [ ] Start Ngrok (`ngrok http 8000`)
- [ ] Copy Ngrok URL
- [ ] Update URL trong Android app code
- [ ] Build và install app
- [ ] **Cùng mạng WiFi** với Raspberry Pi
- [ ] Vào Profile → Quét thiết bị
- [ ] Chọn device để bind
- [ ] Kiểm tra "Đã kết nối" hiển thị

### Lần sau (User usage - Từ xa)

- [ ] Đăng nhập Google
- [ ] Vào Home → Camera tự động hiển thị
- [ ] Nhấn nút camera để chụp
- [ ] Xem kết quả detection
- [ ] Vào Gallery để xem lịch sử
- [ ] Vào Chart để xem thống kê

## 💡 Best Practices

1. **Ngrok URL Management**:
   - Lưu URL vào file config hoặc Remote Config (Firebase)
   - Để user có thể update mà không cần rebuild app

2. **Token Refresh**:
   - Đã implement auto-refresh trước mỗi API call
   - User không cần lo token expire

3. **Error Handling**:
   - Hiển thị message rõ ràng
   - Có nút "Thử lại" khi lỗi
   - Log để debug

4. **Security**:
   - Mỗi device bind với 1 email
   - Firebase token verify mọi request
   - Không hardcode credentials

---

✅ **Hệ thống đã sẵn sàng cho remote access!**

User chỉ cần:
1. Lần đầu: Cùng WiFi để quét và bind device
2. Lần sau: Đăng nhập Google là dùng được, không cần cùng mạng! 🚀

