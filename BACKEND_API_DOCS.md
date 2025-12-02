# Backend API Documentation for Shrimp Detection

Đây là tài liệu mô tả các API endpoint mà backend cần implement để app Android có thể hoạt động đúng.

## Base URL
```
https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev
```

## Endpoints

### 1. Xử lý ảnh với YOLO Model
**POST** `/api/detect-shrimp`

**Request Body:**
```json
{
  "image": "base64_encoded_image_string",
  "source": "https://camera-url/stream"
}
```

**Response:**
```json
{
  "success": true,
  "imageUrl": "http://localhost:8000/images/abc123.jpg",
  "cloudinaryUrl": "https://res.cloudinary.com/your-cloud/image/upload/v123/shrimp.jpg",
  "detections": [
    {
      "className": "Shrimp",
      "confidence": 0.95,
      "bbox": {
        "x": 100.5,
        "y": 150.2,
        "width": 80.0,
        "height": 120.0
      }
    }
  ],
  "mongoId": "507f1f77bcf86cd799439011",
  "message": "Detection completed successfully"
}
```

### 2. Lấy danh sách ảnh đã lưu
**GET** `/api/shrimp-images`

**Response:**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "imageUrl": "http://localhost:8000/images/abc123.jpg",
    "cloudinaryUrl": "https://res.cloudinary.com/your-cloud/image/upload/v123/shrimp.jpg",
    "detections": [
      {
        "className": "Shrimp",
        "confidence": 0.95,
        "bbox": {
          "x": 100.5,
          "y": 150.2,
          "width": 80.0,
          "height": 120.0
        }
      }
    ],
    "timestamp": 1698765432000,
    "capturedFrom": "https://camera-url/stream"
  }
]
```

### 3. Xóa ảnh
**DELETE** `/api/shrimp-images/{imageId}`

**Response:**
```json
{
  "success": true,
  "message": "Image deleted successfully"
}
```

### 4. Camera Stream (Đã có sẵn)
**GET** `/blynk_feed`

Returns MJPEG stream

---

## Backend Implementation Notes

### Python Flask/FastAPI Example

```python
from flask import Flask, request, jsonify
from ultralytics import YOLO
import cloudinary
import cloudinary.uploader
from pymongo import MongoClient
import base64
from io import BytesIO
from PIL import Image
import numpy as np

app = Flask(__name__)

# Load YOLO model
model = YOLO('path/to/your/shrimp_model.pt')

# Cloudinary config
cloudinary.config(
    cloud_name="your-cloud-name",
    api_key="your-api-key",
    api_secret="your-api-secret"
)

# MongoDB config
mongo_client = MongoClient('mongodb://localhost:27017/')
db = mongo_client['shrimp_db']
collection = db['detections']

@app.route('/api/detect-shrimp', methods=['POST'])
def detect_shrimp():
    data = request.json
    image_base64 = data['image']
    source = data['source']
    
    # Decode base64 image
    image_data = base64.b64decode(image_base64)
    image = Image.open(BytesIO(image_data))
    
    # Run YOLO detection
    results = model(image)
    
    # Parse detections
    detections = []
    for r in results:
        boxes = r.boxes
        for box in boxes:
            x, y, w, h = box.xywh[0].tolist()
            detections.append({
                "className": r.names[int(box.cls)],
                "confidence": float(box.conf),
                "bbox": {
                    "x": x,
                    "y": y,
                    "width": w,
                    "height": h
                }
            })
    
    # Save annotated image
    annotated_image = results[0].plot()
    img_pil = Image.fromarray(annotated_image)
    buffer = BytesIO()
    img_pil.save(buffer, format='JPEG')
    buffer.seek(0)
    
    # Upload to Cloudinary
    upload_result = cloudinary.uploader.upload(buffer, folder="shrimp-detections")
    cloudinary_url = upload_result['secure_url']
    
    # Save to MongoDB
    doc = {
        "imageUrl": upload_result['url'],
        "cloudinaryUrl": cloudinary_url,
        "detections": detections,
        "timestamp": int(time.time() * 1000),
        "capturedFrom": source
    }
    result = collection.insert_one(doc)
    
    return jsonify({
        "success": True,
        "imageUrl": upload_result['url'],
        "cloudinaryUrl": cloudinary_url,
        "detections": detections,
        "mongoId": str(result.inserted_id),
        "message": "Detection completed"
    })

@app.route('/api/shrimp-images', methods=['GET'])
def get_images():
    images = list(collection.find().sort('timestamp', -1))
    for img in images:
        img['id'] = str(img['_id'])
        del img['_id']
    return jsonify(images)

@app.route('/api/shrimp-images/<image_id>', methods=['DELETE'])
def delete_image(image_id):
    from bson import ObjectId
    collection.delete_one({'_id': ObjectId(image_id)})
    return jsonify({"success": True, "message": "Deleted"})

if __name__ == '__main__':
    app.run(port=8000)
```

### Requirements
```
flask
ultralytics
cloudinary
pymongo
pillow
numpy
```

