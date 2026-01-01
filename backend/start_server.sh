#!/bin/bash
# Script khởi động server Flask + Ngrok cho Shrimp Detection & Motor Control

echo "🚀 Starting Shrimp Detection & Motor Control Server..."
echo ""

# ==================== CLEANUP OLD PROCESSES ====================
echo "🧹 Checking for running processes..."

# Tìm và kill các process cũ
OLD_PIDS=$(ps aux | grep -E "app_complete\.py|send_reset_email\.py" | grep -v grep | awk '{print $2}')
if [ ! -z "$OLD_PIDS" ]; then
    echo "⚠️  Found old processes running. Stopping them..."
    echo "$OLD_PIDS" | while read pid; do
        kill -9 $pid 2>/dev/null && echo "   ✓ Killed process $pid"
    done
    sleep 1
else
    echo "✅ No old processes found"
fi

# Kill ngrok nếu đang chạy
NGROK_PIDS=$(ps aux | grep ngrok | grep -v grep | awk '{print $2}')
if [ ! -z "$NGROK_PIDS" ]; then
    echo "⚠️  Stopping old ngrok tunnels..."
    echo "$NGROK_PIDS" | while read pid; do
        kill -9 $pid 2>/dev/null && echo "   ✓ Killed ngrok $pid"
    done
    sleep 1
fi

# Kiểm tra và giải phóng ports
for PORT in 8000 5001 50000; do
    PORT_PID=$(lsof -ti:$PORT 2>/dev/null)
    if [ ! -z "$PORT_PID" ]; then
        echo "⚠️  Port $PORT is in use by PID $PORT_PID. Killing it..."
        kill -9 $PORT_PID 2>/dev/null
    fi
done

# Cleanup GPIO pins
python3 -c "import RPi.GPIO as GPIO; GPIO.cleanup()" 2>/dev/null

echo "✅ Cleanup completed"
echo ""

# ==================== TIMEZONE SETUP ====================
echo "🕐 Checking timezone..."
CURRENT_TZ=$(timedatectl show -p Timezone --value 2>/dev/null || echo "Unknown")
EXPECTED_TZ="Asia/Ho_Chi_Minh"

if [ "$CURRENT_TZ" != "$EXPECTED_TZ" ]; then
    echo "⚠️  Current timezone: $CURRENT_TZ"
    echo "🔧 Setting timezone to $EXPECTED_TZ (UTC+7)..."

    # Try to set timezone
    if sudo timedatectl set-timezone "$EXPECTED_TZ" 2>/dev/null; then
        echo "✅ Timezone updated to $EXPECTED_TZ"
    else
        echo "⚠️  Could not set system timezone (need sudo)"
        echo "   Setting TZ environment variable instead..."
        export TZ="$EXPECTED_TZ"
    fi
else
    echo "✅ Timezone is already set to $EXPECTED_TZ"
fi

# Set TZ environment variable for Python
export TZ="$EXPECTED_TZ"

echo "📅 Current time: $(date '+%Y-%m-%d %H:%M:%S %Z')"
echo ""

# Kiểm tra và cài đặt dependencies
echo "📦 Checking dependencies..."

# Kiểm tra Python3
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 is not installed!"
    echo "Please install Python3 first:"
    echo "   sudo apt update && sudo apt install -y python3 python3-pip"
    exit 1
fi

# Kiểm tra pip3
if ! command -v pip3 &> /dev/null; then
    echo "❌ pip3 is not installed!"
    echo "Installing pip3..."
    sudo apt install -y python3-pip
fi

# Kiểm tra ngrok
if ! command -v ngrok &> /dev/null; then
    echo "❌ ngrok is not installed!"
    echo "Installing ngrok..."
    echo "Please download and install ngrok from: https://ngrok.com/download"
    echo "Or run: curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo \"deb https://ngrok-agent.s3.amazonaws.com buster main\" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt update && sudo apt install ngrok"
    exit 1
fi

# Kiểm tra và cài đặt Python packages
# ⚠️ COMMENTED OUT for faster startup - Uncomment if you need to check/install dependencies
# if [ -f requirements.txt ]; then
#     echo "📥 Checking Python packages..."
#
#     # Kiểm tra xem các package đã được cài chưa
#     MISSING_PACKAGES=0
#     while IFS= read -r package || [ -n "$package" ]; do
#         # Bỏ qua dòng trống và comment
#         [[ -z "$package" || "$package" == \#* ]] && continue
#
#         # Lấy tên package (bỏ version)
#         PKG_NAME=$(echo "$package" | cut -d'=' -f1 | cut -d'>' -f1 | cut -d'<' -f1 | xargs)
#
#         if ! python3 -c "import $PKG_NAME" 2>/dev/null; then
#             MISSING_PACKAGES=1
#             break
#         fi
#     done < requirements.txt
#
#     if [ $MISSING_PACKAGES -eq 1 ]; then
#         echo "📦 Installing missing Python packages..."
#         pip3 install -r requirements.txt
#
#         if [ $? -ne 0 ]; then
#             echo "❌ Failed to install Python packages!"
#             exit 1
#         fi
#         echo "✅ Python packages installed successfully"
#     else
#         echo "✅ All Python packages are already installed"
#     fi
# else
#     echo "⚠️  Warning: requirements.txt not found"
# fi
echo "⚡ Skipping dependency check for faster startup"

# Kiểm tra file .env
if [ ! -f .env ]; then
    echo "❌ Lỗi: File .env không tồn tại!"
    echo "Vui lòng tạo file .env với cấu hình cần thiết"
    echo ""
    echo "Example .env content:"
    echo "CLOUDINARY_CLOUD_NAME=your_cloud_name"
    echo "CLOUDINARY_API_KEY=your_api_key"
    echo "CLOUDINARY_API_SECRET=your_api_secret"
    echo "FIREBASE_SERVICE_ACCOUNT_KEY_PATH=path/to/serviceAccountKey.json"
    echo "SENDER_EMAIL=your_email@gmail.com"
    echo "SENDER_PASSWORD=your_app_password"
    exit 1
fi

# Kiểm tra file send_reset_email.py
if [ ! -f "send_reset_email.py" ]; then
    echo "⚠️  Cảnh báo: File send_reset_email.py không tìm thấy!"
    echo "   Email reset service sẽ không hoạt động"
fi

# Kiểm tra model
if [ ! -f "models/best-fp16 (1).tflite" ]; then
    echo "⚠️  Cảnh báo: Model file không tìm thấy!"
    echo "   AI detection sẽ không hoạt động"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ All dependencies are ready!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Khởi động Flask server ở background
echo "📡 Starting Flask server on port 8000..."
python3 app_complete.py &
FLASK_PID=$!

# Đợi Flask server khởi động
sleep 3

# Kiểm tra Flask server có chạy không
if ps -p $FLASK_PID > /dev/null; then
    echo "✅ Flask server started successfully (PID: $FLASK_PID)"
else
    echo "❌ Flask server failed to start"
    exit 1
fi

# Khởi động Email Reset Service ở background
echo "📧 Starting Email Reset Service on port 5001..."
python3 send_reset_email.py &
EMAIL_PID=$!

# Đợi Email service khởi động
sleep 3

# Kiểm tra Email service có chạy không
if ps -p $EMAIL_PID > /dev/null; then
    echo "✅ Email Reset Service started successfully (PID: $EMAIL_PID)"
else
    echo "⚠️  Email Reset Service failed to start (optional service)"
    EMAIL_PID=""
fi

# Khởi động ngrok
echo ""
echo "🌐 Starting ngrok tunnel..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
ngrok http 8000

# Khi ngrok tắt, tắt Flask server
echo ""
echo "🛑 Stopping servers..."
kill $FLASK_PID

# Tắt Email service nếu đang chạy
if [ -n "$EMAIL_PID" ] && ps -p $EMAIL_PID > /dev/null; then
    kill $EMAIL_PID
    echo "✅ Email Reset Service stopped"
fi

# Cleanup GPIO pins (nếu đang chạy trên Raspberry Pi)
echo "🔌 Cleaning up GPIO pins..."
python3 -c "import RPi.GPIO as GPIO; GPIO.cleanup()" 2>/dev/null

echo "✅ Server stopped"
