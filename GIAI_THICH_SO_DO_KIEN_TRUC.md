# 📊 GIẢI THÍCH SƠ ĐỒ KIẾN TRÚC HỆ THỐNG
## Phân tích chi tiết 3 lớp kiến trúc (3-Layer Architecture)

---

## 🎯 TỔNG QUAN SƠ ĐỒ

Hệ thống được thiết kế theo mô hình **3-Layer Architecture** (kiến trúc 3 lớp) gồm:

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER (Lớp Khách hàng)            │
│                      Android Application                     │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/HTTPS
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              APPLICATION LAYER (Lớp Ứng dụng)               │
│                   Flask Backend Server                       │
│   • Authentication Module                                    │
│   • Camera Streaming Module                                  │
│   • AI Detection Module                                      │
│   • Store & Database Module                                  │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  DATA LAYER (Lớp Dữ liệu)                   │
│      Camera Hardware | Cloudinary | MongoDB                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 I. CLIENT LAYER (Lớp Khách Hàng)

### Mô tả
Lớp giao diện người dùng, được xây dựng bằng **Android Application** (Kotlin + Jetpack Compose).

### Các thành phần chính:

#### 1. **Login** (Đăng nhập)
- **Chức năng**: Xác thực người dùng qua Google OAuth 2.0
- **Công nghệ**: Firebase Authentication
- **Output**: Firebase ID Token (JWT)
- **Luồng hoạt động**:
  ```
  User nhấn "Đăng nhập Google" 
    → Firebase Auth popup 
    → User chọn tài khoản 
    → Nhận ID Token 
    → Lưu token vào SharedPreferences
  ```

#### 2. **Camera Stream** (Xem camera trực tiếp)
- **Chức năng**: Hiển thị video real-time từ Raspberry Pi
- **Công nghệ**: 
  - MJPEG Streaming
  - Coil Image Loader (Android)
  - AsyncImage với placeholder
- **URL**: `http://192.168.1.100:8000/blynk_feed`
- **Latency**: 200-300ms

#### 3. **Gallery** (Thư viện ảnh)
- **Chức năng**: Hiển thị lịch sử ảnh đã chụp với kết quả nhận diện
- **Layout**: LazyColumn Grid (2 cột)
- **Data source**: MongoDB (qua API `/api/shrimp-images`)
- **Tính năng**:
  - Thumbnail preview
  - Số lượng tôm phát hiện
  - Thời gian chụp
  - Click để xem chi tiết

#### 4. **Profile** (Hồ sơ & quản lý thiết bị)
- **Chức năng**:
  - Hiển thị thông tin user (email, ảnh đại diện)
  - **Device Binding**: Quét và kết nối Raspberry Pi
  - Unbind device
  - Hiển thị device_id và IP hiện tại
- **Kỹ thuật quét**: UDP Broadcast Discovery

#### 5. **Chart** (Biểu đồ thống kê)
- **Chức năng**: Hiển thị thống kê phát hiện tôm
- **Công nghệ**: Vico Chart Library
- **Metrics**:
  - Số lượng tôm theo thời gian
  - Confidence trung bình
  - Kích thước trung bình
  - Trend analysis

#### 6. **Admin Panel** (Quản trị - chỉ Admin)
- **Chức năng**:
  - Thêm/xóa email vào whitelist
  - Thêm/xóa số điện thoại
  - Xem danh sách users
  - Quản lý permissions
- **Bảo mật**: Chỉ `hodung15032003@gmail.com` có quyền truy cập

### Giao tiếp với Application Layer:
- **Protocol**: HTTP/HTTPS
- **Authentication**: Bearer Token trong header
- **Format**: JSON (Request & Response)
- **Example**:
  ```http
  GET /api/shrimp-images?user_email=user@gmail.com
  Authorization: Bearer eyJhbGciOiJSUzI1Ni...
  ```

---

## 🐍 II. APPLICATION LAYER (Lớp Ứng Dụng)

### Mô tả
Lớp xử lý logic nghiệp vụ, được xây dựng bằng **Flask Backend Server** (Python 3.8+) chạy trên Raspberry Pi.

---

### 1️⃣ **Authentication Module** (Module Xác thực)

#### Thành phần:
- **Firebase Admin SDK**: Verify ID Token từ client
- **Token Verification**: Kiểm tra token hợp lệ và chưa hết hạn
- **Permission Management**: Quản lý whitelist (emails & phones)

#### Workflow:
```python
@requires_google_auth  # Decorator
def protected_endpoint():
    # 1. Extract token from Authorization header
    id_token = request.headers.get('Authorization')
    
    # 2. Verify token
    decoded_token = firebase_auth.verify_id_token(id_token)
    
    # 3. Extract email
    email = decoded_token.get('email')
    
    # 4. Check permission
    permitted_emails = load_permitted_emails()
    if email not in permitted_emails:
        return 403 Forbidden
    
    # 5. Grant access
    request.user_email = email
    return process_request()
```

#### File quản lý:
- `permitted_emails.json`: Danh sách email được phép
- `permitted_phones.json`: Danh sách phone được phép
- `permitted_devices.json`: Mapping user → device

#### Security:
- ✅ JWT Token expiry check
- ✅ Whitelist-based authorization
- ✅ Role-Based Access Control (RBAC)
- ✅ Logging mọi authentication events

---

### 2️⃣ **Camera Streaming Module** (Module phát Camera)

#### Thành phần:
- **OpenCV**: Capture frames từ USB/CSI camera
- **MJPEG Stream Handler**: Encode frames thành MJPEG
- **Frame Buffer Management**: Quản lý buffer (size=1 để giảm latency)

#### Workflow:
```python
def generate_frames():
    while True:
        with camera_lock:
            success, frame = camera.read()
            if not success:
                break
            
            # Encode frame to JPEG
            ret, buffer = cv2.imencode('.jpg', frame, 
                [cv2.IMWRITE_JPEG_QUALITY, 85])
            
            # Yield as multipart stream
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + 
                   buffer.tobytes() + b'\r\n')

@app.route('/blynk_feed')
def blynk_feed():
    return Response(generate_frames(),
        mimetype='multipart/x-mixed-replace; boundary=frame')
```

#### Thông số kỹ thuật:
- **Resolution**: 640×480 pixels
- **FPS**: 30 frames/second
- **Format**: MJPEG
- **Quality**: 85%
- **Buffer**: 1 frame (minimum latency)
- **Thread-safe**: Threading lock để tránh race condition

---

### 3️⃣ **AI Detection Module** (Module Nhận diện AI)

#### Thành phần:
- **YOLO Model (TensorFlow Lite)**: Model nhận diện tôm đã train
- **Image Preprocessing**: Tiền xử lý ảnh (resize, normalize)
- **Frame Buffer Management**: Quản lý ảnh đầu vào

#### Workflow Chi tiết:

```
📷 Nhận ảnh từ Client
         ↓
┌────────────────────────────────────┐
│ 1. TIỀN XỬ LÝ ẢNH                  │
│  • Decode base64 → numpy array     │
│  • Resize: 640×480 → 128×128       │
│  • Normalize: [0, 255] → [0, 1]    │
│  • Convert: BGR → RGB              │
│  • Expand dims: (128,128,3)        │
│               → (1,128,128,3)      │
└────────────────┬───────────────────┘
                 ↓
┌────────────────────────────────────┐
│ 2. YOLO INFERENCE                  │
│  • Load TFLite model               │
│  • interpreter.set_tensor()        │
│  • interpreter.invoke()            │
│  • Thời gian: 2-5 giây             │
└────────────────┬───────────────────┘
                 ↓
┌────────────────────────────────────┐
│ 3. PARSE OUTPUT                    │
│  • Extract bounding boxes          │
│  • Format: [x, y, w, h, conf, cls] │
│  • Filter: confidence >= 0.6       │
└────────────────┬───────────────────┘
                 ↓
┌────────────────────────────────────┐
│ 4. NMS (Non-Maximum Suppression)   │
│  • cv2.dnn.NMSBoxes()              │
│  • IoU threshold: 0.6              │
│  • Loại bỏ duplicate detections    │
└────────────────┬───────────────────┘
                 ↓
┌────────────────────────────────────┐
│ 5. TÍNH TOÁN SIZE & WEIGHT         │
│  • Pixel → CM conversion           │
│  • length_cm = max(w,h) × 0.05     │
│  • weight = 0.0065 × length^3.1    │
└────────────────┬───────────────────┘
                 ↓
┌────────────────────────────────────┐
│ 6. VẼ BOUNDING BOXES               │
│  • cv2.rectangle() - khung màu xanh│
│  • cv2.putText() - label & conf    │
│  • Format: "shrimp 87%"            │
│  • Font: FONT_HERSHEY_SIMPLEX      │
└────────────────┬───────────────────┘
                 ↓
📤 Trả về kết quả JSON
```

#### Code Example:

```python
def calculate_shrimp_length(bbox_width, bbox_height):
    """Tính chiều dài tôm từ bounding box"""
    max_dimension = max(bbox_width, bbox_height)
    length_cm = max_dimension * PIXEL_TO_CM_RATIO  # 0.05
    return round(length_cm, 2)

def calculate_shrimp_weight(length_cm):
    """Ước tính khối lượng từ chiều dài
    Công thức: W = a × L^b
    - a = 0.0065 (hệ số tôm thẻ chân trắng)
    - b = 3.1 (hệ số mũ)
    """
    if length_cm <= 0:
        return 0.0
    weight_gram = 0.0065 * (length_cm ** 3.1)
    return round(weight_gram, 2)
```

#### Thông số AI:
- **Model**: YOLOv8n TFLite (FP16)
- **Input shape**: (1, 128, 128, 3)
- **Classes**: 1 class ("shrimp")
- **Confidence threshold**: 60%
- **IoU threshold**: 60%
- **Inference time**: 2-5 giây (Raspberry Pi 4)

#### Công thức sinh học:
```
Camera height: 20cm
Pixel-to-CM ratio: 0.05 cm/pixel

Length (cm) = max(bbox_width, bbox_height) × 0.05

Weight (gram) = 0.0065 × (Length)^3.1
  ↑                ↑            ↑
  Khối lượng    Hệ số a     Hệ số b
  (Litopenaeus vannamei - tôm thẻ chân trắng)
```

---

### 4️⃣ **Store & Database Module** (Module Lưu trữ)

#### Thành phần:
- **Cloudinary Integration**: Upload ảnh lên cloud
- **MongoDB Operation**: Lưu metadata vào database

#### Workflow Upload:

```python
# 1. Upload ảnh lên Cloudinary
result = cloudinary.uploader.upload(
    annotated_image,
    folder="shrimp_detection",
    public_id=f"{timestamp}_shrimp",
    overwrite=True,
    resource_type="image"
)

image_url = result['secure_url']  # HTTPS URL

# 2. Chuẩn bị metadata
detection_data = {
    "user_email": request.user_email,
    "image_url": image_url,
    "timestamp": datetime.now().isoformat(),
    "shrimp_count": len(detections),
    "detections": [
        {
            "className": "shrimp",
            "confidence": 0.87,
            "bbox": {"x": 320, "y": 240, "width": 80, "height": 120},
            "length_cm": 6.0,
            "weight_gram": 1.41
        }
    ],
    "device_id": device_id
}

# 3. Insert vào MongoDB
result = collection.insert_one(detection_data)
document_id = str(result.inserted_id)
```

#### Cloudinary Configuration:
```python
cloudinary.config(
    cloud_name="your_cloud_name",
    api_key="your_api_key",
    api_secret="your_api_secret",
    secure=True  # HTTPS
)
```

#### MongoDB Schema:
```javascript
{
  _id: ObjectId("..."),
  user_email: "user@gmail.com",
  image_url: "https://res.cloudinary.com/...",
  timestamp: "2025-12-27T10:30:00+07:00",
  shrimp_count: 3,
  detections: [
    {
      className: "shrimp",
      confidence: 0.87,
      bbox: {x: 320, y: 240, width: 80, height: 120},
      length_cm: 6.0,
      weight_gram: 1.41
    }
  ],
  device_id: "raspberrypi-001"
}
```

---

## 💾 III. DATA LAYER (Lớp Dữ liệu)

### Mô tả
Lớp lưu trữ và quản lý dữ liệu vật lý, gồm 3 thành phần chính.

---

### 1️⃣ **Camera (Hardware)**

#### Thông số kỹ thuật:
- **Loại**: USB Camera hoặc CSI Camera
- **Resolution**: 640×480 pixels
- **FPS**: 30 frames/second
- **Format**: MJPEG
- **Interface**: 
  - USB: `/dev/video0`, `/dev/video1`, ...
  - CSI: `/dev/video0` (Raspberry Pi Camera Module)

#### OpenCV Configuration:
```python
camera = cv2.VideoCapture(0, cv2.CAP_V4L2)
camera.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc('M','J','P','G'))
camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
camera.set(cv2.CAP_PROP_FPS, 30)
camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)  # Minimum latency
```

#### Camera Setup (Physical):
```
┌─────────────────┐
│  Raspberry Pi   │
│                 │
│   [Camera]      │  ← USB/CSI Camera
│      ↕          │
│   [Flask App]   │  ← Backend chạy OpenCV
│      ↕          │
│   [Network]     │  ← Stream qua WiFi/LAN
└─────────────────┘
        ↓
   [Android App]   ← Client nhận stream
```

---

### 2️⃣ **Cloudinary (Cloud)**

#### Chức năng:
- **Image Storage**: Lưu trữ ảnh có bounding boxes
- **CDN**: Content Delivery Network (phân phối nhanh toàn cầu)

#### Folder Structure:
```
cloudinary://
└── shrimp_detection/
    ├── 2025-12-27_103000_shrimp.jpg
    ├── 2025-12-27_103015_shrimp.jpg
    ├── 2025-12-27_103030_shrimp.jpg
    └── ...
```

#### URL Format:
```
Original:
https://res.cloudinary.com/dzj6qxxxxx/image/upload/
  v1234567890/shrimp_detection/2025-12-27_103000_shrimp.jpg

Thumbnail (300×300):
https://res.cloudinary.com/dzj6qxxxxx/image/upload/
  c_thumb,w_300,h_300/shrimp_detection/2025-12-27_103000_shrimp.jpg

Optimized:
https://res.cloudinary.com/dzj6qxxxxx/image/upload/
  q_auto,f_auto/shrimp_detection/2025-12-27_103000_shrimp.jpg
```

#### Tối ưu hóa:
- ✅ Auto quality (`q_auto`)
- ✅ Auto format (`f_auto`) - WebP cho browser hỗ trợ
- ✅ Lazy loading
- ✅ Responsive images (nhiều kích thước)
- ✅ CDN caching (giảm latency)

#### Pricing:
- **Free tier**: 25GB storage, 25GB bandwidth/month
- **Paid**: Scale theo nhu cầu

---

### 3️⃣ **MongoDB (Cloud)**

#### Chức năng:
- **Metadata**: Lưu thông tin detection (không lưu ảnh)
- **Detection Result**: Bounding boxes, confidence, size, weight
- **User Info**: Email, device_id, timestamp

#### Collections:

##### `detections` Collection:
```javascript
{
  _id: ObjectId("676e8f..."),
  user_email: "user@gmail.com",
  image_url: "https://res.cloudinary.com/...",
  timestamp: ISODate("2025-12-27T10:30:00.000+07:00"),
  shrimp_count: 3,
  detections: [
    {
      className: "shrimp",
      confidence: 0.87,
      bbox: {
        x: 320,
        y: 240,
        width: 80,
        height: 120
      },
      length_cm: 6.0,
      weight_gram: 1.41
    },
    {
      className: "shrimp",
      confidence: 0.92,
      bbox: {x: 450, y: 180, width: 70, height: 110},
      length_cm: 5.5,
      weight_gram: 1.08
    }
  ],
  device_id: "raspberrypi-001"
}
```

#### Indexes (Tăng tốc query):
```javascript
// Index on user_email (filter by user)
db.detections.createIndex({ "user_email": 1 })

// Index on timestamp (sort by time)
db.detections.createIndex({ "timestamp": -1 })

// Index on device_id (filter by device)
db.detections.createIndex({ "device_id": 1 })

// Compound index (user + time)
db.detections.createIndex({ 
  "user_email": 1, 
  "timestamp": -1 
})
```

#### Queries thường dùng:

```javascript
// 1. Lấy tất cả ảnh của 1 user (mới nhất trước)
db.detections.find({ 
  user_email: "user@gmail.com" 
})
.sort({ timestamp: -1 })
.limit(50)

// 2. Đếm tổng số tôm của user
db.detections.aggregate([
  { $match: { user_email: "user@gmail.com" } },
  { $group: { 
      _id: null, 
      total_shrimp: { $sum: "$shrimp_count" } 
  }}
])

// 3. Thống kê theo ngày
db.detections.aggregate([
  { $match: { user_email: "user@gmail.com" } },
  { $group: {
      _id: { 
        $dateToString: { 
          format: "%Y-%m-%d", 
          date: "$timestamp" 
        }
      },
      count: { $sum: "$shrimp_count" },
      avg_confidence: { $avg: "$detections.confidence" }
  }},
  { $sort: { _id: -1 } }
])
```

#### Connection String:
```python
MONGODB_URI = "mongodb+srv://user:pass@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority"
MONGODB_DATABASE = "shrimp_db"

client = MongoClient(MONGODB_URI)
db = client[MONGODB_DATABASE]
collection = db['detections']
```

---

## 🔄 LUỒNG DỮ LIỆU QUA 3 LỚP

### Ví dụ: User chụp ảnh và nhận diện tôm

```
┌─────────────────────────────────────────────────────────────┐
│ CLIENT LAYER (Android App)                                  │
├─────────────────────────────────────────────────────────────┤
│ 1. User mở Camera Stream                                    │
│ 2. Xem video real-time                                      │
│ 3. Nhấn nút Capture 📷                                       │
│ 4. Capture 1 frame từ stream                                │
│ 5. Convert frame → Base64                                   │
│ 6. Gửi POST /api/detect-shrimp                              │
│    Body: { "image": "data:image/jpeg;base64,/9j/4AA..." }   │
│    Header: Authorization: Bearer <firebase_token>           │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP Request
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER (Flask Backend)                           │
├─────────────────────────────────────────────────────────────┤
│ 7. Authentication Module:                                   │
│    • Verify Firebase token                                  │
│    • Extract email: user@gmail.com                          │
│    • Check whitelist ✅                                      │
│                                                             │
│ 8. AI Detection Module:                                     │
│    • Decode Base64 → numpy array                            │
│    • Preprocess: resize 128×128, normalize                  │
│    • YOLO TFLite inference (2-5 giây)                       │
│    • Parse output: 3 shrimps detected                       │
│    • Calculate: length_cm, weight_gram                      │
│    • Draw bounding boxes + labels                           │
│                                                             │
│ 9. Store & Database Module:                                 │
│    • Upload annotated image → Cloudinary                    │
│    • Save metadata → MongoDB                                │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ DATA LAYER                                                  │
├─────��───────────────────────────────────────────────────────┤
│ 10. Cloudinary:                                             │
│     • Nhận ảnh với bounding boxes                           │
│     • Lưu vào folder: shrimp_detection/                     │
│     • Generate URL: https://res.cloudinary.com/...          │
│                                                             │
│ 11. MongoDB:                                                │
│     • Insert document vào collection "detections"           │
│     • Lưu: image_url, detections[], timestamp, user_email   │
│     • Return: inserted_id                                   │
└────────────────────────────┬────────────────────────────────┘
                             │ Response
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER → CLIENT LAYER                            │
├─────────────────────────────────────────────────────────────┤
│ 12. Backend trả về JSON:                                    │
│     {                                                       │
│       "success": true,                                      │
│       "image_url": "https://res.cloudinary.com/...",        │
│       "shrimp_count": 3,                                    │
│       "detections": [                                       │
│         {                                                   │
│           "className": "shrimp",                            │
│           "confidence": 0.87,                               │
│           "bbox": {...},                                    │
│           "length_cm": 6.0,                                 │
│           "weight_gram": 1.41                               │
│         }                                                   │
│       ]                                                     │
│     }                                                       │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│ CLIENT LAYER (Android App)                                  │
├─────────────────────────────────────────────────────────────┤
│ 13. Nhận response JSON                                      │
│ 14. Load ảnh từ Cloudinary URL                              │
│ 15. Hiển thị ảnh với bounding boxes                         │
│ 16. Show Toast: "Đã phát hiện 3 con tôm"                    │
│ 17. Tự động navigate → Gallery Screen                       │
│ 18. Gallery load lại danh sách (MongoDB query)              │
│ 19. Hiển thị ảnh mới nhất ở đầu danh sách                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎓 TÓM TẮT CHO KHÓA LUẬN

### 1. Kiến trúc 3 lớp (3-Layer Architecture)

| Lớp | Công nghệ | Vai trò |
|-----|-----------|---------|
| **CLIENT** | Android (Kotlin) | Giao diện người dùng, tương tác |
| **APPLICATION** | Flask (Python) | Xử lý logic, AI, authentication |
| **DATA** | Camera, Cloud, MongoDB | Lưu trữ và quản lý dữ liệu |

### 2. Ưu điểm của kiến trúc này

✅ **Separation of Concerns**: Mỗi lớp có trách nhiệm riêng biệt
✅ **Scalability**: Dễ dàng mở rộng từng lớp độc lập
✅ **Maintainability**: Sửa lỗi/nâng cấp 1 lớp không ảnh hưởng lớp khác
✅ **Security**: Authentication/Authorization tập trung ở Application Layer
✅ **Testability**: Có thể test từng lớp riêng biệt
✅ **Flexibility**: Có thể thay đổi công nghệ của 1 lớp mà không ảnh hưởng tổng thể

### 3. Luồng dữ liệu chính

```
User Action (Client) 
  → HTTP Request 
  → Authentication (Application) 
  → Business Logic (Application)
  → Data Storage (Data)
  → Response 
  → UI Update (Client)
```

### 4. Các module quan trọng trong Application Layer

1. **Authentication Module**: Xác thực và phân quyền
2. **Camera Streaming Module**: Stream video real-time
3. **AI Detection Module**: Nhận diện tôm bằng YOLO
4. **Store & Database Module**: Lưu trữ kết quả

### 5. Tính năng nổi bật

- ✅ **Multi-User Support**: Nhiều user độc lập
- ✅ **Real-time Streaming**: Latency thấp (200-300ms)
- ✅ **AI Detection**: Độ chính xác cao (>60%)
- ✅ **Cloud Storage**: Cloudinary + MongoDB
- ✅ **Size Estimation**: Tự động tính size & weight

---

**📅 Ngày tạo**: 27/12/2025  
**👨‍💻 Tác giả**: Hồ Dũng  
**🎓 Mục đích**: Giải thích sơ đồ kiến trúc cho Khóa luận tốt nghiệp  
**📊 Sơ đồ tham chiếu**: 3-Layer Architecture Diagram

