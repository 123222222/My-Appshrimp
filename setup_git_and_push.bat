@echo off
echo ========================================
echo SETUP GIT VA PUSH LEN GITHUB
echo ========================================
echo.

REM Kiem tra Git da cai dat chua
where git >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Git chua duoc cai dat!
    echo Vui long tai Git tai: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo [OK] Git da duoc cai dat
echo.

REM Chuyen den thu muc du an
cd /d "D:\MyAppshrimp"

REM Kiem tra xem da co git init chua
if exist ".git" (
    echo [INFO] Git repository da ton tai
) else (
    echo [STEP 1] Khoi tao Git repository...
    git init
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Khong the khoi tao Git!
        pause
        exit /b 1
    )
    echo [OK] Da khoi tao Git thanh cong
)
echo.

REM Cau hinh Git (neu chua co)
echo [STEP 2] Cau hinh thong tin Git...
git config user.name >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set /p GIT_NAME="Nhap ten cua ban (VD: Nguyen Van A): "
    set /p GIT_EMAIL="Nhap email cua ban: "
    git config --global user.name "!GIT_NAME!"
    git config --global user.email "!GIT_EMAIL!"
    echo [OK] Da cau hinh thong tin Git
) else (
    echo [OK] Git da duoc cau hinh
)
echo.

REM Hien thi thong tin Git hien tai
echo [INFO] Thong tin Git hien tai:
git config user.name
git config user.email
echo.

REM Kiem tra trang thai
echo [STEP 3] Kiem tra trang thai files...
git status
echo.

REM Them tat ca files
echo [STEP 4] Them tat ca files vao Git...
git add .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Khong the them files!
    pause
    exit /b 1
)
echo [OK] Da them files thanh cong
echo.

REM Hien thi files se duoc commit
echo [INFO] Danh sach files se duoc commit:
git status --short
echo.

REM Commit
echo [STEP 5] Commit lan dau...
git commit -m "Initial commit: Shrimp Detection System - Full project upload"
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Co the da commit roi hoac khong co thay doi
)
echo [OK] Da commit thanh cong
echo.

REM Doi ten branch thanh main
echo [STEP 6] Doi ten branch thanh main...
git branch -M main
echo [OK] Da doi ten branch
echo.

REM Nhap URL repository
echo ========================================
echo QUAN TRONG: Ban can tao repository tren GitHub truoc!
echo.
echo 1. Truy cap: https://github.com/123222222
echo 2. Click nut "New" de tao repository moi
echo 3. Dat ten repository (VD: MyAppshrimp)
echo 4. KHONG chon "Initialize this repository with a README"
echo 5. Click "Create repository"
echo.
echo ========================================
echo.

set /p REPO_NAME="Nhap ten repository ban vua tao (VD: MyAppshrimp): "

if "%REPO_NAME%"=="" (
    echo [ERROR] Ban chua nhap ten repository!
    pause
    exit /b 1
)

set REPO_URL=https://github.com/123222222/%REPO_NAME%.git

echo.
echo [INFO] Repository URL: %REPO_URL%
echo.

REM Kiem tra xem da co remote chua
git remote get-url origin >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Remote origin da ton tai, dang cap nhat...
    git remote remove origin
)

echo [STEP 7] Them remote repository...
git remote add origin %REPO_URL%
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Khong the them remote repository!
    pause
    exit /b 1
)
echo [OK] Da them remote repository
echo.

REM Push len GitHub
echo ========================================
echo [STEP 8] Push code len GitHub...
echo.
echo LUU Y: Neu duoc yeu cau dang nhap:
echo - Username: 123222222
echo - Password: Su dung Personal Access Token (khong phai mat khau GitHub)
echo.
echo Cach tao Personal Access Token:
echo 1. Truy cap: https://github.com/settings/tokens
echo 2. Click "Generate new token" ^> "Generate new token (classic)"
echo 3. Dat ten: MyAppshrimp-Upload
echo 4. Chon quyen: repo (tat ca)
echo 5. Click "Generate token"
echo 6. COPY token va su dung lam password
echo.
echo ========================================
echo.

git push -u origin main
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Push that bai!
    echo.
    echo Cac nguyen nhan co the:
    echo 1. Chua dang nhap GitHub
    echo 2. Sai Personal Access Token
    echo 3. Repository chua duoc tao tren GitHub
    echo 4. Khong co quyen truy cap
    echo.
    echo Hay thu lai hoac lam thu cong theo huong dan trong GITHUB_UPLOAD_GUIDE.md
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] HOAN TAT!
echo ========================================
echo.
echo Code da duoc push len GitHub thanh cong!
echo.
echo Truy cap repository tai:
echo https://github.com/123222222/%REPO_NAME%
echo.
echo ========================================
pause

