# 📚 GIẢI THÍCH ĐỌC FILE - HỆ THỐNG PHÁT HIỆN TÔM

## ĐỌC FILE NÀY TRƯỚC! 👈

File kia (`SYSTEM_ARCHITECTURE_DETAILED.md`) quá dài và phức tạp. Đây là bản giải thích **đơn giản, từng bước** cho bạn.

---

## 🎯 BỨC TRANH TỔNG QUAN (Đọc trước để hiểu toàn bộ)

### Hệ thống của bạn có 6 thứ:

```
1. 📹 CAMERA USB
   └─ Cắm vào Raspberry Pi
   └─ Quay video tôm

2. 🥧 RASPBERRY PI (Máy tính nhỏ ở nhà)
   └─ Chạy Python
   └─ Có AI xử lý ảnh
   └─ Kết nối WiFi nhà

3. 🌐 NGROK (Dịch vụ Internet)
   └─ Tạo cái "cửa" từ Internet vào nhà bạn
   └─ Để app điện thoại gọi được vào Pi

4. ☁️ CLOUDINARY (Kho ảnh trên mạng)
   └─ Lưu ảnh đã xử lý
   └─ App tải ảnh về từ đây

5. 🗄️ MONGODB (Database trên mạng)
   └─ Lưu thông tin: ảnh nào, bao nhiêu con tôm, lúc mấy giờ
   
6. 📱 APP ANDROID (Điện thoại của bạn)
   └─ Xem camera trực tiếp
   └─ Chụp ảnh và đếm tôm
   └─ Xem lại ảnh cũ
```

### Chúng làm việc với nhau như thế nào?

```
Camera → Raspberry Pi → Ngrok → App (Xem trực tiếp)

App → Ngrok → Raspberry Pi → AI xử lý → Cloudinary (lưu ảnh) 
                                      → MongoDB (lưu thông tin)
                                      → Trả kết quả về App
```

---

## 📖 PHẦN 1: NGROK LÀ GÌ? TẠI SAO CẦN NÓ?

### Vấn đề ban đầu:

**Raspberry Pi ở nhà bạn:**
- Nối vào WiFi nhà: IP là `192.168.1.100`
- IP này chỉ dùng được **trong nhà**
- Nếu điện thoại dùng 4G (không cùng WiFi) → **KHÔNG KẾT NỐI ĐƯỢC**

**Ví dụ thực tế:**
```
Bạn ở nhà:
  Điện thoại nối WiFi nhà → OK, gọi được Pi (192.168.1.100)

Bạn đi làm/đi học:
  Điện thoại dùng 4G → KHÔNG gọi được Pi (192.168.1.100)
  
Lý do: IP này chỉ có trong nhà, ngoài Internet không thấy!
```

### Giải pháp: NGROK

**Ngrok tạo một "cầu nối, đường hầm" (tunnel):**

```
TRƯỚC KHI CÓ NGROK:
App (4G) ----X----> Raspberry Pi (192.168.1.100)
             ❌ KHÔNG KẾT NỐI ĐƯỢC

SAU KHI CÓ NGROK:
App (4G) → Internet → Ngrok → Đường hầm → Raspberry Pi
                                ✅ KẾT NỐI ĐƯỢC!
```

### Cách hoạt động:

**Bước 1: Khởi động Ngrok trên Raspberry Pi**

```bash
# Terminal 1: Chạy Flask (backend Python)
cd backend
python app_tflite.py
# Flask chạy trên http://localhost:8000

# Terminal 2: Chạy Ngrok
ngrok http 8000
```

**Bước 2: Ngrok cho bạn một URL**

```
Ngrok hiển thị:

Forwarding: https://abc-123-xyz.ngrok-free.app → http://localhost:8000
```

- **URL này:** `https://abc-123-xyz.ngrok-free.app` 
- **Có thể truy cập từ:** BẤT KỲ ĐÂU trên thế giới!
- **Nó sẽ chuyển request về:** Raspberry Pi của bạn (localhost:8000)

**Bước 3: Dùng URL này trong app Android**

```kotlin
// File: ShrimpApiService.kt
private val BACKEND_URL = "https://abc-123-xyz.ngrok-free.app"

// Khi app gọi API:
POST https://abc-123-xyz.ngrok-free.app/api/detect-shrimp
```

### Tóm lại Ngrok:

| Câu hỏi | Trả lời |
|---------|---------|
| **Ngrok là gì?** | Dịch vụ tạo "đường hầm" từ Internet vào máy tính nhà bạn |
| **Tại sao cần?** | Để điện thoại (dùng 4G) gọi được vào Raspberry Pi (WiFi nhà) |
| **Dùng ở đâu?** | URL trong app Android (gọi API) và xem camera |
| **Miễn phí không?** | Có bản free (URL thay đổi mỗi lần chạy) và paid (URL cố định) |

---

## 📖 PHẦN 2: CLOUDINARY LÀ GÌ? LƯU CÁI GÌ?

### Cloudinary = Kho lưu ảnh trên Internet (giống Google Photos)

**Vấn đề:**
- Mỗi lần chụp ảnh → ảnh có kích thước ~2MB
- Chụp 100 ảnh → 200MB
- Raspberry Pi chỉ có SD card 32GB → nhanh đầy!

**Giải pháp: Cloudinary**
- Upload ảnh lên "đám mây" (cloud)
- Không chiếm dung lượng Pi
- Truy cập nhanh từ bất kỳ đâu

### Cloudinary lưu gì?

**Lưu: Ảnh ĐÃ XỬ LÝ (có vẽ khung đỏ/xanh quanh con tôm)**

Ví dụ:
```
Ảnh gốc: camera.jpg (không có khung)
         ↓
         AI xử lý
         ↓
Ảnh sau xử lý: result.jpg (có khung xanh quanh 5 con tôm)
         ↓
         Upload lên Cloudinary
         ↓
URL: https://res.cloudinary.com/myaccount/shrimp-detections/abc123.jpg
```

### Cách hoạt động:

**Trong code Python (backend/app_tflite.py):**

```python
# 1. Config Cloudinary (điền thông tin tài khoản)
cloudinary.config(
    cloud_name="myaccount",      # Tên tài khoản Cloudinary
    api_key="123456789",          # API Key
    api_secret="abcxyz"           # API Secret (giữ bí mật)
)

# 2. Upload ảnh
upload_result = cloudinary.uploader.upload(
    image_buffer,                 # Ảnh đã vẽ khung
    folder="shrimp-detections"    # Lưu vào folder này
)

# 3. Nhận URL
cloudinary_url = upload_result['secure_url']
# URL này dùng để tải ảnh về
```

**Kết quả:**
```python
{
    "secure_url": "https://res.cloudinary.com/myaccount/image/upload/v1698765432/shrimp-detections/abc123.jpg",
    "public_id": "shrimp-detections/abc123",
    "width": 1920,
    "height": 1080
}
```

### Cấu trúc trên Cloudinary:

```
Tài khoản: myaccount
└── Folder: shrimp-detections/
    ├── abc123.jpg    ← Ảnh 1 (chụp lúc 10:30, 5 con tôm)
    ├── def456.jpg    ← Ảnh 2 (chụp lúc 10:35, 3 con tôm)
    ├── ghi789.jpg    ← Ảnh 3 (chụp lúc 10:40, 7 con tôm)
    └── ...

Mỗi ảnh có URL riêng, ai cũng tải được (public)
```

### App Android tải ảnh về:

```kotlin
// Hiển thị ảnh từ Cloudinary
AsyncImage(
    model = "https://res.cloudinary.com/.../abc123.jpg",
    contentDescription = "Kết quả phát hiện tôm"
)

// Coil library tự động:
// 1. Tải ảnh từ Cloudinary
// 2. Cache vào điện thoại
// 3. Hiển thị lên màn hình
```

### Tóm lại Cloudinary:

| Câu hỏi | Trả lời |
|---------|---------|
| **Cloudinary là gì?** | Kho lưu ảnh trên Internet (cloud storage) |
| **Lưu ảnh gì?** | Ảnh ĐÃ XỬ LÝ (có vẽ khung quanh tôm) |
| **Tại sao cần?** | Tiết kiệm dung lượng Pi, tải ảnh nhanh từ bất kỳ đâu |
| **App dùng nó ntn?** | Tải ảnh về từ URL Cloudinary để hiển thị |

---

## 📖 PHẦN 3: MONGODB LÀ GÌ? LƯU CÁI GÌ?

### MongoDB = Database (cơ sở dữ liệu) lưu thông tin

**Khác với Cloudinary:**
- Cloudinary lưu: **ẢNH** (file .jpg)
- MongoDB lưu: **THÔNG TIN VỀ ẢNH** (văn bản/dữ liệu)

### MongoDB lưu gì?

**Lưu: Thông tin chi tiết về mỗi ảnh**

Ví dụ 1 ảnh:
```
- URL ảnh trên Cloudinary: https://res.cloudinary.com/.../abc123.jpg
- Số con tôm phát hiện: 5 con
- Tọa độ từng con tôm: 
    + Con 1: x=640, y=480, rộng=120, cao=80, độ chính xác=95%
    + Con 2: x=1200, y=600, rộng=115, cao=75, độ chính xác=87%
    + ...
- Thời gian chụp: 31/10/2025 10:30:25
- Nguồn: camera_stream (chụp từ camera)
- Thời gian xử lý: 0.342 giây
```

### Cấu trúc trong MongoDB:

**Database:** `shrimp_db` (tên database)  
**Collection:** `detections` (giống "bảng" trong Excel)  
**Document:** Mỗi ảnh là 1 "dòng" trong bảng

**Ví dụ 1 document (1 ảnh):**

```json
{
    "_id": "6720f1234567890abcdef123",
    "cloudinaryUrl": "https://res.cloudinary.com/.../abc123.jpg",
    "detections": [
        {
            "className": "shrimp",
            "confidence": 0.95,
            "bbox": {
                "x": 640,
                "y": 480,
                "width": 120,
                "height": 80
            }
        },
        {
            "className": "shrimp",
            "confidence": 0.87,
            "bbox": {
                "x": 1200,
                "y": 600,
                "width": 115,
                "height": 75
            }
        }
    ],
    "timestamp": 1730347825000,
    "capturedFrom": "camera_stream",
    "inferenceTime": 0.342
}
```

**Giải thích từng dòng:**

| Tên field | Nghĩa là gì | Ví dụ |
|-----------|-------------|-------|
| `_id` | ID duy nhất của ảnh này (MongoDB tự tạo) | `"6720f123..."` |
| `cloudinaryUrl` | Link ảnh trên Cloudinary | `https://res.cloudinary.com/.../abc123.jpg` |
| `detections` | Danh sách các con tôm tìm được | Mảng gồm nhiều objects |
| `detections[0].className` | Loại object | `"shrimp"` (tôm) |
| `detections[0].confidence` | Độ chính xác (0-1) | `0.95` = 95% chắc chắn là tôm |
| `detections[0].bbox` | Tọa độ khung chữ nhật | `{x, y, width, height}` |
| `timestamp` | Thời gian chụp (Unix timestamp) | `1730347825000` (milliseconds) |
| `capturedFrom` | Chụp từ đâu | `"camera_stream"` hoặc `"gallery"` |
| `inferenceTime` | AI xử lý mất bao lâu | `0.342` giây |

### Cách lưu vào MongoDB:

**Trong code Python (backend/app_tflite.py):**

```python
# 1. Kết nối MongoDB
from pymongo import MongoClient

MONGODB_URI = "mongodb+srv://username:password@cluster0.mongodb.net/"
client = MongoClient(MONGODB_URI)
db = client['shrimp_db']          # Chọn database
collection = db['detections']      # Chọn collection

# 2. Tạo document (dữ liệu)
doc = {
    "cloudinaryUrl": "https://res.cloudinary.com/.../abc123.jpg",
    "detections": [
        {"className": "shrimp", "confidence": 0.95, "bbox": {...}},
        {"className": "shrimp", "confidence": 0.87, "bbox": {...}}
    ],
    "timestamp": 1730347825000,
    "capturedFrom": "camera_stream",
    "inferenceTime": 0.342
}

# 3. Insert vào database
result = collection.insert_one(doc)
print(f"Đã lưu với ID: {result.inserted_id}")
```

### App Android lấy dữ liệu:

**Backend cung cấp API:**
```python
@app.route('/api/shrimp-images', methods=['GET'])
def get_images():
    # Lấy 100 ảnh mới nhất từ MongoDB
    images = collection.find().sort('timestamp', -1).limit(100)
    return jsonify({"images": list(images)})
```

**App gọi API:**
```kotlin
// Gọi API lấy danh sách ảnh
GET https://abc123.ngrok-free.app/api/shrimp-images

// Response:
{
    "images": [
        { "cloudinaryUrl": "...", "detections": [...], "timestamp": ... },
        { "cloudinaryUrl": "...", "detections": [...], "timestamp": ... },
        ...
    ]
}
```

**Hiển thị trong Gallery:**
```kotlin
// Hiển thị grid ảnh
LazyVerticalGrid(columns = 2) {
    items(images) { image ->
        AsyncImage(
            model = image.cloudinaryUrl,  // Tải ảnh từ Cloudinary
            onClick = {
                // Hiển thị chi tiết: số tôm, thời gian, độ chính xác
            }
        )
    }
}
```

### Tóm lại MongoDB:

| Câu hỏi | Trả lời |
|---------|---------|
| **MongoDB là gì?** | Database (cơ sở dữ liệu) trên cloud |
| **Lưu gì?** | Thông tin về ảnh: URL, số tôm, tọa độ, thời gian, v.v. |
| **Khác Cloudinary?** | Cloudinary lưu FILE ảnh, MongoDB lưu THÔNG TIN văn bản |
| **App dùng nó ntn?** | Lấy danh sách ảnh cũ để hiển thị Gallery |

---

## 📖 PHẦN 4: CÁC THÀNH PHẦN KẾT NỐI VỚI NHAU NHƯ THẾ NÀO?

### Sơ đồ đơn giản:

```
        INTERNET
            |
    ┌───────┼───────┐
    |       |       |
  NGROK  CLOUDINARY MONGODB
    |
    | (tunnel)
    |
RASPBERRY PI ← USB ← CAMERA
    |
    | (WiFi/4G qua Ngrok)
    |
APP ANDROID
```

### Giải thích chi tiết:

#### 1. Camera → Raspberry Pi

```
Kết nối: Cắm USB
Camera quay video → Pi đọc video
```

**Code Python:**
```python
import cv2
cap = cv2.VideoCapture(0)  # Mở camera /dev/video0

while True:
    ret, frame = cap.read()  # Đọc 1 frame (1 hình ảnh)
    # frame chứa ảnh từ camera
```

#### 2. Raspberry Pi → Ngrok

```
Pi chạy lệnh: ngrok http 8000
→ Ngrok tạo tunnel
→ Cho Pi một URL public: https://abc123.ngrok-free.app
```

**Cách hoạt động:**
```
Ngrok client (trên Pi) → Kết nối đến Ngrok server (trên Internet)
                       → Tạo "đường hầm" WebSocket
                       → Mọi request đến URL public được chuyển qua tunnel về Pi
```

#### 3. App Android → Ngrok → Raspberry Pi

```
App gửi request:
POST https://abc123.ngrok-free.app/api/detect-shrimp

Request đi:
App → Internet → Ngrok server → Tunnel → Raspberry Pi:8000

Response trở về:
Raspberry Pi → Tunnel → Ngrok server → Internet → App
```

**Trong code Android:**
```kotlin
// ShrimpApiService.kt
private val BACKEND_URL = "https://abc123.ngrok-free.app"

suspend fun processImage(bitmap: Bitmap): Result<Response> {
    val request = Request.Builder()
        .url("$BACKEND_URL/api/detect-shrimp")
        .post(jsonBody)
        .build()
    
    val response = client.newCall(request).execute()
    return Result.success(response)
}
```

#### 4. Raspberry Pi → Cloudinary

```
Pi upload ảnh:
Pi → Internet → Cloudinary API

Cloudinary trả về URL ảnh:
https://res.cloudinary.com/.../abc123.jpg
```

**Code Python:**
```python
# Upload ảnh lên Cloudinary
result = cloudinary.uploader.upload(
    image_buffer,
    folder="shrimp-detections"
)

url = result['secure_url']  # Lấy URL
```

#### 5. Raspberry Pi → MongoDB

```
Pi lưu dữ liệu:
Pi → Internet → MongoDB Atlas

MongoDB lưu document và trả về ID
```

**Code Python:**
```python
# Insert vào MongoDB
doc = {
    "cloudinaryUrl": url,
    "detections": [...],
    "timestamp": 1730347825000
}

result = collection.insert_one(doc)
print(result.inserted_id)  # ID của document vừa tạo
```

#### 6. App Android ← Cloudinary

```
App tải ảnh:
App → Internet → Cloudinary CDN → Tải ảnh về

Ảnh được cache trên điện thoại
```

**Code Android:**
```kotlin
// Coil library tự động tải ảnh
AsyncImage(
    model = "https://res.cloudinary.com/.../abc123.jpg"
)
```

#### 7. App Android ← Ngrok ← Raspberry Pi ← MongoDB

```
App lấy danh sách ảnh cũ:
App → Ngrok → Pi → Query MongoDB → Trả về danh sách → Pi → Ngrok → App
```

**Code:**
```kotlin
// Android gọi API
GET https://abc123.ngrok-free.app/api/shrimp-images

// Backend query MongoDB
images = collection.find().sort('timestamp', -1).limit(100)

// Trả về cho app
return {"images": [...]}
```

---

## 📖 PHẦN 5: LUỒNG HOẠT ĐỘNG TỪNG BƯỚC

### Luồng 1: XEM CAMERA TRỰC TIẾP (Real-time stream)

```
Bước 1: User mở app → Màn hình Camera Stream
        ↓
Bước 2: App load image từ URL:
        https://abc123.ngrok-free.app/blynk_feed
        ↓
Bước 3: Request đi qua Ngrok về Pi
        ↓
Bước 4: Pi đọc camera USB, gửi frame mới liên tục
        Camera → Frame (30 FPS) → Pi → Ngrok → App
        ↓
Bước 5: App hiển thị video trực tiếp (như YouTube live)
```

**Timeline:**
```
0ms:     App request frame
33ms:    Camera capture frame 1
66ms:    App nhận frame 1, hiển thị
66ms:    Camera capture frame 2
99ms:    App nhận frame 2, hiển thị
...      (cứ 33ms có frame mới = 30 FPS)
```

---

### Luồng 2: CHỤP VÀ PHÁT HIỆN TÔM (Main feature)

**Ví dụ: User nhấn nút "Chụp" để đếm tôm**

```
┌─────────────────────────────────────────────────────────┐
│ BƯỚC 1: User nhấn nút Capture 📷                        │
└─────────────────────────────────────────────────────────┘

App Android:
├─ Screenshot frame hiện tại từ camera stream
├─ Lấy được Bitmap (ảnh chụp)
└─ Gọi hàm processImage(bitmap)

┌─────────────────────────────────────────────────────────┐
│ BƯỚC 2: Chuẩn bị gửi ảnh                                │
└─────────────────────────────────────────────────────────┘

App:
├─ Convert Bitmap → JPEG bytes
├─ Convert bytes → Base64 string
│  (Base64 là cách mã hóa để gửi file qua JSON)
│
├─ Tạo JSON body:
│  {
│      "image": "data:image/jpeg;base64,/9j/4AAQ...",
│      "source": "camera_stream"
│  }
│
└─ Kích thước: ~500KB - 2MB

┌─────────────────────────────────────────────────────────┐
│ BƯỚC 3: Gửi request qua Ngrok                           │
└─────────────────────────────────────────────────────────┘

POST https://abc123.ngrok-free.app/api/detect-shrimp
Body: JSON chứa ảnh Base64

Request đi:
App → Internet (4G/WiFi) 
    → Ngrok server (Singapore)
    → Tunnel (WebSocket)
    → Raspberry Pi:8000

Thời gian: ~100ms (latency)

┌─────────────────────────────────────────────────────────┐
│ BƯỚC 4: Backend xử lý                                   │
└─────────────────────────────────────────────────────────┘

Raspberry Pi (Python Flask):

[4.1] Decode ảnh từ Base64
      Time: ~50ms
      
      image_base64 = request.json['image']
      image_data = base64.b64decode(image_base64)
      image = Image.open(BytesIO(image_data))
      
      → Có ảnh dạng NumPy array (1920x1080)

[4.2] Preprocess ảnh cho AI
      Time: ~30ms
      
      # Resize về 320x320 (kích thước model cần)
      img = cv2.resize(image_np, (320, 320))
      
      # Chuyển màu BGR → RGB
      img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
      
      # Normalize về [0, 1]
      img = img / 255.0

[4.3] Chạy AI (TFLite inference) 🤖
      Time: ~300ms (quan trọng nhất!)
      
      interpreter.set_tensor(input, img)
      interpreter.invoke()  # ← AI chạy ở đây!
      outputs = interpreter.get_tensor(output)
      
      Output: [1, 25200, 6]
      → 25200 khung có thể chứa tôm
      → Mỗi khung: [x, y, width, height, confidence, class]

[4.4] Lọc kết quả
      Time: ~20ms
      
      detections = []
      for box in outputs[0]:
          x, y, w, h, conf, cls = box
          
          if conf < 0.25:  # Bỏ qua nếu độ chính xác < 25%
              continue
          
          detections.append({
              "className": "shrimp",
              "confidence": 0.95,
              "bbox": {"x": 640, "y": 480, "width": 120, "height": 80}
          })
      
      Sau khi lọc: Còn 5 con tôm

[4.5] Vẽ khung lên ảnh
      Time: ~50ms
      
      for det in detections:
          x1, y1, x2, y2 = calculate_corners(det['bbox'])
          
          # Vẽ hình chữ nhật màu xanh
          cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255, 0), 2)
          
          # Vẽ chữ "shrimp 0.95"
          cv2.putText(img, "shrimp 0.95", (x1, y1-5), ...)
      
      → Ảnh đã có 5 khung xanh quanh tôm

[4.6] Upload ảnh lên Cloudinary
      Time: ~800ms
      
      result = cloudinary.uploader.upload(
          image_buffer,
          folder="shrimp-detections"
      )
      
      cloudinary_url = result['secure_url']
      # URL: https://res.cloudinary.com/.../abc123.jpg

[4.7] Lưu thông tin vào MongoDB
      Time: ~100ms
      
      doc = {
          "cloudinaryUrl": cloudinary_url,
          "detections": detections,
          "timestamp": 1730347825000,
          "capturedFrom": "camera_stream",
          "inferenceTime": 0.3
      }
      
      result = collection.insert_one(doc)
      mongo_id = str(result.inserted_id)

[4.8] Trả response về app
      
      response = {
          "success": True,
          "cloudinaryUrl": cloudinary_url,
          "detections": detections,
          "mongoId": mongo_id,
          "message": "Found 5 shrimps"
      }
      
      return response

┌─────────────────────────────────────────────────────────┐
│ BƯỚC 5: App nhận kết quả và hiển thị                    │
└─────────────────────────────────────────────────────────┘

Response trở về:
Pi → Tunnel → Ngrok → Internet → App

App nhận được JSON:
{
    "success": true,
    "cloudinaryUrl": "https://res.cloudinary.com/.../abc123.jpg",
    "detections": [
        {"className": "shrimp", "confidence": 0.95, ...},
        {"className": "shrimp", "confidence": 0.87, ...},
        ...
    ]
}

App hiển thị:
├─ Ảnh kết quả (có 5 khung xanh)
├─ Text: "Found 5 shrimps"
├─ Danh sách độ chính xác: 95%, 87%, 82%, ...
└─ Sau 5 giây → Tự động quay về màn hình camera
```

**Tổng thời gian:**
```
0ms:        User nhấn nút
100ms:      Ảnh đã gửi đến Pi
150ms:      Ảnh đã decode
480ms:      AI xử lý xong (300ms)
530ms:      Vẽ khung xong
1330ms:     Upload Cloudinary xong (800ms)
1430ms:     Lưu MongoDB xong (100ms)
1530ms:     App nhận response
1580ms:     App hiển thị kết quả

TỔNG: ~1.5-2 giây từ lúc nhấn nút đến lúc thấy kết quả
```

---

### Luồng 3: XEM GALLERY (Lịch sử ảnh cũ)

```
┌─────────────────────────────────────────────────────────┐
│ BƯỚC 1: User mở tab Gallery                             │
└─────────────────────────────────────────────────────────┘

App gọi API:
GET https://abc123.ngrok-free.app/api/shrimp-images

┌─────────────────────────────────────────────────────────┐
│ BƯỚC 2: Backend query MongoDB                           │
└─────────────────────────────────────────────────────────┘

Pi (Python):
images = collection.find()              # Lấy tất cả ảnh
                   .sort('timestamp', -1)  # Mới nhất lên đầu
                   .limit(100)             # Giới hạn 100 ảnh

Ví dụ kết quả:
[
    {
        "_id": "abc123",
        "cloudinaryUrl": "https://res.cloudinary.com/.../img1.jpg",
        "detections": [5 con tôm],
        "timestamp": 1730347825000
    },
    {
        "_id": "def456",
        "cloudinaryUrl": "https://res.cloudinary.com/.../img2.jpg",
        "detections": [3 con tôm],
        "timestamp": 1730347700000
    },
    ...
]

┌─────────────────────────────────────────────────────────┐
│ BƯỚC 3: App hiển thị grid ảnh                           │
└─────────────────────────────────────────────────────────┘

App nhận được list:
{
    "images": [
        {"cloudinaryUrl": "...", "detections": [...]},
        {"cloudinaryUrl": "...", "detections": [...]},
        ...
    ]
}

Hiển thị grid 2 cột:
┌─────────┬─────────┐
│  Ảnh 1  │  Ảnh 2  │  ← Load từ Cloudinary
│ 5 tôm   │ 3 tôm   │
│ 10:30   │ 10:25   │
├─────────┼─────────┤
│  Ảnh 3  │  Ảnh 4  │
│ 7 tôm   │ 2 tôm   │
│ 10:20   │ 10:15   │
└─────────┴─────────┘

User click ảnh → Hiển thị chi tiết:
- URL: https://...
- Số tôm: 5
- Độ chính xác: 95%, 87%, 82%, ...
- Thời gian: 31/10/2025 10:30:25
- Nguồn: Camera Stream
```

---

## 🎯 TÓM TẮT NHANH

### 6 thành phần:

1. **Camera USB:** Quay video tôm
2. **Raspberry Pi:** Xử lý AI, làm backend
3. **Ngrok:** Cầu nối Internet ↔ Pi (để app kết nối được)
4. **Cloudinary:** Lưu ảnh đã xử lý (có khung)
5. **MongoDB:** Lưu thông tin (số tôm, thời gian, tọa độ)
6. **App Android:** Xem camera, chụp ảnh, xem lịch sử

### 3 chức năng chính:

1. **Xem camera trực tiếp:** Camera → Pi → Ngrok → App
2. **Chụp & đếm tôm:** App → Ngrok → Pi → AI → Cloudinary + MongoDB → Kết quả
3. **Xem Gallery:** App → Ngrok → Pi → MongoDB → Danh sách ảnh

### Tại sao cần từng thứ:

| Thứ | Lý do |
|-----|-------|
| Ngrok | Để app (4G) kết nối được Pi (WiFi nhà) |
| Cloudinary | Lưu ảnh, không làm đầy Pi |
| MongoDB | Lưu thông tin, query dễ, làm Gallery |

### Thứ tự đọc:

1. Đọc file này trước (đơn giản)
2. Sau đó đọc `SYSTEM_ARCHITECTURE_DETAILED.md` (chi tiết)
3. Đọc code thực tế để hiểu implementation

---

## ❓ CÂU HỎI THƯỜNG GẶP

**Q: Tại sao không lưu ảnh trực tiếp trên Pi?**  
A: Pi chỉ có 32GB, chụp 100 ảnh là hết. Cloudinary không giới hạn (paid plan).

**Q: Tại sao cần cả Cloudinary LẪN MongoDB?**  
A: Cloudinary lưu FILE ảnh. MongoDB lưu THÔNG TIN (để search, filter, sort).

**Q: Ngrok có free không?**  
A: Có, nhưng URL thay đổi mỗi lần restart. Paid thì URL cố định.

**Q: Nếu không có Internet thì sao?**  
A: Không hoạt động. Hệ thống này cần Internet để:
- Ngrok tunnel
- Upload Cloudinary
- Lưu MongoDB

**Q: App có thể dùng WiFi thay vì 4G không?**  
A: Được! Ngrok hoạt động với cả WiFi và 4G.

**Q: AI chạy ở đâu?**  
A: Trên Raspberry Pi (local). Không cần Internet cho bước AI.

**Q: Mất bao lâu để có kết quả?**  
A: ~1.5-2 giây từ lúc nhấn nút đến lúc thấy kết quả.

---

**ĐỌC FILE NÀY XONG, BẠN SẼ HIỂU HỆ THỐNG HOẠT ĐỘNG NHƯ THẾ NÀO! 🎉**


