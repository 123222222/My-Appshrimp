# 🚀 Quick Start - Phone Authentication

## Build & Install (5 phút)

### 1. Sync Gradle
```
Android Studio → File → Sync Project with Gradle Files
(Hoặc click banner "Sync Now" phía trên)
```

### 2. Build APK
```bash
cd D:\MyAppshrimp
.\gradlew.bat assembleDebug
```

### 3. Install APK
```
File: app\build\outputs\apk\debug\Kl-demo-1.0.apk
→ Copy sang điện thoại và cài đặt
```

## Setup Admin Phone (2 phút)

### Cách 1: Đăng ký trong app rồi update Firestore
```
1. Mở app → Đăng ký
   Phone: +84123456789
   Email: admin@example.com
   Password: admin123

2. Firebase Console → Firestore → users → +84123456789
3. Edit field "role" → "admin"
4. Save
```

### Cách 2: Tạo trực tiếp trong Firestore
```javascript
// Firebase Console → Firestore → users → Add document
// Document ID: +84123456789

{
  phoneNumber: "+84123456789",
  email: "admin@example.com",
  password: "7c6a180b36896a0a8c02787eeafb0e4c",  // hash của "password1"
  username: "admin",
  fullName: "Administrator",
  role: "admin",
  avatarResId: 2131230784,
  bio: "System Administrator",
  createdAt: 1733856000000
}
```

## Test Flow (3 phút)

### Test Admin UI
```
1. Đăng nhập: +84123456789 / admin123
2. Vào Profile (icon dưới cùng)
3. Scroll xuống → Thấy 2 cards:
   ✅ "Quản lý quyền truy cập" (Email)
   ✅ "📱 Quản lý Phone Numbers" (NEW!)
4. Click "+ Thêm Phone"
5. Nhập: +84987654321
6. ✅ Done!
```

### Test Phone User
```
1. Đăng ký user mới: +84987654321
2. Đăng nhập
3. Vào Profile → Click "Quét thiết bị"
4. Chọn thiết bị Raspberry Pi
5. ✅ Kết nối thành công!
6. Vào Home → ✅ Xem camera stream
```

## Troubleshooting (1 phút fix)

### "Unresolved reference 'gson'"
```
Settings → Gradle → Bỏ tick "Offline work" → Sync
```

### "Phone not permitted"
```
Admin thêm phone qua Profile screen → "+ Thêm Phone"
```

### Backend không chạy
```bash
cd D:\MyAppshrimp\backend
python app_complete.py
```

## Admin Commands

### Thêm Phone Permission
```
Profile → 📱 Quản lý Phone Numbers → + Thêm Phone
```

### Xóa Phone Permission  
```
Profile → 📱 Quản lý Phone Numbers → Click icon 🗑️ bên cạnh phone
```

### Xem danh sách Phones
```
Profile → 📱 Quản lý Phone Numbers → Scroll list
```

## Important Notes

⚠️ Phone phải bắt đầu với `+` (ví dụ: +84987654321)
⚠️ Admin phone mặc định: +84123456789
⚠️ Backend phải chạy trước khi test
⚠️ App và Raspberry Pi phải cùng WiFi

## Done! 🎉

Hệ thống đã sẵn sàng. Build và test ngay!

