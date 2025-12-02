# 🦐 Tóm Tắt Hệ Thống Nhận Diện Tôm

## 📌 Tổng Quan

Hệ thống bao gồm:
- **Android App**: Xem stream camera, chụp ảnh, xem thư viện
- **Backend (Raspberry Pi)**: Nhận diện tôm bằng AI (TFLite)
- **Cloudinary**: Lưu trữ ảnh
- **MongoDB**: Lưu trữ metadata (thông tin nhận diện)

## 🎯 Quy Trình Hoạt Động

```
1. Mở app → Xem camera stream từ Raspberry Pi
2. Nhấn nút chụp ảnh 📷
3. Ảnh gửi lên Raspberry Pi backend
4. AI nhận diện tôm (2-5 giây)
5. Ảnh với khung nhận diện hiển thị trên app
6. Ảnh tự động lưu vào:
   - Cloudinary (file ảnh)
   - MongoDB (thông tin: vị trí tôm, độ chính xác, thời gian)
7. Xem lại trong Gallery
```

## 📁 Files Quan Trọng

### Backend (Raspberry Pi)
```
backend/
├── app_tflite.py              ← Code backend chính (TFLite)
├── app.py                     ← Code backend cũ (PyTorch, nặng)
├── requirements_tflite.txt    ← Thư viện cần cài
├── run_tflite.sh              ← Script chạy tự động
├── test_tflite.py             ← Test model trước khi chạy
├── .env                       ← Cấu hình (Cloudinary, MongoDB)
└── models/
    └── best-fp16(1).tflite    ← Model AI đã train
```

### Android App
```
app/src/main/java/com/dung/myapplication/
├── mainUI/
│   ├── home/
│   │   └── CameraStreamScreen.kt    ← Màn hình camera + chụp ảnh
│   └── gallery/
│       ├── GalleryScreen.kt         ← Màn hình thư viện
│       └── GalleryViewModel.kt      ← Logic load ảnh
├── utils/
│   └── ShrimpApiService.kt          ← Gọi API backend
└── models/
    └── ShrimpImage.kt               ← Data models
```

## 🔧 Setup Nhanh

### 1️⃣ Cloudinary (5 phút)
1. Vào https://cloudinary.com → Đăng ký/Đăng nhập
2. Vào Dashboard
3. Copy 3 thông tin:
   - Cloud Name: `dxxxxxxx`
   - API Key: `123456789012345`
   - API Secret: `abcdef...xyz`

### 2️⃣ MongoDB (5 phút)
1. Vào https://www.mongodb.com/cloud/atlas → Đăng ký/Đăng nhập
2. Tạo cluster Free (M0)
3. Database Access → Tạo user + password
4. Network Access → Add IP `0.0.0.0/0`
5. Connect → Copy connection string:
   ```
   mongodb+srv://user:pass@cluster.mongodb.net/...
   ```

### 3️⃣ File .env (2 phút)
Tạo file `backend/.env`:
```bash
CLOUDINARY_CLOUD_NAME=dxxxxxxx
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abcdef...xyz

MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/...
MONGODB_DATABASE=shrimp_db

YOLO_MODEL_PATH=models/best-fp16(1).tflite
```

### 4️⃣ Copy sang Raspberry Pi (5 phút)
```bash
# Từ Windows
scp -r D:\MyAppshrimp\backend pi@192.168.1.100:/home/pi/
```
*(Đổi `192.168.1.100` thành IP của Raspberry Pi)*

### 5️⃣ Chạy Backend trên Raspberry Pi (10 phút)
```bash
# SSH vào Raspberry Pi
ssh pi@192.168.1.100

# Vào folder
cd ~/backend

# Chạy script setup
chmod +x run_tflite.sh
./run_tflite.sh
```

Đợi cài đặt xong, backend sẽ chạy tự động!

### 6️⃣ Cập nhật Android App (2 phút)
File: `app/src/main/java/com/dung/myapplication/utils/ShrimpApiService.kt`

Dòng 27, đổi:
```kotlin
private val BACKEND_URL = "http://192.168.1.100:8000"
```

File: `app/src/main/java/com/dung/myapplication/mainUI/home/CameraStreamScreen.kt`

Dòng 32, đổi:
```kotlin
streamUrl: String = "http://192.168.1.100:8000/blynk_feed"
```

Rebuild app:
```bash
cd D:\MyAppshrimp
gradlew assembleDebug
```

### 7️⃣ Test Hệ Thống (5 phút)

#### Test 1: Backend
Mở browser, vào:
```
http://192.168.1.100:8000/health
```
Phải thấy: `{"status": "healthy", ...}`

#### Test 2: Camera Stream
Mở browser, vào:
```
http://192.168.1.100:8000/blynk_feed
```
Phải thấy video từ camera.

#### Test 3: App
1. Mở app
2. Vào Camera Stream
3. Thấy video
4. Nhấn nút chụp 📷
5. Đợi 5 giây
6. Thấy ảnh với khung nhận diện tôm
7. Vào Gallery, thấy ảnh đã lưu ✅

## ✅ Hoàn Thành!

Nếu tất cả test đều OK → Hệ thống đã sẵn sàng!

## 🐛 Lỗi Thường Gặp

### Lỗi 1: "Connection refused"
**Nguyên nhân:** App không kết nối được backend
**Giải pháp:**
1. Kiểm tra IP Raspberry Pi: `hostname -I`
2. Kiểm tra backend đang chạy: `ps aux | grep python`
3. Ping từ điện thoại: `ping 192.168.1.100`

### Lỗi 2: "Model file not found"
**Nguyên nhân:** Thiếu file model
**Giải pháp:**
```bash
cd ~/backend
ls -lh models/best-fp16\(1\).tflite
# Nếu không có, copy từ Windows
```

### Lỗi 3: "MongoDB connection timeout"
**Nguyên nhân:** Không kết nối được MongoDB
**Giải pháp:**
1. Kiểm tra Internet: `ping google.com`
2. Kiểm tra MongoDB URI trong `.env`
3. Whitelist IP trong MongoDB Atlas

### Lỗi 4: "Cloudinary upload failed"
**Nguyên nhân:** Sai thông tin Cloudinary
**Giải pháp:**
1. Kiểm tra lại Cloud Name, API Key, API Secret
2. Đảm bảo không có khoảng trắng thừa trong `.env`

### Lỗi 5: Backend chậm quá
**Nguyên nhân:** Raspberry Pi yếu hoặc model lớn
**Giải pháp:**
1. Dùng model nhỏ hơn (INT8)
2. Giảm resolution ảnh
3. Dùng Raspberry Pi 4 (8GB)

## 📚 Documents Chi Tiết

- `COMPLETE_CHECKLIST.md` - Checklist đầy đủ từng bước
- `RASPBERRY_PI_SETUP.md` - Hướng dẫn setup chi tiết
- `QUICK_START_TFLITE.md` - Hướng dẫn nhanh
- `BACKEND_COMPARISON.md` - So sánh PyTorch vs TFLite
- `BACKEND_API_DOCS.md` - Tài liệu API

## 💡 Tips

1. **Tốc độ nhanh hơn**: Dùng Google Coral USB Accelerator (tăng 10-100x)
2. **Truy cập từ xa**: Dùng ngrok hoặc port forwarding
3. **Backup**: Backup MongoDB định kỳ
4. **Monitor**: Dùng Grafana + Prometheus để theo dõi

## 🎯 Next Steps

Sau khi hệ thống chạy ổn định:
1. Train model với nhiều ảnh hơn → độ chính xác cao hơn
2. Thêm tính năng thống kê (số tôm theo ngày, tuần)
3. Thêm cảnh báo (nếu phát hiện tôm bệnh)
4. Export báo cáo Excel/PDF
5. Tích hợp với hệ thống quản lý ao tôm

## 📞 Cần Hỗ Trợ?

Check logs:
```bash
# Backend logs
tail -f ~/backend/app.log

# System logs
sudo journalctl -u shrimp-backend -f
```

Happy coding! 🎉

