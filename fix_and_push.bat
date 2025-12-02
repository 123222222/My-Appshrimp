@echo off
echo Dang sua loi Git...
echo.

cd /d "D:\MyAppshrimp"

echo [1] Xoa remote origin cu...
git remote remove origin
echo [OK] Da xoa remote origin cu
echo.

echo [2] Kiem tra trang thai Git...
git status
echo.

echo [3] Nhap URL repository CHINH XAC:
echo.
echo Vi du: https://github.com/123222222/MyAppshrimp.git
echo.
set /p REPO_URL="Nhap URL repository (copy tu GitHub): "

if "%REPO_URL%"=="" (
    echo [ERROR] Ban chua nhap URL!
    pause
    exit /b 1
)

echo.
echo [4] Them remote moi...
git remote add origin %REPO_URL%
echo [OK] Da them remote: %REPO_URL%
echo.

echo [5] Kiem tra remote...
git remote -v
echo.

echo [6] Push len GitHub...
echo LUU Y: Dang nhap voi Personal Access Token (khong phai password)
echo.
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [SUCCESS] Push thanh cong!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo [ERROR] Push that bai!
    echo.
    echo Nguyen nhan co the:
    echo 1. Repository chua duoc tao tren GitHub
    echo 2. URL sai
    echo 3. Khong co quyen truy cap
    echo 4. Chua dang nhap
    echo ========================================
)

echo.
pause

