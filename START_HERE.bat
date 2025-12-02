@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║     ĐƯA CODE LÊN GITHUB - HƯỚNG DẪN NHANH            ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo 📋 CÁC BƯỚC THỰC HIỆN:
echo.
echo ┌─ BƯỚC 1: TẠO REPOSITORY TRÊN GITHUB ─────────────────┐
echo │                                                        │
echo │  1. Mở trình duyệt và truy cập:                      │
echo │     👉 https://github.com/123222222                   │
echo │                                                        │
echo │  2. Click nút "New" (màu xanh lá)                    │
echo │                                                        │
echo │  3. Điền thông tin:                                   │
echo │     • Repository name: MyAppshrimp                    │
echo │     • Description: Shrimp Detection System            │
echo │     • Chọn Private (nếu muốn giữ riêng tư)          │
echo │     • ❌ KHÔNG chọn "Add a README file"              │
echo │     • ❌ KHÔNG chọn .gitignore                        │
echo │                                                        │
echo │  4. Click "Create repository"                         │
echo │                                                        │
echo └────────────────────────────────────────────────────────┘
echo.
pause
echo.
echo ┌─ BƯỚC 2: CHẠY SCRIPT TỰ ĐỘNG ────────────────────────┐
echo │                                                        │
echo │  Script sẽ tự động thực hiện:                         │
echo │  ✓ Khởi tạo Git repository                           │
echo │  ✓ Cấu hình thông tin                                │
echo │  ✓ Thêm tất cả files (trừ file nhạy cảm)           │
echo │  ✓ Commit code                                        │
echo │  ✓ Push lên GitHub                                    │
echo │                                                        │
echo └────────────────────────────────────────────────────────┘
echo.

set /p READY="Bạn đã tạo repository trên GitHub chưa? (Y/N): "
if /i not "%READY%"=="Y" (
    echo.
    echo ❌ Vui lòng tạo repository trên GitHub trước rồi chạy lại script này!
    echo.
    pause
    exit /b 0
)

echo.
echo ═══════════════════════════════════════════════════════
echo.

REM Chuyển đến thư mục dự án
cd /d "D:\MyAppshrimp"

REM Gọi script chính
call setup_git_and_push.bat

echo.
echo ═══════════════════════════════════════════════════════
echo 🎉 HOÀN TẤT!
echo ═══════════════════════════════════════════════════════
pause

