# 🔐 Multi-User Independent Device Binding System

## 📌 Tóm Tắt

Hệ thống cho phép **nhiều user độc lập** sử dụng cùng hoặc khác thiết bị mà **không ảnh hưởng lẫn nhau**.

## 🆚 So Sánh Hệ Thống Cũ vs Mới

### ❌ Hệ Thống Cũ (Shared Device)

```json
// permitted_devices.json
{
  "raspberrypi-001": "admin@gmail.com"
}
```

**Vấn đề:**
- ❌ Chỉ 1 người có thể bind device
- ❌ User khác không thể kết nối vào device đã bind
- ❌ Phải unbind mới cho người khác dùng
- ❌ Admin phải quản lý device cho từng user

### ✅ Hệ Thống Mới (Independent Binding)

```json
// permitted_devices.json
{
  "admin@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1701234567
  },
  "user1@gmail.com": {
    "device_id": "raspberrypi-001",  // ✅ Cùng device!
    "ip": "192.168.1.100",
    "last_updated": 1701234568
  },
  "user2@gmail.com": {
    "device_id": "raspberrypi-002",  // ✅ Device khác
    "ip": "192.168.1.101",
    "last_updated": 1701234569
  }
}
```

**Lợi ích:**
- ✅ Mỗi user có device binding riêng
- ✅ Nhiều user có thể dùng cùng 1 device vật lý
- ✅ Unbind của user này không ảnh hưởng user khác
- ✅ Admin chỉ cần cấp quyền email, không quản lý device

## 🎯 Vai Trò & Quyền Hạn

### 👑 Admin
**Email:** Được định nghĩa trong `.env` → `ADMIN_EMAIL`

**Quyền:**
- ✅ Thêm/Xóa user khỏi danh sách permitted
- ✅ Xem danh sách tất cả user được cấp quyền
- ✅ Quét/Kết nối/Hủy device (của chính mình)
- ✅ Xem camera stream
- ✅ Chụp ảnh & nhận diện tôm
- ✅ Xem Gallery & Chart

**Không thể:**
- ❌ Không thể unbind device của user khác
- ❌ Không thể xem device của user khác

### 👤 User Thường (Permitted User)
**Email:** Được admin thêm vào `permitted_emails.json`

**Quyền:**
- ✅ Quét/Kết nối/Hủy device (của chính mình)
- ✅ Xem camera stream
- ✅ Chụp ảnh & nhận diện tôm
- ✅ Xem Gallery & Chart
- ✅ Hoàn toàn độc lập, tự do

**Không thể:**
- ❌ Không thể thêm/xóa user khác
- ❌ Không thể xem/quản lý device của user khác

### 🚫 User Chưa Được Cấp Quyền
**Trạng thái:** Email chưa có trong `permitted_emails.json`

**Hiển thị:**
```
❌ Tài khoản chưa được cấp quyền

Tài khoản của bạn chưa có quyền truy cập hệ thống.
Vui lòng liên hệ Admin để được cấp quyền.

Email Admin: admin@gmail.com
```

**Không thể làm gì cả!**

## 📱 Quy Trình Sử Dụng

### 1️⃣ Admin Cấp Quyền

```
Admin → Profile → Quản lý quyền truy cập
  ↓
Thêm tài khoản
  ↓
Nhập email: user@gmail.com
  ↓
✅ User được cấp quyền
```

### 2️⃣ User Mới Đăng Nhập

```
User → Login Google
  ↓
Backend kiểm tra email trong permitted_emails.json
  ↓
✅ Có trong list → Cho phép truy cập
❌ Không có → Hiện thông báo lỗi
```

### 3️⃣ User Kết Nối Device

```
User → Profile → Quét thiết bị
  ↓
Chọn device từ danh sách
  ↓
Backend lưu: user_email → {device_id, ip}
  ↓
✅ Kết nối thành công
  ↓
User có thể dùng Home/Camera/Gallery/Chart
```

### 4️⃣ User Hủy Kết Nối

```
User → Profile → Hủy kết nối
  ↓
Backend xóa: user_email (chỉ của mình)
  ↓
✅ Hủy thành công
  ↓
⚠️ User khác KHÔNG bị ảnh hưởng!
```

## 🔄 Migration từ Hệ Thống Cũ

Backend tự động migrate khi khởi động:

```python
# Old format detected
{
  "raspberrypi-001": "admin@gmail.com",
  "raspberrypi-002": {
    "email": "user@gmail.com",
    "ip": "192.168.1.100"
  }
}

# Auto migrated to
{
  "admin@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": null,
    "last_updated": 1701234567
  },
  "user@gmail.com": {
    "device_id": "raspberrypi-002",
    "ip": "192.168.1.100",
    "last_updated": 1701234567
  }
}
```

✅ **Tự động, không cần làm gì!**

## 🔐 Security Flow

### Request Authentication

```
1. App gửi request với Firebase ID Token
   ↓
2. Backend verify token với Firebase
   ↓
3. Extract email từ token
   ↓
4. Kiểm tra email trong permitted_emails.json
   ↓
5. ✅ Có → Allow
   ❌ Không → 403 Forbidden
```

### Device Binding Security

```
User A binds device-001:
  permitted_devices["userA@gmail.com"] = {device: "device-001"}

User B binds device-001 (cùng device!):
  permitted_devices["userB@gmail.com"] = {device: "device-001"}
  ✅ OK! Không conflict!

User A unbind:
  delete permitted_devices["userA@gmail.com"]
  ✅ User B's binding vẫn còn nguyên!
```

## 📂 File Structures

### `backend/permitted_emails.json`
```json
[
  "admin@gmail.com",
  "user1@gmail.com",
  "user2@gmail.com"
]
```

### `backend/permitted_devices.json`
```json
{
  "admin@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1701234567
  },
  "user1@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1701234568
  }
}
```

### `backend/.env`
```bash
ADMIN_EMAIL=admin@gmail.com  # Admin email
```

## 🧪 Test Scenarios

### Scenario 1: Admin thêm user mới
```bash
# Before
permitted_emails.json: ["admin@gmail.com"]

# Admin adds user1
POST /api/admin/add-email
Body: {"email": "user1@gmail.com"}

# After
permitted_emails.json: ["admin@gmail.com", "user1@gmail.com"]

✅ user1 có thể đăng nhập!
```

### Scenario 2: Nhiều user dùng cùng device
```bash
# Admin binds device-001
POST /api/devices/bind
Body: {"device_id": "device-001", "device_ip": "192.168.1.100"}

# User1 binds device-001 (cùng device!)
POST /api/devices/bind
Body: {"device_id": "device-001", "device_ip": "192.168.1.100"}

# Result
permitted_devices.json:
{
  "admin@gmail.com": {"device_id": "device-001", ...},
  "user1@gmail.com": {"device_id": "device-001", ...}
}

✅ Cả 2 đều có thể dùng device-001!
```

### Scenario 3: User unbind không ảnh hưởng user khác
```bash
# User1 unbinds
POST /api/devices/unbind
Body: {"device_id": "device-001"}

# Result
permitted_devices.json:
{
  "admin@gmail.com": {"device_id": "device-001", ...}
  // user1 bị xóa
}

✅ Admin vẫn dùng device-001 bình thường!
```

## 🐛 Troubleshooting

### "Tài khoản chưa được cấp quyền"
**Giải pháp:** Liên hệ admin để được thêm email vào permitted list

### "You don't have permission to unbind this device"
**Lỗi cũ (đã fix!):** Xảy ra khi user cố unbind device của người khác
**Hệ thống mới:** Mỗi user chỉ unbind device của mình → Không còn lỗi này!

### Device binding bị mất sau khi unbind
**Lỗi cũ (đã fix!):** Tất cả users bị mất binding
**Hệ thống mới:** Chỉ user unbind bị mất, users khác không ảnh hưởng

## ✅ Benefits Summary

| Feature | Old System | New System |
|---------|-----------|------------|
| Multiple users per device | ❌ No | ✅ Yes |
| Independent bindings | ❌ No | ✅ Yes |
| Unbind affects others | ⚠️ Yes | ✅ No |
| Admin manages devices | ⚠️ Yes | ✅ No (only emails) |
| User flexibility | ❌ Limited | ✅ Full freedom |
| Migration support | ❌ N/A | ✅ Automatic |

## 🎯 Summary

**Hệ thống mới giúp:**
1. ✅ Admin chỉ quản lý email permissions
2. ✅ Users tự do quản lý device của mình
3. ✅ Nhiều users dùng chung device không conflict
4. ✅ Unbind không ảnh hưởng users khác
5. ✅ Migration tự động từ hệ thống cũ

**Perfect for multi-user IoT systems!** 🎉

