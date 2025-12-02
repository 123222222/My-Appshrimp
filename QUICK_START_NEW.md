# 🚀 Quick Start Guide

## Cài đặt nhanh trong 5 phút

### 📋 Yêu cầu
- Raspberry Pi với camera
- Android phone
- Cùng mạng WiFi

### 🔧 Bước 1: Setup Raspberry Pi (2 phút)

```bash
# 1. Clone hoặc copy code lên Pi
cd ~
git clone https://github.com/yourusername/MyAppshrimp.git
cd MyAppshrimp/backend

# 2. Tạo file .env (dùng .env.example làm template)
cp .env.example .env
nano .env
# Điền thông tin Cloudinary, MongoDB, Firebase

# 3. Khởi động server
python3 app_complete.py
```

**Kiểm tra:** Mở browser và vào `http://<RPI-IP>:8000/health`

### 📱 Bước 2: Cài đặt Android App (2 phút)

```bash
# Build APK
./gradlew assembleDebug

# Install vào điện thoại
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 🎯 Bước 3: Kết nối & Sử dụng (1 phút)

1. **Đăng nhập**
   - Mở app → Đăng nhập Google

2. **Quét thiết bị**
   - Vào tab **Hồ sơ**
   - Nhấn **"Quét thiết bị"**
   - Chọn Raspberry Pi → Kết nối

3. **Xem camera**
   - Vào tab **Home**
   - Camera hiển thị tự động
   - Nhấn nút 📷 để chụp ảnh

## ✅ Xong!

Camera đã hoạt động. Mỗi lần vào app, camera tự động hiển thị.

## 🔍 Kiểm tra nhanh

### Backend đang chạy?
```bash
curl http://localhost:8000/health
```

### UDP responder hoạt động?
```python
python3 -c "
import socket
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.settimeout(2)
s.sendto(b'DISCOVER_RASP', ('255.255.255.255', 50000))
print(s.recvfrom(1024))
"
```

### Camera stream OK?
Mở browser: `http://<RPI-IP>:8000/blynk_feed`

## 🐛 Lỗi thường gặp

### "Không tìm thấy thiết bị"
→ Kiểm tra app và Pi cùng WiFi

### "Camera không hiển thị"
→ Kiểm tra camera đã bật: `vcgencmd get_camera`

### "Device already bound"
→ Unbind từ tài khoản cũ hoặc xóa `backend/permitted_devices.json`

## 📚 Chi tiết hơn?

- [`README.md`](README.md) - Hướng dẫn đầy đủ
- [`DEVICE_BINDING_GUIDE.md`](DEVICE_BINDING_GUIDE.md) - Chi tiết về device binding

