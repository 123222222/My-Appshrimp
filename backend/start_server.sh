#!/bin/bash
# Script khởi động server Flask + Ngrok cho Shrimp Detection

echo "🚀 Starting Shrimp Detection Server..."
echo ""

# Kiểm tra file .env
if [ ! -f .env ]; then
    echo "❌ Lỗi: File .env không tồn tại!"
    echo "Vui lòng tạo file .env từ .env.example"
    exit 1
fi

# Kiểm tra model
if [ ! -f "models/best-fp16 (1).tflite" ]; then
    echo "⚠️  Cảnh báo: Model file không tìm thấy!"
fi

# Khởi động Flask server ở background
echo "📡 Starting Flask server on port 8000..."
python3 app_complete.py &
FLASK_PID=$!

# Đợi Flask server khởi động
sleep 5

# Kiểm tra Flask server có chạy không
if ps -p $FLASK_PID > /dev/null; then
    echo "✅ Flask server started successfully (PID: $FLASK_PID)"
else
    echo "❌ Flask server failed to start"
    exit 1
fi

# Khởi động ngrok
echo ""
echo "🌐 Starting ngrok tunnel..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
ngrok http 8000

# Khi ngrok tắt, tắt Flask server
echo ""
echo "🛑 Stopping Flask server..."
kill $FLASK_PID
echo "✅ Server stopped"
