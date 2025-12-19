# 🎨 Hướng Dẫn Thêm Logo App

## Logo hiện tại
App đã có logo mặc định được tạo bằng XML vector drawable ở `app/src/main/res/drawable/app_logo.xml`

## Cách thay thế bằng ảnh logo của bạn

### Bước 1: Chuẩn bị ảnh logo
- Format: PNG (nền trong suốt) hoặc JPG
- Kích thước đề xuất: 512x512px hoặc 1024x1024px
- Đặt tên file: `app_logo_image.png` hoặc `app_logo_image.jpg`

### Bước 2: Import ảnh vào project

#### Cách 1: Sử dụng Android Studio
1. Mở Android Studio
2. Click chuột phải vào thư mục `app/src/main/res/drawable`
3. Chọn `Show in Explorer` (Windows) hoặc `Reveal in Finder` (Mac)
4. Copy file ảnh logo của bạn vào thư mục này
5. Đặt tên là `app_logo_image.png`

#### Cách 2: Copy trực tiếp
1. Mở thư mục: `D:\MyAppshrimp\app\src\main\res\drawable\`
2. Copy file ảnh logo của bạn vào đó
3. Đặt tên là `app_logo_image.png`

### Bước 3: Cập nhật layout
Mở file `app/src/main/res/layout/activity_login.xml` và thay đổi dòng:

```xml
android:src="@drawable/app_logo"
```

Thành:

```xml
android:src="@drawable/app_logo_image"
```

### Bước 4: Sync & Build
1. Trong Android Studio, click "Sync Now" (nếu có thông báo)
2. Build lại app: `Build > Rebuild Project`
3. Chạy app để xem logo mới

## Các file logo hiện có

1. **app_logo.xml** - Logo vector mặc định (đang được sử dụng)
   - Đường dẫn: `app/src/main/res/drawable/app_logo.xml`
   - Ưu điểm: Không bị vỡ khi phóng to/thu nhỏ
   
2. **logo_background.xml** - Background hình tròn cho logo
   - Đường dẫn: `app/src/main/res/drawable/logo_background.xml`
   - Có thể dùng làm nền cho logo

## Tùy chỉnh kích thước logo

Trong file `activity_login.xml`, thay đổi:

```xml
<ImageView
    android:id="@+id/appLogo"
    android:layout_width="120dp"    <!-- Thay đổi giá trị này -->
    android:layout_height="120dp"   <!-- Thay đổi giá trị này -->
    ...
/>
```

Kích thước đề xuất: 80dp - 150dp

## Màu sắc chủ đạo của app

Logo hiện tại sử dụng màu:
- Đỏ chính: `#D32F2F`
- Cam điểm nhấn: `#FF6F00`
- Trắng: `#FFFFFF`

Bạn có thể thiết kế logo phù hợp với màu sắc này!

## Lưu ý
- File PNG với nền trong suốt sẽ trông đẹp hơn
- Nên chuẩn bị nhiều kích thước cho các độ phân giải khác nhau
- Có thể tạo các thư mục: `drawable-hdpi`, `drawable-xhdpi`, `drawable-xxhdpi` để tối ưu hiển thị

## Vị trí logo trong app

Logo hiện đang hiển thị ở:
- ✅ Màn hình đăng nhập (Login)

Có thể thêm logo vào:
- Màn hình splash screen
- Toolbar/ActionBar
- About/Settings screen

