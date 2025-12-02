# PHÂN QUYỀN HỆ THỐNG - USER vs ADMIN

## Tóm Tắt

✅ **Tài khoản được add vào hoạt động Y CHANG admin**, chỉ trừ việc không thể quản lý email.

## Phân Quyền Chi Tiết

### 1. ADMIN Email (hodung15032003@gmail.com)

**Quyền đầy đủ - Có thể làm TẤT CẢ:**
- ✅ Xem camera stream (`/blynk_feed`)
- ✅ Detect tôm (`/api/detect-shrimp`)
- ✅ Xem gallery (`/api/shrimp-images`)
- ✅ Xem chi tiết ảnh (`/api/shrimp-images/<id>`)
- ✅ Xóa ảnh (`/api/shrimp-images/<id>`)
- ✅ Bind device (`/api/devices/bind`)
- ✅ Unbind device (`/api/devices/unbind`)
- ✅ Xem device của mình (`/api/devices/my-device`)
- ✅ Kiểm tra quyền device (`/api/devices/check`)
- ✅ **THÊM email mới** (`/api/admin/add-email`) ⭐ CHỈ ADMIN
- ✅ **XEM danh sách email** (`/api/admin/list-emails`) ⭐ CHỈ ADMIN
- ✅ **XÓA email** (`/api/admin/remove-email`) ⭐ CHỈ ADMIN

### 2. USER Thường (Email được add vào)

**Quyền y chang admin, trừ quản lý email:**
- ✅ Xem camera stream (`/blynk_feed`)
- ✅ Detect tôm (`/api/detect-shrimp`)
- ✅ Xem gallery (`/api/shrimp-images`)
- ✅ Xem chi tiết ảnh (`/api/shrimp-images/<id>`)
- ✅ Xóa ảnh (`/api/shrimp-images/<id>`)
- ✅ Bind device (`/api/devices/bind`)
- ✅ Unbind device (`/api/devices/unbind`)
- ✅ Xem device của mình (`/api/devices/my-device`)
- ✅ Kiểm tra quyền device (`/api/devices/check`)
- ❌ **KHÔNG thể thêm email** (`/api/admin/add-email`)
- ❌ **KHÔNG thể xem danh sách email** (`/api/admin/list-emails`)
- ❌ **KHÔNG thể xóa email** (`/api/admin/remove-email`)

## So Sánh Quyền

| Chức Năng | Admin | User Thường |
|-----------|-------|-------------|
| **Camera Stream** | ✅ | ✅ |
| **AI Detection** | ✅ | ✅ |
| **Gallery (Xem/Xóa ảnh)** | ✅ | ✅ |
| **Device Binding** | ✅ | ✅ |
| **Device Unbinding** | ✅ | ✅ |
| **Quản lý Email** | ✅ | ❌ |

## Cách Hoạt Động

### Decorator `@requires_google_auth`
```python
def requires_google_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        id_token = request.headers.get('Authorization')
        if not id_token:
            return jsonify({"success": False, "message": "Missing Google ID token"}), 401
        
        decoded_token = firebase_auth.verify_id_token(id_token)
        email = decoded_token.get('email')
        permitted_emails = load_permitted_emails()
        
        # Kiểm tra email có trong danh sách không
        if email not in permitted_emails:
            return jsonify({"success": False, "message": "Email not permitted"}), 403
        
        request.user_email = email  # Lưu email vào request
        return f(*args, **kwargs)
    return decorated
```

**→ Bất kỳ email nào trong `permitted_emails.json` đều pass được decorator này!**

### Kiểm Tra Admin
```python
@app.route('/api/admin/add-email', methods=['POST'])
@requires_google_auth  # ← Đã pass authentication
def add_permitted_email():
    # Kiểm tra thêm xem có phải admin không
    if request.user_email != ADMIN_EMAIL:
        return jsonify({"success": False, "message": "Only admin can add emails"}), 403
    # ... logic thêm email
```

**→ Chỉ có 3 endpoints này kiểm tra admin, tất cả các endpoint khác không cần!**

## Ví Dụ Thực Tế

### Scenario 1: User Thường Sử Dụng App

1. **User đăng nhập** với email `user@example.com` (đã được admin add)
2. **Mở camera** → ✅ Thành công, xem được stream
3. **Chụp ảnh detect** → ✅ Thành công, AI xử lý và lưu ảnh
4. **Xem gallery** → ✅ Thành công, xem được tất cả ảnh
5. **Xóa ảnh** → ✅ Thành công, xóa được ảnh
6. **Bind device** → ✅ Thành công, device thuộc user này
7. **Thử thêm user mới** → ❌ Lỗi "Only admin can add emails"

### Scenario 2: Admin Sử Dụng App

1. **Admin đăng nhập** với email `hodung15032003@gmail.com`
2. **Mở camera** → ✅ Thành công
3. **Chụp ảnh detect** → ✅ Thành công
4. **Xem gallery** → ✅ Thành công
5. **Xóa ảnh** → ✅ Thành công
6. **Bind device** → ✅ Thành công
7. **Thêm user mới** → ✅ Thành công! (vì là admin)
8. **Xem danh sách user** → ✅ Thành công! (vì là admin)

## Danh Sách Endpoints Đầy Đủ

### A. Endpoints CHO TẤT CẢ USER (admin + user thường)

```
✅ GET  /blynk_feed                        # Camera stream
✅ POST /api/detect-shrimp                 # AI detection
✅ GET  /api/shrimp-images                 # Lấy danh sách ảnh
✅ GET  /api/shrimp-images/<id>            # Chi tiết 1 ảnh
✅ DELETE /api/shrimp-images/<id>          # Xóa ảnh
✅ POST /api/devices/bind                  # Bind device
✅ POST /api/devices/unbind                # Unbind device
✅ GET  /api/devices/my-device             # Xem device của mình
✅ POST /api/devices/check                 # Kiểm tra quyền device
✅ POST /api/devices/access-token          # Lấy access token
```

### B. Endpoints CHỈ CHO ADMIN

```
⭐ POST /api/admin/add-email               # Thêm email mới
⭐ GET  /api/admin/list-emails             # Xem danh sách email
⭐ POST /api/admin/remove-email            # Xóa email
```

### C. Endpoints KHÔNG CẦN AUTH (Public)

```
🔓 POST /api/auth/check                    # Debug authentication
🔓 GET  /health                            # Health check
🔓 GET  /snapshot                          # Single frame snapshot
```

## Cách Test

### Test với User Thường

```bash
# 1. Lấy Firebase ID Token của user thường từ Android app
# 2. Gọi API camera stream
curl -H "Authorization: <USER_FIREBASE_TOKEN>" \
     http://localhost:8000/blynk_feed

# ✅ Kết quả: Thành công, xem được camera

# 3. Thử gọi API add email
curl -X POST http://localhost:8000/api/admin/add-email \
     -H "Authorization: <USER_FIREBASE_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"email": "another@example.com"}'

# ❌ Kết quả: {"success": false, "message": "Only admin can add emails"}
```

### Test với Admin

```bash
# 1. Lấy Firebase ID Token của admin từ Android app
# 2. Gọi API add email
curl -X POST http://localhost:8000/api/admin/add-email \
     -H "Authorization: <ADMIN_FIREBASE_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"email": "newuser@example.com"}'

# ✅ Kết quả: {"success": true, "message": "Email added successfully"}
```

## Tổng Kết

### ✅ Điều Đã Đúng

1. User thường có **TOÀN BỘ quyền** như admin
2. Chỉ khác là **không thể quản lý email** (add/remove user)
3. Tất cả các chức năng chính (camera, detection, gallery, device binding) **đều dùng được**
4. Hệ thống phân quyền **rõ ràng và bảo mật**

### 🎯 Kết Luận

**User được add vào = Admin - Quyền quản lý email**

- ✅ Có thể xem camera
- ✅ Có thể detect tôm
- ✅ Có thể xem/xóa ảnh
- ✅ Có thể bind/unbind device
- ❌ Không thể thêm/xóa user khác

**Đây chính xác là những gì bạn yêu cầu!** 🎉

