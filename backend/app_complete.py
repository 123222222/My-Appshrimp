from flask import Flask, Response, request, jsonify
from flask_cors import CORS
from functools import wraps
import cloudinary
import cloudinary.uploader
from pymongo import MongoClient
import base64
from io import BytesIO
from PIL import Image
import numpy as np
import time
import os
from dotenv import load_dotenv
import cv2
from bson import ObjectId
import threading
from firebase_admin import auth as firebase_auth, credentials, initialize_app
import json
import logging
import socket

# Load environment variables
load_dotenv()

app = Flask(__name__)
CORS(app)

# ==================== CAMERA SETUP ====================
print("Initializing camera...")
camera = None
camera_lock = threading.Lock()

for i in range(30):
    test_cam = cv2.VideoCapture(i, cv2.CAP_V4L2)
    if test_cam.isOpened():
        ret, frame = test_cam.read()
        if ret:
            print(f"✅ Camera found at /dev/video{i}")
            camera = test_cam
            break
        test_cam.release()

if camera is None:
    print("⚠️  Warning: No camera found! Camera streaming will not work.")
else:
    time.sleep(1)
    # Optimize camera settings for low-latency streaming
    camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    camera.set(cv2.CAP_PROP_FPS, 30)
    camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)  # Minimal buffer to reduce latency
    camera.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc(*'MJPG'))
    print("✅ Camera initialized successfully!")

# ==================== AI MODEL SETUP ====================
MODEL_PATH = os.getenv('YOLO_MODEL_PATH', 'models/best-fp16(1).tflite')
print(f"\nLoading TFLite model from {MODEL_PATH}...")

try:
    from tflite_runtime.interpreter import Interpreter
    print("Using tflite_runtime")
except ImportError:
    try:
        import tensorflow as tf
        Interpreter = tf.lite.Interpreter
        print("Using tensorflow.lite")
    except ImportError:
        print("⚠️  Warning: No TFLite runtime found! Detection will not work.")
        Interpreter = None

if Interpreter and os.path.exists(MODEL_PATH):
    interpreter = Interpreter(model_path=MODEL_PATH)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    input_shape = input_details[0]['shape']
    INPUT_HEIGHT = input_shape[1]
    INPUT_WIDTH = input_shape[2]
    print(f"✅ TFLite model loaded successfully!")
    print(f"   Input shape: {input_shape}")
else:
    print("⚠️  Warning: Model not loaded!")
    interpreter = None
    INPUT_HEIGHT = 320
    INPUT_WIDTH = 320

# ==================== CLOUDINARY SETUP ====================
cloudinary.config(
    cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME'),
    api_key=os.getenv('CLOUDINARY_API_KEY'),
    api_secret=os.getenv('CLOUDINARY_API_SECRET')
)
print("✅ Cloudinary configured!")

# ==================== MONGODB SETUP ====================
MONGODB_URI = os.getenv('MONGODB_URI', 'mongodb://localhost:27017/')
MONGODB_DB = os.getenv('MONGODB_DATABASE', 'shrimp_db')
try:
    mongo_client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
    mongo_client.server_info()  # Test connection
    db = mongo_client[MONGODB_DB]
    collection = db['detections']
    print(f"✅ Connected to MongoDB: {MONGODB_DB}")
except Exception as e:
    print(f"⚠️  MongoDB connection failed: {e}")
    collection = None

# ==================== AUTH SETUP ====================
USERNAME = os.getenv('CAMERA_USERNAME', 'admin')
PASSWORD = os.getenv('CAMERA_PASSWORD', '123456')

def check_auth(username, password):
    return username == USERNAME and password == PASSWORD

def authenticate():
    return Response(
        'Authentication required', 401,
        {'WWW-Authenticate': 'Basic realm="Login Required"'})

def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not auth or not check_auth(auth.username, auth.password):
            return authenticate()
        return f(*args, **kwargs)
    return decorated

# Khởi tạo Firebase Admin SDK
FIREBASE_CRED_PATH = os.getenv('FIREBASE_CRED_PATH', 'firebase-admin.json')
if os.path.exists(FIREBASE_CRED_PATH):
    cred = credentials.Certificate(FIREBASE_CRED_PATH)
    initialize_app(cred)
    print('✅ Firebase Admin SDK initialized!')
else:
    print('⚠️  Firebase Admin SDK credential file not found!')

# Danh sách email được phép truy cập (admin + user được cấp quyền)
PERMITTED_EMAILS_PATH = 'permitted_emails.json'
PERMITTED_DEVICES_PATH = 'permitted_devices.json'
ADMIN_EMAIL = os.getenv('ADMIN_EMAIL', 'hodung15032003@gmail.com')

# Logging setup
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def load_permitted_emails():
    if os.path.exists(PERMITTED_EMAILS_PATH):
        with open(PERMITTED_EMAILS_PATH, 'r') as f:
            return json.load(f)
    return [ADMIN_EMAIL]

def save_permitted_emails(emails):
    with open(PERMITTED_EMAILS_PATH, 'w') as f:
        json.dump(emails, f)

def requires_google_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        id_token = request.headers.get('Authorization')
        logger.info(f"[AUTH] Received id_token: {id_token}")
        if not id_token:
            logger.warning("[AUTH] Missing Google ID token")
            return jsonify({"success": False, "message": "Missing Google ID token"}), 401
        try:
            decoded_token = firebase_auth.verify_id_token(id_token)
            email = decoded_token.get('email')
            logger.info(f"[AUTH] Decoded email: {email}")
            permitted_emails = load_permitted_emails()
            logger.info(f"[AUTH] Permitted emails: {permitted_emails}")
            if email not in permitted_emails:
                logger.warning(f"[AUTH] Email not permitted: {email}")
                return jsonify({"success": False, "message": "Email not permitted", "email": email, "permitted": permitted_emails}), 403
            request.user_email = email
        except Exception as e:
            logger.error(f"[AUTH] Invalid token: {str(e)}")
            return jsonify({"success": False, "message": f"Invalid token: {str(e)}"}), 401
        return f(*args, **kwargs)
    return decorated

@app.route('/api/auth/check', methods=['POST'])
def check_auth_status():
    """
    Debug endpoint to check authentication status
    Returns detailed info about token and permissions
    No authentication required - for debugging only
    """
    try:
        id_token = request.headers.get('Authorization')

        result = {
            "token_received": id_token is not None,
            "token_length": len(id_token) if id_token else 0,
            "permitted_emails_file_exists": os.path.exists(PERMITTED_EMAILS_PATH),
            "permitted_devices_file_exists": os.path.exists(PERMITTED_DEVICES_PATH),
        }

        if id_token:
            try:
                decoded_token = firebase_auth.verify_id_token(id_token)
                email = decoded_token.get('email')
                result["token_valid"] = True
                result["decoded_email"] = email

                # Check email permissions
                permitted_emails = load_permitted_emails()
                result["permitted_emails"] = permitted_emails
                result["email_permitted"] = email in permitted_emails
                result["admin_email"] = ADMIN_EMAIL
                result["is_admin"] = email == ADMIN_EMAIL

                # Check device bindings
                permitted_devices = load_permitted_devices()
                user_devices = []
                for device_id, binding_info in permitted_devices.items():
                    owner_email = binding_info if isinstance(binding_info, str) else binding_info.get('email')
                    if owner_email == email:
                        user_devices.append({
                            "device_id": device_id,
                            "ip": binding_info.get('ip') if isinstance(binding_info, dict) else None
                        })
                result["user_devices"] = user_devices
                result["has_device_bound"] = len(user_devices) > 0

            except Exception as e:
                result["token_valid"] = False
                result["error"] = str(e)
        else:
            result["token_valid"] = False
            result["error"] = "No token provided"

        return jsonify(result)
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500

# ==================== AI FUNCTIONS ====================
CLASS_NAMES = ['shrimp']

# Hằng số để tính toán kích thước thực tế của tôm
# Giả định: Camera ở độ cao cố định, FOV cố định
# Bạn có thể điều chỉnh các hằng số này dựa trên setup thực tế
PIXEL_TO_CM_RATIO = 0.02  # 1 pixel = 0.02 cm (điều chỉnh theo setup camera của bạn)
# Công thức tính khối lượng tôm: W = a * L^b (W: gram, L: cm)
# Dựa trên nghiên cứu tôm thẻ chân trắng (Litopenaeus vannamei)
LENGTH_WEIGHT_A = 0.0065  # Hệ số a
LENGTH_WEIGHT_B = 3.1     # Hệ số b (thường từ 2.8 - 3.2)

def calculate_shrimp_length(bbox_width, bbox_height):
    """
    Tính chiều dài tôm từ bounding box
    Args:
        bbox_width: chiều rộng bounding box (pixels)
        bbox_height: chiều cao bounding box (pixels)
    Returns:
        length: chiều dài ước tính (cm)
    """
    # Sử dụng cạnh lớn nhất của bounding box làm chiều dài
    max_dimension = max(bbox_width, bbox_height)
    # Chuyển đổi từ pixel sang cm
    length_cm = max_dimension * PIXEL_TO_CM_RATIO
    return round(length_cm, 2)

def calculate_shrimp_weight(length_cm):
    """
    Ước tính khối lượng tôm từ chiều dài
    Sử dụng công thức: W = a * L^b
    Args:
        length_cm: chiều dài tôm (cm)
    Returns:
        weight: khối lượng ước tính (gram)
    """
    if length_cm <= 0:
        return 0.0

    weight_gram = LENGTH_WEIGHT_A * (length_cm ** LENGTH_WEIGHT_B)
    return round(weight_gram, 2)

def preprocess_image(image_np):
    """Tiền xử lý ảnh cho TFLite model"""
    img = cv2.resize(image_np, (INPUT_WIDTH, INPUT_HEIGHT))
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img.astype(np.float32) / 255.0
    img = np.expand_dims(img, axis=0)
    return img

def run_inference(image_np):
    """Chạy inference với TFLite model"""
    if interpreter is None:
        return []

    input_data = preprocess_image(image_np)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()

    outputs = []
    for output in output_details:
        outputs.append(interpreter.get_tensor(output['index']))

    return outputs

def parse_yolo_output(outputs, original_shape, conf_threshold=0.25, iou_threshold=0.45):
    """Parse YOLO TFLite output và apply NMS"""
    detections = []
    orig_h, orig_w = original_shape[:2]

    if len(outputs) == 0:
        return detections

    if len(outputs) == 1:
        output = outputs[0]

        if len(output.shape) == 3:
            output = output[0]

            boxes = []
            scores = []
            class_ids = []

            for detection in output:
                if len(detection) >= 6:
                    x, y, w, h, conf = detection[:5]

                    if conf < conf_threshold:
                        continue

                    if len(detection) == 6:
                        class_id = int(detection[5])
                    else:
                        class_scores = detection[5:]
                        class_id = np.argmax(class_scores)
                        conf = conf * class_scores[class_id]

                    if conf < conf_threshold:
                        continue

                    x1 = int((x - w/2) * orig_w)
                    y1 = int((y - h/2) * orig_h)
                    x2 = int((x + w/2) * orig_w)
                    y2 = int((y + h/2) * orig_h)

                    boxes.append([x1, y1, x2, y2])
                    scores.append(float(conf))
                    class_ids.append(class_id)

            if len(boxes) > 0:
                indices = cv2.dnn.NMSBoxes(boxes, scores, conf_threshold, iou_threshold)

                if len(indices) > 0:
                    for i in indices.flatten():
                        x1, y1, x2, y2 = boxes[i]
                        w = x2 - x1
                        h = y2 - y1
                        x = x1 + w/2
                        y = y1 + h/2

                        # Tính chiều dài và khối lượng tôm
                        length_cm = calculate_shrimp_length(w, h)
                        weight_gram = calculate_shrimp_weight(length_cm)

                        detections.append({
                            "className": CLASS_NAMES[class_ids[i]] if class_ids[i] < len(CLASS_NAMES) else f"class_{class_ids[i]}",
                            "confidence": scores[i],
                            "bbox": {
                                "x": float(x),
                                "y": float(y),
                                "width": float(w),
                                "height": float(h)
                            },
                            "length": length_cm,    # Chiều dài (cm)
                            "weight": weight_gram   # Khối lượng (gram)
                        })

    return detections

def draw_detections(image_np, detections):
    """Vẽ bounding boxes lên ảnh"""
    img = image_np.copy()

    for det in detections:
        bbox = det['bbox']
        x = int(bbox['x'])
        y = int(bbox['y'])
        w = int(bbox['width'])
        h = int(bbox['height'])

        x1 = int(x - w/2)
        y1 = int(y - h/2)
        x2 = int(x + w/2)
        y2 = int(y + h/2)

        color = (0, 255, 0)
        cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)

        # Label với confidence, length và weight
        conf = det['confidence']
        length = det.get('length', 0)
        weight = det.get('weight', 0)
        label = f"{det['className']} {conf:.2f}"
        label2 = f"L:{length}cm W:{weight}g"

        # Vẽ background cho label chính
        label_size, _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.5, 2)
        cv2.rectangle(img, (x1, y1 - label_size[1] - 10),
                     (x1 + label_size[0], y1), color, -1)
        cv2.putText(img, label, (x1, y1 - 5),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)

        # Vẽ background cho label length/weight
        label2_size, _ = cv2.getTextSize(label2, cv2.FONT_HERSHEY_SIMPLEX, 0.4, 1)
        cv2.rectangle(img, (x1, y2),
                     (x1 + label2_size[0] + 4, y2 + label2_size[1] + 8),
                     color, -1)
        cv2.putText(img, label2, (x1 + 2, y2 + label2_size[1] + 4),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.4, (0, 0, 0), 1)

    return img

# ==================== CAMERA STREAMING ====================
def generate_frames():
    """Generate camera frames for MJPEG streaming - Optimized for low latency"""
    if camera is None:
        # Nếu không có camera, trả về ảnh placeholder
        while True:
            placeholder = np.zeros((480, 640, 3), dtype=np.uint8)
            cv2.putText(placeholder, "No Camera", (200, 240),
                       cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 255, 255), 3)
            ret, buffer = cv2.imencode('.jpg', placeholder, [cv2.IMWRITE_JPEG_QUALITY, 80])
            if ret:
                frame = buffer.tobytes()
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')
            time.sleep(0.1)
    else:
        while True:
            try:
                with camera_lock:
                    # Chỉ grab 1 lần để lấy frame mới nhất từ buffer
                    camera.grab()
                    success, frame = camera.retrieve()

                if not success:
                    continue

                # Encode ngay với chất lượng tốt, không skip frame
                ret, buffer = cv2.imencode('.jpg', frame, [
                    cv2.IMWRITE_JPEG_QUALITY, 80,
                    cv2.IMWRITE_JPEG_OPTIMIZE, 1
                ])

                if ret:
                    frame_bytes = buffer.tobytes()
                    yield (b'--frame\r\n'
                           b'Content-Type: image/jpeg\r\n'
                           b'Content-Length: ' + str(len(frame_bytes)).encode() + b'\r\n'
                           b'\r\n' + frame_bytes + b'\r\n')

            except Exception as e:
                print(f"[WARN] Frame generation error: {e}")
                time.sleep(0.01)
                continue

@app.route('/blynk_feed')
@requires_google_auth
def blynk_feed():
    """
    Camera stream endpoint - Requires authentication
    Users with permitted email can access camera stream
    Device binding is optional for monitoring purposes
    """
    try:
        email = request.user_email

        logger.info(f"[STREAM] User {email} accessing camera stream")
        response = Response(generate_frames(),
                           mimetype='multipart/x-mixed-replace; boundary=frame')
        # Thêm headers để giảm buffering và latency
        response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate, max-age=0'
        response.headers['Pragma'] = 'no-cache'
        response.headers['Expires'] = '0'
        response.headers['Connection'] = 'close'
        response.headers['X-Accel-Buffering'] = 'no'  # Tắt buffering của nginx
        return response
    except Exception as e:
        logger.error(f"[STREAM] Error: {str(e)}")
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/video_feed')
@requires_auth
def video_feed():
    """Camera stream endpoint (with auth)"""
    response = Response(generate_frames(),
                       mimetype='multipart/x-mixed-replace; boundary=frame')
    response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
    response.headers['Pragma'] = 'no-cache'
    response.headers['Expires'] = '0'
    response.headers['Connection'] = 'close'
    return response

@app.route('/snapshot')
def snapshot():
    """Get single frame snapshot - faster than streaming for single images"""
    if camera is None:
        placeholder = np.zeros((480, 640, 3), dtype=np.uint8)
        cv2.putText(placeholder, "No Camera", (200, 240),
                   cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 255, 255), 3)
        ret, buffer = cv2.imencode('.jpg', placeholder, [cv2.IMWRITE_JPEG_QUALITY, 85])
    else:
        with camera_lock:
            camera.grab()
            success, frame = camera.retrieve()

        if success:
            ret, buffer = cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 85])
        else:
            placeholder = np.zeros((480, 640, 3), dtype=np.uint8)
            cv2.putText(placeholder, "Frame Error", (180, 240),
                       cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 0, 0), 3)
            ret, buffer = cv2.imencode('.jpg', placeholder, [cv2.IMWRITE_JPEG_QUALITY, 85])

    if ret:
        response = Response(buffer.tobytes(), mimetype='image/jpeg')
        response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
        return response
    else:
        return "Error capturing frame", 500

@app.route('/blynk_player')
def blynk_player():
    """HTML player for camera stream"""
    return '''
    <html>
    <head><title>Camera Stream</title></head>
    <body style="margin:0;padding:0;">
    <img src="/blynk_feed" style="width:100%;height:100%;">
    </body>
    </html>
    '''

# ==================== EMAIL PERMISSION MANAGEMENT ====================
@app.route('/api/admin/add-email', methods=['POST'])
@requires_google_auth
def add_permitted_email():
    """
    Chỉ admin mới được thêm email mới vào danh sách được phép
    """
    if request.user_email != ADMIN_EMAIL:
        return jsonify({"success": False, "message": "Only admin can add emails"}), 403
    data = request.json
    new_email = data.get('email')
    if not new_email:
        return jsonify({"success": False, "message": "Missing email"}), 400
    permitted_emails = load_permitted_emails()
    if new_email in permitted_emails:
        return jsonify({"success": False, "message": "Email already permitted"}), 400
    permitted_emails.append(new_email)
    save_permitted_emails(permitted_emails)
    logger.info(f"[ADMIN] Added email: {new_email}")
    return jsonify({"success": True, "message": "Email added successfully"})

@app.route('/api/admin/list-emails', methods=['GET'])
@requires_google_auth
def list_permitted_emails():
    """
    Lấy danh sách email được phép truy cập
    Chỉ admin mới được xem
    """
    if request.user_email != ADMIN_EMAIL:
        return jsonify({"success": False, "message": "Only admin can view emails"}), 403
    permitted_emails = load_permitted_emails()
    return jsonify({
        "success": True,
        "emails": permitted_emails,
        "admin_email": ADMIN_EMAIL
    })

@app.route('/api/admin/remove-email', methods=['POST'])
@requires_google_auth
def remove_permitted_email():
    """
    Xóa email khỏi danh sách được phép
    Chỉ admin mới được xóa
    Không được xóa email admin
    """
    if request.user_email != ADMIN_EMAIL:
        return jsonify({"success": False, "message": "Only admin can remove emails"}), 403
    data = request.json
    email_to_remove = data.get('email')
    if not email_to_remove:
        return jsonify({"success": False, "message": "Missing email"}), 400
    if email_to_remove == ADMIN_EMAIL:
        return jsonify({"success": False, "message": "Cannot remove admin email"}), 403
    permitted_emails = load_permitted_emails()
    if email_to_remove not in permitted_emails:
        return jsonify({"success": False, "message": "Email not in permitted list"}), 404
    permitted_emails.remove(email_to_remove)
    save_permitted_emails(permitted_emails)
    logger.info(f"[ADMIN] Removed email: {email_to_remove}")
    return jsonify({"success": True, "message": "Email removed successfully"})

# ==================== DETECTION API ====================

@app.route('/api/detect-shrimp', methods=['POST'])
@requires_google_auth
def detect_shrimp():
    """
    Endpoint nhận ảnh từ Android app, xử lý với YOLO TFLite,
    lưu lên Cloudinary và MongoDB, trả về kết quả
    """
    try:
        data = request.json
        image_base64 = data.get('image')
        source = data.get('source', 'unknown')

        if not image_base64:
            return jsonify({
                "success": False,
                "message": "No image data provided"
            }), 400

        print(f"[INFO] Receiving image from {source}")

        # Decode base64 image
        image_data = base64.b64decode(image_base64)
        image = Image.open(BytesIO(image_data))
        image_np = np.array(image)

        if len(image_np.shape) == 3 and image_np.shape[2] == 3:
            image_np = cv2.cvtColor(image_np, cv2.COLOR_RGB2BGR)

        print(f"[INFO] Image size: {image.size}")

        # Run TFLite inference
        print("[INFO] Running TFLite detection...")
        start_time = time.time()
        outputs = run_inference(image_np)
        inference_time = time.time() - start_time
        print(f"[INFO] Inference time: {inference_time:.3f}s")

        # Parse detections
        detections = parse_yolo_output(outputs, image_np.shape)
        print(f"[INFO] Found {len(detections)} detections")

        # Generate annotated image
        annotated_image = draw_detections(image_np, detections)
        annotated_image_rgb = cv2.cvtColor(annotated_image, cv2.COLOR_BGR2RGB)
        img_pil = Image.fromarray(annotated_image_rgb)

        buffer = BytesIO()
        img_pil.save(buffer, format='JPEG', quality=90)
        buffer.seek(0)

        # Upload to Cloudinary
        print("[INFO] Uploading to Cloudinary...")
        upload_result = cloudinary.uploader.upload(
            buffer,
            folder="shrimp-detections",
            resource_type="image"
        )
        cloudinary_url = upload_result['secure_url']
        print(f"[INFO] Uploaded to: {cloudinary_url}")

        # Save to MongoDB
        if collection is not None:
            doc = {
                "imageUrl": upload_result['url'],
                "cloudinaryUrl": cloudinary_url,
                "detections": detections,
                "timestamp": int(time.time() * 1000),
                "capturedFrom": source,
                "inferenceTime": inference_time
            }
            result = collection.insert_one(doc)
            mongo_id = str(result.inserted_id)
            print(f"[INFO] Saved to MongoDB with ID: {mongo_id}")
        else:
            mongo_id = "no-mongodb"

        return jsonify({
            "success": True,
            "imageUrl": upload_result['url'],
            "cloudinaryUrl": cloudinary_url,
            "detections": detections,
            "mongoId": mongo_id,
            "inferenceTime": inference_time,
            "message": "Detection completed successfully"
        })

    except Exception as e:
        print(f"[ERROR] {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({
            "success": False,
            "message": f"Error: {str(e)}"
        }), 500

@app.route('/api/shrimp-images', methods=['GET'])
@requires_google_auth
def get_images():
    """Lấy danh sách tất cả ảnh đã lưu"""
    try:
        if collection is None:
            return jsonify([])

        images = list(collection.find().sort('timestamp', -1).limit(100))
        for img in images:
            img['id'] = str(img['_id'])
            del img['_id']

        print(f"[INFO] Returning {len(images)} images")
        return jsonify(images)
    except Exception as e:
        print(f"[ERROR] {str(e)}")
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/api/shrimp-images/<image_id>', methods=['GET'])
def get_image_detail(image_id):
    """Lấy chi tiết 1 ảnh"""
    try:
        if collection is None:
            return jsonify({
                "success": False,
                "message": "MongoDB not available"
            }), 503

        image = collection.find_one({'_id': ObjectId(image_id)})
        if image:
            image['id'] = str(image['_id'])
            del image['_id']
            return jsonify(image)
        else:
            return jsonify({
                "success": False,
                "message": "Image not found"
            }), 404
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/api/shrimp-images/<image_id>', methods=['DELETE'])
@requires_google_auth
def delete_image(image_id):
    """Xóa ảnh"""
    try:
        if collection is None:
            return jsonify({
                "success": False,
                "message": "MongoDB not available"
            }), 503

        result = collection.delete_one({'_id': ObjectId(image_id)})
        if result.deleted_count > 0:
            print(f"[INFO] Deleted image {image_id}")
            return jsonify({
                "success": True,
                "message": "Image deleted successfully"
            })
        else:
            return jsonify({
                "success": False,
                "message": "Image not found"
            }), 404
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500


def load_permitted_devices():
    """
    Load device bindings: { email: { device_id, ip, last_updated } }
    Mỗi user có device binding riêng, độc lập với nhau
    """
    if os.path.exists(PERMITTED_DEVICES_PATH):
        try:
            with open(PERMITTED_DEVICES_PATH, 'r') as f:
                content = f.read().strip()
                if not content:
                    return {}
                data = json.loads(content)

                # Migrate old format { device_id: email } to new format { email: {device_id, ip} }
                # Check if old format by looking for any device_id keys
                needs_migration = False
                for key, value in data.items():
                    if isinstance(value, str):  # Old format: device_id -> email
                        needs_migration = True
                        break
                    elif isinstance(value, dict) and 'email' in value:  # Old owner format
                        needs_migration = True
                        break

                if needs_migration:
                    logger.info("[DEVICES] Migrating to new format: email -> device_info")
                    new_data = {}
                    for key, value in data.items():
                        if isinstance(value, str):
                            # Old format: device_id -> email
                            email = value
                            device_id = key
                            new_data[email] = {'device_id': device_id, 'ip': None, 'last_updated': time.time()}
                        elif isinstance(value, dict) and 'email' in value:
                            # Old owner format: device_id -> {email, ip}
                            email = value['email']
                            device_id = key
                            ip = value.get('ip')
                            new_data[email] = {'device_id': device_id, 'ip': ip, 'last_updated': time.time()}
                    data = new_data
                    save_permitted_devices(data)  # Save migrated data

                return data
        except json.JSONDecodeError as e:
            logger.error(f"[DEVICES] Error loading {PERMITTED_DEVICES_PATH}: {e}")
            backup_path = f"{PERMITTED_DEVICES_PATH}.corrupt.{int(time.time())}"
            try:
                with open(PERMITTED_DEVICES_PATH, 'r') as f:
                    with open(backup_path, 'w') as fb:
                        fb.write(f.read())
                logger.info(f"[DEVICES] Backed up to {backup_path}")
            except Exception:
                pass
            return {}
        except Exception as e:
            logger.error(f"[DEVICES] Unexpected error: {e}")
            return {}
    return {}

def save_permitted_devices(devices):
    with open(PERMITTED_DEVICES_PATH, 'w') as f:
        json.dump(devices, f, indent=2)

@app.route('/api/devices/bind', methods=['POST'])
@requires_google_auth
def bind_device():
    """
    Endpoint để bind thiết bị với user đang đăng nhập
    Mỗi user có device binding riêng, độc lập với các user khác
    Format: { email: { device_id, ip, last_updated } }
    """
    try:
        data = request.json
        device_id = data.get('device_id')
        device_ip = data.get('device_ip')

        if not device_id:
            return jsonify({
                "success": False,
                "message": "Missing device_id"
            }), 400

        email = request.user_email
        permitted_devices = load_permitted_devices()

        # Check if user already has a device bound
        if email in permitted_devices:
            existing_device = permitted_devices[email]
            existing_device_id = existing_device.get('device_id')

            # If binding the same device, update IP
            if existing_device_id == device_id:
                permitted_devices[email] = {
                    'device_id': device_id,
                    'ip': device_ip,
                    'last_updated': time.time()
                }
                save_permitted_devices(permitted_devices)
                logger.info(f"[BIND] Updated device for {email}: {device_id} with IP {device_ip}")

                return jsonify({
                    "success": True,
                    "message": "Device already bound to your account",
                    "device_id": device_id,
                    "device_ip": device_ip
                })
            else:
                # User is switching to a different device
                permitted_devices[email] = {
                    'device_id': device_id,
                    'ip': device_ip,
                    'last_updated': time.time()
                }
                save_permitted_devices(permitted_devices)
                logger.info(f"[BIND] User {email} switched device from {existing_device_id} to {device_id}")

                return jsonify({
                    "success": True,
                    "message": "Device switched successfully",
                    "device_id": device_id,
                    "device_ip": device_ip
                })

        # Bind new device to this user
        permitted_devices[email] = {
            'device_id': device_id,
            'ip': device_ip,
            'last_updated': time.time()
        }
        save_permitted_devices(permitted_devices)

        logger.info(f"[BIND] Device {device_id} bound to {email} with IP {device_ip}")

        return jsonify({
            "success": True,
            "message": "Device bound successfully",
            "device_id": device_id,
            "device_ip": device_ip
        })
    except Exception as e:
        logger.error(f"[BIND] Error: {str(e)}")
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/api/devices/check', methods=['POST'])
@requires_google_auth
def check_device_permission():
    """
    Kiểm tra xem user có device nào đã bind không
    Mỗi user có device riêng, độc lập
    """
    try:
        data = request.json
        device_id = data.get('device_id')
        if not device_id:
            return jsonify({
                "success": False,
                "message": "Missing device_id"
            }), 400

        email = request.user_email
        permitted_devices = load_permitted_devices()

        # Check if user has this device bound
        if email not in permitted_devices:
            return jsonify({
                "success": False,
                "message": "You don't have any device bound"
            }), 404

        user_device = permitted_devices[email]
        user_device_id = user_device.get('device_id')

        if user_device_id != device_id:
            return jsonify({
                "success": False,
                "message": f"You have a different device bound: {user_device_id}"
            }), 403

        logger.info(f"[CHECK] User {email} has access to device {device_id}")

        return jsonify({
            "success": True,
            "message": "Access granted",
            "device_id": device_id
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/api/devices/unbind', methods=['POST'])
@requires_google_auth
def unbind_device():
    """
    Hủy bind thiết bị của chính user này
    Mỗi user có thể unbind device của riêng mình
    """
    try:
        data = request.json
        device_id = data.get('device_id')
        if not device_id:
            return jsonify({
                "success": False,
                "message": "Missing device_id"
            }), 400

        email = request.user_email
        permitted_devices = load_permitted_devices()

        # Check if user has any device bound
        if email not in permitted_devices:
            return jsonify({
                "success": False,
                "message": "You don't have any device bound"
            }), 404

        user_device = permitted_devices[email]
        user_device_id = user_device.get('device_id')

        # Check if user is trying to unbind their own device
        if user_device_id != device_id:
            return jsonify({
                "success": False,
                "message": f"Device mismatch. Your bound device is: {user_device_id}"
            }), 400

        # Remove user's device binding
        del permitted_devices[email]
        save_permitted_devices(permitted_devices)

        logger.info(f"[UNBIND] User {email} unbound device {device_id}")

        return jsonify({
            "success": True,
            "message": "Device unbound successfully"
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/api/devices/my-device', methods=['GET'])
@requires_google_auth
def get_my_device():
    """
    Lấy thông tin thiết bị của user này
    Mỗi user có device riêng, độc lập
    Trả về cả IP nếu có
    """
    try:
        email = request.user_email
        permitted_devices = load_permitted_devices()

        # Check if user has device bound
        if email in permitted_devices:
            user_device = permitted_devices[email]
            device_id = user_device.get('device_id')
            device_ip = user_device.get('ip')

            logger.info(f"[MY-DEVICE] User {email} has device {device_id} with IP {device_ip}")

            return jsonify({
                "success": True,
                "device_id": device_id,
                "device_ip": device_ip,
                "bound": True
            })
        else:
            logger.info(f"[MY-DEVICE] User {email} has no device bound")
            return jsonify({
                "success": True,
                "device_id": None,
                "device_ip": None,
                "bound": False,
                "message": "No device bound to this account"
            })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/api/devices/access-token', methods=['POST'])
@requires_google_auth
def get_access_token():
    """
    Endpoint trả về access token cho thiết bị (deprecated - dùng bind thay thế)
    """
    try:
        data = request.json
        device_id = data.get('device_id')
        if not device_id:
            return jsonify({
                "success": False,
                "message": "Missing device_id"
            }), 400

        email = request.user_email
        permitted_devices = load_permitted_devices()

        # Kiểm tra quyền
        if device_id in permitted_devices:
            owner = permitted_devices[device_id]
            if owner != email:
                return jsonify({
                    "success": False,
                    "message": f"Device belongs to {owner}"
                }), 403

        # Sinh token giả lập
        access_token = f"token_{device_id}_{email}_123456"
        return jsonify({
            "success": True,
            "access_token": access_token,
            "message": "Access token generated successfully"
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "camera": "available" if camera is not None else "not found",
        "model": MODEL_PATH,
        "model_type": "TFLite",
        "model_loaded": interpreter is not None,
        "mongodb": "connected" if collection is not None else "not connected",
        "cloudinary": "configured"
    })

# ==================== UDP RESPONDER FOR DEVICE DISCOVERY ====================
DEVICE_ID = os.getenv('DEVICE_ID', 'raspberrypi-001')
UDP_PORT = 50000

def udp_responder():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    sock.bind(("", UDP_PORT))
    print(f"[UDP] Listening for DISCOVER_RASP on UDP port {UDP_PORT}...")
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            message = data.decode("utf-8").strip()
            print(f"[UDP] Received: {message} from {addr}")
            if message == "DISCOVER_RASP":
                response = f"{DEVICE_ID}"
                sock.sendto(response.encode("utf-8"), addr)
                print(f"[UDP] Sent response: {response} to {addr}")
        except Exception as e:
            print(f"[UDP] Error: {e}")

if __name__ == '__main__':
    print("\n" + "="*50)
    print("🦐 Shrimp Detection Server (TFLite) Starting...")
    print("="*50)
    print(f"Camera: {'✅ Available' if camera else '❌ Not found'}")
    print(f"Model: {'✅ Loaded' if interpreter else '❌ Not loaded'}")
    print(f"MongoDB: {'✅ Connected' if collection is not None else '❌ Not connected'}")
    print(f"Cloudinary: ✅ Configured")
    print("\nEndpoints:")
    print("  - Camera Stream: /blynk_feed")
    print("  - Detection API: /api/detect-shrimp")
    print("  - Gallery API: /api/shrimp-images")
    print("  - Health Check: /health")
    print("="*50 + "\n")

    # Start UDP responder in a background thread
    udp_thread = threading.Thread(target=udp_responder, daemon=True)
    udp_thread.start()

    app.run(host='0.0.0.0', port=8000, debug=False, threaded=True)
