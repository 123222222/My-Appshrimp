# 📧 Hướng dẫn Reset Mật khẩu qua Email

## 🎯 Tính năng mới

**Quên mật khẩu** giờ đây gửi link reset qua email thay vì OTP qua SMS!

## 🚀 Cách sử dụng

### 1. Đăng ký tài khoản (lần đầu)
- Nhập **số điện thoại** (+84...)
- Nhập **email** (quan trọng để reset mật khẩu sau này!)
- Nhập **mật khẩu** (tối thiểu 6 ký tự)

### 2. Quên mật khẩu
1. Click "Quên mật khẩu"
2. Nhập **email** đã đăng ký
3. Click "Gửi link đặt lại mật khẩu"
4. **Kiểm tra email** → Nhấn vào link trong email
5. App sẽ tự mở và hiện dialog nhập mật khẩu mới
6. Nhập mật khẩu mới → Xong!

## ⚙️ Cài đặt Backend (Lần đầu tiên)

### Bước 1: Cài đặt Python dependencies

```bash
cd backend
pip install -r requirements_email.txt
```

### Bước 2: Cấu hình Email Gmail

1. Vào https://myaccount.google.com/security
2. Bật **2-Step Verification**
3. Vào **App passwords** → Tạo mới
4. Copy password (dạng: `abcd efgh ijkl mnop`)

### Bước 3: Sửa file `backend/send_reset_email.py`

```python
SENDER_EMAIL = "your_email@gmail.com"      # ← Thay bằng email của bạn
SENDER_PASSWORD = "abcd efgh ijkl mnop"   # ← Thay bằng App Password
```

### Bước 4: Chạy backend

```bash
python send_reset_email.py
```

Bạn sẽ thấy:
```
🚀 Email Reset Password API đang chạy...
📧 Nhớ cấu hình SENDER_EMAIL và SENDER_PASSWORD!
 * Running on http://0.0.0.0:5001
```

**Lưu ý**: Backend phải chạy khi test tính năng quên mật khẩu!

## 📱 Test trên thiết bị thật

Nếu test trên điện thoại thật (không phải emulator):

1. Tìm IP máy tính: `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)
2. Sửa file `ForgotPassword.kt`:

```kotlin
private val API_URL = "http://192.168.1.100:5001"  // ← Thay bằng IP máy tính
```

3. Đảm bảo điện thoại và máy tính **cùng mạng WiFi**

## 🔐 Cấu trúc dữ liệu Firestore

```
users/
  +84987648717/
    phoneNumber: "+84987648717"
    email: "user@example.com"     ← Trường mới
    password: "hash_sha256..."
    createdAt: 1702234567890
```

## 🐛 Troubleshooting

### Lỗi "Lỗi kết nối"
- ✅ Kiểm tra backend đã chạy chưa
- ✅ Kiểm tra IP đúng chưa (nếu test trên thiết bị thật)
- ✅ Kiểm tra firewall không chặn port 5001

### Email không gửi được
- ✅ Kiểm tra App Password đã đúng chưa
- ✅ Kiểm tra Gmail đã bật 2-Step Verification
- ✅ Thử gửi email test từ code Python

### Link trong email không mở được app
- ✅ Đảm bảo đã cài app từ Android Studio
- ✅ Kiểm tra deep link trong `AndroidManifest.xml`
- ✅ Thử click link nhiều lần

## 📊 Flow hoàn chỉnh

```
User nhập email → App gửi request đến Backend API
                                    ↓
Backend tìm phoneNumber trong Firestore (qua email)
                                    ↓
Backend tạo token ngẫu nhiên và gửi email
                                    ↓
User check email → Click link: myappshrimp://reset-password?token=...
                                    ↓
App mở và verify token với Backend
                                    ↓
User nhập mật khẩu mới → App cập nhật Firestore
```

## 🎉 Hoàn tất!

Giờ bạn có thể:
- ✅ Đăng ký với số điện thoại + email + mật khẩu
- ✅ Đăng nhập với số điện thoại + mật khẩu
- ✅ Reset mật khẩu qua email (không cần OTP!)

---

**Lưu ý bảo mật**: 
- Token reset có hiệu lực 30 phút
- Mỗi token chỉ dùng được 1 lần
- Mật khẩu được hash SHA-256 trước khi lưu

