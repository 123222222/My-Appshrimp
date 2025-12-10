# ✅ Cập nhật Admin Phone sang +84987648717

## Đã thay đổi:

### 1. Backend (app_complete.py)
```python
ADMIN_PHONE = '+84987648717'  # Updated
```

### 2. Backend (permitted_phones.json)
```json
["+84987648717"]
```

### 3. Android (ProfileScreen.kt)
```kotlin
val ADMIN_PHONE = "+84987648717"  // Your admin phone
```

## ⚠️ CẦN LÀM THÊM:

### Cập nhật Firestore để set role admin

Vào **Firebase Console → Firestore → Collection `users`**:

#### Nếu đã có document với ID `+84987648717`:
1. Click vào document **+84987648717**
2. Edit field **`role`** → Đổi thành **`"admin"`**
3. Save

#### Nếu chưa có:
**Cách 1: Đăng ký trong app**
```
1. Mở app → Đăng ký
   Phone: +84987648717
   Email: youremail@example.com
   Password: yourpassword

2. Sau khi đăng ký → Vào Firestore
3. Tìm document +84987648717
4. Edit field "role" → "admin"
```

**Cách 2: Tạo thủ công trong Firestore**
```
Document ID: +84987648717

Fields:
{
  phoneNumber: "+84987648717",
  email: "admin@example.com",
  password: "hashed_password",
  username: "admin",
  fullName: "Admin User",
  role: "admin",  ← QUAN TRỌNG!
  avatarResId: 2131230784,
  bio: "Administrator",
  createdAt: 1733856000000
}
```

## 🚀 Test Flow

### 1. Restart Backend
```bash
cd D:\MyAppshrimp\backend
python app_complete.py
```

### 2. Build App
```bash
cd D:\MyAppshrimp
.\gradlew.bat assembleDebug
```

### 3. Test Admin Features
```
1. Đăng nhập với: +84987648717
2. Vào Profile
3. Thấy card "🔐 Quản lý quyền truy cập"
4. Tab "📧 Email" và "📱 Phone"
5. ✅ Số +84987648717 hiển thị với badge "👑 Admin Phone"
```

## ✅ Summary

Admin phone đã được đổi từ:
- ❌ **+84123456789** (default)
- ✅ **+84987648717** (your number)

Backend và app đã sync. Chỉ cần:
1. Update Firestore (set role = "admin")
2. Restart backend
3. Test!

