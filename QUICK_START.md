# 🚀 Quick Start Guide - Shrimp Detection App

Hướng dẫn nhanh để chạy toàn bộ hệ thống từ đầu!

---

## 📋 Mục Lục
1. [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
2. [Setup Backend](#-setup-backend)
3. [Setup Android App](#-setup-android-app)
4. [Chạy hệ thống](#-chạy-hệ-thống)
5. [Hướng dẫn sử dụng](#-hướng-dẫn-sử-dụng)
6. [Troubleshooting](#-troubleshooting)

---

## 📌 Yêu cầu hệ thống

### Backend
- **Python**: 3.8 hoặc cao hơn
- **pip**: Python package manager
- **RAM**: Tối thiểu 4GB (khuyến nghị 8GB)
- **Disk Space**: 2GB trống

### Android
- **Android Studio**: Latest version (2024+)
- **Android Device/Emulator**: Android 8.0 (API 26) trở lên
- **JDK**: Version 17 hoặc cao hơn

### Accounts (All FREE)
- **Cloudinary**: Lưu trữ ảnh (25GB free)
- **MongoDB**: Database (512MB free hoặc local)
- **ngrok** (Optional): Public URL cho backend

---

## 🐍 Setup Backend

### Bước 1: Clone/Download Project
```bash
cd D:\MyAppshrimp\backend
```

### Bước 2: Cài đặt Python Dependencies
```bash
# Tự động setup (Windows)
setup.bat

# Hoặc thủ công
pip install -r requirements.txt
```

**Dependencies bao gồm:**
- Flask: Web framework
- ultralytics: YOLO model
- opencv-python: Image processing
- cloudinary: Cloud storage
- pymongo: MongoDB driver
- pillow: Image manipulation

### Bước 3: Cấu hình Environment Variables

#### 3.1. Tạo Cloudinary Account
1. Truy cập: https://cloudinary.com/users/register/free
2. Đăng ký tài khoản miễn phí
3. Vào **Dashboard** → Copy:
   - Cloud Name
   - API Key
   - API Secret

#### 3.2. Setup MongoDB

**Option A: Local MongoDB (Khuyến nghị)**
1. Download: https://www.mongodb.com/try/download/community
2. Cài đặt với default settings
3. MongoDB sẽ chạy tại: `mongodb://localhost:27017`

**Option B: MongoDB Atlas (Cloud)**
1. Truy cập: https://www.mongodb.com/cloud/atlas
2. Tạo free cluster
3. Get connection string

#### 3.3. Tạo file .env
```bash
# Trong thư mục backend/
notepad .env
```

**Nội dung file .env:**
```env
# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/shrimp_detection
# Hoặc nếu dùng Atlas:
# MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/shrimp_detection

# Flask Configuration
FLASK_ENV=development
FLASK_DEBUG=True
```

### Bước 4: Đặt YOLO Model
```bash
# Đặt file model vào thư mục models/
D:\MyAppshrimp\backend\models\shrimp_best.pt
```

**Lưu ý:** 
- Bạn cần có trained YOLO model (`shrimp_best.pt`)
- Hoặc có thể dùng pretrained model từ Ultralytics

### Bước 5: Test Backend
```bash
# Chạy server
python app.py

# Hoặc dùng script
run.bat
```

**Kết quả mong đợi:**
```
 * Running on http://0.0.0.0:8000
 * Environment: development
 * Debug mode: on
 * Connected to MongoDB
 * Cloudinary configured
 * YOLO model loaded
```

### Bước 6: Setup ngrok (Optional - để truy cập qua Internet)
```bash
# Download ngrok
# https://ngrok.com/download

# Đăng ký và lấy auth token
ngrok config add-authtoken YOUR_AUTHTOKEN

# Chạy ngrok
ngrok http 8000
```

**Output:**
```
Forwarding: https://abc123.ngrok-free.app -> http://localhost:8000
```

Copy URL `abc123.ngrok-free.app` để dùng trong Android app.

---

## 📱 Setup Android App

### Bước 1: Mở Project trong Android Studio
```bash
# Mở Android Studio
File → Open → D:\MyAppshrimp
```

### Bước 2: Sync Gradle
- Android Studio sẽ tự động sync Gradle
- Đợi download dependencies (có thể mất 5-10 phút lần đầu)

### Bước 3: Cập nhật Backend URL

**File cần sửa:**
- `HomeScreenViewModel.kt` (dòng 24)
- `GalleryViewModel.kt` (dòng 36)
- `ShrimpApiService.kt` (nếu có)

```kotlin
// Đổi từ:
private val NGROK_URL = "old-url.ngrok-free.dev"

// Thành URL ngrok của bạn:
private val NGROK_URL = "abc123.ngrok-free.app"

// Backend URL
private val BACKEND_URL = "https://abc123.ngrok-free.app"
```

### Bước 4: Build & Install
```bash
# Option 1: Qua Android Studio
Run → Run 'app' (Shift+F10)

# Option 2: Command line
gradlew clean
gradlew installDebug
```

### Bước 5: Cấp quyền cho App
Khi app mở lần đầu:
- ✅ Allow Camera permission (nếu có)
- ✅ Allow Storage permission
- ✅ Allow Internet access

---

## ▶️ Chạy hệ thống

### Step-by-step Workflow

#### 1. Start Backend
```bash
cd D:\MyAppshrimp\backend
run.bat

# Hoặc
python app.py
```

**Đảm bảo thấy:**
```
✅ MongoDB connected
✅ Cloudinary configured  
✅ YOLO model loaded
✅ Server running on port 8000
```

#### 2. Start ngrok (nếu dùng)
```bash
ngrok http 8000
```

Copy URL: `https://your-url.ngrok-free.app`

#### 3. Launch Android App
- Mở app trên điện thoại hoặc emulator
- Đợi splash screen load

#### 4. Connect to Camera Server
1. **Home Screen** → Nhấn nút **Cloud** (☁️) ở góc dưới bên phải
2. App sẽ kết nối với camera server
3. Thấy card hiển thị: **"Camera Server (Internet)"**
4. Nhấn vào card → Chọn **"Đăng nhập"**

#### 5. View Camera Stream
- Camera stream sẽ hiển thị real-time
- Có thể thấy video từ ESP32/camera nguồn

#### 6. Capture & Detect
1. Nhấn nút **Camera** (📷) ở giữa dưới màn hình
2. Đợi 4-8 giây:
   - ⏳ "Đang xử lý ảnh..."
   - 🦐 "Phát hiện X tôm"
3. Xong! Ảnh đã được lưu

#### 7. View Gallery
1. Nhấn nút **Gallery** (🖼️) trên bottom bar
2. Xem tất cả ảnh đã chụp
3. Nhấn vào ảnh để xem chi tiết:
   - Số tôm phát hiện
   - Confidence score mỗi con
   - Vị trí bounding box

---

## 📖 Hướng dẫn sử dụng

### 🏠 Home Screen
**Chức năng:**
- Quản lý danh sách camera servers
- Kết nối với ngrok server

**Actions:**
- Nhấn nút **Cloud** → Add camera server
- Nhấn vào device card → Xem thông tin
- Nhấn **Đăng nhập** → Vào camera stream

### 📹 Camera Stream Screen
**Chức năng:**
- Xem real-time video stream
- Chụp ảnh và phát hiện tôm

**Actions:**
- Nhấn **Camera button** → Chụp & phân tích
- Nhấn **Back** → Về home
- Dùng bottom bar để navigate

### 🖼️ Gallery Screen
**Chức năng:**
- Xem tất cả ảnh đã chụp
- Grid view 2 cột

**Actions:**
- Nhấn **Refresh** → Tải lại danh sách
- Nhấn vào ảnh → Xem chi tiết
- Swipe down → Refresh

### 🔍 Image Detail Screen
**Chức năng:**
- Xem ảnh full size
- Chi tiết từng detection
- Thông tin metadata

**Actions:**
- Nhấn **Delete** → Xóa ảnh
- Nhấn **Share** → Chia sẻ (TODO)
- Nhấn **Back** → Về gallery

### 📋 Menu Screen
**Chức năng:**
- Menu chính (hiện tại trống)
- Dự phòng cho tính năng sau

### 👤 Profile Screen
**Chức năng:**
- Xem danh sách users
- Quản lý profile (demo)

### 🚪 Logout
**Chức năng:**
- Đăng xuất khỏi app
- Xóa session (nếu có)

---

## 🐛 Troubleshooting

### ❌ Backend không start được

**Lỗi: `ModuleNotFoundError`**
```bash
# Solution
pip install -r requirements.txt --upgrade
```

**Lỗi: `MongoDB connection failed`**
```bash
# Check MongoDB service
net start MongoDB

# Hoặc sử dụng MongoDB Atlas (cloud)
```

**Lỗi: `Cloudinary not configured`**
```bash
# Check file .env tồn tại
# Check các biến môi trường đúng
notepad .env
```

**Lỗi: `YOLO model not found`**
```bash
# Đảm bảo file model tồn tại:
D:\MyAppshrimp\backend\models\shrimp_best.pt
```

---

### ❌ Android App không kết nối được

**Lỗi: `Cannot connect to server`**
1. **Check backend đang chạy:**
   ```bash
   # Vào http://localhost:8000 trên trình duyệt
   # Phải thấy: "Shrimp Detection API is running!"
   ```

2. **Check ngrok đang chạy:**
   ```bash
   ngrok http 8000
   # Copy URL mới nếu ngrok restart
   ```

3. **Update URL trong app:**
   ```kotlin
   // HomeScreenViewModel.kt
   private val NGROK_URL = "your-new-url.ngrok-free.app"
   
   // GalleryViewModel.kt
   private val BACKEND_URL = "https://your-new-url.ngrok-free.app"
   ```

4. **Rebuild app:**
   ```bash
   gradlew clean
   gradlew installDebug
   ```

**Lỗi: `Server not responding (401)`**
- ngrok có thể yêu cầu bypass warning
- Thêm header: `ngrok-skip-browser-warning: true`

---

### ❌ Camera stream không hiển thị

**Lỗi: `No stream data`**
1. Check camera nguồn đang phát stream
2. Verify URL stream đúng format:
   ```
   https://your-url.ngrok-free.app/blynk_feed
   ```

**Lỗi: `Connection timeout`**
- Tăng timeout trong `CameraStreamScreen.kt`:
   ```kotlin
   .connectTimeout(30, TimeUnit.SECONDS)
   .readTimeout(60, TimeUnit.SECONDS)
   ```

---

### ❌ Detection không hoạt động

**Lỗi: `Processing failed`**
1. **Check YOLO model:**
   - File `shrimp_best.pt` phải tồn tại
   - Model phải tương thích với ultralytics version

2. **Check Cloudinary:**
   - Credentials đúng trong `.env`
   - Còn quota (25GB free tier)

3. **Check MongoDB:**
   - Database connection thành công
   - Collection `shrimp_images` được tạo

**Lỗi: `Image too large`**
```python
# Trong app.py, tăng max size:
app.config['MAX_CONTENT_LENGTH'] = 20 * 1024 * 1024  # 20MB
```

---

### ❌ Gallery trống

**Không có ảnh hiển thị:**
1. **Check backend API:**
   ```bash
   # Test bằng browser/Postman:
   GET https://your-url.ngrok-free.app/api/shrimp-images
   ```

2. **Check MongoDB:**
   ```bash
   # Dùng MongoDB Compass
   # Connect: mongodb://localhost:27017
   # Database: shrimp_detection
   # Collection: shrimp_images
   ```

3. **Check Cloudinary:**
   - Login vào dashboard
   - Verify ảnh đã upload

**Lỗi: `Image loading failed`**
- Check Cloudinary URLs còn valid
- Check internet connection

---

### ❌ Build errors

**Gradle sync failed:**
```bash
# Clear cache
gradlew clean

# Invalidate caches in Android Studio
File → Invalidate Caches / Restart
```

**Dependency resolution failed:**
```kotlin
// Trong build.gradle.kts, thử sync lại hoặc
// update version của dependencies
```

**Kotlin version incompatible:**
```kotlin
// Check gradle/libs.versions.toml
// Đảm bảo tất cả versions tương thích
```

---

## 📊 Testing Checklist

### Backend Tests
- [ ] Server starts without errors
- [ ] MongoDB connects successfully
- [ ] Cloudinary configured properly
- [ ] YOLO model loads correctly
- [ ] `/api/detect-shrimp` endpoint works
- [ ] `/api/shrimp-images` returns data
- [ ] Images upload to Cloudinary
- [ ] Metadata saves to MongoDB

### Android Tests
- [ ] App installs successfully
- [ ] Home screen loads
- [ ] Can add camera server
- [ ] Can connect to stream
- [ ] Camera stream displays
- [ ] Capture button works
- [ ] Detection results show
- [ ] Gallery loads images
- [ ] Image detail shows correctly
- [ ] Delete image works
- [ ] Navigation works smoothly

---

## 🎯 Next Steps

Sau khi setup thành công:

1. **Improve Model**: Train YOLO model với dataset tốt hơn
2. **Add Features**: 
   - Statistics dashboard
   - Export reports
   - Real-time notifications
3. **Optimize Performance**:
   - Cache images
   - Compress uploads
   - Lazy loading
4. **Deploy Production**:
   - Use AWS/GCP instead of ngrok
   - Setup proper domain
   - Add authentication

---

## 📚 Tài liệu liên quan

- 📖 [README.md](README.md) - Tổng quan dự án
- 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) - Kiến trúc hệ thống
- 🔌 [BACKEND_API_DOCS.md](BACKEND_API_DOCS.md) - API Documentation
- 🐍 [backend/README.md](backend/README.md) - Backend Guide

---

## 💡 Tips & Tricks

### 1. Nhanh hơn với Scripts
Tạo file `start.bat` trong root:
```batch
@echo off
echo Starting Backend...
cd backend
start cmd /k run.bat

echo Starting ngrok...
start cmd /k ngrok http 8000

echo Done! Press any key to close this window...
pause
```

### 2. Debug Backend
```bash
# Enable debug logging
export FLASK_DEBUG=1
python app.py
```

### 3. Monitor Performance
```python
# Trong app.py, thêm timing:
import time
start = time.time()
# ... code ...
print(f"Took {time.time() - start:.2f}s")
```

### 4. Batch Testing
```bash
# Test nhiều ảnh cùng lúc
for %%f in (test_images\*.jpg) do (
    python test_backend.py %%f
)
```

---

## 🎉 Hoàn tất!

Chúc mừng! Bạn đã setup thành công toàn bộ hệ thống **Shrimp Detection App**!

### Hệ thống của bạn bao gồm:
✅ Backend API với YOLO AI  
✅ Cloud storage với Cloudinary  
✅ Database với MongoDB  
✅ Android app với UI đẹp mắt  
✅ Real-time camera streaming  
✅ Image gallery & management  

**Enjoy coding! 🦐🚀**

---

*Last updated: December 2024*

