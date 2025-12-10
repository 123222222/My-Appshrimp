# ⚡ HƯỚNG DẪN NHANH - LẤY SHA-1

## ❗ LỖI BẠN GẶP PHẢI

```
gradlew : The term 'gradlew' is not recognized...
```

**NGUYÊN NHÂN**: PowerShell yêu cầu thêm `.\` trước lệnh trong thư mục hiện tại.

---

## ✅ GIẢI PHÁP

### Cách 1: Dùng PowerShell (Khuyến nghị)

```powershell
cd D:\MyAppshrimp
.\gradlew signingReport
```

⚠️ **CHÚ Ý**: Phải có dấu `.\` trước `gradlew`

### Cách 2: Dùng CMD thay vì PowerShell

1. Mở **Command Prompt** (CMD) thay vì PowerShell
2. Chạy lệnh:

```cmd
cd D:\MyAppshrimp
gradlew signingReport
```

---

## 📋 COPY LỆNH ĐÂY (PowerShell)

```powershell
cd D:\MyAppshrimp
.\gradlew signingReport
```

---

## 🔍 TÌM SHA-1 TRONG KẾT QUẢ

Sau khi chạy lệnh, tìm đoạn này trong output:

```
Variant: debug
Config: debug
Store: C:\Users\YourName\.android\debug.keystore
Alias: AndroidDebugKey
MD5: 12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF
SHA1: A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0
SHA-256: ...
```

**COPY ĐOẠN SHA1** (toàn bộ chuỗi sau "SHA1:")

Ví dụ: `A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0`

---

## 🎯 SAU KHI CÓ SHA-1

1. **Vào Firebase Console**: https://console.firebase.google.com/
2. Chọn project của bạn
3. Click icon ⚙️ → **Project settings**
4. Scroll xuống **"Your apps"**
5. Tìm app: `com.dung.myapplication`
6. Scroll xuống **"SHA certificate fingerprints"**
7. Click **"Add fingerprint"**
8. Paste SHA-1
9. Click **"Save"**
10. **Download google-services.json mới**
11. Thay thế file cũ tại: `D:\MyAppshrimp\app\google-services.json`

---

## 🚀 BUILD VÀ CHẠY APP

Sau khi thêm SHA-1 và cập nhật google-services.json:

```powershell
.\gradlew clean
.\gradlew installDebug
```

Hoặc trong Android Studio: **Build → Clean Project** → **Run**

---

## ❓ CÁC LỆNH KHÁC

### Kiểm tra Gradle có hoạt động không:
```powershell
.\gradlew --version
```

### Clean project:
```powershell
.\gradlew clean
```

### Build APK:
```powershell
.\gradlew assembleDebug
```

### Cài đặt app lên điện thoại:
```powershell
.\gradlew installDebug
```

---

## 💡 TIP: Tạo alias cho PowerShell (Tùy chọn)

Nếu không muốn gõ `.\` mỗi lần:

```powershell
Set-Alias -Name gradlew -Value .\gradlew
```

Sau đó có thể chạy:
```powershell
gradlew signingReport
```

**Lưu ý**: Alias này chỉ tồn tại trong phiên PowerShell hiện tại.

---

## 📞 HỖ TRỢ

Nếu gặp lỗi khác, kiểm tra:
- ✅ Java đã cài đặt chưa: `java -version`
- ✅ JAVA_HOME đã set chưa: `echo $env:JAVA_HOME`
- ✅ File `gradlew.bat` có trong thư mục `D:\MyAppshrimp` không

---

## ⏰ THỜI GIAN

- Chạy lệnh: **30 giây - 2 phút**
- Thêm SHA-1 vào Firebase: **2 phút**
- Build và test: **3-5 phút**

**TỔNG**: ~5-10 phút

---

## ✅ CHECKLIST

- [ ] Chạy `.\gradlew signingReport` (có dấu `.\`)
- [ ] Copy SHA-1 từ kết quả
- [ ] Thêm SHA-1 vào Firebase Console
- [ ] Download google-services.json mới
- [ ] Thay thế file google-services.json
- [ ] Clean và build lại project
- [ ] Chạy app và test Phone Authentication

