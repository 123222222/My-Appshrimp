# Hướng Dẫn Đưa Code Lên GitHub

## Bước 1: Chuẩn Bị

Trước tiên, đảm bảo bạn đã:
1. Cài đặt Git trên Windows (tải từ https://git-scm.com/download/win)
2. Có tài khoản GitHub: https://github.com/123222222
3. Đã đăng nhập vào GitHub

## Bước 2: Tạo Repository Mới Trên GitHub

1. Truy cập: https://github.com/123222222
2. Click nút **"New"** hoặc **"New repository"**
3. Đặt tên repository: `MyAppshrimp` hoặc `shrimp-detection-system`
4. Chọn **Private** nếu muốn giữ riêng tư
5. **KHÔNG** chọn "Initialize this repository with a README" (vì chúng ta đã có README)
6. Click **"Create repository"**

## Bước 3: Khởi Tạo Git Local

Mở **Command Prompt** (cmd.exe) và chạy các lệnh sau:

```cmd
cd D:\MyAppshrimp

git init
```

## Bước 4: Cấu Hình Git (Lần Đầu)

Nếu đây là lần đầu tiên sử dụng Git, cấu hình thông tin:

```cmd
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

## Bước 5: Thêm Files Vào Git

```cmd
git add .
```

Lệnh này sẽ thêm tất cả files (trừ những file trong .gitignore)

## Bước 6: Commit Lần Đầu

```cmd
git commit -m "Initial commit: Shrimp Detection System"
```

## Bước 7: Kết Nối Với GitHub Repository

Thay `<repository-name>` bằng tên repository bạn đã tạo ở Bước 2:

```cmd
git branch -M main
git remote add origin https://github.com/123222222/<repository-name>.git
```

Ví dụ:
```cmd
git remote add origin https://github.com/123222222/MyAppshrimp.git
```

## Bước 8: Push Code Lên GitHub

```cmd
git push -u origin main
```

Nếu được yêu cầu đăng nhập:
- Nhập username GitHub: `123222222`
- Nhập password: Sử dụng **Personal Access Token** (không phải mật khẩu GitHub)

### Cách Tạo Personal Access Token:

1. Truy cập: https://github.com/settings/tokens
2. Click **"Generate new token"** > **"Generate new token (classic)"**
3. Đặt tên: `MyAppshrimp-Upload`
4. Chọn quyền: **repo** (tất cả các quyền trong repo)
5. Click **"Generate token"**
6. **COPY** token ngay (chỉ hiển thị 1 lần)
7. Sử dụng token này làm password khi push

## Bước 9: Kiểm Tra

Truy cập repository trên GitHub để xác nhận code đã được upload:
```
https://github.com/123222222/<repository-name>
```

## ⚠️ Lưu Ý Quan Trọng

### Files Nhạy Cảm Đã Được Loại Trừ (.gitignore):
- `google-services.json` (Firebase config)
- `firebase-admin.json` (Firebase credentials)
- `.env` (environment variables)
- `permitted_devices.json` (device bindings)
- `permitted_emails.json` (email permissions)
- `local.properties` (Android local config)

### Nếu Đã Commit Nhầm File Nhạy Cảm:

1. Xóa file khỏi Git (giữ lại local):
```cmd
git rm --cached google-services.json
git rm --cached firebase-admin.json
git rm --cached .env
```

2. Commit lại:
```cmd
git commit -m "Remove sensitive files"
git push
```

## Các Lệnh Git Hữu Ích Khác

### Xem trạng thái:
```cmd
git status
```

### Xem lịch sử commit:
```cmd
git log --oneline
```

### Thêm file mới sau này:
```cmd
git add .
git commit -m "Your commit message"
git push
```

### Xem remote repository:
```cmd
git remote -v
```

## Nếu Gặp Lỗi

### Lỗi: "remote origin already exists"
```cmd
git remote remove origin
git remote add origin https://github.com/123222222/<repository-name>.git
```

### Lỗi: "failed to push some refs"
```cmd
git pull origin main --rebase
git push origin main
```

### Lỗi xác thực (Authentication)
- Sử dụng Personal Access Token thay vì mật khẩu
- Hoặc sử dụng GitHub Desktop: https://desktop.github.com/

## Hoàn Tất! 🎉

Code của bạn đã được đưa lên GitHub thành công!

