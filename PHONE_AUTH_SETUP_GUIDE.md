# Hướng dẫn cấu hình Phone Authentication

## Tổng quan
Hệ thống hiện đã hỗ trợ đăng nhập bằng số điện thoại với đầy đủ tính năng như đăng nhập Google (device binding, camera stream, detection, etc.)

## Cấu hình Backend

### 1. Tạo file `permitted_phones.json` trong thư mục backend

```json
["+84123456789"]
```

**Lưu ý**: 
- Admin phone mặc định là `+84123456789` (có thể thay đổi qua biến môi trường `ADMIN_PHONE`)
- Phone number phải bắt đầu với `+` theo format quốc tế

### 2. Cập nhật file `.env` (nếu có)

Thêm dòng:
```
ADMIN_PHONE=+84123456789
```

### 3. Restart backend server

```bash
cd backend
python app_complete.py
```

## Cấu hình Firestore

### 1. Set role "admin" cho phone user trong Firestore

Vào Firebase Console → Firestore Database → Collection `users` → Tìm document có ID là số điện thoại admin (ví dụ `+84123456789`)

Thêm/cập nhật field:
```
role: "admin"
```

### 2. Cấu trúc document user trong Firestore

```javascript
{
  phoneNumber: "+84xxxxxxxxx",
  email: "user@example.com",
  password: "hashed_password",
  username: "username",
  fullName: "Full Name",
  role: "user",  // hoặc "admin"
  avatarResId: 2131230784,  // resource ID
  bio: "User bio",
  createdAt: 1234567890
}
```

## Sử dụng

### Dành cho Admin (đăng nhập bằng email Google hoặc phone)

1. **Cấp quyền cho phone numbers:**
   - Vào Profile → "Quản lý quyền truy cập" (chỉ admin mới thấy)
   - Click "Thêm Phone" để thêm số điện thoại mới
   - Nhập số điện thoại theo format: `+84xxxxxxxxx`
   
2. **Xóa quyền:**
   - Click icon xóa bên cạnh phone number cần xóa
   - Không thể xóa phone admin

### Dành cho User thường (đăng nhập bằng phone)

1. **Đăng ký tài khoản:**
   - Tại màn hình Login, click "Đăng ký"
   - Nhập số điện thoại (format: `+84xxxxxxxxx`), email, và mật khẩu
   - Hệ thống tự động tạo user với role "user"

2. **Chờ Admin cấp quyền:**
   - Admin cần thêm số điện thoại của bạn vào danh sách permitted phones
   - Sau khi được cấp quyền, bạn có thể:
     - Quét và kết nối thiết bị Raspberry Pi
     - Xem camera stream
     - Sử dụng tất cả tính năng như user Google

3. **Kết nối thiết bị:**
   - Vào Profile → "Quản lý thiết bị"
   - Click "Quét thiết bị" để tìm Raspberry Pi
   - Chọn thiết bị muốn kết nối

## API Endpoints mới

### Admin endpoints (chỉ admin mới gọi được)

#### 1. Thêm phone number
```http
POST /api/admin/add-phone
Authorization: <firebase_token> hoặc X-Phone-Auth: <admin_phone>
Content-Type: application/json

{
  "phone": "+84xxxxxxxxx"
}
```

#### 2. Xóa phone number
```http
POST /api/admin/remove-phone
Authorization: <firebase_token> hoặc X-Phone-Auth: <admin_phone>
Content-Type: application/json

{
  "phone": "+84xxxxxxxxx"
}
```

#### 3. Danh sách phone numbers
```http
GET /api/admin/list-phones
Authorization: <firebase_token> hoặc X-Phone-Auth: <admin_phone>
```

### User endpoints (cả email và phone đều gọi được)

Tất cả các endpoint hiện có (`/api/devices/bind`, `/api/devices/unbind`, `/blynk_feed`, etc.) đều hỗ trợ cả 2 loại authentication:

**Google Authentication:**
```http
Authorization: <firebase_id_token>
```

**Phone Authentication:**
```http
X-Phone-Auth: +84xxxxxxxxx
```

## Kiểm tra quyền

### Kiểm tra authentication status
```http
POST /api/auth/check
X-Phone-Auth: +84xxxxxxxxx (hoặc Authorization: <token>)
```

Response:
```json
{
  "auth_type": "phone",
  "phone_number": "+84xxxxxxxxx",
  "phone_permitted": true,
  "is_admin": false,
  "user_devices": []
}
```

## Troubleshooting

### Lỗi "Phone number not permitted"
- Kiểm tra file `permitted_phones.json` đã được tạo chưa
- Kiểm tra phone number có trong danh sách không
- Phone number phải đúng format (bắt đầu với `+`)

### Lỗi "Missing authentication"
- Kiểm tra header `X-Phone-Auth` đã được gửi chưa
- Kiểm tra phone number chính xác

### Admin phone không hoạt động
- Kiểm tra biến `ADMIN_PHONE` trong backend
- Mặc định là `+84123456789`
- Đảm bảo phone number trong Firestore có `role: "admin"`

## Lưu ý bảo mật

1. **Phone number validation:**
   - Backend chỉ chấp nhận phone với format `+[country_code][number]`
   - Không chấp nhận phone number local (không có `+`)

2. **Admin protection:**
   - Không thể xóa admin phone khỏi danh sách permitted
   - Admin có thể là email hoặc phone number

3. **Device binding:**
   - Mỗi device chỉ có thể bind với 1 user (email hoặc phone)
   - User identifier được lưu trong `permitted_devices.json`

## Cập nhật UI Android

ProfileScreen đã được cập nhật để:
- Tự động phát hiện loại đăng nhập (Google hoặc Phone)
- Gửi đúng header tùy theo loại authentication
- Hiển thị thông tin user từ UserSession
- Hỗ trợ admin phone quản lý permissions

## Testing

1. **Đăng ký user mới với phone:**
   ```
   Phone: +84987654321
   Email: test@example.com
   Password: 123456
   ```

2. **Admin cấp quyền:**
   - Đăng nhập bằng admin account
   - Vào Profile → Thêm phone `+84987654321`

3. **Login và test:**
   - Đăng nhập lại bằng phone `+84987654321`
   - Vào Profile → Quét thiết bị
   - Kết nối và test camera stream

## Checklist

- [ ] Backend đã restart với code mới
- [ ] File `permitted_phones.json` đã được tạo
- [ ] Admin phone đã được set trong Firestore với `role: "admin"`
- [ ] App Android đã rebuild với code mới
- [ ] Test đăng ký user phone mới
- [ ] Test admin cấp quyền cho phone
- [ ] Test device binding với phone user
- [ ] Test camera stream với phone user

