@echo off
chcp 65001 >nul
echo ═══════════════════════════════════════════════════════════════
echo DỌN DẸP CÁC FILE .MD KHÔNG CẦN THIẾT
echo ═══════════════════════════════════════════════════════════════
echo.

cd /d "D:\MyAppshrimp"

echo Đang xóa các file FIX_* (log sửa lỗi)...
del /f /q FIX_CAMERA_AUTHENTICATION.md 2>nul
del /f /q FIX_DEVICE_BINDING_MISSING_IP.md 2>nul
del /f /q FIX_DEVICE_BINDING_PERMISSION.md 2>nul
del /f /q FIX_EMAIL_PERMISSION.md 2>nul
del /f /q FIX_LOG_CHART_CAMERA.md 2>nul
del /f /q FIX_LOG_IMAGE_DETAIL.md 2>nul
del /f /q FIX_PERSIST_DEVICE_CONNECTION.md 2>nul
echo ✓ Đã xóa các file FIX_*

echo.
echo Đang xóa các file implementation/changes log...
del /f /q IMPLEMENTATION_COMPLETE.md 2>nul
del /f /q CHANGES_SUMMARY.md 2>nul
del /f /q TOKEN_REFRESH_IMPLEMENTATION.md 2>nul
echo ✓ Đã xóa các file implementation log

echo.
echo Đang xóa các file duplicate...
del /f /q QUICK_START_NEW.md 2>nul
del /f /q DEVICE_BINDING_SECURITY_FIX.md 2>nul
del /f /q ANDROID_DEVICE_BINDING_FOR_ALL_USERS.md 2>nul
del /f /q EMAIL_PERMISSION_MANAGEMENT_GUIDE.md 2>nul
del /f /q USER_PERMISSIONS_EXPLAINED.md 2>nul
del /f /q HOW_TO_CLEAR_DEVICE_BINDING.md 2>nul
del /f /q GIAI_THICH_DON_GIAN.md 2>nul
echo ✓ Đã xóa các file duplicate

echo.
echo Đang xóa file hướng dẫn upload GitHub (đã xong)...
del /f /q GITHUB_UPLOAD_GUIDE.md 2>nul
echo ✓ Đã xóa GITHUB_UPLOAD_GUIDE.md

echo.
echo Đang xóa file UPDATE_NGROK_URL_GUIDE.md...
del /f /q UPDATE_NGROK_URL_GUIDE.md 2>nul
echo ✓ Đã xóa UPDATE_NGROK_URL_GUIDE.md

echo.
echo ═══════════════════════════════════════════════════════════════
echo HOÀN TẤT! CÁC FILE ĐÃ GIỮ LẠI:
echo ═══════════════════════════════════════════════════════════════
echo.
echo ✓ README.md - Giới thiệu dự án chính
echo ✓ ARCHITECTURE.md - Kiến trúc hệ thống
echo ✓ BACKEND_API_DOCS.md - Tài liệu API
echo ✓ DEVICE_BINDING_GUIDE.md - Hướng dẫn device binding
echo ✓ REMOTE_ACCESS_GUIDE.md - Hướng dẫn truy cập từ xa
echo ✓ QUICK_START.md - Hướng dẫn bắt đầu nhanh
echo ✓ TOM_TAT_HE_THONG.md - Tóm tắt hệ thống (tiếng Việt)
echo ✓ backend\models\README.md - Thông tin model AI
echo.
echo ═══════════════════════════════════════════════════════════════
echo.
pause

