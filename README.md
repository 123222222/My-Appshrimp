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
- **Device Binding**: Secure device-to-user binding system
- **Multi-User Support**: Email-based permission management

## 📱 Components

### Backend (Flask API)
- Camera streaming endpoint
- AI detection API
- User authentication & authorization
- Device management
- Image gallery management

### Android App
- Google Sign-In
- Live camera feed viewer
- Camera capture & detection
- Gallery view with detection history
- Device binding management

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

4. Run the server:
```bash
python app_complete.py
```

### Android Setup

1. Open project in Android Studio
2. Add `google-services.json` to `app/` directory
3. Update backend URL in app configuration
4. Build and run

## 📖 API Documentation

See [BACKEND_API_DOCS.md](BACKEND_API_DOCS.md) for detailed API documentation.

## 🔐 Security

- Firebase Authentication for user verification
- Device binding for secure access control
- Email-based permission system
- Admin-only endpoints for user management

## 📚 Documentation

- [Architecture](ARCHITECTURE.md)
- [Device Binding Guide](DEVICE_BINDING_GUIDE.md)
- [Quick Start Guide](QUICK_START.md)
- [Remote Access Setup](REMOTE_ACCESS_GUIDE.md)

## 📄 License

This project is private and proprietary.

## 👥 Author

- GitHub: [@123222222](https://github.com/123222222)

## 🤝 Contributing

This is a private project. Contact the owner for collaboration opportunities.

---

**Note**: Make sure to keep sensitive files (`.env`, `google-services.json`, `firebase-admin.json`) secure and never commit them to the repository.

