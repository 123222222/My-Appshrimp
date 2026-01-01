**Backend:**
- ✅ Camera buffer size = 1 (giảm latency)
- ✅ MJPEG format (nén real-time tốt)
- ✅ Threading cho camera stream
- ✅ TFLite FP16 (nhanh hơn FP32)
- ✅ NMS (Non-Maximum Suppression) để loại bỏ duplicate detections

**Android:**
- ✅ Coil image loading với disk cache
- ✅ LazyColumn cho Gallery (lazy loading)
- ✅ ViewModel để persist data qua config changes
- ✅ Coroutines cho async operations
- ✅ Hilt Dependency Injection (fast startup)

**Network:**
- ✅ Cloudinary CDN (tốc độ tải ảnh nhanh)
- ✅ MongoDB Atlas (low latency queries)
- ✅ Firebase Authentication (distributed auth)
- ✅ Keep-Alive connections

### 3. Scalability

**Horizontal Scaling:**
```
[App 1]  ──┐
[App 2]  ──┼──► [Load Balancer] ──┬──► [Backend 1] → [MongoDB]
[App 3]  ──┘                       ├──► [Backend 2] → [Cloudinary]
                                   └──► [Backend 3]
```

**Khả năng mở rộng:**
- Hỗ trợ thêm nhiều Raspberry Pi (1 app → nhiều devices)
- Có thể deploy backend trên cloud (AWS, GCP, Azure)
- MongoDB Atlas có thể scale tự động
- Cloudinary xử lý được hàng triệu ảnh

---

## 🎓 IX. ĐẶC ĐIỂM NÔI BẬT CHO KHÓA LUẬN

### 1. Tính độc đáo
- ✅ **Multi-User Independent Device Binding**: Hệ thống đầu tiên cho phép nhiều user bind vào cùng 1 thiết bị mà không ảnh hưởng lẫn nhau
- ✅ **Real-time Size & Weight Estimation**: Tích hợp công thức sinh học để ước tính kích thước tôm
- ✅ **Hybrid Authentication**: Hỗ trợ cả Google OAuth và Phone Authentication

### 2. Công nghệ hiện đại
- ✅ **AI/ML**: YOLOv8 TFLite (Edge AI)
- ✅ **Cloud-Native**: Firebase + MongoDB Atlas + Cloudinary
- ✅ **Mobile-First**: Jetpack Compose (UI hiện đại nhất của Android)
- ✅ **Microservices-ready**: REST API architecture

### 3. Ứng dụng thực tế
- ✅ Giải quyết bài toán thực tế trong nuôi trồng thủy sản
- ✅ Giảm công sức giám sát thủ công
- ✅ Tự động hóa việc đếm và đo kích thước tôm
- ✅ Lưu trữ lịch sử để phân tích xu hướng

### 4. Khả năng mở rộng
- ✅ Dễ dàng thêm classes mới (fish, crab...)
- ✅ Có thể tích hợp IoT sensors (pH, temperature, oxygen)
- ✅ Mở rộng sang web dashboard
- ✅ Thêm tính năng auto-scheduling feed

---

## 📚 X. TÀI LIỆU THAM KHẢO

### Công nghệ sử dụng
1. **YOLOv8**: Ultralytics - You Only Look Once v8
2. **TensorFlow Lite**: Google - Machine Learning on Edge Devices
3. **Firebase Authentication**: Google - Identity Platform
4. **MongoDB**: Document Database for Modern Applications
5. **Cloudinary**: Cloud-based Image Management
6. **Jetpack Compose**: Android Modern UI Toolkit
7. **Flask**: Python Micro Web Framework

### Nghiên cứu liên quan
1. **Length-Weight Relationship**: Nghiên cứu về tôm thẻ chân trắng (Litopenaeus vannamei)
   - Công thức: W = 0.0065 × L^3.1
2. **Computer Vision in Aquaculture**: Các nghiên cứu về ứng dụng AI trong nuôi trồng thủy sản
3. **Edge AI**: Triển khai Machine Learning trên thiết bị nhúng

---

## 📞 XI. THÔNG TIN HỆ THỐNG

### Cấu hình khuyến nghị

**Raspberry Pi:**
- Model: Raspberry Pi 4 (4GB RAM trở lên)
- OS: Raspberry Pi OS (64-bit)
- Camera: USB/CSI Camera (1080p)
- Storage: 32GB SD Card
- Network: WiFi 802.11ac hoặc Ethernet

**Android Device:**
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- RAM: 2GB+
- Storage: 100MB+ available

**Network:**
- Bandwidth: 5 Mbps+ (để streaming mượt)
- Latency: < 50ms trong LAN
- WiFi: 2.4GHz hoặc 5GHz

### Admin Contact
- **Email**: hodung15032003@gmail.com
- **Phone**: +84987648717
- **Device ID**: raspberrypi-001

---

## ✅ XII. KẾT LUẬN

Hệ thống nhận diện tôm tự động đã được thiết kế và triển khai thành công với đầy đủ các tính năng:

1. **Xác thực bảo mật** qua Firebase Authentication
2. **Phát hiện AI** với độ chính xác cao (confidence > 60%)
3. **Multi-user support** với device binding độc lập
4. **Real-time streaming** với latency thấp
5. **Cloud storage** với Cloudinary và MongoDB
6. **Admin panel** để quản lý người dùng
7. **Mobile app** với UI hiện đại

Hệ thống có thể **ứng dụng thực tế** trong các trang trại nuôi tôm, giúp **tự động hóa** quy trình giám sát và **tối ưu hóa** hiệu quả sản xuất.

---

**📅 Ngày tạo**: 27/12/2025  
**👨‍💻 Tác giả**: Hồ Dũng  
**🎓 Mục đích**: Khóa luận tốt nghiệp  
**🔖 Version**: 2.0 (Multi-User System)
# 🦐 KIẾN TRÚC HỆ THỐNG NHẬN DIỆN TÔM TỰ ĐỘNG
## Dành cho Khóa Luận Tốt Nghiệp

---

## 📋 I. TỔNG QUAN HỆ THỐNG

Hệ thống nhận diện tôm tự động là một giải pháp IoT toàn diện, tích hợp AI/ML, Cloud Computing và Mobile App, phục vụ cho ngành nuôi trồng thủy sản. Hệ thống cho phép giám sát, phát hiện và phân tích tôm tự động thông qua camera và trí tuệ nhân tạo.

### Thành phần chính:
1. **Ứng dụng Android** - Giao diện người dùng
2. **Backend Server** - Xử lý AI và quản lý hệ thống
3. **Firebase** - Xác thực người dùng
4. **MongoDB** - Cơ sở dữ liệu
5. **Cloudinary** - Lưu trữ hình ảnh
6. **YOLO AI Model** - Nhận diện tôm

---

## 🏗️ II. KIẾN TRÚC HỆ THỐNG

```
┌─────────────────────────────────────────────────────────────────┐
│                     📱 ANDROID APP (Kotlin)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │  Google      │  │  Camera      │  │  Gallery &         │    │
│  │  Sign-In     │  │  Stream      │  │  Statistics        │    │
│  └──────────────┘  └──────────────┘  └────────────────────┘    │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/HTTPS + Firebase ID Token
                             │
┌────────────────────────────▼────────────────────────────────────┐
│              🐍 FLASK BACKEND SERVER (Python)                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  🔐 Authentication Layer (Firebase Admin SDK)           │   │
│  │  • Verify ID Token • Check Permission • Role Management │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐    │
│  │ 📹 Camera    │  │ 🤖 YOLO AI   │  │ 📊 API Routes    │    │
│  │ • MJPEG      │  │ • TFLite     │  │ • REST APIs      │    │
│  │ • Streaming  │  │ • Detection  │  │ • Device Mgmt    │    │
│  └──────────────┘  └──────────────┘  └──────────────────┘    │
└────────┬──────────────┬──────────────────┬─────────────────────┘
         │              │                  │
    ┌────▼────┐   ┌─────▼──────┐   ┌──────▼────────┐
    │🔥Firebase│   │☁️Cloudinary│   │🗄️MongoDB     │
    │ Auth     │   │  Images    │   │  Metadata    │
    └──────────┘   └────────────┘   └──────────────┘
```

---

## 🔐 III. HỆ THỐNG XÁC THỰC VÀ PHÂN QUYỀN

### 1. Quy trình xác thực (Authentication Flow)

```
User Login → Google OAuth 2.0 → Firebase Authentication 
           → ID Token → Backend Verification → Access Granted
```

**Chi tiết:**
1. User đăng nhập bằng Google trên Android App
2. Firebase Authentication tạo ID Token (JWT)
3. App gửi ID Token kèm theo mọi request đến Backend
4. Backend verify token qua Firebase Admin SDK
5. Kiểm tra email trong whitelist (`permitted_emails.json`)
6. Cấp quyền truy cập dựa trên role (Admin/User)

### 2. Hệ thống phân quyền (Authorization)

**Admin (hodung15032003@gmail.com):**
- ✅ Quản lý danh sách người dùng được phép
- ✅ Thêm/xóa email vào whitelist
- ✅ Xem logs và thống kê hệ thống
- ✅ Toàn quyền truy cập camera và detection

**User (được admin cấp phép):**
- ✅ Xem camera stream
- ✅ Chụp ảnh và nhận diện tôm
- ✅ Xem gallery và thống kê cá nhân
- ✅ Bind/unbind thiết bị của riêng mình

**Unpermitted User:**
- ❌ Không thể truy cập hệ thống
- ❌ Nhận thông báo: "Tài khoản chưa được cấp quyền"

### 3. Device Binding - Multi-User System

Hệ thống cho phép nhiều user bind vào cùng một Raspberry Pi **độc lập**:

```json
{
  "admin@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1735291200
  },
  "user1@gmail.com": {
    "device_id": "raspberrypi-001",  // Cùng device!
    "ip": "192.168.1.100",
    "last_updated": 1735291300
  }
}
```

**Ưu điểm:**
- Mỗi user có binding riêng
- Unbind của user này không ảnh hưởng user khác
- Nhiều user có thể cùng xem camera một thiết bị

---

## 🔄 IV. QUY TRÌNH HOẠT ĐỘNG CHÍNH

### 1. Device Discovery (UDP Broadcast)

```
[Android App] --UDP Broadcast "DISCOVER_RASP"--> [Raspberry Pi]
[Android App] <--UDP Reply "RASP_HERE:device_id"-- [Raspberry Pi]
      ↓
[App hiển thị danh sách devices]
      ↓
[User chọn device và bind]
```

**Kỹ thuật:**
- UDP Broadcast trên port 50000
- Hoạt động trong cùng mạng WiFi/LAN
- Timeout 2 giây
- Tự động phát hiện tất cả Raspberry Pi trong mạng

### 2. Camera Streaming

```
[Android] --GET /blynk_feed--> [Backend]
                                    ↓
                            [Camera Capture]
                                    ↓
                            [MJPEG Encoding]
                                    ↓
[Android] <--Multipart Stream--- [Backend]
```

**Thông số kỹ thuật:**
- Format: MJPEG (Motion JPEG)
- Resolution: 640x480 pixels
- FPS: 30 frames/second
- Latency: ~200-300ms
- Bandwidth: ~1-2 Mbps

### 3. AI Detection Workflow

```
[User nhấn Capture] 
       ↓
[Gửi frame tới Backend] --POST /api/detect-shrimp-->
       ↓
[Tiền xử lý ảnh] (resize, normalize)
       ↓
[YOLO TFLite Model] (inference ~2-3 giây)
       ↓
[Phát hiện tôm] (bounding boxes, confidence)
       ↓
[Tính toán] 
  • Chiều dài tôm (pixel → cm)
  • Khối lượng (công thức W = a × L^b)
       ↓
[Vẽ bounding boxes] + [Gán nhãn]
       ↓
[Upload Cloudinary] (lưu ảnh kết quả)
       ↓
[Lưu MongoDB] (metadata: vị trí, confidence, size, weight)
       ↓
[Trả về kết quả] --JSON--> [Android App]
       ↓
[Hiển thị ảnh với bounding boxes trong Gallery]
```

**Chi tiết công thức tính toán:**

```python
# Tính chiều dài (dựa vào độ cao camera 20cm)
length_cm = max(bbox_width, bbox_height) × 0.05

# Tính khối lượng (công thức sinh học tôm)
weight_gram = 0.0065 × (length_cm)^3.1
```

**Thông số AI:**
- Model: YOLOv8 TFLite (FP16)
- Input: 128×128 hoặc 320×320 pixels
- Confidence threshold: 60%
- IoU threshold: 60%
- Class: "shrimp" (1 class)
- Inference time: 2-5 giây (Raspberry Pi 4)

---

## 💾 V. QUẢN LÝ DỮ LIỆU

### 1. MongoDB Schema

**Collection: `detections`**
```json
{
  "_id": "ObjectId(...)",
  "user_email": "user@gmail.com",
  "image_url": "https://res.cloudinary.com/...",
  "timestamp": "2025-12-27T10:30:00+07:00",
  "shrimp_count": 3,
  "detections": [
    {
      "className": "shrimp",
      "confidence": 0.87,
      "bbox": {"x": 320, "y": 240, "width": 80, "height": 120},
      "length_cm": 6.0,
      "weight_gram": 1.41
    }
  ],
  "device_id": "raspberrypi-001"
}
```

**Indexes:**
- `user_email` (tăng tốc query theo user)
- `timestamp` (sắp xếp thời gian)
- `device_id` (filter theo thiết bị)

### 2. Cloudinary Storage

**Folder structure:**
```
/shrimp_detection/
  ├── 2025-12-27_103000_shrimp.jpg
  ├── 2025-12-27_103015_shrimp.jpg
  └── ...
```

**Tối ưu hóa:**
- Auto upload với quality=auto
- Format: JPEG (nén tối ưu)
- Thumbnail tự động: 300x300px
- CDN delivery (tốc độ cao toàn cầu)

### 3. Local Files (Backend)

```
backend/
├── permitted_emails.json      # Whitelist emails
├── permitted_phones.json      # Whitelist phone numbers
└── permitted_devices.json     # Device bindings (user → device)
```

---

## 🛡️ VI. BẢO MẬT HỆ THỐNG

### 1. Authentication Layers

```
┌─────────────────────────────────────────────────┐
│ Layer 1: Firebase Authentication (OAuth 2.0)   │
│ • Google Sign-In                                │
│ • Phone Authentication                          │
│ • JWT Token với expiry                          │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ Layer 2: Backend Token Verification             │
│ • Firebase Admin SDK verify token               │
│ • Extract email/phone from token                │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ Layer 3: Permission Whitelist                   │
│ • Check email in permitted_emails.json          │
│ • Check phone in permitted_phones.json          │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ Layer 4: Role-Based Access Control (RBAC)      │
│ • Admin: Full permissions                       │
│ • User: Limited permissions                     │
└─────────────────────────────────────────────────┘
```

### 2. API Security

**Tất cả API endpoints (trừ /health) yêu cầu:**
- `Authorization` header với Firebase ID Token
- Token phải còn hạn (expiry check)
- Email/Phone trong whitelist

**Example request:**
```http
POST /api/detect-shrimp HTTP/1.1
Host: 192.168.1.100:8000
Authorization: eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmE...
Content-Type: application/json

{
  "image": "base64_encoded_image_data",
  "device_id": "raspberrypi-001"
}
```

### 3. Data Protection

| Dữ liệu | Bảo mật |
|---------|---------|
| Firebase Credentials | ✅ File `.json` không commit vào Git |
| Cloudinary API Key | ✅ Stored in `.env`, not hardcoded |
| MongoDB URI | ✅ Connection string in `.env` |
| Camera Stream | ✅ Yêu cầu authentication |
| User Emails | ✅ Whitelist-based, không public |

---

## 📊 VII. TÍNH NĂNG NÂNG CAO

### 1. Real-time Statistics

```
┌─────────────────────────────────────┐
│  📊 Chart Screen                    │
├─────────────────────────────────────┤
│  • Số lượng tôm theo ngày/tuần/tháng│
│  • Độ tin cậy trung bình            │
│  • Kích thước trung bình            │
│  • Khối lượng ước tính tổng         │
│  • Biểu đồ line chart               │
└─────────────────────────────────────┘
```

**Công nghệ:**
- Android: Vico Chart Library
- Backend: MongoDB Aggregation Pipeline
- Real-time update mỗi 5 giây

### 2. Multi-Device Management

User có thể:
- ✅ Scan nhiều Raspberry Pi trong mạng
- ✅ Bind vào bất kỳ device nào
- ✅ Unbind và bind lại device khác
- ✅ Xem lịch sử detection từ tất cả devices đã bind

**Persistence:**
- Android: SharedPreferences lưu `device_id` và `ip`
- Backend: `permitted_devices.json` mapping user → device

### 3. Image Gallery

```
┌──────┬──────┬──────┐
│ IMG1 │ IMG2 │ IMG3 │  ← Grid layout 2 columns
├──────┼──────┼──────┤
│ IMG4 │ IMG5 │ IMG6 │
└──────┴──────┴──────┘

Mỗi ảnh hiển thị:
• Thumbnail
• Số lượng tôm
• Thời gian chụp
• Click → Full screen detail
```

**Chi tiết ảnh:**
- Ảnh gốc với bounding boxes
- Danh sách detections (confidence, size, weight)
- Thời gian chụp (định dạng "27/12/2025 10:30")
- Device ID đã chụp

### 4. Admin Panel

```
┌─────────────────────────────────────┐
│  👑 Admin Panel                     │
├─────────────────────────────────────┤
│  📧 Quản lý Email Permissions       │
│  ┌─────────────────────────────┐   │
│  │ user1@gmail.com       [Xóa] │   │
│  │ user2@gmail.com       [Xóa] │   │
│  └─────────────────────────────┘   │
│  [+ Thêm email mới]                 │
│                                     │
│  📱 Quản lý Phone Permissions       │
│  ┌─────────────────────────────┐   │
│  │ +84987654321          [Xóa] │   │
│  └─────────────────────────────┘   │
│  [+ Thêm số điện thoại]             │
└─────────────────────────────────────┘
```

**API Endpoints:**
- `POST /api/admin/add-email`
- `DELETE /api/admin/remove-email`
- `POST /api/admin/add-phone`
- `DELETE /api/admin/remove-phone`
- `GET /api/admin/list-users`

---

## 🚀 VIII. HIỆU NĂNG & TỐI ƯU HÓA

### 1. Performance Metrics

| Component | Metric | Value |
|-----------|--------|-------|
| Camera Stream | Latency | 200-300ms |
| AI Inference | Time | 2-5 seconds |
| Image Upload | Time | 1-2 seconds |
| API Response | Time | 100-500ms |
| App Load | Time | < 2 seconds |

### 2. Optimization Techniques


