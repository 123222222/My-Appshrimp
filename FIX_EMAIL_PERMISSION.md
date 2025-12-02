# HƯỚNG DẪN: Sửa Lỗi "Email not permitted"

## Vấn Đề Đã Sửa

Bạn đã gặp vấn đề: Sau khi thêm email vào quyền truy cập, nhưng khi đăng nhập vào tài khoản mới đó thì hệ thống không cho phép sử dụng.

## Nguyên Nhân

1. **File `permitted_emails.json` không tồn tại**: Hệ thống chỉ cho phép admin email truy cập
2. **Yêu cầu device binding**: Trước đây endpoint `/blynk_feed` yêu cầu user phải bind device trước, gây phức tạp không cần thiết

## Các Thay Đổi Đã Thực Hiện

### 1. Tạo File `permitted_emails.json`
✅ Đã tạo file với admin email mặc định:
```json
["hodung15032003@gmail.com"]
```

### 2. Loại Bỏ Yêu Cầu Device Binding
✅ Endpoint `/blynk_feed` (camera stream) không còn yêu cầu device binding
- User chỉ cần email được thêm vào danh sách là có thể truy cập camera
- Device binding giờ là **tùy chọn**, chỉ để quản lý thiết bị

### 3. Thêm Debug Endpoint
✅ Endpoint mới `/api/auth/check` để kiểm tra authentication status
- Không cần authentication
- Giúp debug khi có vấn đề về quyền truy cập

### 4. Thêm Script Quản Lý Email
✅ File `manage_emails.py` để quản lý email qua command line:
```bash
# Xem danh sách
python manage_emails.py list

# Thêm email
python manage_emails.py add user@example.com

# Xóa email  
python manage_emails.py remove user@example.com
```

## Cách Sử Dụng

### Cách 1: Qua Android App (Admin)

1. **Đăng nhập** với admin email (`hodung15032003@gmail.com`)

2. **Thêm email mới**:
```
POST /api/admin/add-email
Headers: 
  Authorization: <FIREBASE_ID_TOKEN>
  Content-Type: application/json
Body:
  {"email": "newuser@example.com"}
```

3. **User mới đăng nhập** và có thể:
   - ✅ Xem camera stream (`/blynk_feed`)
   - ✅ Detect tôm (`/api/detect-shrimp`)
   - ✅ Xem gallery (`/api/shrimp-images`)
   - ✅ (Tùy chọn) Bind device (`/api/devices/bind`)

### Cách 2: Qua Command Line

```bash
cd D:\MyAppshrimp\backend

# Thêm email mới
python manage_emails.py add newuser@example.com

# Xem danh sách
python manage_emails.py list
```

## Kiểm Tra Quyền Truy Cập

### Test với Android App:

Gọi API debug (không cần auth):
```
POST /api/auth/check
Headers:
  Authorization: <FIREBASE_ID_TOKEN>
```

Response sẽ cho biết:
- ✅ Token có hợp lệ không
- ✅ Email đã được decode ra sao
- ✅ Email có trong danh sách permitted không
- ✅ Có phải admin không
- ✅ Có device nào đã bind không

### Example Response:
```json
{
  "token_valid": true,
  "decoded_email": "newuser@example.com",
  "permitted_emails": ["hodung15032003@gmail.com", "newuser@example.com"],
  "email_permitted": true,
  "is_admin": false,
  "has_device_bound": false
}
```

## Luồng Hoạt Động Mới

```
1. Admin thêm email → permitted_emails.json
2. User đăng nhập với email đó
3. Server verify Firebase token
4. Server check email trong permitted_emails.json
5. ✅ Cho phép truy cập TẤT CẢ APIs (không cần bind device)
```

## Troubleshooting

### Vẫn báo "Email not permitted"?

1. **Kiểm tra file có tồn tại không**:
```bash
dir D:\MyAppshrimp\backend\permitted_emails.json
```

2. **Xem nội dung file**:
```bash
type D:\MyAppshrimp\backend\permitted_emails.json
```

3. **Thêm lại email**:
```bash
python manage_emails.py add your-email@gmail.com
```

4. **Restart server**:
```bash
# Ctrl+C để stop
python app_complete.py
```

### Token không hợp lệ?

1. **Kiểm tra Firebase config** trong Android app
2. **Đảm bảo** đã đăng nhập Google trên app
3. **Xem logs** server khi gọi API

## Files Mới Được Tạo

1. ✅ `permitted_emails.json` - Danh sách email được phép
2. ✅ `manage_emails.py` - Script quản lý email
3. ✅ `EMAIL_PERMISSION_MANAGEMENT_GUIDE.md` - Hướng dẫn chi tiết

## Tổng Kết

Bây giờ hệ thống đơn giản hơn:
- ✅ Admin chỉ cần thêm email → User có thể dùng ngay
- ✅ Không cần bind device để xem camera
- ✅ Device binding là tùy chọn (để quản lý thiết bị)
- ✅ Có endpoint debug để kiểm tra quyền

Mọi thứ đã được fix! 🎉

