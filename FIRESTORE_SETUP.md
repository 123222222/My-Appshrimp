# Cấu hình Firestore cho hệ thống đăng ký/đăng nhập

## Bước 1: Bật Firestore trên Firebase Console

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Firestore Database** (bên trái menu)
4. Click **Create database**
5. **QUAN TRỌNG**: Chọn **"Start in test mode"** (KHÔNG chọn production mode)
   - Test mode cho phép truy cập dễ dàng khi phát triển
   - Production mode sẽ chặn tất cả truy cập mặc định
6. Chọn region gần Việt Nam nhất (ví dụ: `asia-southeast1`)
7. Click **Enable**

## Bước 2: Cấu hình Security Rules

Vào tab **Rules** trong Firestore và paste đoạn code sau:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Cho phép đọc/ghi collection users
    match /users/{phoneNumber} {
      // Cho phép tạo user mới (đăng ký)
      allow create: if request.auth == null;
      
      // Cho phép đọc thông tin user (đăng nhập + tìm email)
      allow read: if request.auth == null;
      
      // Cho phép cập nhật mật khẩu (quên mật khẩu)
      allow update: if request.auth != null || request.auth == null;
    }
  }
}
```

## Bước 3: Cấu hình Email Backend

### 3.1. Tạo App Password cho Gmail

1. Vào https://myaccount.google.com/security
2. Bật **2-Step Verification** (nếu chưa bật)
3. Vào **App passwords** → Tạo password mới
4. Chọn **Mail** và **Other** → Đặt tên "MyAppShrimp"
5. Copy password (dạng: `xxxx xxxx xxxx xxxx`)

### 3.2. Cấu hình backend

Mở file `backend/send_reset_email.py` và sửa:

```python
SENDER_EMAIL = "your_email@gmail.com"  # Email Gmail của bạn
SENDER_PASSWORD = "xxxx xxxx xxxx xxxx"  # App Password vừa tạo
```

### 3.3. Cài đặt dependencies

```bash
cd backend
pip install -r requirements_email.txt
```

### 3.4. Chạy backend

```bash
python send_reset_email.py
```

Backend sẽ chạy tại `http://localhost:5001`

## Bước 4: Kiểm tra cấu trúc dữ liệu

Sau khi đăng ký thành công, dữ liệu trong Firestore sẽ có dạng:

```
Collection: users
└── Document: +84987648717 (số điện thoại)
    ├── phoneNumber: "+84987648717"
    ├── email: "user@example.com"
    ├── password: "hashed_password_sha256"
    └── createdAt: 1702234567890
```

## Bước 5: Test thử

### Test Đăng ký
1. Mở app và chọn "Đăng ký"
2. Nhập:
   - Số điện thoại: `+84987648717`
   - Email: `your_email@gmail.com`
   - Mật khẩu: `123456`
3. Click "Đăng ký"
4. Kiểm tra Firestore Console → sẽ thấy document mới

### Test Đăng nhập
1. Nhập số điện thoại + mật khẩu vừa đăng ký
2. Click "Đăng nhập" → Vào app thành công

### Test Quên mật khẩu
1. Click "Quên mật khẩu"
2. Nhập email đã đăng ký
3. Click "Gửi link đặt lại mật khẩu"
4. Kiểm tra email → nhấn vào link trong email
5. Nhập mật khẩu mới → hoàn tất
6. Đăng nhập lại với mật khẩu mới

## Các tính năng đã thay đổi:

✅ **Đăng ký (SignUp)**: Cần số điện thoại + **EMAIL** + mật khẩu (KHÔNG CẦN OTP)
✅ **Đăng nhập (Login)**: Dùng số điện thoại + mật khẩu từ Firestore
✅ **Quên mật khẩu (ForgotPassword)**: Nhập email → nhận link reset qua email → nhấn link → nhập mật khẩu mới

**Lưu ý**: Tính năng "Quên mật khẩu" không dùng OTP nữa, thay vào đó gửi link reset qua email. Link có hiệu lực trong 30 phút.

## Bảo mật:

- Mật khẩu được hash bằng SHA-256 trước khi lưu vào Firestore
- Reset token có thời gian hết hạn (30 phút)
- Token chỉ dùng được 1 lần
- Deep link an toàn với scheme `myappshrimp://`

## Troubleshooting:

### Lỗi "Permission denied" trên Firestore:
1. Kiểm tra Firestore Rules đã cấu hình đúng chưa
2. Đảm bảo Firestore đã được bật
3. Kiểm tra file `google-services.json`

### Lỗi "Lỗi kết nối" khi gửi email:
1. Kiểm tra backend đã chạy chưa: `python send_reset_email.py`
2. Kiểm tra `SENDER_EMAIL` và `SENDER_PASSWORD` đã đúng chưa
3. Nếu test trên thiết bị thật, sửa `API_URL` trong `ForgotPassword.kt`:
   ```kotlin
   private val API_URL = "http://192.168.1.100:5001"  // IP máy tính
   ```

### Email không gửi được:
1. Kiểm tra Gmail đã bật 2-Step Verification
2. Kiểm tra App Password đã tạo đúng
3. Kiểm tra firewall không chặn port 587

## Deploy Production

Khi deploy lên production:
1. Đổi `SMTP_SERVER` sang dịch vụ chuyên nghiệp (SendGrid, AWS SES, ...)
2. Lưu reset tokens vào Redis thay vì memory
3. Tạo domain chính thức cho deep link
4. Bật HTTPS cho backend API
5. Cập nhật Firestore Rules bảo mật hơn

