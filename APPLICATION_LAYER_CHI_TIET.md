└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  MODULE 3: AI DETECTION                                 │
│  6. Decode Base64 → numpy array                         │
│  7. Preprocess image:                                   │
│     - Resize 640×480 → 128×128                          │
│     - Normalize [0-255] → [0-1]                         │
│     - BGR → RGB                                         │
│  8. Run YOLO inference (2-5 giây)                       │
│  9. Parse output: 3 shrimps detected                    │
│  10. Apply NMS (remove duplicates)                      │
│  11. Calculate:                                         │
│     - length_cm = max(w,h) × 0.05                       │
│     - weight_gram = 0.0065 × length^3.1                 │
│  12. Draw bounding boxes on original image              │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  MODULE 4: STORE & DATABASE                             │
│  13. Upload annotated image → Cloudinary                │
│      → URL: https://res.cloudinary.com/...              │
│  14. Prepare metadata document                          │
│  15. Insert to MongoDB:                                 │
│      {                                                  │
│        user_email: "user@gmail.com",                    │
│        image_url: "https://...",                        │
│        shrimp_count: 3,                                 │
│        detections: [...],                               │
│        timestamp: "2025-12-27T10:30:00"                 │
│      }                                                  │
│  16. Get document_id                                    │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  RESPONSE TO CLIENT                                     │
│  {                                                      │
│    "success": true,                                     │
│    "image_url": "https://res.cloudinary.com/...",       │
│    "shrimp_count": 3,                                   │
│    "detections": [                                      │
│      {                                                  │
│        "className": "shrimp",                           │
│        "confidence": 0.87,                              │
│        "bbox": {...},                                   │
│        "length_cm": 6.0,                                │
│        "weight_gram": 1.41                              │
│      },                                                 │
│      // ... 2 detections khác                           │
│    ],                                                   │
│    "document_id": "676e8f..."                           │
│  }                                                      │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 TÓM TẮT CHO KHÓA LUẬN

### Đặc điểm của APPLICATION LAYER

| Đặc điểm | Mô tả |
|----------|-------|
| **Vị trí** | Lớp trung gian giữa Client và Data Layer |
| **Công nghệ** | Flask (Python 3.8+) trên Raspberry Pi |
| **Vai trò** | Xử lý logic nghiệp vụ, AI, bảo mật |
| **Modules** | 4 modules độc lập nhưng liên kết |

### 4 Module chính

1. **Authentication Module:**
   - Firebase Admin SDK
   - Token verification
   - Permission management (whitelist)

2. **Camera Streaming Module:**
   - OpenCV capture frames
   - MJPEG encoding
   - Real-time streaming (30 FPS)

3. **AI Detection Module:**
   - YOLO TFLite model
   - Image preprocessing
   - Size/weight calculation

4. **Store & Database Module:**
   - Cloudinary upload (ảnh)
   - MongoDB insert (metadata)

### Luồng xử lý chính

```
Request → Authentication → AI Detection → Storage → Response
```

### Thời gian xử lý

- **Authentication**: < 100ms
- **Camera Stream**: Real-time (30 FPS)
- **AI Detection**: 2-5 giây
- **Storage**: 1-2 giây
- **Total**: ~3-8 giây cho 1 detection

### Công nghệ sử dụng

- **Framework**: Flask (Python web framework)
- **AI**: TensorFlow Lite + YOLOv8
- **CV**: OpenCV (computer vision)
- **Auth**: Firebase Admin SDK
- **Storage**: Cloudinary API
- **Database**: PyMongo (MongoDB driver)

---

**📅 Ngày tạo**: 27/12/2025  
**👨‍💻 Tác giả**: Hồ Dũng  
**🎓 Mục đích**: Giải thích chi tiết APPLICATION LAYER cho Khóa luận  
**📊 Tham chiếu**: Sơ đồ kiến trúc 3 lớp
# 🐍 APPLICATION LAYER - GIẢI THÍCH CHI TIẾT
## Flask Backend Server trên Raspberry Pi

---

## 📦 TỔNG QUAN APPLICATION LAYER

**Application Layer** là lớp trung gian xử lý toàn bộ logic nghiệp vụ của hệ thống, gồm 4 module chính:

```
┌─────────────────────────────────────────────────────┐
│         FLASK BACKEND SERVER (Python 3.8+)          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌───────────────────────────────────────────┐     │
│  │   1. AUTHENTICATION MODULE                │     │
│  │   • Firebase Admin SDK                    │     │
│  │   • Token Verification                    │     │
│  │   • Permission Management                 │     │
│  └───────────────────────────────────────────┘     │
│                                                     │
│  ┌───────────────────────────────────────────┐     │
│  │   2. CAMERA STREAMING MODULE              │     │
│  │   • OpenCV                                │     │
│  │   • MJPEG Stream Handler                  │     │
│  │   • Frame Buffer Management               │     │
│  └───────────────────────────────────────────┘     │
│                                                     │
│  ┌───────────────────────────────────────────┐     │
│  │   3. AI DETECTION MODULE                  │     │
│  │   • YOLO model (TensorFlow Lite)          │     │
│  │   • Image Preprocessing                   │     │
│  │   • Frame Buffer Management               │     │
│  └───────────────────────────────────────────┘     │
│                                                     │
│  ┌───────────────────────────────────────────┐     │
│  │   4. STORE & DATABASE MODULE              │     │
│  │   • Cloudinary Integration                │     │
│  │   • MongoDB Operation                     │     │
│  └───────────────────────────────────────────┘     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🔐 1. AUTHENTICATION MODULE (Module Xác Thực)

### Mục đích
- Bảo vệ hệ thống khỏi truy cập trái phép
- Quản lý danh sách người dùng được phép
- Phân quyền Admin và User thông thường

### Thành phần: Firebase Admin SDK

**Chức năng:**
- Verify (xác minh) Firebase ID Token từ Android App
- Đảm bảo token hợp lệ và chưa hết hạn
- Extract (trích xuất) thông tin user từ token

**Cách hoạt động:**
- Backend nhận token từ header `Authorization`
- Gọi `firebase_auth.verify_id_token(token)` để kiểm tra
- Nếu token hợp lệ → lấy email của user
- Nếu token không hợp lệ → trả về lỗi 401 (Unauthorized)

**File cấu hình:**
- `my-app-shrimp-v2-0-firebase-adminsdk.json` - Chứa credentials để kết nối Firebase

### Thành phần: Token Verification

**Chức năng:**
- Kiểm tra token có còn hiệu lực không
- Kiểm tra token có bị giả mạo không
- Kiểm tra token có đúng format không

**Quy trình kiểm tra:**
- **Bước 1:** Nhận token từ request header
- **Bước 2:** Decode token thành JSON object
- **Bước 3:** Kiểm tra signature (chữ ký số)
- **Bước 4:** Kiểm tra expiry time (thời gian hết hạn)
- **Bước 5:** Trích xuất email từ token đã verify

**Kết quả:**
- ✅ Token hợp lệ → Cho phép truy cập
- ❌ Token không hợp lệ → Trả về lỗi 401
- ❌ Token hết hạn → Yêu cầu đăng nhập lại

### Thành phần: Permission Management

**Chức năng:**
- Quản lý danh sách email được phép truy cập
- Quản lý danh sách số điện thoại được phép
- Phân quyền Admin và User

**File quản lý:**
- `permitted_emails.json` - Danh sách email whitelist
  ```json
  [
    "hodung15032003@gmail.com",
    "user1@example.com",
    "user2@example.com"
  ]
  ```

- `permitted_phones.json` - Danh sách phone whitelist
  ```json
  [
    "+84987648717",
    "+84912345678"
  ]
  ```

- `permitted_devices.json` - Mapping user → device
  ```json
  {
    "hodung15032003@gmail.com": {
      "device_id": "raspberrypi-001",
      "ip": "192.168.1.100",
      "last_updated": 1735291200
    }
  }
  ```

**Cơ chế phân quyền:**
- **Admin** (`hodung15032003@gmail.com`):
  - Thêm/xóa email vào whitelist
  - Thêm/xóa phone vào whitelist
  - Xem tất cả dữ liệu của tất cả users
  - Quản lý device bindings

- **User** (email trong whitelist):
  - Xem camera stream
  - Chụp ảnh và nhận diện
  - Xem gallery và chart của chính mình
  - Bind/unbind device của chính mình

- **Unpermitted User** (không trong whitelist):
  - Không thể truy cập bất kỳ chức năng nào
  - Nhận thông báo: "Tài khoản chưa được cấp quyền"

**Luồng hoạt động:**
```
1. Client gửi request + Firebase token
        ↓
2. Backend verify token → lấy email
        ↓
3. Load permitted_emails.json
        ↓
4. Check: email có trong list không?
        ↓
   ┌────┴────┐
   │         │
  YES       NO
   │         │
   ↓         ↓
 Allow    Deny (403)
```

**Bảo mật:**
- Token có thời gian hết hạn (1 giờ)
- Mỗi request đều phải verify token
- Không lưu token trong backend (stateless)
- Log mọi authentication attempts

---

## 📹 2. CAMERA STREAMING MODULE (Module Phát Camera)

### Mục đích
- Phát video trực tiếp từ camera USB/CSI
- Cho phép Android App xem real-time
- Tối ưu độ trễ (latency) thấp nhất

### Thành phần: OpenCV

**Chức năng:**
- Kết nối với camera hardware (USB hoặc CSI)
- Capture (chụp) frames liên tục từ camera
- Xử lý và encode frames thành định dạng JPEG

**Cấu hình camera:**
- **Device**: `/dev/video0` hoặc `/dev/video1` (Linux)
- **Driver**: V4L2 (Video for Linux 2)
- **Resolution**: 640×480 pixels
- **FPS**: 30 frames/giây
- **Format**: MJPEG (Motion JPEG)
- **Buffer size**: 1 frame (giảm độ trễ)

**Quy trình capture:**
```
1. Mở camera device → cv2.VideoCapture(0)
        ↓
2. Set resolution → 640×480
        ↓
3. Set FPS → 30
        ↓
4. Set format → MJPEG
        ↓
5. Loop vô hạn:
   - Read frame → camera.read()
   - Encode JPEG → cv2.imencode('.jpg', frame)
   - Yield frame → gửi qua HTTP stream
```

**Xử lý lỗi:**
- Nếu camera không tìm thấy → thử từ video0 đến video14
- Nếu read frame thất bại → log warning và retry
- Nếu camera bị disconnect → tự động reconnect

### Thành phần: MJPEG Stream Handler

**Chức năng:**
- Encode frames thành MJPEG stream
- Gửi stream qua HTTP multipart response
- Maintain (duy trì) connection với nhiều clients

**MJPEG là gì:**
- Motion JPEG = chuỗi các ảnh JPEG liên tiếp
- Mỗi frame là 1 JPEG độc lập
- Không nén giữa các frames (khác H.264)
- Dễ implement, độ trễ thấp

**Format HTTP Response:**
```
HTTP/1.1 200 OK
Content-Type: multipart/x-mixed-replace; boundary=frame

--frame
Content-Type: image/jpeg

[JPEG binary data của frame 1]
--frame
Content-Type: image/jpeg

[JPEG binary data của frame 2]
--frame
Content-Type: image/jpeg

[JPEG binary data của frame 3]
...
```

**API Endpoint:**
- **URL**: `http://192.168.1.100:8000/blynk_feed`
- **Method**: GET
- **Authentication**: Không yêu cầu (public)
- **Response**: Multipart stream vô hạn

**Cách Client sử dụng:**
```kotlin
// Android App
AsyncImage(
    model = "http://192.168.1.100:8000/blynk_feed",
    contentDescription = "Camera Stream"
)
```

**Thông số hiệu năng:**
- **Bandwidth**: 1-2 Mbps (tùy quality)
- **Latency**: 200-300ms
- **Concurrent clients**: Hỗ trợ nhiều clients cùng lúc

### Thành phần: Frame Buffer Management

**Chức năng:**
- Quản lý buffer frames để tránh lag
- Tối ưu bộ nhớ
- Đồng bộ hóa multi-threading

**Buffer size = 1:**
- Chỉ giữ 1 frame trong buffer
- Frame mới đến → ghi đè frame cũ
- Giảm độ trễ xuống mức thấp nhất
- Trade-off: Có thể skip frames nếu network chậm

**Thread Safety:**
- Sử dụng `threading.Lock()` để tránh race condition
- Khi 1 thread đang đọc frame → lock
- Các thread khác phải đợi → unlock
- Đảm bảo không có 2 threads cùng truy cập camera

**Code minh họa:**
```python
camera_lock = threading.Lock()

def generate_frames():
    while True:
        with camera_lock:  # Acquire lock
            success, frame = camera.read()
            if success:
                ret, buffer = cv2.imencode('.jpg', frame)
                yield buffer.tobytes()
        # Lock tự động release
```

**Memory Management:**
- Mỗi frame JPEG ≈ 30-50KB
- Buffer 1 frame = 30-50KB RAM
- Không lưu frames vào disk
- Frames được giải phóng ngay sau khi gửi

---

## 🤖 3. AI DETECTION MODULE (Module Nhận Diện AI)

### Mục đích
- Nhận diện tôm trong ảnh
- Đếm số lượng tôm
- Tính toán kích thước và khối lượng tôm

### Thành phần: YOLO model (TensorFlow Lite)

**YOLO là gì:**
- You Only Look Once - thuật toán AI nhanh
- Detect objects trong 1 lần xử lý
- Phù hợp cho real-time detection

**TensorFlow Lite:**
- Phiên bản nhẹ của TensorFlow
- Tối ưu cho thiết bị nhúng (Raspberry Pi)
- File model: `best-fp16.tflite`
- Size: ~5-10MB
- Format: FP16 (16-bit floating point) - nhanh hơn FP32

**Model đã train:**
- **Dataset**: Hình ảnh tôm (custom dataset)
- **Classes**: 1 class duy nhất ("shrimp")
- **Input size**: 128×128 hoặc 320×320 pixels
- **Output**: Bounding boxes + confidence scores

**Cách load model:**
```python
from tflite_runtime.interpreter import Interpreter

interpreter = Interpreter(model_path='models/best-fp16.tflite')
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
```

**Inference (Suy diễn):**
```
1. Load model vào memory
        ↓
2. Nhận ảnh input (640×480)
        ↓
3. Preprocess → resize to 128×128
        ↓
4. Feed vào model → interpreter.invoke()
        ↓
5. Get output → bounding boxes
        ↓
6. Post-process → filter confidence > 60%
```

**Thông số model:**
- **Confidence threshold**: 0.6 (60%)
  - Chỉ giữ detections có độ tin cậy ≥ 60%
  - Loại bỏ false positives (phát hiện sai)

- **IoU threshold**: 0.6
  - IoU = Intersection over Union
  - Dùng cho NMS (Non-Maximum Suppression)
  - Loại bỏ duplicate detections

- **Inference time**: 2-5 giây trên Raspberry Pi 4

### Thành phần: Image Preprocessing

**Mục đích:**
- Chuẩn bị ảnh đúng format cho model
- Normalize dữ liệu
- Resize về đúng kích thước

**Các bước preprocessing:**

1. **Decode Base64:**
   - Client gửi ảnh dạng Base64 string
   - Backend decode → binary data
   - Convert binary → numpy array

2. **Resize:**
   - Ảnh gốc: 640×480 pixels
   - Model cần: 128×128 pixels
   - Dùng `cv2.resize()` để scale down
   - Method: INTER_LINEAR (smooth scaling)

3. **Color Conversion:**
   - OpenCV đọc ảnh: BGR (Blue-Green-Red)
   - Model cần: RGB (Red-Green-Blue)
   - Convert: `cv2.cvtColor(img, cv2.COLOR_BGR2RGB)`

4. **Normalization:**
   - Giá trị pixel gốc: 0-255 (uint8)
   - Model cần: 0.0-1.0 (float32)
   - Công thức: `pixel_value / 255.0`

5. **Add Batch Dimension:**
   - Shape gốc: (128, 128, 3)
   - Model cần: (1, 128, 128, 3)
   - Dùng `np.expand_dims(img, axis=0)`

**Code minh họa:**
```python
def preprocess_image(image_np):
    # Resize
    img = cv2.resize(image_np, (128, 128))
    
    # BGR → RGB
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    
    # Normalize
    img = img.astype(np.float32) / 255.0
    
    # Add batch dimension
    img = np.expand_dims(img, axis=0)
    
    return img  # Shape: (1, 128, 128, 3)
```

**Post-processing (sau khi inference):**

1. **Parse YOLO output:**
   - Output format: `[x, y, w, h, confidence, class_id]`
   - x, y: Tọa độ center của bounding box
   - w, h: Chiều rộng và cao của box
   - confidence: Độ tin cậy (0-1)
   - class_id: ID của class (0 = "shrimp")

2. **Filter by confidence:**
   - Chỉ giữ detections có `confidence >= 0.6`

3. **NMS (Non-Maximum Suppression):**
   - Loại bỏ overlapping boxes
   - Chỉ giữ box có confidence cao nhất
   - Dùng `cv2.dnn.NMSBoxes()`

4. **Convert coordinates:**
   - Coordinates từ model: normalized (0-1)
   - Cần convert về pixels (0-640, 0-480)
   - Scale factor: `scale_x = 640/128`, `scale_y = 480/128`

5. **Calculate size & weight:**
   - Chiều dài (cm) = max(width, height) × 0.05
   - Khối lượng (gram) = 0.0065 × (chiều_dài)^3.1

### Thành phần: Frame Buffer Management

**Chức năng:**
- Quản lý ảnh input đang được xử lý
- Tránh memory leak
- Optimize performance

**Workflow:**
```
1. Nhận ảnh từ client (Base64)
        ↓
2. Decode → numpy array
        ↓
3. Copy vào buffer
        ↓
4. Preprocess → model input
        ↓
5. Inference → predictions
        ↓
6. Post-process → final results
        ↓
7. Draw bounding boxes trên ảnh gốc
        ↓
8. Release buffer (giải phóng memory)
```

**Memory optimization:**
- Không lưu ảnh vào disk trong quá trình xử lý
- Dùng in-memory buffer (RAM)
- Clear buffer sau mỗi detection
- Tránh memory leak bằng proper cleanup

**Thread safety:**
- Mỗi request detection chạy trong thread riêng
- Không share buffer giữa các threads
- Model interpreter có thể được share (read-only)

---

## 💾 4. STORE & DATABASE MODULE (Module Lưu Trữ)

### Mục đích
- Lưu ảnh kết quả lên cloud
- Lưu metadata vào database
- Cung cấp URL để client tải ảnh

### Thành phần: Cloudinary Integration

**Cloudinary là gì:**
- Dịch vụ cloud storage cho ảnh/video
- CDN (Content Delivery Network) toàn cầu
- Tự động optimize ảnh
- Miễn phí tier: 25GB storage

**Chức năng:**
- Upload ảnh đã vẽ bounding boxes
- Generate public URL
- Tự động optimize quality
- Tạo thumbnails

**Cấu hình:**
```python
cloudinary.config(
    cloud_name="dzj6qxxxxx",    # Tên cloud của bạn
    api_key="123456789012345",  # API key
    api_secret="abcdef...xyz",  # API secret
    secure=True                 # Dùng HTTPS
)
```

**Upload workflow:**
```
1. Nhận ảnh (numpy array) với bounding boxes
        ↓
2. Encode → JPEG binary
        ↓
3. Upload to Cloudinary
   - Folder: "shrimp_detection/"
   - Public ID: "2025-12-27_103000_shrimp"
        ↓
4. Cloudinary trả về:
   - URL: https://res.cloudinary.com/...
   - Public ID
   - Width/Height
        ↓
5. Lưu URL vào MongoDB
```

**Code example:**
```python
result = cloudinary.uploader.upload(
    image_data,
    folder="shrimp_detection",
    public_id=f"{timestamp}_shrimp",
    overwrite=True,
    resource_type="image"
)

image_url = result['secure_url']
```

**URL format:**
```
Original:
https://res.cloudinary.com/dzj6qxxxxx/image/upload/
  v1735291200/shrimp_detection/2025-12-27_103000_shrimp.jpg

Thumbnail (300×300):
https://res.cloudinary.com/dzj6qxxxxx/image/upload/
  c_thumb,w_300,h_300/shrimp_detection/2025-12-27_103000_shrimp.jpg
```

**Ưu điểm:**
- ✅ Upload nhanh (multi-region servers)
- ✅ CDN delivery (tốc độ tải ảnh nhanh)
- ✅ Auto backup
- ✅ Không lo về storage trên Raspberry Pi
- ✅ Responsive images (tự động resize)

### Thành phần: MongoDB Operation

**MongoDB là gì:**
- NoSQL document database
- Lưu data dạng JSON
- Flexible schema
- Cloud-based (MongoDB Atlas)

**Chức năng:**
- Lưu metadata của mỗi detection
- Lưu thông tin user
- Lưu detection history
- Query và statistics

**Collection structure:**

**Collection: `detections`**
```javascript
{
  _id: ObjectId("676e8f..."),              // Auto-generated ID
  user_email: "user@gmail.com",            // Email của user chụp
  image_url: "https://res.cloudinary...",  // URL ảnh từ Cloudinary
  timestamp: ISODate("2025-12-27T10:30:00"), // Thời gian chụp
  shrimp_count: 3,                         // Số lượng tôm phát hiện
  detections: [                            // Mảng các detections
    {
      className: "shrimp",
      confidence: 0.87,                    // Độ tin cậy 87%
      bbox: {
        x: 320,                            // Tọa độ center X
        y: 240,                            // Tọa độ center Y
        width: 80,                         // Chiều rộng box
        height: 120                        // Chiều cao box
      },
      length_cm: 6.0,                      // Chiều dài tôm (cm)
      weight_gram: 1.41                    // Khối lượng (gram)
    },
    // ... detection thứ 2, 3, ...
  ],
  device_id: "raspberrypi-001"             // Device đã chụp
}
```

**Insert operation:**
```python
detection_data = {
    "user_email": request.user_email,
    "image_url": image_url,
    "timestamp": datetime.now().isoformat(),
    "shrimp_count": len(detections),
    "detections": detections,
    "device_id": device_id
}

result = collection.insert_one(detection_data)
document_id = str(result.inserted_id)
```

**Query operations:**

1. **Lấy tất cả ảnh của 1 user:**
```python
images = collection.find({
    "user_email": "user@gmail.com"
}).sort("timestamp", -1).limit(50)
```

2. **Đếm tổng số tôm:**
```python
pipeline = [
    {"$match": {"user_email": "user@gmail.com"}},
    {"$group": {
        "_id": None,
        "total_shrimp": {"$sum": "$shrimp_count"}
    }}
]
result = collection.aggregate(pipeline)
```

3. **Thống kê theo ngày:**
```python
pipeline = [
    {"$match": {"user_email": "user@gmail.com"}},
    {"$group": {
        "_id": {
            "$dateToString": {
                "format": "%Y-%m-%d",
                "date": "$timestamp"
            }
        },
        "count": {"$sum": "$shrimp_count"},
        "avg_confidence": {"$avg": "$detections.confidence"}
    }},
    {"$sort": {"_id": -1}}
]
```

**Indexes (tăng tốc query):**
```python
# Index on user_email
collection.create_index("user_email")

# Index on timestamp (descending)
collection.create_index([("timestamp", -1)])

# Compound index
collection.create_index([
    ("user_email", 1),
    ("timestamp", -1)
])
```

**Connection:**
```python
MONGODB_URI = "mongodb+srv://user:pass@cluster0.xxxxx.mongodb.net/"
MONGODB_DATABASE = "shrimp_db"

client = MongoClient(MONGODB_URI)
db = client[MONGODB_DATABASE]
collection = db['detections']
```

**Ưu điểm:**
- ✅ Cloud-based (không lo backup)
- ✅ Scalable (dễ mở rộng)
- ✅ Flexible schema (thay đổi structure dễ dàng)
- ✅ Powerful aggregation (thống kê phức tạp)
- ✅ Auto-indexing (tự động tối ưu query)

---

## 🔄 TƯƠNG TÁC GIỮA CÁC MODULE

### Ví dụ: User chụp ảnh và nhận diện

```
┌─────────────────────────────────────────────────────────┐
│  CLIENT: User nhấn nút Capture                          │
│  → POST /api/detect-shrimp                              │
│  → Body: { "image": "base64...", "device_id": "..." }   │
│  → Header: Authorization: Bearer <token>                │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  MODULE 1: AUTHENTICATION                               │
│  1. Extract token from header                           │
│  2. Verify token with Firebase                          │
│  3. Get user email: user@gmail.com                      │
│  4. Check whitelist ✅                                   │
│  5. Allow request to proceed                            │

