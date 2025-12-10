# Hướng dẫn Cấu hình Firebase Phone Authentication - CHI TIẾT

## ✅ BƯỚC 1: Bật Phone trong Sign-in Method (Bạn đang ở đây)

Bạn đã đến đúng màn hình! Bây giờ làm theo:

### Trên màn hình hiện tại:
1. ✅ **Phone** đã được Enable (màu xanh) - ĐÚNG RỒI!
2. 📱 **Phone numbers for testing (optional)** - Có thể bỏ qua hoặc thêm số test
3. ⚡ Nhấn nút **"Save"** ở góc dưới bên phải

### Nếu muốn thêm số điện thoại test (KHÔNG BẮT BUỘC):
- **Phone number**: Ví dụ `+84123456789`
- **Verification code**: Ví dụ `123456` (mã OTP giả để test)
- Nhấn **"Add"**
- Sau đó nhấn **"Save"**

**Lợi ích của số test**: Bạn có thể test app mà không tốn SMS thật.

---

## ⚠️ BƯỚC 2: THÊM SHA-1 FINGERPRINT (BẮT BUỘC - QUAN TRỌNG NHẤT!)

**Phone Authentication SẼ KHÔNG HOẠT ĐỘNG** nếu thiếu bước này!

### Cách lấy SHA-1 Fingerprint:

#### Option 1: Dùng Gradle (KHUYẾN NGHỊ)

**Trên PowerShell (Windows):**
```powershell
cd D:\MyAppshrimp
.\gradlew signingReport
```

**Hoặc trên CMD:**
```cmd
cd D:\MyAppshrimp
gradlew signingReport
```

Tìm dòng có chữ **"SHA1:"** trong kết quả, ví dụ:
```
SHA1: A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0
```

Copy đoạn SHA-1 này (toàn bộ chuỗi sau "SHA1:")

#### Option 2: Dùng Keytool
```cmd
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Tìm dòng **"SHA1:"** và copy.

#### Option 3: Từ Android Studio
1. Mở Android Studio
2. Bên phải, nhấn tab **"Gradle"**
3. Mở: **MyAppshrimp → app → Tasks → android → signingReport**
4. Double-click vào **signingReport**
5. Xem kết quả trong tab "Run", copy SHA-1

---

## 📝 BƯỚC 3: Thêm SHA-1 vào Firebase Console

### Các bước chi tiết:

1. **Quay lại Firebase Console**
   - Vào: https://console.firebase.google.com/
   - Chọn project của bạn

2. **Vào Project Settings**
   - Nhấn vào icon ⚙️ (bánh răng) bên cạnh "Project Overview"
   - Chọn **"Project settings"**

3. **Tìm app Android của bạn**
   - Scroll xuống phần **"Your apps"**
   - Tìm app có Package name: `com.dung.myapplication`
   - (Nếu chưa có app, nhấn "Add app" → "Android" để thêm)

4. **Thêm SHA-1**
   - Trong mục app Android, scroll xuống phần **"SHA certificate fingerprints"**
   - Nhấn nút **"Add fingerprint"**
   - Paste SHA-1 mà bạn đã copy ở Bước 2
   - Nhấn **"Save"**

5. **Tải lại google-services.json**
   - Sau khi thêm SHA-1, nhấn nút **"Download google-services.json"**
   - **QUAN TRỌNG**: Thay thế file cũ tại:
     ```
     D:\MyAppshrimp\app\google-services.json
     ```

---

## 🎯 BƯỚC 4: Verify trong AndroidManifest.xml

Đảm bảo file `AndroidManifest.xml` có quyền Internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

✅ File của bạn đã có sẵn, không cần thay đổi!

---

## 🧪 BƯỚC 5: Test ứng dụng

### Build và chạy ứng dụng:

**PowerShell:**
```powershell
cd D:\MyAppshrimp
.\gradlew clean
.\gradlew installDebug
```

**CMD:**
```cmd
cd D:\MyAppshrimp
gradlew clean
gradlew installDebug
```

Hoặc trong Android Studio: Nhấn nút **Run** (▶️)

### Test trên thiết bị thật (KHUYẾN NGHỊ):
1. Kết nối điện thoại Android qua USB
2. Bật **Developer Options** và **USB Debugging**
3. Chạy app từ Android Studio

### Test với số điện thoại:

**Nếu dùng số TEST (đã thêm ở Bước 1):**
1. Mở app
2. Nhập số test: `+84123456789`
3. Nhấn "Đăng nhập"
4. Nhập mã OTP test: `123456`
5. ✅ Đăng nhập thành công!

**Nếu dùng số THẬT:**
1. Mở app
2. Nhập số điện thoại thật của bạn: `+84xxxxxxxxx`
3. Nhấn "Đăng nhập"
4. Nhận SMS với mã OTP
5. Nhập mã OTP
6. ✅ Đăng nhập thành công!

---

## ❌ Xử lý lỗi thường gặp

### Lỗi 1: "This app is not authorized to use Firebase Authentication"
**Nguyên nhân**: Chưa thêm SHA-1 fingerprint
**Giải pháp**: Làm lại BƯỚC 2 và BƯỚC 3

### Lỗi 2: "An internal error has occurred"
**Nguyên nhân**: File google-services.json cũ
**Giải pháp**: Tải lại google-services.json từ Firebase Console (sau khi thêm SHA-1)

### Lỗi 3: "We have blocked all requests from this device"
**Nguyên nhân**: Firebase phát hiện hoạt động bất thường
**Giải pháp**: 
- Sử dụng số điện thoại test
- Hoặc đợi vài giờ

### Lỗi 4: "Invalid phone number"
**Nguyên nhân**: Số điện thoại sai định dạng
**Giải pháp**: Đảm bảo có **+84** ở đầu (không phải 0)
- ✅ Đúng: `+84987654321`
- ❌ Sai: `0987654321`
- ❌ Sai: `84987654321`

### Lỗi 5: SMS không gửi đến
**Nguyên nhân**: 
- Chưa cấu hình đúng SHA-1
- Hoặc hết quota SMS miễn phí
**Giải pháp**: 
- Kiểm tra lại SHA-1
- Sử dụng số test
- Kiểm tra Firebase Console → Authentication → Usage

---

## 📋 Checklist hoàn chỉnh

Trước khi test app, đảm bảo đã làm đủ các bước:

- [ ] ✅ Bật Phone trong Sign-in method (Firebase Console)
- [ ] ✅ Lấy SHA-1 fingerprint (dùng gradlew signingReport)
- [ ] ✅ Thêm SHA-1 vào Firebase Console (Project Settings → Your apps)
- [ ] ✅ Tải lại file google-services.json mới
- [ ] ✅ Thay thế file google-services.json trong `app/` folder
- [ ] ✅ Clean và build lại project
- [ ] ✅ Test trên thiết bị thật hoặc số test

---

## 🎓 Tóm tắt các lệnh cần chạy

### 1. Lấy SHA-1:

**PowerShell:**
```powershell
cd D:\MyAppshrimp
.\gradlew signingReport
```

**CMD:**
```cmd
cd D:\MyAppshrimp
gradlew signingReport
```

### 2. Build và chạy app:

**PowerShell:**
```powershell
.\gradlew clean
.\gradlew installDebug
```

**CMD:**
```cmd
gradlew clean
gradlew installDebug
```

### 3. Hoặc build từ Android Studio:
- Build → Clean Project
- Build → Rebuild Project
- Run → Run 'app' (▶️)

---

## 📞 Số điện thoại test được khuyến nghị

Để test nhanh mà không tốn SMS, thêm các số này vào Firebase Console:

| Số điện thoại | Mã OTP |
|--------------|--------|
| +84123456789 | 123456 |
| +84987654321 | 654321 |
| +84111111111 | 111111 |

**Cách thêm**: Firebase Console → Authentication → Sign-in method → Phone → Phone numbers for testing

---

## 🚀 Bạn đang ở đâu?

**▶️ BẠN ĐANG Ở: BƯỚC 1** - Bật Phone trong Firebase Console

**➡️ TIẾP THEO LÀM GÌ:**
1. Nhấn nút **"Save"** trên màn hình hiện tại
2. Chạy lệnh `gradlew signingReport` để lấy SHA-1
3. Thêm SHA-1 vào Firebase Console (Project Settings)
4. Tải lại google-services.json
5. Test app!

---

## ⏱️ Thời gian ước tính

- Bước 1 (Bật Phone): **30 giây** ✅ Bạn đã làm xong!
- Bước 2 (Lấy SHA-1): **2 phút**
- Bước 3 (Thêm SHA-1): **3 phút**
- Bước 4 (Test): **5 phút**

**Tổng cộng**: ~10 phút

---

## 💡 Tips quan trọng

1. **SHA-1 là bắt buộc** - Không có SHA-1 = Phone Auth sẽ lỗi 100%
2. **Test trên điện thoại thật** - Emulator thường không nhận được SMS
3. **Dùng số test** để không tốn SMS khi phát triển
4. **Rebuild app** sau khi thay google-services.json

---

## 📸 Bước tiếp theo với ảnh minh họa

Sau khi nhấn **"Save"**, bạn cần vào:
1. **Firebase Console** → Click vào icon ⚙️ (góc trên bên trái)
2. **Project settings**
3. Scroll xuống **"Your apps"**
4. Tìm app Android: `com.dung.myapplication`
5. Scroll xuống **"SHA certificate fingerprints"**
6. Nhấn **"Add fingerprint"**
7. Paste SHA-1 và Save

Sau đó tải lại **google-services.json**!

