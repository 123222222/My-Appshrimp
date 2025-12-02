# 🦐 Shrimp Detection System Architecture

## 📊 System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER PERSPECTIVE                        │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
        ┌────────────────────────────────────────┐
        │         📱 Android App                 │
        │    (Kotlin + Jetpack Compose)          │
        │                                        │
        │  Features:                             │
        │  • Home (Device List)                  │
        │  • Camera Stream (MJPEG)               │
        │  • Capture Button                      │
        │  • Gallery (Image Grid)                │
        │  • Image Details                       │
        └────────────────┬───────────────────────┘
                         │
                         │ HTTP/HTTPS
                         │
                         ▼
        ┌────────────────────────────────────────┐
        │      🐍 Flask Backend Server           │
        │         (Python 3.8+)                  │
        │                                        │
        │  Endpoints:                            │
        │  • GET  /blynk_feed                    │
        │  • POST /api/detect-shrimp             │
        │  • GET  /api/shrimp-images             │
        │  • GET  /api/shrimp-images/:id         │
        │  • DELETE /api/shrimp-images/:id       │
        └────┬──────────┬──────────┬─────────────┘
             │          │          │
             │          │          │
    ┌────────▼──┐  ┌────▼─────┐  ┌▼──────────┐
    │  🤖 YOLO  │  │ ☁️ Cloudi │  │ 🗄️ MongoDB │
    │   Model   │  │  -nary   │  │  Database  │
    │           │  │          │  │            │
    │ Detect &  │  │  Image   │  │  Metadata  │
    │ Annotate  │  │  Storage │  │   Store    │
    └───────────┘  └──────────┘  └────────────┘
```

---

## 🔄 Data Flow

### 1️⃣ Capture & Detection Flow

```
┌──────────┐
│   User   │
└────┬─────┘
     │ Presses Camera Button
     ▼
┌──────────────────┐
│ CameraStreamScreen│
│                  │
│ currentFrame     │ ◄─── MJPEG Stream
│ (Bitmap)         │
└────┬─────────────┘
     │ onClick Capture
     │
     ▼
┌──────────────────┐
│ ShrimpApiService │
│                  │
│ 1. bitmap→base64 │
│ 2. POST request  │
└────┬─────────────┘
     │
     │ HTTP POST /api/detect-shrimp
     │ Body: { image: "base64...", source: "url" }
     │
     ▼
┌──────────────────────────┐
│  Flask Backend           │
│                          │
│  1. Decode base64        │
│  2. Call YOLO model      │
└────┬─────────────────────┘
     │
     ▼
┌──────────────────────────┐
│  YOLO Model Processing   │
│                          │
│  Input:  RGB Image       │
│  Output: Detections[]    │
│    {                     │
│      class: "Shrimp"     │
│      confidence: 0.95    │
│      bbox: [x,y,w,h]     │
│    }                     │
└────┬─────────────────────┘
     │
     ▼
┌──────────────────────────┐
│  Draw Bounding Boxes     │
│                          │
│  • Draw rectangles       │
│  • Add labels            │
│  • Add confidence %      │
└────┬─────────────────────┘
     │
     ├────────────────┐
     │                │
     ▼                ▼
┌─────────┐    ┌──────────┐
│Cloudinary│    │ MongoDB  │
│         │    │          │
│ Upload  │    │ Insert   │
│ Image   │    │ Document │
│         │    │          │
│ Returns │    │ Returns  │
│ URL     │    │ ObjectId │
└────┬────┘    └────┬─────┘
     │              │
     └──────┬───────┘
            ▼
    ┌───────────────┐
    │   Response    │
    │   {           │
    │  success: ✓   │
    │  cloudinaryUrl│
    │  detections[] │
    │  mongoId      │
    │   }           │
    └───────┬───────┘
            │
            ▼
    ┌───────────────┐
    │  Android App  │
    │               │
    │  Show result  │
    │  "5 shrimps"  │
    └───────────────┘
```

---

## 🗄️ Data Storage

### MongoDB Document Structure

```json
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "imageUrl": "http://res.cloudinary.com/.../image.jpg",
  "cloudinaryUrl": "https://res.cloudinary.com/.../image.jpg",
  "detections": [
    {
      "className": "Shrimp",
      "confidence": 0.95,
      "bbox": {
        "x": 320.5,      // Center X
        "y": 240.2,      // Center Y
        "width": 80.0,   // Box width
        "height": 120.0  // Box height
      }
    }
  ],
  "timestamp": 1698765432000,  // Unix timestamp in ms
  "capturedFrom": "https://camera-url/stream"
}
```

### Cloudinary Folder Structure

```
cloudinary.com/your-cloud/
  └── shrimp-detections/
      ├── abc123_xyz789.jpg   (annotated image 1)
      ├── def456_uvw012.jpg   (annotated image 2)
      └── ...
```

---

## 🎯 Why Each Technology?

### YOLO (Ultralytics)
**Purpose**: AI Object Detection
**Why**: 
- Fast real-time detection
- High accuracy
- Pre-trained models available
- Easy to train custom models

### Cloudinary
**Purpose**: Cloud Image Storage
**Why**: 
- ❌ **Problem**: Storing images on local server
  - Lost when server restarts
  - No backup
  - Limited storage
  - Slow access

- ✅ **Solution**: Cloud storage
  - Permanent storage
  - Auto-backup
  - Unlimited (plan-based)
  - CDN for fast global access
  - Image optimization

### MongoDB
**Purpose**: Metadata Database
**Why**:
- ❌ **Don't store**: Raw image files (too large)
- ✅ **Do store**: 
  - Image URLs
  - Detection results
  - Timestamps
  - Search/filter metadata

**Benefits**:
- Flexible schema (JSON-like)
- Fast queries
- Scalable
- Easy to add new fields

---

## 📱 Android App Structure

```
app/src/main/java/com/dung/myapplication/
│
├── mainUI/
│   ├── home/
│   │   ├── HomeScreen.kt              # Device list
│   │   ├── CameraStreamScreen.kt      # Stream + Capture
│   │   └── ...
│   │
│   ├── gallery/                        # ⭐ NEW
│   │   ├── GalleryScreen.kt           # Image grid
│   │   ├── ImageDetailScreen.kt       # Detail view
│   │   └── GalleryViewModel.kt        # Business logic
│   │
│   ├── menu/
│   │   └── MenuScreen.kt              # Menu with Gallery button
│   │
│   └── NavGraph.kt                    # Navigation routes
│
├── models/
│   └── ShrimpImage.kt                 # ⭐ NEW Data models
│
└── utils/
    └── ShrimpApiService.kt            # ⭐ NEW API calls
```

---

## 🔐 Environment Variables

```bash
# .env file in backend/

# Cloudinary (FREE tier: 25GB storage)
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=123456789
CLOUDINARY_API_SECRET=abc123xyz

# MongoDB (Local or Atlas FREE tier)
MONGODB_URI=mongodb://localhost:27017/
MONGODB_DATABASE=shrimp_db

# YOLO Model
YOLO_MODEL_PATH=models/shrimp_best.pt
```

---

## 🚀 Deployment Options

### Development (Local)
```
Android App → http://localhost:8000 (PC only)
```

### Production (Internet Access)
```
Android App → https://xxx.ngrok-free.app → Backend
             (ngrok tunnel)
```

### Future (Professional)
```
Android App → https://api.yourdomain.com → Backend
             (AWS/GCP/Azure)
```

---

## 📊 Performance

| Action | Time |
|--------|------|
| Capture Image | < 1s |
| YOLO Detection | 2-4s |
| Upload to Cloudinary | 1-2s |
| Save to MongoDB | < 0.5s |
| **Total** | **4-8s** |

---

## 🔄 Future Enhancements

1. **Real-time Streaming Detection**: Detect in live stream (not just capture)
2. **Statistics Dashboard**: Count shrimps over time
3. **Export Reports**: PDF/Excel reports
4. **Multiple Cameras**: Support multiple camera sources
5. **Offline Mode**: Cache images when no internet
6. **Push Notifications**: Alert when shrimp detected
7. **User Accounts**: Multi-user support

---

**Made with ❤️ for Shrimp Farming** 🦐

