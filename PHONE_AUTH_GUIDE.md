# Hướng dẫn Đăng nhập bằng Số điện thoại với OTP

## Các thay đổi đã triển khai

### 1. **Đăng nhập (LoginActivity)**
- ✅ Đổi từ đăng nhập bằng Email sang **Số điện thoại**
- ✅ Sử dụng Firebase Phone Authentication với **OTP**
- ✅ Khi người dùng nhập số điện thoại và nhấn "Đăng nhập":
  - Hệ thống gửi mã OTP đến số điện thoại
  - Hiển thị dialog để nhập mã OTP
  - Xác thực OTP và đăng nhập thành công

### 2. **Quên mật khẩu (ForgotPasswordActivity)**
- ✅ Đổi từ reset bằng Email sang **Số điện thoại**
- ✅ Quy trình:
  1. Người dùng nhập số điện thoại
  2. Nhấn "Gửi mã OTP"
  3. Nhập mã OTP nhận được
  4. Nhập mật khẩu mới
  5. Mật khẩu được cập nhật thành công

### 3. **Đăng ký (SignUpActivity)**
- ✅ Đổi từ đăng ký bằng Email sang **Số điện thoại**
- ✅ Quy trình:
  1. Người dùng nhập số điện thoại và mật khẩu
  2. Nhấn "Đăng ký"
  3. Nhập mã OTP nhận được
  4. Tài khoản được tạo và mật khẩu được thiết lập

### 4. **Giữ nguyên các tính năng khác**
- ✅ Đăng nhập bằng Google
- ✅ Đăng nhập bằng vân tay (Biometric)

## Định dạng số điện thoại

Số điện thoại **bắt buộc** phải có mã quốc gia:
- ✅ Đúng: `+84123456789` (Việt Nam)
- ✅ Đúng: `+84987654321`
- ❌ Sai: `0123456789` (thiếu mã quốc gia)
- ❌ Sai: `84123456789` (thiếu dấu +)

## Cấu hình Firebase cần thiết

### Bước 1: Bật Phone Authentication trong Firebase Console
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Authentication** → **Sign-in method**
4. Bật **Phone** authentication

### Bước 2: Thêm SHA-1 fingerprint (quan trọng!)
Phone Authentication yêu cầu SHA-1 fingerprint của ứng dụng:

#### Debug SHA-1:
```cmd
cd D:\MyAppshrimp
gradlew signingReport
```

Hoặc:
```cmd
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

#### Thêm SHA-1 vào Firebase:
1. Copy SHA-1 fingerprint từ kết quả lệnh trên
2. Vào Firebase Console → Project Settings → Your apps
3. Thêm SHA-1 fingerprint vào mục "SHA certificate fingerprints"
4. Tải lại file `google-services.json` và thay thế file cũ

### Bước 3: Cấu hình Phone Number cho testing (tùy chọn)
Nếu muốn test mà không cần gửi SMS thật:
1. Vào Firebase Console → Authentication → Sign-in method
2. Scroll xuống phần "Phone numbers for testing"
3. Thêm số điện thoại test và mã OTP (ví dụ: +84123456789 với OTP: 123456)

## Dependencies đã có sẵn

File `build.gradle.kts` đã có các dependencies cần thiết:
```kotlin
implementation("com.google.firebase:firebase-auth:22.3.1")
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

## Lưu ý quan trọng

### 1. **SMS Quota**
Firebase có giới hạn SMS miễn phí mỗi ngày. Xem quota tại Firebase Console.

### 2. **Testing trên thiết bị thật**
- Phone Authentication **không hoạt động** trên Emulator thông thường
- Cần test trên **thiết bị Android thật** hoặc Emulator với Google Play Services

### 3. **Timeout OTP**
- Mã OTP có hiệu lực trong 60 giây (đã cấu hình trong code)
- Người dùng cần nhập OTP trong khoảng thời gian này

### 4. **Auto-verification**
Trên một số thiết bị, Firebase có thể tự động xác thực OTP mà không cần người dùng nhập.

## Testing

### Test đăng nhập:
1. Mở ứng dụng
2. Nhập số điện thoại: `+84123456789`
3. Nhấn "Đăng nhập"
4. Nhập mã OTP nhận được (hoặc mã test nếu đã cấu hình)
5. Đăng nhập thành công

### Test đăng ký:
1. Nhấn "Tạo tài khoản"
2. Nhập số điện thoại: `+84987654321`
3. Nhập mật khẩu: `test123`
4. Nhấn "Đăng ký"
5. Nhập mã OTP
6. Tài khoản được tạo thành công

### Test quên mật khẩu:
1. Nhấn "Quên mật khẩu?"
2. Nhập số điện thoại đã đăng ký
3. Nhấn "Gửi mã OTP"
4. Nhập mã OTP
5. Nhập mật khẩu mới
6. Mật khẩu được cập nhật

## Xử lý lỗi thường gặp

### Lỗi: "This app is not authorized to use Firebase Authentication"
**Giải pháp:** Thêm SHA-1 fingerprint vào Firebase Console

### Lỗi: "We have blocked all requests from this device due to unusual activity"
**Giải pháp:** Đợi một thời gian hoặc sử dụng số điện thoại test

### Lỗi: "Invalid phone number"
**Giải pháp:** Đảm bảo số điện thoại có mã quốc gia (+84)

### Lỗi: "TOO_SHORT" hoặc "INVALID_PHONE_NUMBER"
**Giải pháp:** Kiểm tra định dạng số điện thoại (+84 cho Việt Nam)

## Build và Run

```cmd
cd D:\MyAppshrimp
gradlew clean
gradlew build
gradlew installDebug
```

Hoặc chạy trực tiếp từ Android Studio bằng nút Run.

## Cấu trúc File

```
app/src/main/
├── java/com/dung/myapplication/login/
│   ├── login.kt                    # Đăng nhập với Phone OTP
│   ├── signup.kt                   # Đăng ký với Phone OTP
│   └── ForgotPassword.kt           # Quên mật khẩu với Phone OTP
└── res/layout/
    ├── activity_login.xml          # Layout đăng nhập
    ├── activity_sign_up.xml        # Layout đăng ký
    └── activity_forgot_password.xml # Layout quên mật khẩu
```

## Tính năng bổ sung có thể mở rộng

- [ ] Thêm nút "Gửi lại OTP" sau 60 giây
- [ ] Hiển thị countdown timer cho OTP
- [ ] Lưu số điện thoại đã đăng nhập để tự động điền
- [ ] Xác thực số điện thoại trước khi đăng ký
- [ ] Hỗ trợ nhiều quốc gia (country code picker)

