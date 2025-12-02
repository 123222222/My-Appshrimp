# Email Permission Management Guide

## Tổng quan

Hệ thống sử dụng 2 cơ chế phân quyền:
1. **Email Permission**: Xác định ai có thể truy cập hệ thống
2. **Device Binding**: (Tùy chọn) Liên kết thiết bị với user

## 1. Email Permission

### File: `permitted_emails.json`

File này chứa danh sách email được phép truy cập hệ thống.

```json
[
  "hodung15032003@gmail.com",
  "user1@example.com",
  "user2@example.com"
]
```

### Quản lý Email qua API (Từ Android App)

#### 1.1. Thêm Email Mới (Admin Only)

**Endpoint**: `POST /api/admin/add-email`

**Headers**:
```
Authorization: <FIREBASE_ID_TOKEN>
Content-Type: application/json
```

**Body**:
```json
{
  "email": "newuser@example.com"
}
```

**Response Success**:
```json
{
  "success": true,
  "message": "Email added successfully"
}
```

**Response Error (Not Admin)**:
```json
{
  "success": false,
  "message": "Only admin can add emails"
}
```

#### 1.2. Xem Danh Sách Email (Admin Only)

**Endpoint**: `GET /api/admin/list-emails`

**Headers**:
```
Authorization: <FIREBASE_ID_TOKEN>
```

**Response**:
```json
{
  "success": true,
  "emails": [
    "hodung15032003@gmail.com",
    "user1@example.com"
  ],
  "admin_email": "hodung15032003@gmail.com"
}
```

#### 1.3. Xóa Email (Admin Only)

**Endpoint**: `POST /api/admin/remove-email`

**Headers**:
```
Authorization: <FIREBASE_ID_TOKEN>
Content-Type: application/json
```

**Body**:
```json
{
  "email": "user1@example.com"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Email removed successfully"
}
```

**Note**: Không thể xóa email admin

### Quản lý Email qua Command Line

Sử dụng script `manage_emails.py`:

```bash
# Xem danh sách email
python manage_emails.py list

# Thêm email mới
python manage_emails.py add newuser@example.com

# Xóa email
python manage_emails.py remove user@example.com
```

## 2. Device Binding (Tùy chọn)

Device binding liên kết thiết bị với một user cụ thể. Một thiết bị chỉ có thể bind với 1 user tại một thời điểm.

### File: `permitted_devices.json`

```json
{
  "raspberrypi-001": {
    "email": "hodung15032003@gmail.com",
    "ip": "192.168.1.100",
    "last_updated": 1701518400.0
  }
}
```

### 2.1. Bind Device

**Endpoint**: `POST /api/devices/bind`

**Headers**:
```
Authorization: <FIREBASE_ID_TOKEN>
Content-Type: application/json
```

**Body**:
```json
{
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Device bound successfully",
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

### 2.2. Kiểm Tra Device Binding

**Endpoint**: `GET /api/devices/my-device`

**Headers**:
```
Authorization: <FIREBASE_ID_TOKEN>
```

**Response (Có device)**:
```json
{
  "success": true,
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100",
  "bound": true
}
```

**Response (Chưa có device)**:
```json
{
  "success": true,
  "device_id": null,
  "device_ip": null,
  "bound": false,
  "message": "No device bound to this account"
}
```

### 2.3. Unbind Device

**Endpoint**: `POST /api/devices/unbind`

**Headers**:
```
Authorization: <FIREBASE_ID_TOKEN>
Content-Type: application/json
```

**Body**:
```json
{
  "device_id": "raspberrypi-001"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Device unbound successfully"
}
```

## 3. Luồng Hoạt Động

### 3.1. Admin Cấp Quyền Cho User Mới

1. Admin đăng nhập vào app với email `hodung15032003@gmail.com`
2. Admin gọi API `/api/admin/add-email` với email của user mới
3. User mới được thêm vào `permitted_emails.json`
4. User mới có thể đăng nhập và truy cập các chức năng:
   - ✅ Camera Stream (`/blynk_feed`)
   - ✅ Detection API (`/api/detect-shrimp`)
   - ✅ Gallery API (`/api/shrimp-images`)

### 3.2. User Đăng Nhập Lần Đầu

1. User đăng nhập với Google Sign-In trên Android app
2. App gửi Firebase ID Token lên server
3. Server verify token và kiểm tra email trong `permitted_emails.json`
4. Nếu email có trong danh sách → Cho phép truy cập
5. Nếu email không có → Trả về lỗi 403 Forbidden

### 3.3. User Bind Device (Tùy chọn)

1. User discover device qua UDP broadcast
2. User gọi API `/api/devices/bind` với `device_id`
3. Device được liên kết với email của user
4. User có thể quản lý device của mình

## 4. Troubleshooting

### Vấn đề: "Email not permitted" sau khi đã thêm email

**Nguyên nhân**: File `permitted_emails.json` không tồn tại hoặc không đúng format

**Giải pháp**:
```bash
# Kiểm tra file có tồn tại không
ls -la permitted_emails.json

# Xem nội dung file
cat permitted_emails.json

# Nếu file không tồn tại, tạo mới
echo '["hodung15032003@gmail.com"]' > permitted_emails.json

# Hoặc dùng script
python manage_emails.py list
```

### Vấn đề: User không thể truy cập camera stream

**Nguyên nhân**: Email chưa được thêm vào permitted list

**Giải pháp**:
```bash
# Thêm email qua command line
python manage_emails.py add user@example.com

# Hoặc gọi API từ admin account
curl -X POST http://localhost:8000/api/admin/add-email \
  -H "Authorization: <ADMIN_FIREBASE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'
```

### Vấn đề: Admin không thể thêm email mới

**Nguyên nhân**: 
1. Token không đúng
2. Email trong token không phải admin email

**Giải pháp**:
```bash
# Kiểm tra ADMIN_EMAIL trong .env
cat .env | grep ADMIN_EMAIL

# Kiểm tra email trong token
# Decode JWT token tại https://jwt.io
```

## 5. Best Practices

1. **Backup Files**: Backup `permitted_emails.json` và `permitted_devices.json` thường xuyên
2. **Admin Email**: Không được xóa hoặc thay đổi admin email
3. **Security**: Không share Firebase ID Token với người khác
4. **Logging**: Kiểm tra server logs để debug authentication issues

## 6. API Endpoints Summary

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/admin/add-email` | POST | Admin Only | Thêm email mới |
| `/api/admin/list-emails` | GET | Admin Only | Xem danh sách email |
| `/api/admin/remove-email` | POST | Admin Only | Xóa email |
| `/api/devices/bind` | POST | User | Bind device |
| `/api/devices/unbind` | POST | User | Unbind device |
| `/api/devices/my-device` | GET | User | Xem device của user |
| `/blynk_feed` | GET | User | Camera stream |
| `/api/detect-shrimp` | POST | User | Detect tôm |
| `/api/shrimp-images` | GET | User | Lấy danh sách ảnh |

## 7. Kiểm Tra Quyền Truy Cập

```bash
# Check logs khi user đăng nhập
tail -f backend.log | grep AUTH

# Example output:
# [AUTH] Received id_token: eyJhbGc...
# [AUTH] Decoded email: user@example.com
# [AUTH] Permitted emails: ['hodung15032003@gmail.com', 'user@example.com']
# [AUTH] Email not permitted: other@example.com  # ← Nếu email không được phép
```

