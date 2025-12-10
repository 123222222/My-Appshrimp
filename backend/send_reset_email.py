#!/usr/bin/env python3
"""
API để gửi email reset mật khẩu
Chạy: python send_reset_email.py
"""

from flask import Flask, request, jsonify, render_template, redirect
from flask_cors import CORS
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import secrets
import time
import os
import hashlib
from datetime import datetime, timedelta
import requests
import json

# Load biến môi trường từ file .env (nếu có)
try:
    from dotenv import load_dotenv
    load_dotenv()
    print("✅ Đã load file .env")
except ImportError:
    print("ℹ️  python-dotenv chưa cài đặt. Dùng biến môi trường hệ thống.")
except Exception as e:
    print(f"ℹ️  Không tìm thấy file .env: {e}")

app = Flask(__name__)
CORS(app)  # Cho phép Android app gọi API

# Firestore REST API Configuration
FIRESTORE_PROJECT_ID = "my-app-shrimp-v2-0"
FIRESTORE_API_KEY = os.getenv("FIREBASE_API_KEY", "AIzaSyDgmgPRzC-dLXl3e5oMl_k07N_OBjq8Gio")  # Lấy từ Firebase Console
FIRESTORE_BASE_URL = f"https://firestore.googleapis.com/v1/projects/{FIRESTORE_PROJECT_ID}/databases/(default)/documents"

print(f"✅ Sử dụng Firestore REST API cho project: {FIRESTORE_PROJECT_ID}")

# Cấu hình email (Gmail) - đọc từ biến môi trường hoặc dùng default
SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT = 587
SENDER_EMAIL = os.getenv("SENDER_EMAIL", "hodung15032003@gmail.com")
SENDER_PASSWORD = os.getenv("SENDER_PASSWORD", "wcef zrkv iwhu qbod")  # ← Thay bằng App Password 16 ký tự!

# Kiểm tra cấu hình email
if SENDER_PASSWORD == "YOUR_APP_PASSWORD_HERE" or SENDER_PASSWORD == "your_app_password":
    print("⚠️  CẢNH BÁO: Chưa cấu hình App Password!")
    print("📧 QUAN TRỌNG: Không dùng mật khẩu Gmail thường!")
    print("   1. Vào: https://myaccount.google.com/apppasswords")
    print("   2. Tạo App Password cho 'MyAppShrimp' (16 ký tự)")
    print("   3. Thay YOUR_APP_PASSWORD_HERE bằng App Password")
    print("   4. Ví dụ: SENDER_PASSWORD = 'abcd efgh ijkl mnop'")
    print()

# Lưu trữ reset tokens tạm thời (trong production nên dùng Redis hoặc database)
reset_tokens = {}  # {token: {"email": email, "phone": phone, "expires": timestamp}}

@app.route('/send-reset-link', methods=['POST'])
def send_reset_link():
    """
    API endpoint để gửi link reset mật khẩu
    Request body: {"email": "user@example.com", "phone": "+84987648717"}
    """
    try:
        data = request.get_json()
        email = data.get('email')
        phone = data.get('phone')

        if not email or not phone:
            return jsonify({"error": "Email và số điện thoại là bắt buộc"}), 400

        # Tạo token ngẫu nhiên
        token = secrets.token_urlsafe(32)

        # Lưu token với thời gian hết hạn (30 phút)
        reset_tokens[token] = {
            "email": email,
            "phone": phone,
            "expires": time.time() + 1800  # 30 phút
        }

        # Tạo link reset (web link để mở trong browser)
        reset_link = f"http://192.168.137.125:5001/reset-password?token={token}"

        # Tạo nội dung email
        subject = "Đặt lại mật khẩu - MyAppShrimp"
        html_content = f"""
        <html>
        <body>
            <h2>Yêu cầu đặt lại mật khẩu</h2>
            <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản: <strong>{phone}</strong></p>
            <p>Nhấn vào link bên dưới để đặt lại mật khẩu (có hiệu lực trong 30 phút):</p>
            <p><a href="{reset_link}" style="padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px;">Đặt lại mật khẩu</a></p>
            <p>Hoặc copy link này vào trình duyệt:</p>
            <p><code>{reset_link}</code></p>
            <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
            <br>
            <p><em>Token: {token}</em></p>
        </body>
        </html>
        """

        # Gửi email
        try:
            send_email(email, subject, html_content)
            print(f"✅ Email đã gửi thành công đến: {email}")
        except Exception as email_error:
            print(f"❌ Lỗi gửi email: {email_error}")
            return jsonify({
                "error": f"Không thể gửi email: {str(email_error)}",
                "details": "Kiểm tra SENDER_EMAIL và SENDER_PASSWORD trong code"
            }), 500

        return jsonify({
            "success": True,
            "message": "Link đặt lại mật khẩu đã được gửi đến email của bạn",
            "token": token  # Chỉ để test, production không trả về
        }), 200

    except Exception as e:
        print(f"❌ Lỗi tổng quát: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

@app.route('/verify-reset-token', methods=['POST'])
def verify_reset_token():
    """
    Xác thực token reset
    Request body: {"token": "xyz"}
    """
    try:
        data = request.get_json()
        token = data.get('token')

        if not token:
            return jsonify({"error": "Token là bắt buộc"}), 400

        # Kiểm tra token
        token_data = reset_tokens.get(token)

        if not token_data:
            return jsonify({"error": "Token không hợp lệ"}), 400

        # Kiểm tra hết hạn
        if time.time() > token_data['expires']:
            del reset_tokens[token]
            return jsonify({"error": "Token đã hết hạn"}), 400

        return jsonify({
            "success": True,
            "phone": token_data['phone'],
            "email": token_data['email']
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/complete-reset', methods=['POST'])
def complete_reset():
    """
    Hoàn tất reset mật khẩu
    Request body: {"token": "xyz"}
    """
    try:
        data = request.get_json()
        token = data.get('token')

        if not token:
            return jsonify({"error": "Token là bắt buộc"}), 400

        # Xóa token sau khi dùng
        if token in reset_tokens:
            del reset_tokens[token]

        return jsonify({"success": True}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

def send_email(to_email, subject, html_content):
    """Gửi email qua Gmail SMTP"""
    msg = MIMEMultipart('alternative')
    msg['Subject'] = subject
    msg['From'] = SENDER_EMAIL
    msg['To'] = to_email

    html_part = MIMEText(html_content, 'html')
    msg.attach(html_part)

    # Gửi email
    with smtplib.SMTP(SMTP_SERVER, SMTP_PORT) as server:
        server.starttls()
        server.login(SENDER_EMAIL, SENDER_PASSWORD)
        server.sendmail(SENDER_EMAIL, to_email, msg.as_string())

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "ok", "timestamp": datetime.now().isoformat()}), 200

@app.route('/reset-password', methods=['GET'])
def reset_password_page():
    """Hiển thị trang web reset password"""
    return render_template('reset_password.html')

@app.route('/reset-password', methods=['POST'])
def reset_password_submit():
    """
    API endpoint để cập nhật mật khẩu mới từ web
    Request body: {"token": "xyz", "newPassword": "newpass123"}
    """
    try:
        data = request.get_json()
        token = data.get('token')
        new_password = data.get('newPassword')

        if not token or not new_password:
            return jsonify({"error": "Token và mật khẩu mới là bắt buộc"}), 400

        # Kiểm tra token
        token_data = reset_tokens.get(token)

        if not token_data:
            return jsonify({"error": "Token không hợp lệ"}), 400

        # Kiểm tra hết hạn
        if time.time() > token_data['expires']:
            del reset_tokens[token]
            return jsonify({"error": "Token đã hết hạn"}), 400

        phone = token_data['phone']
        email = token_data['email']

        # Hash mật khẩu mới
        hashed_password = hashlib.sha256(new_password.encode()).hexdigest()

        print(f"✅ Mật khẩu mới cho {phone}: {hashed_password}")
        print(f"📧 Email: {email}")

        # Xóa token sau khi dùng
        del reset_tokens[token]

        # Cập nhật mật khẩu vào Firestore qua REST API
        try:
            # Thử nhiều format số điện thoại
            phone_formats = [
                phone,  # +84987648717
                phone.replace('+', ''),  # 84987648717
                phone.replace('+84', '0') if phone.startswith('+84') else phone  # 0987648717
            ]

            check_response = None
            user_url = None
            found_phone = None

            # Thử từng format
            for test_phone in phone_formats:
                # URL encode phone number (dấu + thành %2B)
                import urllib.parse
                encoded_phone = urllib.parse.quote(test_phone, safe='')
                user_url = f"{FIRESTORE_BASE_URL}/users/{encoded_phone}?key={FIRESTORE_API_KEY}"
                print(f"🔍 Thử kiểm tra user với format: {test_phone}")
                print(f"📡 URL encoded: {encoded_phone}")

                check_response = requests.get(user_url)

                if check_response.status_code == 200:
                    found_phone = test_phone
                    print(f"✅ Tìm thấy user với format: {test_phone}")
                    break
                elif check_response.status_code == 404:
                    print(f"❌ Không tìm thấy user với format: {test_phone}")
                else:
                    print(f"⚠️ Lỗi {check_response.status_code}: {check_response.text}")

            if not found_phone or check_response.status_code != 200:
                print(f"❌ Không tìm thấy tài khoản {phone} với bất kỳ format nào")
                print(f"📋 Đã thử: {phone_formats}")
                return jsonify({
                    "error": f"Không tìm thấy tài khoản {phone} trong hệ thống. Đã thử các format: {', '.join(phone_formats)}"
                }), 404

            # Cập nhật user_url với phone đúng (cần encode)
            import urllib.parse
            encoded_phone = urllib.parse.quote(found_phone, safe='')
            user_url = f"{FIRESTORE_BASE_URL}/users/{encoded_phone}?key={FIRESTORE_API_KEY}"
            phone = found_phone  # Dùng format đúng cho log

            # Cập nhật mật khẩu mới
            update_data = {
                "fields": {
                    "password": {"stringValue": hashed_password},
                    "lastPasswordReset": {"timestampValue": datetime.utcnow().isoformat() + "Z"}
                }
            }

            # PATCH request để cập nhật
            update_response = requests.patch(
                user_url,
                json=update_data,
                params={"updateMask.fieldPaths": ["password", "lastPasswordReset"]}
            )

            if update_response.status_code == 200:
                print(f"✅ Đã cập nhật mật khẩu vào Firestore cho {phone}")
            else:
                print(f"❌ Lỗi cập nhật Firestore: {update_response.text}")
                return jsonify({
                    "error": f"Không thể cập nhật mật khẩu: {update_response.text}"
                }), 500

        except Exception as firestore_error:
            print(f"❌ Lỗi cập nhật Firestore: {firestore_error}")
            return jsonify({
                "error": f"Không thể cập nhật mật khẩu: {str(firestore_error)}"
            }), 500

        return jsonify({
            "success": True,
            "message": "Mật khẩu đã được cập nhật thành công",
            "phone": phone
        }), 200

    except Exception as e:
        print(f"❌ Lỗi reset password: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    print("🚀 Email Reset Password API đang chạy...")
    print("📧 Nhớ cấu hình SENDER_EMAIL và SENDER_PASSWORD!")
    app.run(host='0.0.0.0', port=5001, debug=True)

