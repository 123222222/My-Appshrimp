# 🦐 Shrimp Detection System

A comprehensive IoT system for shrimp farming that uses AI/ML for automated shrimp detection, monitoring, and analysis.

## 🌟 Features

- **AI-Powered Detection**: YOLOv8 TFLite model for real-time shrimp detection
- **Live Camera Streaming**: Real-time monitoring via camera feed
- **Size & Weight Estimation**: Automatic calculation of shrimp length and weight
- **Android App**: Mobile application for remote monitoring
- **Cloud Storage**: Image storage using Cloudinary
- **Database**: MongoDB for detection history
- **Authentication**: Firebase Authentication with Google Sign-In
- **Multi-User Support**: Independent device binding per user
- **Email Permission Management**: Admin can add/remove users
- **Role-Based Access**: Admin and regular user roles
- **📡 WiFi Configuration**: Remote WiFi management for Raspberry Pi from mobile app

## 🔐 Authentication & Authorization

### Multi-User System
- **Admin**: Full access + user management
- **Permitted Users**: Can bind devices, view streams, capture images
- **Unpermitted Users**: Access denied with clear message

### Device Binding
- **Independent Bindings**: Each user can bind their own device
- **Multiple Users**: Can connect to the same physical device
- **No Interference**: Unbinding doesn't affect other users
- **Secure**: Firebase ID Token authentication for all APIs

## 📱 Components

### Backend (Flask API)
- Camera streaming endpoint
- AI detection API
- User authentication & authorization (Firebase Admin SDK)
- Device management (bind/unbind)
- Email permission management
- Image gallery management
- WiFi configuration & management

### Android App
- Google Sign-In
- Profile screen with device management
- WiFi configuration for Raspberry Pi
- Live camera feed viewer
- Camera capture & detection
- Gallery view with detection history
- Chart for statistics
- Admin panel for user management

## 🛠️ Tech Stack

### Backend
- Python 3.x
- Flask
- OpenCV
- TensorFlow Lite
- MongoDB
- Cloudinary
- Firebase Admin SDK

### Android
- Kotlin
- Jetpack Compose
- Hilt (Dependency Injection)
- Retrofit (API calls)
- Coil (Image loading)
- Firebase Authentication

## 📋 Requirements

### Backend
```
Flask
flask-cors
opencv-python
numpy
pymongo
cloudinary
python-dotenv
firebase-admin
pillow
tflite-runtime
```

### Android
- Android Studio
- Min SDK: 24
- Target SDK: 34
- Kotlin 1.9+

## 🚀 Getting Started

### Backend Setup

1. Install dependencies:
```bash
cd backend
pip install -r requirements.txt
```

2. Configure environment variables (create `.env` file):
```
MONGODB_URI=your_mongodb_uri
MONGODB_DATABASE=shrimp_db
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
ADMIN_EMAIL=your_admin_email@gmail.com
CAMERA_USERNAME=admin
CAMERA_PASSWORD=123456
DEVICE_ID=raspberrypi-001
```

3. Add Firebase credentials:
- Place `firebase-admin.json` in backend directory

4. Initialize permitted emails (first time):
```bash
python manage_emails.py add your_admin_email@gmail.com
```

5. Run the server:
```bash
python app_complete.py
```

### Android Setup

1. Open project in Android Studio
2. Add `google-services.json` to `app/` directory
3. Update backend URL in `Config.kt`
4. Build and run

## 👥 User Management

### Admin Setup
1. Admin email is defined in `.env` file
2. Admin automatically has full permissions
3. Admin can add/remove users via Profile screen

### Adding New Users
**Via App (Recommended):**
1. Admin logs in
2. Goes to Profile → "Quản lý quyền truy cập"
3. Clicks "Thêm tài khoản"
4. Enters user's Google email
5. User can now login and use the app

**Via Command Line:**
```bash
cd backend
python manage_emails.py add user@gmail.com
python manage_emails.py list  # View all users
python manage_emails.py remove user@gmail.com
```

### User Workflow
1. User logs in with Google (must be in permitted list)
2. Scans for Raspberry Pi devices
3. Binds to a device (independent binding)
4. Views camera stream, captures images, views gallery
5. Can unbind device anytime without affecting other users

## 📖 API Documentation

### Authentication Endpoints
- `POST /api/auth/check` - Check token and permission status

### Device Management
- `POST /api/devices/bind` - Bind device to current user
- `POST /api/devices/unbind` - Unbind user's device
- `GET /api/devices/my-device` - Get user's bound device
- `POST /api/devices/check` - Check device permission

### Admin Endpoints (Admin Only)
- `GET /api/admin/list-emails` - List all permitted emails
- `POST /api/admin/add-email` - Add user to permitted list
- `POST /api/admin/remove-email` - Remove user from permitted list

### Detection & Gallery
- `GET /blynk_feed` - Camera stream (MJPEG)
- `POST /api/detect-shrimp` - Detect shrimp in image
- `GET /api/shrimp-images` - Get all images
- `DELETE /api/shrimp-images/:id` - Delete image

See [BACKEND_API_DOCS.md](BACKEND_API_DOCS.md) for detailed API documentation.

## 🔐 Security

- Firebase Authentication for user verification
- Email-based whitelist system (`permitted_emails.json`)
- Independent device bindings per user (`permitted_devices.json`)
- Admin-only endpoints protected with email verification
- All API calls require valid Firebase ID Token

## 📚 Documentation

- [Architecture](ARCHITECTURE.md) - System architecture and data flow
- [Device Binding Guide](DEVICE_BINDING_GUIDE.md) - Multi-user device binding
- [WiFi Configuration Guide](WIFI_CONFIGURATION_GUIDE.md) - Remote WiFi management
- [WiFi Troubleshooting](WIFI_TROUBLESHOOTING.md) - Debug WiFi issues
- [Quick Start Guide](QUICK_START.md) - Step-by-step setup
- [Remote Access Setup](REMOTE_ACCESS_GUIDE.md) - ngrok setup
- [System Summary](TOM_TAT_HE_THONG.md) - Vietnamese summary

## 🎯 Key Features Explained

### Independent Device Binding
Each user can bind their own device without affecting others:
```json
{
  "admin@gmail.com": { "device_id": "rasp-001", "ip": "192.168.1.100" },
  "user1@gmail.com": { "device_id": "rasp-001", "ip": "192.168.1.100" },
  "user2@gmail.com": { "device_id": "rasp-002", "ip": "192.168.1.101" }
}
```

### Migration Support
Old format automatically migrates to new format on first load.

## 📄 License

This project is private and proprietary.

## 👥 Author

- GitHub: [@123222222](https://github.com/123222222)

## 🤝 Contributing

This is a private project. Contact the owner for collaboration opportunities.

---

**Note**: Keep sensitive files secure:
- `.env` - Environment variables
- `google-services.json` - Firebase Android config
- `firebase-admin.json` - Firebase admin credentials
- `permitted_emails.json` - User whitelist
- `permitted_devices.json` - Device bindings

Never commit these files to the repository!

