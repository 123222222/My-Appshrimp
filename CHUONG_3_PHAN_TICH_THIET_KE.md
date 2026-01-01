- Máy chủ đám mây Ngrok cấp một địa chỉ công khai ngẫu nhiên (ví dụ: `https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev`)
- Kết nối được duy trì liên tục, không đóng cho đến khi tắt máy
4. **num_detections [1]**: Số lượng detections hợp lệ (không phải zero/padding). Thường < N vì không phải lúc nào cũng detect đầy 100+ objects.
**2. Luồng Yêu Cầu (Từ Điện Thoại → Máy Chủ):**
**Extract Valid Detections**: `boxes[0][:num_detections]` lấy batch đầu tiên và slice chỉ lấy num_detections phần tử đầu, bỏ qua padding zeros.
**Bước 1:** Ứng dụng Android gửi yêu cầu HTTPS đến địa chỉ công khai của Ngrok
**Performance**: 
Ứng dụng Android → POST https://xxx.ngrok-free.dev/api/detect-shrimp
Tiêu đề: Authorization: <Mã-Xác-Thực-Firebase>
Nội dung: { "image": "<ảnh-mã-hóa-base64>", "source": "camera_stream" }
- TFLite INT8 (quantized): ~1-1.5 giây (có thể implement sau)

**Bước 2:** Máy chủ đám mây Ngrok nhận yêu cầu
- Giải mã lớp mã hóa TLS (kết thúc mã hóa HTTPS ở đây)
- Xác định yêu cầu này thuộc về đường hầm nào (dựa vào tên miền phụ)
- Tìm kết nối tương ứng với Raspberry Pi
Post-processing lọc và format raw outputs từ model thành kết quả clean, ready để visualize và lưu database.
**Bước 3:** Máy chủ đám mây Ngrok chuyển tiếp qua đường hầm bảo mật
- Chuyển đổi yêu cầu HTTP thành dạng tin nhắn nhị phân
- Gửi qua đường hầm (được mã hóa) đến chương trình Ngrok trên Raspberry Pi
- Đường hầm cho phép gửi/nhận đồng thời hai chiều
        'success': False,
**Bước 4:** Chương trình Ngrok trên Raspberry Pi nhận và xử lý
- Nhận tin nhắn từ đường hầm, chuyển đổi ngược thành yêu cầu HTTP
- Chuyển tiếp đến `localhost:8000/api/detect-shrimp` (kết nối cục bộ trong cùng máy Raspberry Pi)
- Máy chủ Flask nhận yêu cầu như máy chủ bình thường
        
**3. Luồng Phản Hồi (Từ Máy Chủ → Điện Thoại):**
        list: Filtered detection objects
**Bước 1:** Máy chủ Flask xử lý và trả kết quả
        'message': 'Email already in whitelist'
# File app_complete.py xử lý nhận diện
```

**Backend workflow:**
1. Verify admin token
2. Validate email format
3. Check không trùng lặp
4. Load permitted_emails.json
5. Append email vào list
**Bước 2:** Chương trình Ngrok nhận phản hồi HTTP từ localhost
- Chuyển đổi phản hồi thành tin nhắn
- Gửi ngược về máy chủ đám mây Ngrok qua cùng đường hầm
**Client usage (Admin Panel):**
**Bước 3:** Máy chủ đám mây Ngrok chuyển tiếp về điện thoại
- Chuyển đổi tin nhắn ngược thành phản hồi HTTP
- Mã hóa lại bằng TLS
- Gửi về ứng dụng Android qua kết nối HTTPS ban đầu (vẫn đang mở và chờ)
        showError("Invalid email format")
**Bước 4:** Ứng dụng Android nhận kết quả
    }
// File ShrimpApiService.kt
    val result = apiService.addPermittedEmail(email)
// Hiển thị ảnh nhận diện và số lượng tôm
        _emails.value = result.emails
        showSuccess("Added ${email} successfully")
**4. Trường Hợp Đặc Biệt: Truyền Hình Camera**
}
Với truyền hình camera, luồng dữ liệu là dòng liên tục thay vì hỏi-đáp:
**Giải thích:**
**Bước 1:** Ứng dụng Android mở kết nối liên tục
**Confidence Filtering**: Threshold 0.5 (50%) là balance tốt:
// File CameraStreamScreen.kt
client.newCall(request).execute() // Không đóng kết nối
- 0.5 là standard value cho YOLO detection

**Bước 2:** Máy chủ Flask tạo khung hình liên tục

**Coordinate Conversion**:
Model output coordinates đã normalized về [0, 1] (relative to image size). Để vẽ hoặc display, cần convert về pixels. Ví dụ:
- Normalized: x1=0.3, image width=640 → Pixel x1 = 0.3 * 640 = 192
- Format output: {x: 0.3, y: 0.2, width: 0.4, height: 0.3} giữ normalized để flexible với different image sizes

        # 30 khung hình mỗi giây

**Non-Maximum Suppression (NMS)**: Trong code hoàn chỉnh, bước này cũng áp dụng NMS để loại bỏ duplicate detections. Khi model detect cùng một object nhiều lần với boxes overlap, NMS giữ box có confidence cao nhất và xóa các box overlap > 50%. TFLite model đã có NMS built-in nên không cần implement riêng.
**Bước 3:** Truyền qua đường hầm Ngrok
- Mỗi khung hình JPEG (~20-50KB) được đóng gói
- Chương trình Ngrok chuyển đổi từng khung hình thành tin nhắn
- Gửi liên tục qua đường hầm với tốc độ 30 khung/giây
- Máy chủ đám mây Ngrok chuyển tiếp đến ứng dụng Android

**Bước 4:** Ứng dụng Android giải mã và hiển thị
- Đọc dòng dữ liệu, tìm đánh dấu JPEG (0xFFD8 bắt đầu, 0xFFD9 kết thúc)
- Giải mã thành ảnh bitmap, cập nhật giao diện
- Quá trình lặp lại 30 lần mỗi giây → tạo video mượt
Headers:
  - Authorization: <Admin-Firebase-ID-Token>
Body:
{
  "email": "user@example.com"
**Lớp 1:** Ứng dụng Android ↔ Máy chủ đám mây Ngrok = **HTTPS/TLS 1.3**
- Chứng chỉ bảo mật của Ngrok cho tên miền *.ngrok-free.dev
Response:
{
**Lớp 2:** Máy chủ đám mây Ngrok ↔ Chương trình Ngrok = **Đường Hầm Bảo Mật**
- Kết nối đường hầm được mã hóa
- Chỉ máy chủ đám mây Ngrok và chương trình Ngrok mới giải mã được
}
**Kết quả:** Mã hóa đầu cuối cho truyền hình camera, mã xác thực, ảnh nhận diện, lệnh điều khiển động cơ.

**Request Body:**
- `email`: Email cần xóa khỏi whitelist
- **Không Cần Cấu Hình**: Không cần IP công khai, không cấu hình bộ định tuyến, không mở cổng
- **Vượt Tường Lửa**: Kết nối đi ra ngoài luôn được phép, không bị chặn
- **Tái Sử Dụng Kết Nối**: 1 đường hầm xử lý hàng nghìn yêu cầu HTTP
- **Trong Suốt**: Mã nguồn máy chủ/điện thoại không cần biết về Ngrok, chỉ cần địa chỉ
- **HTTPS Tự Động**: Chứng chỉ bảo mật miễn phí từ Ngrok
- **Liên Tục & Độ Trễ Thấp**: Kết nối liên tục, không tốn thời gian kết nối lại
        'success': False,
        'error': 'FORBIDDEN',
        'message': 'Cannot remove admin email'
- **Địa Chỉ Ngẫu Nhiên**: Gói miễn phí cấp địa chỉ ngẫu nhiên mỗi khi khởi động lại → phải cập nhật Config.kt
- **Độ Trễ Thêm Vào**: Thêm ~50-150 mili giây so với kết nối trực tiếp
- **Giới Hạn 1 Đường Hầm**: Gói miễn phí chỉ 1 đường hầm cùng lúc
- **Phụ Thuộc Bên Ngoài**: Phụ thuộc dịch vụ đám mây Ngrok (nếu Ngrok gặp sự cố, hệ thống không truy cập được)
```json
**Luồng Dữ Liệu Chi Tiết Theo Chức Năng:**
  "success": false,
**1. Luồng Nhận Diện Tôm (Gửi Yêu Cầu API):**
  "message": "Email not found in whitelist"
Ứng dụng Android (ShrimpApiService.kt)
```
    │ Yêu cầu: POST https://xxx.ngrok-free.dev/api/detect-shrimp
    │ Tiêu đề: Authorization: <Mã-Xác-Thực-Firebase>
    │ Nội dung: { "image": "<ảnh-base64>", "source": "camera_stream" }
@app.route('/api/admin/permitted-emails', methods=['DELETE'])
Máy Chủ Đám Mây Ngrok
    │ Nhận yêu cầu HTTPS
    │ Giải mã TLS
    │ Định tuyến đến đường hầm cho thiết bị "xxx.ngrok-free.dev"
    
Chương Trình Ngrok (Raspberry Pi)
    │ Nhận qua đường hầm
    │ Chuyển tiếp đến localhost:8000/api/detect-shrimp
            'success': False,
Máy Chủ Flask (app_complete.py)
            'message': 'Cannot remove admin email'
    │ @requires_google_auth (xác minh mã Firebase)
    │ Giải mã ảnh base64 → Ảnh PIL
    │ Tiền xử lý: thu nhỏ 320x320, chuẩn hóa
    │ Chạy AI: interpreter.invoke()
    │ Hậu xử lý: lọc độ tin cậy > 0.5
    │ Vẽ khung bao quanh tôm bằng OpenCV
    │ Tải lên Cloudinary: cloudinary.uploader.upload()
    │ Lưu vào MongoDB: collection.insert_one()
    │ Trả về: { "success": true, "imageUrl": "...", "detections": [...] }
            'error': 'NOT_FOUND',
Chương Trình Ngrok
    │ Chuyển phản hồi ngược qua đường hầm
    
Máy Chủ Đám Mây Ngrok
    │ Mã hóa lại bằng TLS
    save_permitted_emails(permitted_emails)
Ứng Dụng Android
    return jsonify({
    │ Hiển thị ảnh + số lượng tôm trên giao diện
        'message': 'Email removed successfully',
        'emails': permitted_emails
**2. Luồng Truyền Hình Camera:**
```
Ứng Dụng Android (CameraStreamScreen.kt)
**Side Effects:**
Khi một email bị xóa khỏi whitelist:
    │ Yêu cầu: GET https://xxx.ngrok-free.dev/blynk_feed
    │ Kết nối liên tục (không giới hạn thời gian)
3. User phải liên hệ admin để được cấp lại quyền
Ngrok → Máy Chủ Flask

**Tương tự với Permitted Phones:**
    │ Trả về: Response(generate_frames(), mimetype='multipart/x-mixed-replace')
- `GET /api/admin/permitted-phones` - List phones
- `POST /api/admin/permitted-phones` - Add phone (validate E.164 format)
- `DELETE /api/admin/permitted-phones` - Remove phone
    │       ret, frame = camera.read()  # Chụp từ camera
### 3.3.3. Thiết Kế Module AI Detection

Module AI Detection là trái tim của hệ thống, chịu trách nhiệm nhận diện tôm từ ảnh. Module này được thiết kế modular, tách biệt rõ ràng các bước xử lý để dễ debug, optimize và thay thế model.
Chương Trình Ngrok → Máy Chủ Đám Mây Ngrok
    │ Truyền dữ liệu MJPEG qua đường hầm
    │ Dòng liên tục: 30 khung hình/giây
Pipeline xử lý ảnh tuân theo workflow chuẩn của computer vision và deep learning:
Ứng Dụng Android
  - `detections`: Array of detection objects (có thể rỗng nếu không phát hiện tôm nào)
    │ Tìm đánh dấu JPEG: 0xFFD8 (bắt đầu), 0xFFD9 (kết thúc)
  - `capturedFrom`: Email của người chụp
    │ currentFrame = bitmap (kích hoạt vẽ lại giao diện)
    │ Hiển thị trong Image composable

**Backend implementation:**
**3. Luồng Điều Khiển Động Cơ:**
@app.route('/api/shrimp-images', methods=['GET'])
Ứng Dụng Android (MotorControlScreen.kt)
def get_shrimp_images():
    │ Yêu cầu: POST https://xxx.ngrok-free.dev/api/motor/DONG_CO_1/on
    limit = min(int(request.args.get('limit', 20)), 100)  # Cap at 100
Ngrok → Máy Chủ Flask
    start_date = request.args.get('startDate')
    end_date = request.args.get('endDate')
    
    │ Trả về: { "success": true, "motor": "DONG_CO_1", "state": "on" }
    query = {'capturedFrom': g.user['email']}  # User only sees own images
Ứng Dụng Android
    │ Cập nhật giao diện: motorStates["DONG_CO_1"] = true
            '$gte': int(start_date),
            '$lte': int(end_date)
**Phân Tích Mã Nguồn Sử Dụng Ngrok:**
    
**Trong Ứng Dụng Android:**
    cursor = db.detections.find(query).sort('timestamp', -1).skip(skip).limit(limit)
    images = list(cursor)
    
    # Count total (for pagination)
    // ⚠️ CẬP NHẬT URL này khi Ngrok khởi động lại
    
    # Format response
    return jsonify({
        'success': True,
Chú thích "CẬP NHẬT URL này khi Ngrok khởi động lại" chỉ ra hạn chế chính: Gói miễn phí Ngrok cấp địa chỉ ngẫu nhiên mỗi lần khởi động lại.
        'total': total,
        'limit': limit,
        'skip': skip
    })
```

**Client usage with pagination:**
```kotlin
// GalleryViewModel.kt
class GalleryViewModel : ViewModel() {
    private var currentPage = 0
    private val pageSize = 20
    
    fun loadNextPage() {
        viewModelScope.launch {
            val result = apiService.getShrimpImages(
                limit = pageSize,
                skip = currentPage * pageSize
            )
            
            _images.value += result.images  // Append to list
            currentPage++
            
            _hasMore.value = (currentPage * pageSize) < result.total
        }
    }
}
```

# CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG

## 3.1. PHÂN TÍCH YÊU CẦU HỆ THỐNG

Việc phân tích yêu cầu hệ thống là bước quan trọng đầu tiên trong quá trình phát triển phần mềm, giúp xác định rõ ràng các chức năng cần thiết cũng như các ràng buộc kỹ thuật của hệ thống. Dựa trên bài toán giám sát và nhận diện tôm trong ao nuôi, hệ thống được phân tích thành hai nhóm yêu cầu chính: yêu cầu chức năng (Functional Requirements) và yêu cầu phi chức năng (Non-Functional Requirements).

### 3.1.1. Yêu Cầu Chức Năng

Yêu cầu chức năng mô tả các tính năng cụ thể mà hệ thống cần cung cấp cho người dùng. Trong hệ thống nhận diện tôm, các yêu cầu chức năng được chia thành 5 nhóm chính.

#### 3.1.1.1. Quản lý người dùng và xác thực

Hệ thống cần có cơ chế quản lý người dùng và xác thực chặt chẽ để đảm bảo chỉ những người được phép mới có thể truy cập vào hệ thống giám sát. Cụ thể, hệ thống cho phép người dùng đăng nhập bằng hai phương thức: tài khoản Google hoặc số điện thoại (RF-01). Việc hỗ trợ đa dạng phương thức đăng nhập giúp tăng tính linh hoạt và tiện lợi cho người dùng, đặc biệt là những người làm việc tại ao nuôi có thể không có tài khoản Google.

Để thực hiện xác thực, hệ thống tích hợp Firebase Authentication (RF-02), một dịch vụ xác thực mạnh mẽ của Google hỗ trợ nhiều phương thức đăng nhập và quản lý phiên làm việc an toàn. Trong file `app_complete.py` của backend, decorator `@requires_google_auth` được triển khai để xác minh ID Token từ Firebase: "decoded_token = firebase_auth.verify_id_token(id_token)". Đối với xác thực bằng số điện thoại, hệ thống kiểm tra header "X-Phone-Auth" và so sánh với danh sách số điện thoại được phép trong file `permitted_phones.json`.

Hệ thống áp dụng cơ chế phân quyền dựa trên danh sách trắng (whitelist) (RF-03), nghĩa là chỉ những email hoặc số điện thoại có trong danh sách `permitted_emails.json` và `permitted_phones.json` mới được truy cập. Như trong code backend: "if email not in permitted_emails: return jsonify({'success': False, 'message': 'Email not permitted'}), 403". Cơ chế này đảm bảo rằng ngay cả khi một người dùng đã xác thực thành công qua Firebase, họ vẫn cần được admin cấp quyền trước.

Quản trị viên (admin) có toàn quyền quản lý danh sách người dùng được phép truy cập (RF-04) thông qua các API endpoint `/api/admin/permitted-emails` và `/api/admin/permitted-phones`. Admin có thể thêm người dùng mới bằng hàm `save_permitted_emails(emails)` hoặc xóa người dùng khỏi danh sách, giúp kiểm soát chặt chẽ quyền truy cập hệ thống.

Mỗi người dùng có thể liên kết thiết bị của mình với hệ thống thông qua cơ chế device binding (RF-05). Trong file `permitted_devices.json`, mỗi thiết bị được gắn với một email hoặc số điện thoại cụ thể: "device-id-123": {"email": "user@example.com", "bindTime": 1703318400000}". Điều này ngăn chặn việc một tài khoản được sử dụng đồng thời trên nhiều thiết bị không được phép, tăng cường bảo mật.

#### 3.1.1.2. Giám sát camera thời gian thực

Chức năng giám sát camera thời gian thực là trọng tâm của hệ thống, cho phép người dùng theo dõi tình hình trong ao nuôi tôm từ xa. Hệ thống hiển thị luồng video trực tiếp từ camera (RF-06) thông qua endpoint `/blynk_feed` được triển khai trong backend Flask. Trong file `app_complete.py`, camera được khởi tạo bằng OpenCV: "camera = cv2.VideoCapture(i, cv2.CAP_V4L2)" và được cấu hình với độ phân giải 640x480 pixel và tốc độ 30 FPS để cân bằng giữa chất lượng hình ảnh và băng thông mạng.

Hệ thống sử dụng giao thức MJPEG (Motion JPEG) để streaming video (RF-07), một giao thức phù hợp cho streaming qua HTTP với độ trễ thấp. Trong code, mỗi frame được encode thành JPEG và gửi về client với header "Content-Type: multipart/x-mixed-replace; boundary=frame". MJPEG được chọn vì tính đơn giản, độ tương thích cao với các trình duyệt và ứng dụng mobile, đồng thời không yêu cầu codec phức tạp như H.264.

Người dùng có thể xem video với độ trễ thấp dưới 1 giây (RF-08) nhờ vào việc tối ưu hóa buffer và sử dụng MJPEG streaming. Trong `CameraStreamScreen.kt`, ứng dụng Android đọc stream bằng OkHttpClient với timeout được cấu hình hợp lý: "readTimeout(30, TimeUnit.SECONDS)" và xử lý từng frame ngay khi nhận được mà không đợi buffer đầy, giảm thiểu độ trễ.

Hệ thống có khả năng tự động kết nối lại khi mất kết nối (RF-09) thông qua cơ chế retry logic trong coroutine của Kotlin. Khi phát hiện lỗi kết nối (catch Exception), ứng dụng sẽ tự động thử kết nối lại sau một khoảng thời gian ngắn mà không cần người dùng can thiệp, đảm bảo tính liên tục của việc giám sát.

#### 3.1.1.3. Nhận diện và phát hiện tôm

Chức năng nhận diện tôm sử dụng trí tuệ nhân tạo là tính năng cốt lõi giúp tự động hóa việc đếm và theo dõi tôm. Người dùng có thể chụp ảnh từ camera stream (RF-10) bằng cách nhấn nút capture trên giao diện `CameraStreamScreen`. Khi đó, frame hiện tại từ biến `currentFrame` được chuyển đổi sang định dạng Base64: "val base64Image = bitmapToBase64(bitmap)" và gửi đến backend qua API.

Hệ thống tự động phát hiện và đánh dấu vị trí tôm trong ảnh (RF-11) bằng cách vẽ bounding box (hộp giới hạn) xung quanh mỗi con tôm được phát hiện. Trong backend, sau khi model AI trả về tọa độ các detection, hàm `draw_detections()` sử dụng OpenCV để vẽ hình chữ nhật màu xanh lá: "cv2.rectangle(annotated, (x1, y1), (x2, y2), color=(0, 255, 0), thickness=2)" cùng với nhãn hiển thị độ tin cậy.

Hệ thống sử dụng mô hình YOLO được tối ưu hóa dưới dạng TensorFlow Lite (RF-12) để có thể chạy hiệu quả trên Raspberry Pi. File model "best-fp16(1).tflite" sử dụng định dạng FP16 (16-bit floating point) giúp giảm kích thước model xuống một nửa so với FP32 mà vẫn giữ được độ chính xác cao. Trong code khởi tạo: "interpreter = Interpreter(model_path=MODEL_PATH)" và "interpreter.allocate_tensors()" chuẩn bị model để thực hiện inference.

Với mỗi đối tượng phát hiện được, hệ thống hiển thị độ chính xác (confidence score) (RF-13), một giá trị từ 0 đến 1 cho biết mức độ chắc chắn của model. Trong hàm `post_process()`, chỉ những detection có score lớn hơn ngưỡng 0.5 mới được giữ lại: "if score < confidence_threshold: continue". Điều này lọc bỏ các detection không chính xác, giảm false positive.

Số lượng tôm phát hiện được trong ảnh (RF-14) được tính tự động bằng cách đếm số phần tử trong mảng `filtered_detections` và hiển thị trên giao diện: "detectionCount = result.detections.size". Thông tin này giúp người nuôi tôm nhanh chóng đánh giá mật độ tôm trong từng lần chụp.

Thời gian xử lý nhận diện được tối ưu để dưới 5 giây (RF-15) nhờ việc sử dụng TensorFlow Lite thay vì TensorFlow đầy đủ, cùng với việc resize ảnh về kích thước cố định 320x320 pixel trước khi đưa vào model. Trong thực tế, với Raspberry Pi 4, thời gian trung bình cho một lần inference là khoảng 2-3 giây, đáp ứng yêu cầu thời gian thực.

#### 3.1.1.4. Quản lý thư viện ảnh

Sau khi nhận diện, các ảnh cần được lưu trữ và quản lý hiệu quả để người dùng có thể xem lại lịch sử. Hệ thống tự động lưu ảnh đã nhận diện vào thư viện (RF-16) ngay sau khi quá trình detection hoàn tất. Ảnh được upload lên Cloudinary: "result = cloudinary.uploader.upload(image_bytes)" và metadata được lưu vào MongoDB: "mongo_id = collection.insert_one(document).inserted_id". Việc tự động hóa này đảm bảo không có ảnh nào bị mất và người dùng không cần thao tác thủ công.

Người dùng có thể xem danh sách ảnh dạng lưới (RF-17) thông qua `GalleryScreen` được xây dựng bằng Jetpack Compose. Giao diện sử dụng `LazyVerticalGrid` với 2 cột để hiển thị nhiều ảnh cùng lúc một cách tối ưu: "LazyVerticalGrid(columns = GridCells.Fixed(2))". Mỗi ảnh được tải bất đồng bộ bằng thư viện Coil: "AsyncImage(model = image.imageUrl)" giúp giao diện mượt mà không bị lag.

Thông tin chi tiết của mỗi ảnh bao gồm thời gian chụp và số lượng tôm (RF-18) được hiển thị dưới dạng overlay trên mỗi card trong grid. Trong model `ShrimpImage`, các trường "timestamp" và "detections" chứa thông tin này, và được format trước khi hiển thị: "SimpleDateFormat('HH:mm dd/MM/yyyy').format(Date(timestamp))".

Người dùng có thể xem ảnh ở chế độ toàn màn hình (RF-19) bằng cách nhấn vào bất kỳ ảnh nào trong gallery. Khi đó, ứng dụng navigate đến màn hình chi tiết với ảnh được hiển thị full screen cùng với các thông tin detection chi tiết bên dưới, cho phép người dùng xem rõ từng con tôm được đánh dấu.

Hệ thống hỗ trợ tìm kiếm và lọc ảnh theo thời gian (RF-20) thông qua API endpoint `/api/shrimp-images` với các tham số query như "startDate" và "endDate". Trong MongoDB, index được tạo trên trường timestamp: "db.detections.createIndex({'timestamp': -1})" giúp truy vấn nhanh chóng ngay cả với hàng nghìn ảnh.

#### 3.1.1.5. Thống kê và báo cáo

Chức năng thống kê giúp người nuôi tôm theo dõi xu hướng và đưa ra quyết định dựa trên dữ liệu. Hệ thống hiển thị biểu đồ thống kê số lượng tôm theo thời gian (RF-21) trong `ChartScreen`, sử dụng thư viện charting để vẽ line chart hoặc bar chart. Dữ liệu được aggregate từ MongoDB bằng pipeline: "db.detections.aggregate([{$group: {_id: '$date', total: {$sum: '$detectionCount'}}}])".

Người dùng có thể xem thống kê theo các khung thời gian khác nhau: ngày, tuần, tháng (RF-22) thông qua các nút lựa chọn trên giao diện. Mỗi lựa chọn sẽ trigger một query khác nhau đến backend với tham số "period" tương ứng, và dữ liệu được nhóm theo đúng khung thời gian đã chọn.

Các chỉ số tổng hợp như tổng số ảnh đã chụp và tổng số tôm phát hiện được (RF-23) được hiển thị ở đầu màn hình thống kê trong một card riêng. Các giá trị này được tính bằng MongoDB aggregation: "db.detections.count()" cho số ảnh và "db.detections.aggregate([{$group: {_id: null, total: {$sum: '$detectionCount'}}}])" cho tổng số tôm.

### 3.1.2. Yêu Cầu Phi Chức Năng

Yêu cầu phi chức năng định nghĩa các ràng buộc về chất lượng, hiệu năng và các thuộc tính hệ thống mà không liên quan trực tiếp đến chức năng nghiệp vụ. Những yêu cầu này quyết định tính khả dụng và độ tin cậy của hệ thống trong môi trường thực tế.

#### 3.1.2.1. Hiệu năng

Hiệu năng là yếu tố quan trọng quyết định trải nghiệm người dùng, đặc biệt với ứng dụng thời gian thực như giám sát camera. Thời gian tải camera stream được yêu cầu dưới 3 giây (NFR-01) để người dùng không phải chờ đợi lâu khi mở ứng dụng. Điều này đạt được nhờ việc backend luôn duy trì kết nối với camera sẵn sàng: "camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)" giảm buffer để frame mới được gửi đi ngay lập tức.

Thời gian nhận diện tôm cần dưới 5 giây (NFR-02) để người dùng có thể chụp nhiều ảnh liên tiếp mà không bị gián đoạn. Trong thực nghiệm, với model TFLite FP16 trên Raspberry Pi 4, thời gian trung bình là 2.45 giây bao gồm cả preprocessing, inference, postprocessing và upload lên cloud. Con số này được đo và lưu trong metadata: "processingTime: 2.45".

Hệ thống xử lý được tối thiểu 30 FPS cho video stream (NFR-03) để đảm bảo video mượt mà và không bị giật lag. Trong code cấu hình camera: "camera.set(cv2.CAP_PROP_FPS, 30)" đặt frame rate của camera về 30 FPS. Backend cũng tối ưu hóa vòng lặp capture và encode để không làm giảm tốc độ: "delay(10)" chỉ delay 10ms giữa các frame.

Ứng dụng mobile phản hồi mượt mà ở 60 FPS (NFR-04) nhờ vào việc sử dụng Jetpack Compose, framework UI hiện đại của Android với rendering pipeline được tối ưu hóa. Các thao tác nặng như network request được thực hiện trong coroutine trên Dispatchers.IO để không block UI thread, đảm bảo giao diện luôn responsive.

Hệ thống backend xử lý được đồng thời 5-10 request (NFR-05) dù chạy trên Raspberry Pi với tài nguyên hạn chế. Flask server sử dụng threading để xử lý nhiều request song song, và các thao tác I/O như đọc camera hoặc upload cloud được tối ưu hóa để giải phóng thread nhanh chóng.

#### 3.1.2.2. Bảo mật

Bảo mật là ưu tiên hàng đầu khi hệ thống cho phép truy cập từ xa vào camera giám sát. Mọi API đều yêu cầu xác thực (NFR-06) thông qua decorator `@requires_google_auth` được áp dụng cho tất cả các endpoint nhạy cảm. Không có endpoint nào cho phép anonymous access ngoại trừ health check: "if not id_token and not phone_number: return 401".

Hệ thống sử dụng HTTPS cho truyền tải dữ liệu (NFR-07) khi triển khai production. Trong thực tế, backend được đặt sau Ngrok hoặc reverse proxy với SSL certificate: "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev" như trong URL của `CameraStreamScreen.kt`. Điều này mã hóa toàn bộ dữ liệu truyền giữa client và server, bảo vệ khỏi bị nghe lén.

Token xác thực có thời gian hết hạn (NFR-08) được quản lý bởi Firebase Authentication. ID Token mặc định có hiệu lực 1 giờ, sau đó ứng dụng tự động làm mới token: "user.getIdToken(true).await()" với tham số force refresh. Trong `TokenManager`, token cũ được kiểm tra thời gian hết hạn: "if (System.currentTimeMillis() > expiry)" trước khi sử dụng.

Mật khẩu và thông tin nhạy cảm được mã hóa (NFR-09) và không bao giờ lưu trực tiếp. Firebase Authentication xử lý việc hash mật khẩu phía server, còn các API key như Cloudinary được lưu trong file `.env` và không commit lên Git: "CLOUDINARY_API_SECRET=xxx" chỉ tồn tại trên máy chủ production.

Chỉ người dùng được phép mới truy cập được hệ thống (NFR-10) nhờ cơ chế whitelist trong `permitted_emails.json` và `permitted_phones.json`. Ngay cả khi ai đó có token hợp lệ từ Firebase, nếu email/phone không nằm trong danh sách, request sẽ bị từ chối với HTTP 403 Forbidden.

#### 3.1.2.3. Khả năng mở rộng

Hệ thống được thiết kế với khả năng mở rộng để đáp ứng nhu cầu tương lai. Kiến trúc hiện tại có thể mở rộng để hỗ trợ nhiều camera (NFR-11) bằng cách thêm các endpoint khác nhau cho mỗi camera: "/camera1/stream", "/camera2/stream". Config có thể lưu danh sách camera trong database thay vì hard-code, và người dùng chọn camera muốn xem từ dropdown menu.

Cơ sở dữ liệu MongoDB được thiết kế để lưu trữ hàng nghìn ảnh (NFR-12) với schema tối ưu và indexes phù hợp. MongoDB là NoSQL database có khả năng scale horizontal tốt, có thể sharding khi dữ liệu lớn. Với index trên timestamp và capturedFrom: "db.detections.createIndex({'capturedFrom': 1, 'timestamp': -1})", truy vấn vẫn nhanh ngay cả với millions documents.

Hệ thống có thể hỗ trợ thêm các loại nhận diện khác ngoài tôm (NFR-13) bằng cách thay thế hoặc thêm model AI khác. Kiến trúc AI module được tách riêng: model path được config trong biến môi trường "YOLO_MODEL_PATH", và class labels có thể thay đổi tùy theo model. Việc này cho phép nhận diện cá, tảo, hoặc các đối tượng khác trong ao nuôi.

Kiến trúc module hóa giúp dễ bảo trì và nâng cấp (NFR-14). Backend được tổ chức thành các module rõ ràng: Authentication Module, Camera Module, AI Detection Module, Storage Module. Mỗi module có thể được update độc lập mà không ảnh hưởng đến phần khác. Ứng dụng Android cũng tuân theo Clean Architecture với separation of concerns rõ ràng.

#### 3.1.2.4. Độ tin cậy

Hệ thống cần hoạt động ổn định 24/7 (NFR-15) vì việc giám sát ao nuôi là liên tục. Backend được cấu hình như một systemd service: "sudo systemctl enable shrimp-backend.service" để tự động khởi động khi Raspberry Pi boot. Log được ghi liên tục để theo dõi tình trạng hệ thống: "logging.basicConfig(level=logging.INFO, handlers=[logging.FileHandler('app.log')])".

Khi xảy ra lỗi, hệ thống tự động khởi động lại (NFR-16) nhờ cấu hình trong systemd service file: "Restart=always" và "RestartSec=10" có nghĩa là sau 10 giây nếu service bị crash, systemd sẽ tự động restart. Điều này đảm bảo downtime tối thiểu và hệ thống có thể tự phục hồi mà không cần can thiệp thủ công.

Dữ liệu được sao lưu định kỳ (NFR-17) để tránh mất mát. Ảnh được lưu trên Cloudinary, một dịch vụ cloud có khả năng backup và redundancy cao. MongoDB Atlas cũng tự động backup hàng ngày. Các file cấu hình quan trọng như permitted lists được version control bằng Git và có thể restore bất cứ lúc nào.

Độ chính xác nhận diện cần đạt trên 80% (NFR-18) để hệ thống thực sự hữu ích. Model YOLO được train trên dataset tôm với nhiều điều kiện khác nhau (ánh sáng, góc chụp, màu nước) để đảm bảo độ chính xác cao. Confidence threshold được đặt ở 0.5: "if score < 0.5: continue" để lọc bỏ các detection không chắc chắn, tăng precision tổng thể.

#### 3.1.2.5. Khả năng sử dụng

Giao diện được thiết kế đơn giản, trực quan, dễ sử dụng (NFR-19) dành cho người nuôi tôm có thể không am hiểu công nghệ. Jetpack Compose với Material Design 3 cung cấp các component chuẩn, quen thuộc với người dùng Android. Navigation bar ở bottom với icon rõ ràng: "🏠 Home, 📷 Camera, 📚 Gallery, 📊 Chart, 👤 Profile" giúp người dùng dễ dàng di chuyển giữa các màn hình.

Thời gian học cách sử dụng được tối ưu dưới 15 phút (NFR-20) nhờ vào luồng sử dụng đơn giản: đăng nhập → xem camera → nhấn nút chụp → xem kết quả → xem lại trong gallery. Không có quá nhiều tùy chọn phức tạp, mọi thứ được tự động hóa tối đa. Ví dụ, ảnh tự động lưu sau khi nhận diện mà không cần người dùng chọn vị trí lưu.

Hệ thống hỗ trợ đa ngôn ngữ, hiện tại là tiếng Việt và tiếng Anh (NFR-21), có thể mở rộng thêm. String resources được tách riêng trong file `strings.xml` với các variant cho từng ngôn ngữ: "values/strings.xml" cho tiếng Anh, "values-vi/strings.xml" cho tiếng Việt. Người dùng có thể chọn ngôn ngữ trong Settings.

Ứng dụng hoạt động tốt trên các thiết bị Android từ version 6.0 trở lên (NFR-22), được cấu hình trong `build.gradle.kts`: "minSdk = 26" (Android 8.0). Việc support từ API level 26 đảm bảo ứng dụng chạy được trên hơn 90% thiết bị Android đang lưu hành, bao gồm cả các thiết bị giá rẻ phổ biến ở Việt Nam.

---

## 3.2. KIẾN TRÚC HỆ THỐNG

### 3.2.1. Tổng Quan Kiến Trúc

Kiến trúc hệ thống là nền tảng quyết định khả năng hoạt động, bảo trì và mở rộng của toàn bộ giải pháp. Hệ thống nhận diện tôm được thiết kế theo mô hình **Client-Server** phân tầng, một kiến trúc phổ biến và hiệu quả cho các ứng dụng IoT và AI. Mô hình này tách biệt rõ ràng giữa giao diện người dùng, logic xử lý và lưu trữ dữ liệu, giúp dễ dàng phát triển, kiểm thử và triển khai từng thành phần độc lập.

Hệ thống được chia thành 3 tầng chính, mỗi tầng đảm nhiệm một vai trò cụ thể và giao tiếp với nhau qua các giao thức chuẩn. **Tầng Presentation (Client)** là ứng dụng Android được phát triển bằng Kotlin và Jetpack Compose, chạy trên thiết bị di động của người dùng. Đây là tầng giao tiếp trực tiếp với người dùng, nhận input, hiển thị thông tin và cung cấp trải nghiệm tương tác. Trong file `MainActivity.kt`, ứng dụng sử dụng Jetpack Navigation để quản lý các màn hình: Login, Camera Stream, Gallery, Chart, Profile và Admin. Jetpack Compose được chọn vì là framework UI hiện đại của Android, cho phép xây dựng giao diện declarative với code ngắn gọn và performance tốt.

**Tầng Application (Server)** là backend xử lý chạy trên Raspberry Pi, được xây dựng bằng Flask framework của Python. Tầng này đảm nhiệm các nhiệm vụ nặng như xác thực người dùng, streaming camera, nhận diện AI, và quản lý dữ liệu. Trong file `app_complete.py`, Flask server expose ra các RESTful API endpoints để client gọi đến. Server được cấu hình chạy ở chế độ production với "app.run(host='0.0.0.0', port=8000)" để có thể nhận request từ mạng LAN hoặc qua Ngrok tunnel từ Internet. Backend được tối ưu hóa để chạy trên Raspberry Pi 4 với 4GB RAM, sử dụng TensorFlow Lite thay vì TensorFlow đầy đủ để giảm yêu cầu tài nguyên.

**Tầng Data** bao gồm ba thành phần lưu trữ khác nhau, mỗi thành phần phục vụ một mục đích riêng. Camera hardware (USB hoặc CSI camera) kết nối trực tiếp với Raspberry Pi, cung cấp nguồn video thô để streaming và chụp ảnh. Cloudinary là dịch vụ cloud storage chuyên dụng cho ảnh và video, được sử dụng để lưu trữ tất cả các ảnh đã nhận diện. Trong backend, sau khi AI xử lý xong, ảnh được upload lên Cloudinary: "result = cloudinary.uploader.upload(image_bytes, folder='shrimp_detection')" và nhận về một URL public có thể truy cập từ bất kỳ đâu. Cloudinary được chọn vì cung cấp CDN toàn cầu, tự động tối ưu hóa ảnh, và có free tier hào phóng phù hợp cho project này. MongoDB Atlas là cơ sở dữ liệu NoSQL trên cloud, lưu trữ metadata của các ảnh như thời gian chụp, số lượng tôm phát hiện, tọa độ bounding boxes, và thông tin người dùng. MongoDB được chọn vì schema linh hoạt (không cần định nghĩa cứng nhắc), query mạnh mẽ, và dễ dàng scale horizontal khi dữ liệu tăng lên.

**Ngrok - Giải Pháp Truy Cập Từ Xa**

Một thành phần quan trọng trong kiến trúc triển khai là **Ngrok**, một công cụ tạo đường hầm (tunnel) cho phép truy cập máy chủ từ Internet mà không cần địa chỉ IP công khai hay cấu hình bộ định tuyến phức tạp. Sau khi phân tích toàn bộ mã nguồn của hệ thống, Ngrok đóng vai trò then chốt trong việc kết nối ứng dụng Android với máy chủ Raspberry Pi qua Internet.

**Vấn Đề Cần Giải Quyết:**

Trong điều kiện thông thường, Raspberry Pi chỉ có địa chỉ IP nội bộ (192.168.x.x) trong mạng cục bộ gia đình. Ứng dụng Android chỉ có thể kết nối khi cùng WiFi với Raspberry Pi. Khi người dùng rời khỏi nhà và sử dụng mạng di động 4G/5G, không thể truy cập camera hay điều khiển hệ thống. Các giải pháp truyền thống:
- **Thuê Máy Chủ Đám Mây (AWS/GCP)**: Chi phí cao (120-1.200 ngàn/tháng), phức tạp, phải chuyển toàn bộ mã nguồn
- **Chuyển Tiếp Cổng (Port Forwarding)**: Cần quyền quản trị bộ định tuyến, có rủi ro bảo mật, khó xử lý lỗi
- **Mạng Riêng Ảo (VPN)**: Cấu hình phức tạp, tốn tài nguyên, không thân thiện với người dùng
- **DNS Động (Dynamic DNS)**: Vẫn cần chuyển tiếp cổng, không có mã hóa HTTPS tự động

Ngrok giải quyết tất cả vấn đề trên mà không cần cấu hình và hoàn toàn miễn phí.

**Cơ Chế Hoạt Động Của Ngrok Trong Hệ Thống:**

Khi khởi động hệ thống, tập lệnh `start_server.sh` thực hiện các bước sau:

1. **Khởi động Máy Chủ Flask**: Lệnh `python3 app_complete.py &` chạy máy chủ Flask ở chế độ nền trên `localhost:8000`. Máy chủ chỉ lắng nghe trên giao diện cục bộ, không thể truy cập từ Internet.

2. **Khởi động Đường Hầm Ngrok**: Lệnh `ngrok http 8000` kết nối đến hạ tầng đám mây Ngrok và tạo đường hầm bảo mật. Ngrok đám mây cấp một địa chỉ công khai dạng `https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev` (tên miền phụ ngẫu nhiên).

3. **Duy Trì Đường Hầm**: Chương trình Ngrok trên Raspberry Pi duy trì kết nối liên tục với máy chủ đám mây Ngrok. Mọi yêu cầu gửi đến địa chỉ công khai sẽ được chuyển tiếp theo thời gian thực qua đường hầm này về `localhost:8000`.

**Sơ Đồ Kiến Trúc Với Ngrok:**

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INTERNET                                    │
│                                                                     │
│   ┌──────────────────────────────────────────────────────────┐    │
│   │   🌍 Ứng Dụng Android (Điện Thoại Người Dùng)           │    │
│   │   • Bất kỳ đâu: WiFi nhà, 4G, 5G, WiFi quán cà phê      │    │
│   │   • File Config.kt: BACKEND_URL                          │    │
│   │     = "https://unstrengthening-...ngrok-free.dev"        │    │
│   └──────────────────────────────────────────────────────────┘    │
│                           │                                        │
│                           │ Yêu cầu HTTPS                          │
│                           ▼                                        │
│   ┌──────────────────────────────────────────────────────────┐    │
│   │   ☁️  Máy Chủ Đám Mây Ngrok                              │    │
│   │   • Địa chỉ công khai: https://xxx.ngrok-free.dev        │    │
│   │   • Xử lý mã hóa SSL/TLS (HTTPS → HTTP)                  │    │
│   │   • Cân bằng tải & Bảo vệ DDoS                           │    │
│   │   • Định tuyến yêu cầu đến đường hầm                     │    │
│   └──────────────────────────────────────────────────────────┘    │
│                           │                                        │
└───────────────────────────┼────────────────────────────────────────┘
                            │ Đường Hầm WebSocket Bảo Mật
                            │ (Mã Hóa Đầu Cuối)
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    MẠNG CỤC BỘ GIA ĐÌNH (192.168.x.x)               │
│                                                                     │
│   ┌──────────────────────────────────────────────────────────┐    │
│   │   🖥️  Raspberry Pi 4B                                     │    │
│   │   • Địa chỉ IP nội bộ: 192.168.1.100 (ví dụ)            │    │
│   │   • Không có IP công khai                                │    │
│   │   • Đằng sau bộ định tuyến/tường lửa                     │    │
│   │                                                           │    │
│   │   ┌─────────────────────────────────────────────┐        │    │
│   │   │  📡 Chương Trình Ngrok (ngrok http 8000)    │        │    │
│   │   │  • Khởi động bởi start_server.sh            │        │    │
│   │   │  • Duy trì kết nối đường hầm liên tục       │        │    │
│   │   │  • Chuyển tiếp yêu cầu đến localhost:8000   │        │    │
│   │   └─────────────────────────────────────────────┘        │    │
│   │                           │                               │    │
│   │                           │ Chuyển tiếp cục bộ            │    │
│   │                           ▼                               │    │
│   │   ┌─────────────────────────────────────────────┐        │    │
│   │   │  🐍 Máy Chủ Flask (app_complete.py)         │        │    │
│   │   │  • Lắng nghe: localhost:8000                 │        │    │
│   │   │  • Các điểm truy cập:                        │        │    │
│   │   │    - /blynk_feed (truyền hình camera)        │        │    │
│   │   │    - /api/detect-shrimp (nhận diện AI)       │        │    │
│   │   │    - /api/shrimp-images (thư viện ảnh)       │        │    │
│   │   │    - /api/admin/* (quản trị hệ thống)        │        │    │
│   │   │    - /api/motor/* (điều khiển động cơ)       │        │    │
│   │   └─────────────────────────────────────────────┘        │    │
│   │            │              │              │                │    │
│   │            ▼              ▼              ▼                │    │
│   │      ┌─────────┐    ┌─────────┐    ┌─────────┐          │    │
│   │      │ Camera  │    │ Mô Hình │    │  Chân   │          │    │
│   │      │ USB/CSI │    │   AI    │    │  GPIO   │          │    │
│   │      └─────────┘    └─────────┘    └─────────┘          │    │
│   └──────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

**Cách Thức Hoạt Động: Truyền và Nhận Dữ Liệu Qua Ngrok**

Dựa trên sơ đồ kiến trúc trên, quy trình truyền nhận dữ liệu qua Ngrok được tóm tắt như sau:

**1. Giai Đoạn Khởi Tạo Đường Hầm:**

Khi chạy lệnh `ngrok http 8000`, chương trình Ngrok trên Raspberry Pi thực hiện các bước sau:

**Bước 1: Mở Kết Nối WebSocket Secure (WSS)**
- Ngrok Agent chủ động mở kết nối WebSocket Secure (WSS) ra ngoài Internet đến máy chủ đám mây Ngrok
- Kết nối này đi từ **trong ra ngoài** (outbound), giống như khi bạn lướt web bình thường
- Vì là kết nối outbound, bộ định tuyến/tường lửa tự động cho phép mà không cần cấu hình gì
- WebSocket là giao thức cho phép giao tiếp hai chiều liên tục qua một kết nối duy nhất

**Bước 2: Nhận Địa Chỉ Công Khai**
- Máy chủ đám mây Ngrok cấp một địa chỉ công khai ngẫu nhiên
- Ví dụ: `https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev`
- Địa chỉ này có thể truy cập từ bất kỳ đâu trên Internet
- Mỗi lần khởi động lại Ngrok, địa chỉ này sẽ thay đổi (trừ khi dùng gói trả phí)

**Bước 3: Duy Trì Kết Nối Liên Tục**
- Kết nối WebSocket được duy trì liên tục (persistent connection)
- Không đóng cho đến khi tắt máy chủ hoặc dừng Ngrok (Ctrl+C)
- Ngrok định kỳ gửi "heartbeat" (nhịp tim) để giữ kết nối sống
- Nếu mất kết nối (mất mạng), Ngrok tự động kết nối lại

**2. Luồng Yêu Cầu (Từ Ứng Dụng → Máy Chủ):**

**Bước 1: Ứng dụng Android gửi yêu cầu HTTPS**
```
Ứng dụng Android → POST https://xxx.ngrok-free.dev/api/detect-shrimp
Tiêu đề: Authorization: <Mã-Xác-Thực-Firebase>
Nội dung: { "image": "<ảnh-mã-hóa-base64>", "source": "camera_stream" }
```
- Ứng dụng gửi yêu cầu đến địa chỉ công khai của Ngrok (không biết đằng sau là Raspberry Pi)
- Yêu cầu được mã hóa bằng HTTPS/TLS 1.3 (bảo mật trên đường truyền Internet)

**Bước 2: Máy chủ đám mây Ngrok nhận yêu cầu**
- Giải mã lớp TLS (kết thúc mã hóa HTTPS ở đây)
- Xác định yêu cầu này thuộc về đường hầm nào (dựa vào tên miền phụ `xxx.ngrok-free.dev`)
- Tìm kết nối WebSocket tương ứng với Raspberry Pi của bạn

**Bước 3: Máy chủ đám mây Ngrok chuyển tiếp qua đường hầm bảo mật**
- Chuyển đổi yêu cầu HTTP thành dạng tin nhắn nhị phân (serialize)
- Gửi qua WebSocket (được mã hóa lại bằng TLS) đến chương trình Ngrok trên Raspberry Pi
- WebSocket cho phép giao tiếp hai chiều (full-duplex), có thể gửi/nhận đồng thời

**Bước 4: Chương trình Ngrok trên Raspberry Pi nhận và xử lý**
- Nhận tin nhắn từ WebSocket, chuyển đổi ngược thành yêu cầu HTTP (deserialize)
- Chuyển tiếp đến `localhost:8000/api/detect-shrimp` (kết nối cục bộ trong cùng máy Raspberry Pi)
- Máy chủ Flask nhận yêu cầu như một máy chủ web bình thường, không biết gì về Ngrok

**3. Luồng Phản Hồi (Từ Máy Chủ → Ứng Dụng):**

**Bước 1: Máy chủ Flask xử lý và trả kết quả**
```python
# File app_complete.py xử lý nhận diện tôm
return jsonify({
    "success": True,
    "imageUrl": "https://cloudinary.com/...",
    "detections": [...],
    "detectionCount": 5
})
```
- Máy chủ Flask nhận diện tôm, vẽ khung bao, tải ảnh lên Cloudinary, lưu MongoDB
- Trả về kết quả dạng JSON cho `localhost:8000`

**Bước 2: Chương trình Ngrok nhận phản hồi HTTP từ localhost**
- Chuyển đổi phản hồi HTTP thành tin nhắn (serialize)
- Gửi ngược về máy chủ đám mây Ngrok qua cùng kết nối WebSocket (vẫn mở sẵn)

**Bước 3: Máy chủ đám mây Ngrok chuyển tiếp về ứng dụng**
- Chuyển đổi tin nhắn ngược thành phản hồi HTTP (deserialize)
- Mã hóa lại bằng TLS
- Gửi về ứng dụng Android qua kết nối HTTPS ban đầu (vẫn đang mở và chờ)

**Bước 4: Ứng dụng Android nhận kết quả**
```kotlin
// File ShrimpApiService.kt
val result = json.decodeFromString<YoloProcessResponse>(responseBody)
// Hiển thị ảnh nhận diện và số lượng tôm trên giao diện
```
- Ứng dụng nhận JSON, giải mã và hiển thị kết quả cho người dùng

**4. Trường Hợp Đặc Biệt: Truyền Hình Camera**

Với truyền hình camera, luồng dữ liệu là dòng liên tục (continuous stream) thay vì hỏi-đáp một lần:

**Bước 1: Ứng dụng Android mở kết nối liên tục**
```kotlin
// File CameraStreamScreen.kt
client.newCall(request).execute() // Mở kết nối và giữ mở, không đóng
```
- Ứng dụng gửi yêu cầu GET đến `/blynk_feed` và giữ kết nối mở
- Không có timeout, kết nối được duy trì cho đến khi người dùng rời khỏi màn hình camera

**Bước 2: Máy chủ Flask tạo khung hình liên tục**
```python
def generate_frames():
    while True:  # Vòng lặp vô hạn
        ret, frame = camera.read()  # Chụp 1 khung hình từ camera USB
        jpeg = cv2.imencode('.jpg', frame, quality=80)  # Nén thành JPEG chất lượng 80%
        yield '--frame\r\n' + jpeg.tobytes() + '\r\n'  # Gửi đi
        # Lặp lại 30 lần mỗi giây (30 FPS)
```
**Chi tiết kỹ thuật:**
- Camera USB chụp liên tục với tốc độ 30 FPS (30 khung hình/giây)
- Mỗi khung hình có kích thước 640×480 pixels (chiều rộng × chiều cao)
- Khung hình được nén thành định dạng JPEG với chất lượng 80% để giảm dung lượng
- Mỗi ảnh JPEG có kích thước khoảng 20-50 KB (kiloByte)
- Ảnh được gói trong định dạng MJPEG (Motion JPEG) - chuẩn để truyền video qua HTTP

**Bước 3: Truyền qua đường hầm Ngrok**
```
Camera → Khung hình 1 (30KB)
   ↓
Nén JPEG chất lượng 80%
   ↓
Đóng gói MJPEG:
   "--frame\r\n"
   "Content-Type: image/jpeg\r\n"
   <dữ liệu ảnh JPEG 30KB>
   "\r\n"
   ↓
Chương trình Ngrok nhận gói dữ liệu
   ↓
Chuyển đổi thành tin nhắn qua đường hầm WebSocket (mã hóa TLS)
   ↓
Gửi qua Internet đến máy chủ đám mây Ngrok
   ↓
Máy chủ đám mây Ngrok chuyển tiếp đến ứng dụng Android
```
**Tốc độ truyền:**
- Mỗi giây truyền 30 khung hình
- Mỗi khung ~30KB
- Tổng: 30 × 30KB = 900KB/giây ≈ 7.2 Mbps (MegaBit per second)
- Băng thông WiFi nhà (~50 Mbps) đủ xử lý thoải mái

**Bước 4: Ứng dụng Android giải mã và hiển thị**
```kotlin
while (isActive) {  // Lặp liên tục
    inputStream.read(buffer)  // Đọc dữ liệu từ stream
    
    // Tìm điểm bắt đầu ảnh JPEG (mã 0xFFD8)
    if (tìm thấy 0xFFD8) {
        bắt đầu thu thập dữ liệu ảnh
    }
    
    // Tìm điểm kết thúc ảnh JPEG (mã 0xFFD9)  

**5. Bảo Mật: Mã Hóa 2 Lớp**

Toàn bộ quá trình truyền nhận đều được mã hóa:

**Layer 1:** Android App ↔ Ngrok Cloud = **HTTPS/TLS 1.3**
- Certificate của Ngrok cho domain *.ngrok-free.dev
- Bảo vệ dữ liệu khi đi qua Internet công cộng

**Layer 2:** Ngrok Cloud ↔ Ngrok Agent = **WebSocket Secure (WSS/TLS)**
- Tunnel connection được mã hóa
- Chỉ Ngrok Cloud và Agent mới decrypt được

**Kết quả:** End-to-End encryption cho camera stream, authentication tokens, detection images, motor control commands.

**6. Ưu Điểm Của Cơ Chế Này:**

- **Zero Configuration**: Không cần public IP, không config router, không mở port
- **Bypass Firewall**: WebSocket outbound luôn được phép, không bị chặn
- **Single Connection Reuse**: 1 WebSocket tunnel xử lý hàng nghìn HTTP requests
- **Transparent**: Code backend/frontend không cần biết về Ngrok, chỉ cần URL
- **Automatic HTTPS**: SSL certificate miễn phí từ Ngrok
- **Persistent & Low Latency**: Connection liên tục, không tốn overhead reconnect

**7. Hạn Chế:**

- **Random URL**: Free tier cấp URL ngẫu nhiên mỗi restart → phải update Config.kt
- **Latency Overhead**: Thêm ~50-150ms so với direct connection
- **Single Tunnel Limit**: Free tier chỉ 1 tunnel đồng thời
- **Dependency**: Phụ thuộc Ngrok cloud service (nếu Ngrok down, hệ thống không accessible)

**Luồng Dữ Liệu Chi Tiết Theo Use Case:**

**1. API Request Flow (ví dụ: Detection):**
```
Android App (ShrimpApiService.kt)
    │ processImage(bitmap)
    │ Request: POST https://xxx.ngrok-free.dev/api/detect-shrimp
    │ Headers: Authorization: <Firebase-Token>
    │ Body: { "image": "<base64>", "source": "camera_stream" }
    ▼
Ngrok Cloud
    │ Receive HTTPS request
    │ Decrypt TLS
    │ Route to tunnel for device "xxx.ngrok-free.dev"
    ▼
Ngrok Client (Raspberry Pi)
    │ Receive via WebSocket tunnel
    │ Forward to localhost:8000/api/detect-shrimp
    ▼
Flask Backend (app_complete.py)
    │ @app.route('/api/detect-shrimp')
    │ @requires_google_auth (verify Firebase token)
    │ Decode base64 image → PIL Image
    │ Preprocess: resize 320x320, normalize
    │ TFLite inference: interpreter.invoke()
    │ Post-process: filter confidence > 0.5
    │ Draw bounding boxes với OpenCV
    │ Upload to Cloudinary: cloudinary.uploader.upload()
    │ Save to MongoDB: collection.insert_one()
    │ Return: { "success": true, "imageUrl": "...", "detections": [...] }
    ▼
Ngrok Client
    │ Forward response back through tunnel
    ▼
Ngrok Cloud
    │ Encrypt with TLS
    ▼
Android App
    │ Result.success(YoloProcessResponse)
    │ Display image + detection count in UI
```

**2. Camera Streaming Flow:**
```
Android App (CameraStreamScreen.kt)
    │ LaunchedEffect { streamUrl }
    │ OkHttpClient.newCall(Request.url(streamUrl))
    │ Request: GET https://xxx.ngrok-free.dev/blynk_feed
    │ Persistent connection (no timeout)
    ▼
Ngrok Cloud → Ngrok Client → Flask Backend
    │ @app.route('/blynk_feed')
    │ @requires_google_auth
    │ Return: Response(generate_frames(), mimetype='multipart/x-mixed-replace')
    │
    │ generate_frames():
    │   while True:
    │       ret, frame = camera.read()  # OpenCV capture
    │       jpeg = cv2.imencode('.jpg', frame, quality=80)
    │       yield '--frame' + jpeg.tobytes()
    ▼
Ngrok Client → Ngrok Cloud
    │ Stream MJPEG data through tunnel
    │ Continuous flow: 30 FPS
    ▼
Android App
    │ InputStream.read(buffer)
    │ Find JPEG markers: 0xFFD8 (start), 0xFFD9 (end)
    │ BitmapFactory.decodeByteArray(frameData)
    │ currentFrame = bitmap (trigger Compose recomposition)
    │ Display in Image composable
```

**3. Motor Control Flow:**
```
Android App (MotorControlScreen.kt)
    │ onMotorStart("DONG_CO_1")
    │ Request: POST https://xxx.ngrok-free.dev/api/motor/DONG_CO_1/on
    ▼
Ngrok → Flask Backend
    │ @app.route('/api/motor/<motor_name>/on')
    │ @requires_google_auth
    │ GPIO.output(MOTOR_PINS[motor_name], GPIO.HIGH)
    │ Return: { "success": true, "motor": "DONG_CO_1", "state": "on" }
    ▼
Android App
    │ Update UI: motorStates["DONG_CO_1"] = true
```

**Phân Tích Code Sử Dụng Ngrok:**

**Trong Android App:**

File `Config.kt`:
```kotlin
object Config {
    // ⚠️ UPDATE URL này khi Ngrok restart
    const val BACKEND_URL = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev"
    const val STREAM_URL = "$BACKEND_URL/blynk_feed"
}
```
Comment "UPDATE URL này khi Ngrok restart" chỉ ra hạn chế chính: Ngrok free tier cấp random URL mỗi lần restart.

File `ShrimpApiService.kt`:
```kotlin
private val BACKEND_URL = Config.BACKEND_URL

suspend fun processImage(bitmap: Bitmap, sourceUrl: String): Result<YoloProcessResponse> {
    val request = Request.Builder()
        .url("$BACKEND_URL/api/detect-shrimp")
        .post(jsonBody.toRequestBody("application/json".toMediaType()))
        .build()
}
```
Tất cả API requests đều sử dụng `BACKEND_URL` từ Config, pointing đến Ngrok URL.

File `NavGraph.kt`:
```kotlin
composable<MotorControl> {
    MotorControlScreen(
        baseUrl = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev",
        ...
    )
}
```
Motor control screen cũng hardcode Ngrok URL (có thể refactor để dùng Config).

**Trong Backend:**

File `start_server.sh`:
```bash
# Khởi động Flask server ở background
python3 app_complete.py &
FLASK_PID=$!
sleep 3  # Đợi Flask khởi động

# Khởi động ngrok
echo "🌐 Starting ngrok tunnel..."
ngrok http 8000

# Khi ngrok tắt, tắt Flask server
kill $FLASK_PID
```
Script đảm bảo Flask chạy trước, sau đó mới start Ngrok. Khi user tắt Ngrok (Ctrl+C), script cleanup Flask server.

File `app_complete.py`:
```python
@app.route('/blynk_feed')
@requires_google_auth
def blynk_feed():
    response = Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')
    response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
    response.headers['X-Accel-Buffering'] = 'no'  # Tắt buffering của nginx (nếu có)
    return response
```
Máy chủ không biết gì về Ngrok. Nó chỉ lắng nghe trên localhost:8000. Ngrok xử lý tất cả việc công khai ra Internet.

**Tại Sao Ngrok Hoạt Động Tốt Cho Hệ Thống Này:**

1. **Truyền Hình Camera Thời Gian Thực**: Đường hầm Ngrok có băng thông đủ cho truyền hình MJPEG 640x480@30 khung/giây (~3-5 Mbps). Độ trễ tăng ~100-200ms so với mạng cục bộ nhưng vẫn chấp nhận được cho giám sát.

2. **Nhận Diện AI**: Mô hình yêu cầu-phản hồi của API nhận diện hoạt động hoàn hảo qua đường hầm HTTP. Tải ảnh ~200KB mất 1-2 giây trên mạng 4G.

3. **Điều Khiển Động Cơ**: Điều khiển GPIO qua API có độ trễ ~200-500ms qua Ngrok, chấp nhận được cho điều khiển không yêu cầu thời gian thực cao.

4. **Xác Thực**: Xác minh mã Firebase vẫn hoạt động bình thường. Máy chủ xác minh mã với máy chủ Firebase (không bị ảnh hưởng bởi Ngrok).

5. **HTTPS Miễn Phí**: Ngrok cung cấp chứng chỉ SSL, mã hóa toàn bộ dữ liệu truyền qua Internet (truyền hình camera, thông tin đăng nhập, kết quả nhận diện).

**Ưu Điểm:**
- **Không Cần Cấu Hình**: Không cần IP công khai, không cần cấu hình NAT/chuyển tiếp cổng trên bộ định tuyến
- **Triển Khai Tức Thì**: Chỉ cần `ngrok http 8000`, máy chủ truy cập được ngay từ Internet
- **HTTPS Tự Động**: Chứng chỉ TLS miễn phí, đảm bảo bảo mật đầu cuối
- **Đa Nền Tảng**: Chạy trên Raspberry Pi (Linux), Windows, macOS
- **Gói Miễn Phí Hào Phóng**: 40 kết nối/phút, băng thông không giới hạn (với điều chỉnh tốc độ), đủ cho 1-5 người dùng đồng thời
- **Thân Thiện Phát Triển**: Bảng điều khiển Ngrok hiển thị nhật ký yêu cầu, phát lại yêu cầu, kiểm tra lưu lượng

**Hạn Chế:**
- **Địa Chỉ Ngẫu Nhiên**: Gói miễn phí cấp tên miền phụ ngẫu nhiên mỗi lần khởi động lại. Phải cập nhật `Config.kt` và biên dịch lại ứng dụng.
  - *Giải pháp*: Gói trả phí Ngrok (200 ngàn/tháng) có tên miền tùy chỉnh: `ung-dung-cua-ban.ngrok.io`
- **Chỉ 1 Đường Hầm**: Gói miễn phí chỉ 1 đường hầm đồng thời. Không thể mở nhiều cổng (ví dụ Flask 8000 + Dịch vụ Email 5001).
  - *Giải pháp tạm thời*: Mã nguồn hiện tại chạy 2 dịch vụ nhưng chỉ mở cổng 8000 qua Ngrok.
- **Phụ Thuộc Bên Ngoài**: Phụ thuộc vào đám mây Ngrok. Nếu Ngrok gặp sự cố (hiếm), hệ thống không truy cập được từ Internet.
- **Hiệu Năng**: Thêm 1 điểm trung gian (đám mây Ngrok), tăng độ trễ ~50-200ms tùy vị trí người dùng.

**So Sánh Với Các Phương Án Khác:**

| Giải Pháp | Chi Phí | Cài Đặt | Độ Trễ | HTTPS | Phù Hợp |
|-----------|---------|---------|--------|-------|---------|
| Ngrok | Miễn phí/200k | 1 lệnh | +100ms | ✅ Tự động | ✅ Tốt nhất cho phát triển/mẫu thử |
| Chuyển Tiếp Cổng | Miễn phí | Khó, cần quyền admin bộ định tuyến | 0ms | ❌ Phải tự cài | Chỉ cho người dùng am hiểu kỹ thuật |
| Máy Chủ AWS EC2 | 120-500k/tháng | Khó, phải chuyển mã nguồn | Thay đổi | ✅ Thủ công | Quy mô lớn cho sản xuất |
| Cloudflare Tunnel | Miễn phí | Trung bình | +50ms | ✅ Tự động | Thay thế tốt cho sản xuất |
| VPN (Tailscale) | Miễn phí/120k | Dễ | +30ms | ✅ Tự động | Tốt cho truy cập riêng tư |

**Kết Luận:**

Ngrok là xương sống của khả năng truy cập từ xa trong hệ thống. Nó cho phép triển khai nhanh chóng các ứng dụng IoT/AI mẫu thử mà không cần hạ tầng phức tạp. Đối với sản xuất, có thể chuyển sang Cloudflare Tunnel (miễn phí, địa chỉ ổn định) hoặc triển khai máy chủ lên đám mây (AWS Lambda + API Gateway, Google Cloud Run). Tuy nhiên, với trường hợp sử dụng hiện tại (1-5 người dùng, giám sát tại nhà), gói miễn phí Ngrok là đủ và tiết kiệm chi phí.

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  ┌──────────────────────────────────────────────────────┐       │
│  │          📱 Android Application                      │       │
│  │              (Kotlin + Jetpack Compose)              │       │
│  │                                                      │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │       │
│  │  │   Login      │  │   Camera     │  │  Gallery  │ │       │
│  │  │   Screen     │  │   Stream     │  │  Screen   │ │       │
│  │  └──────────────┘  └──────────────┘  └───────────┘ │       │
│  │  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │       │
│  │  │   Profile    │  │   Chart      │  │   Admin   │ │       │
│  │  │   Screen     │  │   Screen     │  │   Panel   │ │       │
│  │  └──────────────┘  └──────────────┘  └───────────┘ │       │
│  └──────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
                              │
                    HTTP/HTTPS + WebSocket
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                          │
│  ┌──────────────────────────────────────────────────────┐       │
│  │          🐍 Flask Backend Server                     │       │
│  │            (Python 3.8+ / Raspberry Pi)              │       │
│  │                                                      │       │
│  │  ┌─────────────────────────────────────────────┐    │       │
│  │  │        Authentication Module                │    │       │
│  │  │  • Firebase Admin SDK                       │    │       │
│  │  │  • Token Verification                       │    │       │
│  │  │  • Permission Management                    │    │       │
│  │  └─────────────────────────────────────────────┘    │       │
│  │  ┌─────────────────────────────────────────────┐    │       │
│  │  │        Camera Streaming Module              │    │       │
│  │  │  • OpenCV (cv2)                             │    │       │
│  │  │  • MJPEG Stream Handler                     │    │       │
│  │  │  • Frame Buffer Management                  │    │       │
│  │  └─────────────────────────────────────────────┘    │       │
│  │  ┌─────────────────────────────────────────────┐    │       │
│  │  │        AI Detection Module                  │    │       │
│  │  │  • TensorFlow Lite Interpreter              │    │       │
│  │  │  • YOLO Model (best-fp16.tflite)            │    │       │
│  │  │  • Image Preprocessing                      │    │       │
│  │  │  • Post-processing & Annotation             │    │       │
│  │  └─────────────────────────────────────────────┘    │       │
│  │  ┌─────────────────────────────────────────────┐    │       │
│  │  │        Storage & Database Module            │    │       │
│  │  │  • Cloudinary Integration                   │    │       │
│  │  │  • MongoDB Operations                       │    │       │
│  │  └─────────────────────────────────────────────┘    │       │
│  └──────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                              │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Camera     │  │  Cloudinary  │  │   MongoDB    │         │
│  │  (Hardware)  │  │   (Cloud)    │  │   (Cloud)    │         │
│  │              │  │              │  │              │         │
│  │  • USB/CSI   │  │  • Image     │  │  • Metadata  │         │
│  │    Camera    │  │    Storage   │  │  • Detection │         │
│  │  • 640x480   │  │  • CDN       │  │    Results   │         │
│  │  • 30 FPS    │  │              │  │  • User Info │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2.2. Luồng Xử Lý Dữ Liệu Chính

Để hiểu rõ cách thức hoạt động của hệ thống, cần phân tích chi tiết các luồng xử lý dữ liệu từ khi người dùng tương tác đến khi nhận được kết quả. Mỗi luồng xử lý đại diện cho một use case quan trọng và được triển khai với các bước cụ thể, đảm bảo tính nhất quán và bảo mật.

#### 3.2.2.1. Luồng Đăng Nhập và Xác Thực

Luồng xác thực là cơ sở cho mọi tương tác khác trong hệ thống, đảm bảo chỉ người dùng hợp lệ mới có thể truy cập các tài nguyên nhạy cảm như camera và dữ liệu. Quy trình bắt đầu khi người dùng mở ứng dụng và được chuyển đến màn hình đăng nhập nếu chưa có phiên làm việc hợp lệ. Người dùng có thể chọn đăng nhập bằng tài khoản Google hoặc số điện thoại thông qua Firebase Authentication.

Với đăng nhập Google, khi người dùng nhấn nút "Sign in with Google", ứng dụng gọi Firebase Auth API để hiển thị dialog chọn tài khoản Google. Sau khi người dùng chọn tài khoản và đồng ý cấp quyền, Firebase Authentication xác minh thông tin với Google servers và trả về một ID Token có chứa thông tin người dùng được mã hóa. Token này có thời hạn 1 giờ và được lưu trong ứng dụng: "prefs.edit().putString('idToken', token).apply()" trong class `TokenManager`.

Với đăng nhập số điện thoại, Firebase gửi mã OTP qua SMS đến số điện thoại của người dùng. Sau khi người dùng nhập đúng mã xác thực, Firebase cũng trả về một ID Token tương tự. Số điện thoại được lưu trong Firestore và sử dụng để xác thực trong các request tiếp theo thông qua header "X-Phone-Auth".

Khi ứng dụng thực hiện bất kỳ API request nào đến backend, nó phải kèm theo token xác thực trong header. Ví dụ trong `ShrimpApiService.kt`, trước khi gọi API detect-shrimp, hàm `getAuthHeaders()` được gọi để lấy token: "val authHeaders = getAuthHeaders()" và add vào request: "requestBuilder.addHeader(key, value)". Header có thể là "Authorization: <ID-Token>" cho Google auth hoặc "X-Phone-Auth: +84987654321" cho phone auth.

Khi backend nhận được request, decorator `@requires_google_auth` được kích hoạt trước khi vào hàm xử lý chính. Decorator này trích xuất token từ header và gửi đến Firebase Admin SDK để xác minh: "decoded_token = firebase_auth.verify_id_token(id_token)". Firebase Admin SDK kết nối với Firebase servers để kiểm tra chữ ký số, thời gian hết hạn và tính hợp lệ của token. Nếu token hợp lệ, nó trả về thông tin người dùng bao gồm email, user ID và các claims khác.

Sau khi token được xác minh thành công, backend thực hiện bước kiểm tra quyền truy cập thứ hai bằng cách load danh sách người dùng được phép từ file JSON: "permitted_emails = load_permitted_emails()" và "permitted_phones = load_permitted_phones()". Email hoặc số điện thoại từ token được so sánh với danh sách này: "if email not in permitted_emails: return jsonify({'success': False, 'message': 'Email not permitted'}), 403". Cơ chế double-check này (xác thực + phân quyền) tạo thành lớp bảo mật kép, đảm bảo ngay cả khi token bị lộ, kẻ tấn công vẫn không thể truy cập nếu không nằm trong whitelist.

Chỉ khi cả hai bước đều pass, request mới được xử lý và backend trả về response thành công. Nếu bất kỳ bước nào fail, backend trả về HTTP 401 (Unauthorized) cho lỗi xác thực hoặc HTTP 403 (Forbidden) cho lỗi phân quyền, kèm theo message cụ thể. Ứng dụng Android nhận được error code này và hiển thị thông báo phù hợp hoặc redirect về màn hình login.

#### 3.2.2.2. Luồng Camera Streaming

Camera streaming là tính năng realtime quan trọng nhất của hệ thống, cho phép người dùng theo dõi ao nuôi từ xa. Luồng này được thiết kế để đạt được độ trễ thấp và sử dụng băng thông hiệu quả. Khi người dùng mở màn hình Home trong ứng dụng (`CameraStreamScreen`), một coroutine được khởi chạy trong `LaunchedEffect` để bắt đầu nhận stream.

Ứng dụng tạo một HTTP GET request đến endpoint "/blynk_feed" trên backend server với header "User-Agent: Android-Camera-App". Request này không có body và được giữ mở (persistent connection) để nhận dữ liệu liên tục. Trong code: "val request = Request.Builder().url(streamUrl).addHeader('User-Agent', 'Android-Camera-App').build()" và "client.newCall(request).execute()".

Khi backend nhận được request stream, endpoint handler được định nghĩa với generator function trong Flask: "@app.route('/blynk_feed') def blynk_feed(): return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')". Hàm `generate_frames()` chạy trong một vòng lặp vô hạn, liên tục capture frame từ camera.

Backend sử dụng OpenCV để kết nối và đọc dữ liệu từ camera. Trong khởi tạo: "camera = cv2.VideoCapture(i, cv2.CAP_V4L2)" tìm camera khả dụng trên các video device của Linux. Sau khi tìm thấy camera, các tham số được tối ưu hóa: "camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)", "camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)", "camera.set(cv2.CAP_PROP_FPS, 30)" để cân bằng giữa chất lượng và performance. Đặc biệt, "camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)" giảm buffer xuống chỉ 1 frame để tránh tình trạng frames bị queue và gây delay.

Trong vòng lặp streaming, backend đọc frame từ camera: "ret, frame = camera.read()". Nếu đọc thành công (ret == True), frame được encode thành JPEG format: "ret, jpeg = cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 80])". Quality 80 là sweet spot giữa chất lượng ảnh và kích thước file. JPEG data sau đó được wrap trong MJPEG boundary: "yield (b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + jpeg.tobytes() + b'\r\n')". Mỗi frame được gửi như một phần của multipart response, và boundary "frame" giúp client phân biệt các frame riêng lẻ.

Ở phía client Android, InputStream từ response được đọc liên tục trong một vòng lặp: "while (isActive) { val read = inputStream.read(buffer, bytesRead, buffer.size - bytesRead) }". Ứng dụng tìm kiếm JPEG markers trong buffer: start marker "0xFF 0xD8" đánh dấu đầu ảnh và end marker "0xFF 0xD9" đánh dấu cuối ảnh. Khi tìm thấy một frame hoàn chỉnh, data được extract và decode thành Bitmap: "val bitmap = BitmapFactory.decodeByteArray(frameData, 0, frameLength)". Bitmap này được update vào state: "currentFrame = bitmap", trigger Compose recomposition và hiển thị frame mới lên màn hình.

Quá trình này lặp lại liên tục với tốc độ 30 FPS, tạo ra video stream mượt mà. Một frame mới được capture, encode, transmit, decode và display trong vòng 30-50 milliseconds, đảm bảo độ trễ tổng thể dưới 1 giây. Khi người dùng rời khỏi màn hình camera hoặc đóng app, coroutine bị cancel, connection được đóng gracefully, và backend dừng streaming để tiết kiệm tài nguyên.

#### 3.2.2.3. Luồng Chụp Ảnh và Nhận Diện

Luồng nhận diện tôm là trung tâm của giá trị mà hệ thống mang lại, tự động hóa việc đếm và theo dõi tôm thay cho quan sát thủ công. Luồng này kết hợp nhiều công nghệ: image processing, deep learning, cloud storage và database.

Khi người dùng đang xem camera stream và nhấn nút capture (biểu tượng máy ảnh), ứng dụng capture frame hiện tại từ state: "val bitmap = currentFrame ?: return". Bitmap này chính là ảnh từ camera stream tại thời điểm nhấn nút. Để gửi qua network, bitmap được chuyển đổi sang định dạng Base64 trong hàm `bitmapToBase64()`: "val outputStream = ByteArrayOutputStream()", "bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)", "val bytes = outputStream.toByteArray()", "Base64.encodeToString(bytes, Base64.NO_WRAP)". Base64 là text encoding cho phép gửi binary data qua JSON.

Ứng dụng tạo JSON payload với ảnh Base64 và thông tin nguồn: "val jsonBody = """{"image": "$base64Image", "source": "camera_stream"}"""" và gửi POST request đến "/api/detect-shrimp" kèm auth headers. Trong khi chờ response, UI hiển thị loading indicator: "isProcessing.value = true" và "processingMessage.value = 'Đang nhận diện...'".

Backend nhận request và bắt đầu quá trình xử lý AI. Đầu tiên, ảnh Base64 được decode: "image_data = request.json['image']", "image_bytes = base64.b64decode(image_data)", "image = Image.open(BytesIO(image_bytes))". PIL Image sau đó được convert sang NumPy array: "image_np = np.array(image)" để xử lý với OpenCV và TensorFlow.

Ảnh gốc thường có kích thước 640x480 nhưng model YOLO được train với input 320x320, do đó cần resize: "image_resized = cv2.resize(image_np, (INPUT_WIDTH, INPUT_HEIGHT))". OpenCV sử dụng color format BGR còn model cần RGB, nên phải convert: "image_rgb = cv2.cvtColor(image_resized, cv2.COLOR_BGR2RGB)". Pixel values được normalize từ [0, 255] về [0, 1]: "image_normalized = image_rgb.astype(np.float32) / 255.0". Cuối cùng, thêm batch dimension: "input_data = np.expand_dims(image_normalized, axis=0)" vì model expect input shape [1, 320, 320, 3].

TensorFlow Lite interpreter được load model một lần khi server khởi động: "interpreter = Interpreter(model_path=MODEL_PATH)", "interpreter.allocate_tensors()". Để thực hiện inference, input tensor được set: "interpreter.set_tensor(input_details[0]['index'], input_data)" và invoke: "interpreter.invoke()". Model inference mất khoảng 1-2 giây trên Raspberry Pi 4.

Output từ model bao gồm 4 tensor: boxes chứa tọa độ bounding box [y1, x1, y2, x2] normalized về [0, 1], classes chứa class ID của đối tượng, scores chứa confidence score [0, 1], và num_detections cho biết có bao nhiêu detection hợp lệ. Code trích xuất: "boxes = interpreter.get_tensor(output_details[0]['index'])[0]", "scores = interpreter.get_tensor(output_details[2]['index'])[0]".

Post-processing lọc bỏ các detection có score thấp: "if score < confidence_threshold: continue" với threshold mặc định 0.5. Tọa độ normalized được convert về pixel coordinates: "x1 = int(bbox['x'] * width)", "y1 = int(bbox['y'] * height)". Kết quả là một list các detection, mỗi cái có className, confidence và bbox.

Backend vẽ visualization lên ảnh gốc bằng OpenCV: "cv2.rectangle(annotated, (x1, y1), (x2, y2), color=(0, 255, 0), thickness=2)" vẽ hình chữ nhật màu xanh lá xung quanh mỗi con tôm, và "cv2.putText(annotated, label, (x1, y1-10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)" thêm text label với confidence score.

Ảnh đã annotate được upload lên Cloudinary: "result = cloudinary.uploader.upload(image_bytes, folder='shrimp_detection', resource_type='image')". Cloudinary xử lý upload, store file trên server distributed, và trả về URL: "cloudinary_url = result['secure_url']". URL này là permanent và có thể truy cập công khai, giúp ứng dụng mobile load ảnh nhanh chóng từ CDN gần nhất.

Metadata được lưu vào MongoDB collection 'detections': "document = {'imageUrl': cloudinary_url, 'detections': filtered_detections, 'timestamp': int(time.time() * 1000), 'capturedFrom': user_email, 'detectionCount': len(filtered_detections)}". MongoDB insert trả về ID: "mongo_id = collection.insert_one(document).inserted_id" để có thể reference document sau này.

Backend tổng hợp tất cả thông tin và trả về JSON response: "return jsonify({'success': True, 'imageUrl': cloudinary_url, 'detections': filtered_detections, 'mongoId': str(mongo_id), 'detectionCount': len(filtered_detections)})". Client nhận response, parse JSON, update UI với ảnh kết quả và số lượng tôm phát hiện được. Ảnh được hiển thị toàn màn hình một lúc rồi tự động thêm vào gallery local, đồng thời state được reset để sẵn sàng cho lần chụp tiếp theo.

#### 3.2.2.4. Luồng Xem Thư Viện

    │                        │    (Sorted by time)     │
    │                        │                         │
    │<─── Image List ────────│                         │
    │    (URLs + Metadata)   │                         │
    │                        │                         │
    │    Display Grid        │                         │
    │    (Lazy Loading)      │                         │
    │                        │                         │
```

---

## 3.3. THIẾT KẾ CHI TIẾT

Thiết kế chi tiết là bước cụ thể hóa các thành phần của hệ thống từ kiến trúc tổng quan xuống các module, class, function và data structure. Phần này trình bày chi tiết về cơ sở dữ liệu, API, module xử lý AI, giao diện người dùng, và các cơ chế bảo mật. Mỗi thiết kế đều được giải thích về lý do lựa chọn, cách hoạt động, và mối quan hệ với các thành phần khác trong hệ thống.

### 3.3.1. Thiết Kế Cơ Sở Dữ Liệu

Cơ sở dữ liệu là nền tảng lưu trữ toàn bộ thông tin của hệ thống, bao gồm metadata của ảnh, kết quả nhận diện, thông tin người dùng và thiết bị. Hệ thống sử dụng kiến trúc hybrid storage, kết hợp MongoDB (NoSQL) cho metadata và Cloudinary (Cloud Storage) cho ảnh thực tế. Kiến trúc này tận dụng ưu điểm của từng loại storage: MongoDB linh hoạt về schema và query mạnh mẽ cho dữ liệu có cấu trúc, còn Cloudinary chuyên dụng cho ảnh với CDN toàn cầu và tự động tối ưu hóa.

#### 3.3.1.1. MongoDB Schema

MongoDB được chọn vì là NoSQL database có schema linh hoạt, phù hợp với dữ liệu detection có thể thay đổi theo thời gian (ví dụ thêm trường mới như chiều dài, trọng lượng tôm). MongoDB cũng có khả năng scale horizontal tốt khi dữ liệu tăng lên hàng triệu records. Hệ thống sử dụng MongoDB Atlas, dịch vụ cloud database với free tier 512MB, đủ cho việc lưu metadata (không lưu ảnh thực tế).

Collection `detections` là collection chính, lưu trữ thông tin của mỗi lần nhận diện. Mỗi document trong collection đại diện cho một ảnh đã được xử lý. Thiết kế schema như sau:

**Collection: `detections`**

Đây là collection trung tâm lưu trữ tất cả thông tin về các lần nhận diện tôm. Mỗi document có cấu trúc như sau:

```json
{
  "_id": ObjectId("..."),
  "imageUrl": "https://res.cloudinary.com/.../image.jpg",
  "cloudinaryUrl": "https://res.cloudinary.com/.../image.jpg",
  "detections": [
    {
      "className": "shrimp",
      "confidence": 0.92,
      "bbox": {
        "x": 120.5,
        "y": 80.3,
        "width": 150.2,
        "height": 100.8
      },
      "length": 12.5,
      "weight": 25.3
    }
  ],
  "timestamp": 1703318400000,
  "capturedFrom": "user@example.com",
  "deviceId": "android-device-123",
  "detectionCount": 3,
  "processingTime": 2.45
}
```

**Giải thích các trường:**

- `_id`: Primary key tự động generate bởi MongoDB, dạng ObjectId 12-byte unique identifier. Trường này được dùng để reference document và đảm bảo tính duy nhất.

- `imageUrl` và `cloudinaryUrl`: Lưu URL của ảnh đã được annotate (vẽ bounding box) trên Cloudinary. Hai trường này hiện tại giống nhau nhưng tách riêng để dễ mở rộng sau này (có thể có thumbnail URL riêng). URL này là permanent link có thể truy cập công khai, ví dụ: "https://res.cloudinary.com/xxx/image/upload/v1703318400/shrimp_detection/image.jpg".

- `detections`: Mảng chứa danh sách các con tôm được phát hiện trong ảnh. Mỗi phần tử là một object với:
  - `className`: Tên class của đối tượng, hiện tại là "shrimp". Trong tương lai có thể mở rộng cho các loại thủy sản khác như "fish", "crab".
  - `confidence`: Độ tin cậy của detection, giá trị từ 0 đến 1. Ví dụ 0.92 nghĩa là model 92% chắc chắn đây là tôm. Chỉ những detection có confidence > 0.5 (threshold) mới được lưu.
  - `bbox`: Bounding box là hình chữ nhật bao quanh đối tượng, gồm:
    - `x`, `y`: Tọa độ góc trên bên trái của box (pixel)
    - `width`, `height`: Chiều rộng và chiều cao của box (pixel)
  - `length`, `weight`: Các thông tin mở rộng về kích thước tôm (cm) và trọng lượng (gram). Hiện tại chưa được tính toán tự động nhưng có thể thêm vào sau bằng công thức ước lượng từ kích thước bbox.

- `timestamp`: Thời điểm chụp ảnh, lưu dạng Unix timestamp (milliseconds từ epoch 1/1/1970). Ví dụ 1703318400000 tương ứng 23/12/2023 10:00:00 GMT+7. Dùng millisecond thay vì second để có độ chính xác cao hơn, tránh trùng lặp khi chụp liên tục.

- `capturedFrom`: Email hoặc số điện thoại của người dùng đã chụp ảnh, dùng để phân quyền và lọc ảnh theo user. Ví dụ "user@example.com" hoặc "+84987654321".

- `deviceId`: ID duy nhất của thiết bị Android đã chụp ảnh, được lấy từ Settings.Secure.ANDROID_ID. Dùng để tracking thiết bị, phát hiện thiết bị bất thường, và implement device binding.

- `detectionCount`: Tổng số tôm phát hiện được trong ảnh, bằng length của mảng detections. Trường này redundant (có thể tính từ detections.length) nhưng được lưu riêng để query và aggregate nhanh hơn mà không cần parse mảng.

- `processingTime`: Thời gian xử lý nhận diện tính bằng giây, từ lúc nhận ảnh đến khi trả về kết quả. Thông tin này hữu ích cho việc monitoring performance và tối ưu hóa. Ví dụ 2.45 giây bao gồm: decode base64 (0.1s), preprocess (0.2s), inference (1.8s), postprocess (0.15s), upload Cloudinary (0.2s).

**Indexes:**

Indexes là cấu trúc dữ liệu giúp MongoDB tìm kiếm documents nhanh hơn, tương tự như index trong sách. Không có index, MongoDB phải scan toàn bộ collection (collection scan) rất chậm với dữ liệu lớn.

```javascript
db.detections.createIndex({ "capturedFrom": 1, "timestamp": -1 })
db.detections.createIndex({ "timestamp": -1 })
db.detections.createIndex({ "deviceId": 1 })
```

- **Compound Index { "capturedFrom": 1, "timestamp": -1 }**: Index kết hợp trên hai trường, hỗ trợ query "lấy ảnh của user X sắp xếp theo thời gian mới nhất". Số 1 nghĩa là ascending (tăng dần), -1 là descending (giảm dần). Index này được dùng trong API `/api/shrimp-images` khi filter theo email: "db.detections.find({capturedFrom: email}).sort({timestamp: -1})". Với index, query này chỉ mất vài milliseconds dù có hàng nghìn documents.

- **Single Index { "timestamp": -1 }**: Index riêng trên timestamp, hỗ trợ query lấy tất cả ảnh sắp xếp theo thời gian mới nhất không phụ thuộc vào user. Dùng cho admin dashboard hoặc thống kê tổng quan. Dù đã có compound index trên (capturedFrom, timestamp), MongoDB vẫn cần index riêng trên timestamp vì compound index chỉ hiệu quả khi query bao gồm trường đầu tiên (capturedFrom).

- **Single Index { "deviceId": 1 }**: Index trên deviceId giúp nhanh chóng tìm tất cả ảnh từ một thiết bị cụ thể, hữu ích cho device binding logic khi kiểm tra "thiết bị này đã chụp bao nhiêu ảnh" hoặc "thiết bị này có bị bind với user khác không".

**Collection: `permitted_users`** (Được lưu trong file JSON trên server)

Collection này không được lưu trong MongoDB mà được lưu trong hai file JSON cục bộ trên Raspberry Pi: `permitted_emails.json` và `permitted_phones.json`. Lý do lựa chọn file JSON thay vì database là:
1. **Performance**: Whitelist được load vào memory khi server khởi động, việc kiểm tra quyền không cần query database mỗi lần, giảm latency.
2. **Simplicity**: Danh sách whitelist thường nhỏ (< 100 users), không cần sức mạnh của database.
3. **Security**: File được lưu local trên server, không expose ra internet qua API, chỉ admin có thể chỉnh sửa.
4. **Backup dễ dàng**: File JSON đơn giản, có thể backup bằng Git hoặc copy file.

Cấu trúc file:

```json
{
  "permitted_emails": [
    "admin@example.com",
    "user1@example.com"
  ],
  "permitted_phones": [
    "+84987654321",
    "+84123456789"
  ]
}
```

- `permitted_emails`: Mảng chứa danh sách email được phép truy cập hệ thống. Những user đăng nhập bằng Google với email không nằm trong list này sẽ bị từ chối ngay cả khi Firebase authentication thành công.

- `permitted_phones`: Mảng chứa danh sách số điện thoại được phép, dành cho user đăng nhập bằng phone authentication. Số điện thoại phải ở format quốc tế E.164 (+84...) để nhất quán.

Backend load file này vào memory khi khởi động: `permitted_emails = load_permitted_emails()`, sau đó mọi request đều được check: `if email not in permitted_emails: return 403`. Admin có thể thêm/xóa user qua API endpoints `/api/admin/permitted-emails` hoặc chỉnh sửa trực tiếp file JSON trên server.

**Collection: `device_bindings`** (Được lưu trong file JSON trên server)

Tương tự permitted_users, device_bindings cũng được lưu trong file JSON (`permitted_devices.json`) thay vì MongoDB. Collection này implement cơ chế device binding - mỗi thiết bị chỉ được liên kết với một tài khoản duy nhất, ngăn chặn việc chia sẻ tài khoản trái phép.

```json
{
  "device-id-123": {
    "email": "user@example.com",
    "bindTime": 1703318400000,
    "deviceModel": "Samsung Galaxy S21",
    "lastAccess": 1703404800000
  }
}
```

Cấu trúc là object với key là device ID và value là thông tin binding:

- **Key (device-id-123)**: Android device ID được lấy từ `Settings.Secure.ANDROID_ID`, một string duy nhất cho mỗi thiết bị (persistent qua app reinstall nhưng thay đổi khi factory reset). Device ID này được gửi trong header của mọi API request.

- `email`: Email hoặc số điện thoại của user mà thiết bị này được bind. Một khi đã bind, chỉ user này mới có thể sử dụng thiết bị để truy cập hệ thống.

- `bindTime`: Timestamp lúc thiết bị được bind lần đầu tiên (milliseconds). Thông tin này giúp tracking và audit log.

- `deviceModel`: Tên model của thiết bị Android, được lấy từ `Build.MODEL` và `Build.BRAND`, ví dụ "Samsung Galaxy S21". Thông tin này giúp admin nhận biết thiết bị dễ dàng hơn thay vì chỉ nhìn device ID.

- `lastAccess`: Timestamp của lần truy cập gần nhất từ thiết bị này. Backend update trường này mỗi khi nhận request, dùng để phát hiện thiết bị inactive lâu ngày (có thể unbind để giải phóng slot).

**Workflow của Device Binding:**
1. User đăng nhập lần đầu trên thiết bị mới → Backend kiểm tra device ID chưa tồn tại trong permitted_devices → Tự động bind thiết bị với email của user → Lưu vào file JSON.
2. User đăng nhập lần sau → Backend kiểm tra device ID đã tồn tại → So sánh email trong binding với email từ token → Nếu khớp, cho phép truy cập → Update lastAccess.
3. Nếu user khác cố đăng nhập trên cùng thiết bị → Backend phát hiện device ID đã bind với email khác → Từ chối với message "Device already bound to another user".
4. Admin có thể xóa binding thông qua API `/api/admin/clear-device-binding` nếu user đổi thiết bị hoặc thiết bị bị mất.

#### 3.3.1.2. Cloudinary Storage Structure

Cloudinary là dịch vụ cloud storage và CDN chuyên dụng cho media (ảnh, video). Hệ thống sử dụng Cloudinary vì những lý do sau:

1. **CDN toàn cầu**: Ảnh được phân phối qua mạng lưới server trên toàn thế giới, user tải ảnh từ server gần nhất, giảm latency. Quan trọng cho ứng dụng mobile với băng thông không ổn định.

2. **Tự động tối ưu hóa**: Cloudinary tự động convert format (WebP cho browser hỗ trợ), resize, compress ảnh mà không cần code. URL có thể thêm transformation parameters: `/w_200,h_200,c_fill/` để lấy thumbnail 200x200.

3. **Reliability**: 99.99% uptime SLA, data được replicate across multiple data centers. Ảnh sẽ không bị mất ngay cả khi Raspberry Pi hỏng.

4. **Cost-effective**: Free tier cho phép 25GB storage và 25GB bandwidth/month, đủ cho khoảng 10,000 ảnh chất lượng trung bình.

5. **Python SDK dễ dùng**: Chỉ cần vài dòng code để upload: `cloudinary.uploader.upload(image_bytes, folder='shrimp_detection')`.

Cấu trúc thư mục trên Cloudinary:

```
cloudinary://
  └── shrimp_detection/
      ├── 2024/
      │   ├── 12/
      │   │   ├── 23/
      │   │   │   ├── detection_1703318400_001.jpg
      │   │   │   ├── detection_1703318401_002.jpg
      │   │   │   └── ...
```

**Giải thích cấu trúc:**

- **Root folder `shrimp_detection/`**: Tất cả ảnh của project được lưu trong folder này, tách biệt với các project khác nếu dùng chung tài khoản Cloudinary. Folder name được chỉ định khi upload: `cloudinary.uploader.upload(image, folder='shrimp_detection')`.

- **Phân cấp theo thời gian `YYYY/MM/DD/`**: Ảnh được tổ chức theo năm/tháng/ngày để dễ quản lý và tìm kiếm. Ví dụ ảnh chụp ngày 23/12/2024 sẽ nằm trong `2024/12/23/`. Cấu trúc này giúp:
  - Tránh tình trạng quá nhiều file trong một folder (Cloudinary limits 1000 assets per folder view)
  - Dễ dàng backup/delete theo khoảng thời gian
  - Query nhanh hơn với prefix search

- **Tên file `detection_{timestamp}_{sequence}.jpg`**: Mỗi ảnh có tên duy nhất gồm:
  - Prefix "detection_" để phân biệt với các loại ảnh khác (nếu có)
  - Unix timestamp (10 chữ số, giây từ epoch) để sort theo thời gian
  - Sequence number 3 chữ số (001, 002, ...) để xử lý trường hợp nhiều ảnh cùng một giây
  - Extension ".jpg" vì ảnh đã được encode JPEG

**Metadata được lưu:**

Khi upload ảnh lên Cloudinary, ngoài file ảnh còn có metadata được lưu kèm:

- **Original filename**: Tên file gốc trước khi upload, lưu trong metadata để có thể trace back.
- **Upload timestamp**: Thời điểm upload lên Cloudinary (có thể khác thời điểm chụp vài giây do network delay).
- **Image dimensions**: Width và height của ảnh (pixel), Cloudinary tự động detect. Ví dụ 640x480.
- **File size**: Kích thước file (bytes), dùng để tracking storage quota. Một ảnh JPEG chất lượng 80% thường khoảng 50-100KB.
- **CDN URLs**: Cloudinary generate nhiều URL variants:
  - **Original URL**: `https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/shrimp_detection/2024/12/23/detection_1703318400_001.jpg`
  - **Secure URL**: HTTPS version (luôn được dùng trong production)
  - **Thumbnail URLs**: Có thể generate on-the-fly bằng cách thêm transformation vào URL: `/w_200,h_200,c_thumb/` sẽ tạo thumbnail 200x200 crop từ center. Mobile app có thể load thumbnail cho gallery grid và full image khi xem chi tiết, tiết kiệm bandwidth.

**URL Structure Example:**
```
https://res.cloudinary.com/democloud/image/upload/v1703318400/shrimp_detection/2024/12/23/detection_1703318400_001.jpg
│                      │              │            │          │                    │                                    │
│                      │              │            │          │                    │                                    └─ Filename
│                      │              │            │          │                    └────────────────────────────────────── Folder path
│                      │              │            │          └───────────────────────────────────────────────────────────── Version (timestamp)
│                      │              │            └────────────────────────────────────────────────────────────────────────── Upload type (image/upload)
│                      │              └───────────────────────────────────────────────────────────────────────────────────────── Cloud name
│                      └──────────────────────────────────────────────────────────────────────────────────────────────────────── Domain
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────── Protocol
```

**Transformations Examples:**
- Thumbnail: `.../w_200,h_200,c_fill/...` - Resize và crop về 200x200
- Quality: `.../q_80/...` - Adjust JPEG quality
- Format: `.../f_auto/...` - Auto chọn format tốt nhất (WebP cho browser support)
- Multiple: `.../w_200,h_200,c_fill,q_80,f_auto/...` - Kết hợp nhiều transformation

Android app lưu URL gốc trong MongoDB, khi hiển thị có thể modify URL để thêm transformation phù hợp với màn hình device.

### 3.3.2. Thiết Kế API Endpoints

Hệ thống sử dụng RESTful API để giao tiếp giữa mobile app và backend server. Base URL: `http://<raspberry-pi-ip>:8000` hoặc `https://xxx.ngrok-free.dev`.

#### 3.3.2.1. Authentication APIs

- **POST /api/auth/check**: Kiểm tra tính hợp lệ của token xác thực và quyền truy cập của người dùng. Được gọi khi app khởi động để verify user session và quyết định redirect về màn hình nào (login hay home). Trả về thông tin: token valid, email, permission status, admin role, và danh sách thiết bị đã bind.

- **GET /api/auth/verify**: Verify quyền truy cập người dùng, dùng như middleware cho các API khác. Trả về 403 Forbidden nếu user không trong whitelist.

#### 3.3.2.2. Camera APIs

- **GET /blynk_feed**: Streaming video realtime từ camera qua MJPEG protocol. Backend capture frame từ OpenCV, encode JPEG và yield continuous stream. Client parse stream tìm JPEG markers và decode thành Bitmap hiển thị ~30 FPS.

- **GET /health**: Health check monitoring trạng thái hệ thống (camera, model, uptime), không cần authentication.

#### 3.3.2.3. Detection APIs

- **POST /api/detect-shrimp**: Nhận ảnh Base64 từ client, chạy AI detection với TFLite, vẽ bounding boxes, upload lên Cloudinary, lưu metadata vào MongoDB, trả về kết quả detection với URL ảnh và số lượng tôm phát hiện. Pipeline: Decode Base64 → Preprocess → AI inference → Postprocess → Draw bbox → Upload → Save. Thời gian xử lý trung bình ~2.5 giây.

#### 3.3.2.4. Gallery APIs

- **GET /api/shrimp-images**: Lấy danh sách ảnh với pagination (limit, skip parameters) và filter theo thời gian (startDate, endDate). User chỉ xem được ảnh của mình, admin xem được tất cả. Trả về list ảnh với metadata và total count cho pagination.

- **GET /api/shrimp-images/:id**: Lấy thông tin chi tiết của một ảnh cụ thể theo MongoDB ObjectId. Dùng khi user tap vào ảnh trong gallery để xem fullscreen với đầy đủ detection details.

#### 3.3.2.5. Admin APIs

- **GET /api/admin/permitted-emails**: Lấy danh sách tất cả emails được phép truy cập hệ thống. Chỉ admin có quyền truy cập.

- **POST /api/admin/permitted-emails**: Thêm email mới vào whitelist, cấp quyền truy cập cho user mới. Backend validate email format và check duplicate trước khi thêm.

- **DELETE /api/admin/permitted-emails**: Xóa email khỏi whitelist, thu hồi quyền truy cập. Không cho phép admin xóa chính email của mình. User bị xóa sẽ bị từ chối truy cập ngay lập tức.

- **GET /api/admin/permitted-phones**: Lấy danh sách số điện thoại được phép.

- **POST /api/admin/permitted-phones**: Thêm số điện thoại mới (validate E.164 format).

- **DELETE /api/admin/permitted-phones**: Xóa số điện thoại khỏi whitelist.

### 3.3.3. Thiết Kế Module AI Detection 
{
  "token_valid": true,
  "decoded_email": "user@example.com",
  "email_permitted": true,
  "is_admin": false,
  "user_devices": [...]
}
```

- `token_valid`: Token hợp lệ và chưa expired
- `decoded_email`: Email/phone từ token
- `email_permitted`: User có trong whitelist
- `is_admin`: User có quyền admin
- `user_devices`: Danh sách thiết bị đã bind

**GET /api/auth/verify**: Verify quyền truy cập, dùng như middleware cho các API khác.

Cấu trúc:
```
Headers: Authorization: <Firebase-ID-Token>

Response:
{
  "success": true,
  "email": "user@example.com",
  "permitted": true,
  "role": "user"
}
```

Trả về 403 Forbidden nếu user không trong whitelist.

#### 3.3.2.2. Camera APIs

**GET /blynk_feed**: Streaming video realtime từ camera qua MJPEG (Motion JPEG) protocol.

Cấu trúc:
```
Headers: User-Agent: Android-Camera-App
Response: multipart/x-mixed-replace; boundary=frame (continuous JPEG stream)
```

Backend streaming với OpenCV: camera capture frame → encode JPEG → wrap với boundary → yield continuous. Client parse stream tìm JPEG markers (0xFF 0xD8 start, 0xFF 0xD9 end) và decode thành Bitmap hiển thị ~30 FPS.

**GET /health**: Health check monitoring, không cần authentication.

Response: `{"status": "healthy", "camera": "connected", "model": "loaded", "uptime": 12345}`

#### 3.3.2.3. Detection APIs

**POST /api/detect-shrimp**: Nhận ảnh, chạy AI detection, vẽ annotations, upload Cloudinary, lưu MongoDB, trả kết quả.

```
Headers: Authorization: <Firebase-ID-Token>
Body: {"image": "<base64-image>", "source": "camera_stream"}

Response:
{
  "success": true,
  "imageUrl": "https://cloudinary.com/.../image.jpg",
  "detections": [
    {"className": "shrimp", "confidence": 0.92, "bbox": {...}}
  ],
  "mongoId": "507f1f77bcf86cd799439011",
  "detectionCount": 3,
  "processingTime": 2.45
}
```

Pipeline xử lý: Decode Base64 → Preprocess → AI inference (TFLite) → Postprocess → Draw bbox → Upload Cloudinary → Save MongoDB → Return. Thời gian trung bình ~2.5s.

#### 3.3.2.4. Gallery APIs

APIs để quản lý và truy vấn thư viện ảnh đã lưu.

**1. Get All Images**

Lấy danh sách ảnh với pagination (phân trang) để tránh load quá nhiều dữ liệu một lúc.

```
GET /api/shrimp-images?limit=20&skip=0
Headers:
  - Authorization: <Firebase-ID-Token>

Response:
{
  "success": true,
  "images": [
    {
      "id": "507f1f77bcf86cd799439011",
      "imageUrl": "https://cloudinary.com/.../image.jpg",
      "detections": [...],
      "timestamp": 1703318400000,
      "capturedFrom": "user@example.com"
    }
  ],
  "total": 150,
  "limit": 20,
  "skip": 0
}
```

**Query Parameters:**
- `limit`: Số lượng ảnh tối đa trả về trong một request (default 20, max 100). Giới hạn này tránh response quá lớn làm app lag.
- `skip`: Số lượng ảnh bỏ qua từ đầu (default 0). Dùng cho pagination: page 1 → skip=0, page 2 → skip=20, page 3 → skip=40.
- `startDate` / `endDate`: (Optional) Filter theo thời gian, Unix timestamp milliseconds. Ví dụ: `?startDate=1703232000000&endDate=1703318400000` lấy ảnh từ 00:00 đến 24:00 ngày 23/12.

**Response Fields:**
- `images`: Mảng các image objects, sort theo timestamp descending (mới nhất trước). Mỗi object chứa:
  - `id`: MongoDB ObjectId dạng string
**2. Get Single Image Detail**

Lấy thông tin chi tiết của một ảnh cụ thể theo ID, thường được dùng khi user tap vào ảnh trong gallery để xem fullscreen.

```
GET /api/shrimp-images/:id
Headers:
  - Authorization: <Firebase-ID-Token>

Response:
{
  "success": true,
  "image": {
    "id": "507f1f77bcf86cd799439011",
    "imageUrl": "https://cloudinary.com/.../image.jpg",
    "cloudinaryUrl": "https://cloudinary.com/.../image.jpg",
    "detections": [...],
    "timestamp": 1703318400000,
    "capturedFrom": "user@example.com",
    "deviceId": "android-device-123"
  }
}
```

**URL Parameter:**
- `:id`: MongoDB ObjectId của image document, ví dụ "507f1f77bcf86cd799439011". Client lấy ID này từ response của GET /api/shrimp-images.

**Response:**
Trả về toàn bộ thông tin chi tiết của image, bao gồm cả device ID mà GET list không trả về (để tiết kiệm bandwidth).

**Authorization:**
Backend verify rằng user chỉ có thể xem ảnh của chính mình: `if image.capturedFrom != g.user['email']: return 403`. Admin có thể xem ảnh của bất kỳ user nào.

**Error Response khi không tìm thấy (404):**
```json
{
  "success": false,
  "error": "NOT_FOUND",
  "message": "Image with ID 507f1f77bcf86cd799439011 not found"
}
```

**Backend implementation:**
```python
@app.route('/api/shrimp-images/<image_id>', methods=['GET'])
@requires_google_auth
def get_image_detail(image_id):
    from bson import ObjectId
    
    try:
        # Query by ObjectId
        image = db.detections.find_one({'_id': ObjectId(image_id)})
        
        if not image:
            return jsonify({
                'success': False,
                'error': 'NOT_FOUND',
                'message': f'Image with ID {image_id} not found'
            }), 404
        
        # Check permission (user can only see own images, admin sees all)
        user_email = g.user['email']
        if image['capturedFrom'] != user_email and user_email != ADMIN_EMAIL:
            return jsonify({
                'success': False,
                'error': 'FORBIDDEN',
                'message': 'You do not have permission to view this image'
            }), 403
        
        return jsonify({
            'success': True,
            'image': format_image(image)
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': 'INVALID_ID',
            'message': 'Invalid ObjectId format'
        }), 400
```

#### 3.3.2.5. Admin APIs

Group APIs này chỉ dành cho admin, quản lý whitelist của users và phones. Tất cả endpoints đều require admin authentication.

**1. Get Permitted Emails**

Lấy danh sách tất cả emails được phép truy cập hệ thống.

```
GET /api/admin/permitted-emails
Headers:
  - Authorization: <Admin-Firebase-ID-Token>

Response:
{
  "success": true,
  "emails": [
    "admin@example.com",
    "user1@example.com"
  ]
}
```

**Authorization:**
Chỉ admin mới có quyền gọi API này. Backend check: `if g.user['email'] != ADMIN_EMAIL: return 403`.

**Response:**
Trả về mảng strings chứa tất cả emails trong `permitted_emails.json`. Admin có thể review danh sách user hiện tại.

**2. Add Permitted Email**

Thêm một email mới vào whitelist, cấp quyền truy cập cho user mới.

```
POST /api/admin/permitted-emails
Headers:
  - Authorization: <Admin-Firebase-ID-Token>
Body:
{
  "email": "newuser@example.com"
}

Response:
{
  "success": true,
  "message": "Email added successfully",
  "emails": [...]
}
```

**Request Body:**
- `email`: Email muốn thêm vào whitelist. Backend validate format email trước khi thêm.

**Response:**
- `message`: Thông báo thành công
- `emails`: Danh sách emails mới sau khi thêm, để client update UI ngay

**Validation:**
```python
# Check email format
if not re.match(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', email):
    return jsonify({
        'success': False,
        'error': 'INVALID_EMAIL',
        'message': 'Invalid email format'
```
GET /api/admin/permitted-emails
Headers:
  - Authorization: <Admin-Firebase-ID-Token>

Response:
{
  "success": true,
  "emails": [
    "admin@example.com",
    "user1@example.com"
  ]
}
```

**2. Add Permitted Email**
```
POST /api/admin/permitted-emails
Headers:
  - Authorization: <Admin-Firebase-ID-Token>
Body:
{
  "email": "newuser@example.com"
}

Response:
{
  "success": true,
  "message": "Email added successfully",
  "emails": [...]
}
```

**3. Remove Permitted Email**
```
DELETE /api/admin/permitted-emails
Headers:
  - Authorization: <Admin-Firebase-ID-Token>
Body:
{
  "email": "user@example.com"
}

Response:
{
  "success": true,
  "message": "Email removed successfully",
  "emails": [...]
}
```

### 3.3.3. Thiết Kế Module AI Detection

Module AI Detection là trái tim của hệ thống, sử dụng deep learning để tự động nhận diện và đếm số lượng tôm trong ảnh. Module được thiết kế theo kiến trúc pipeline với các stages rõ ràng, mỗi stage có input/output chuẩn và có thể test độc lập.

#### 3.3.3.1. Quy Trình Xử Lý Ảnh

Pipeline xử lý ảnh tuân theo workflow chuẩn của computer vision, từ ảnh thô đến kết quả cuối cùng qua 7 bước:

```python
def process_image_pipeline(image_data):
    """
    Pipeline xử lý ảnh đầy đủ từ Base64 đến MongoDB
    
    Args:
        image_data: Base64 encoded image string
        
    Returns:
        dict: {imageUrl, detections, mongoId}
    """
    # Step 1: Decode Base64
    image_bytes = base64.b64decode(image_data)
    image = Image.open(BytesIO(image_bytes))
    image_np = np.array(image)
    
    # Step 2: Preprocess
    input_data = preprocess_image(image_np)
    
    # Step 3: Inference
    detections = run_inference(input_data)
    
    # Step 4: Post-process
    filtered_detections = post_process(detections)
    
    # Step 5: Draw Annotations
    annotated_image = draw_detections(image_np, filtered_detections)
    
    # Step 6: Upload to Cloud
    cloud_url = upload_to_cloudinary(annotated_image)
    
    # Step 7: Save to Database
    mongo_id = save_to_mongodb(cloud_url, filtered_detections)
    
    return {
        "imageUrl": cloud_url,
        "detections": filtered_detections,
        "mongoId": mongo_id
    }
```

**Giải thích từng bước:**

**Step 1 - Decode Base64**: Ảnh được gửi từ Android dưới dạng Base64 string qua JSON. Bước này decode về binary bytes và load thành PIL Image object, sau đó convert sang NumPy array để xử lý với OpenCV và TensorFlow. NumPy array có shape (height, width, channels), ví dụ (480, 640, 3) cho ảnh RGB 640x480.

**Step 2 - Preprocess**: Chuẩn bị ảnh cho model AI. Bao gồm resize về kích thước input của model (320x320), convert color space (BGR→RGB), normalize pixel values (0-255 → 0-1), và thêm batch dimension. Bước này critical vì model chỉ accept input đúng format.

**Step 3 - Inference**: Chạy model AI để detect objects. Input là ảnh đã preprocess, output là tensor chứa bounding boxes, class IDs, và confidence scores. Đây là bước tốn thời gian nhất (~1.8s) vì phải chạy hàng triệu phép tính matrix.

**Step 4 - Post-process**: Lọc và format kết quả từ model. Loại bỏ detections có confidence thấp (<0.5), convert normalized coordinates về pixel coordinates, group thành format JSON chuẩn. Bước này cũng áp dụng NMS (Non-Maximum Suppression) để loại bỏ duplicate detections.

**Step 5 - Draw Annotations**: Vẽ bounding boxes (hình chữ nhật màu xanh) và labels (text "Shrimp 0.92") lên ảnh gốc bằng OpenCV. Ảnh kết quả là visualization giúp user dễ dàng verify kết quả detection.

**Step 6 - Upload to Cloud**: Upload ảnh đã annotate lên Cloudinary để lưu trữ lâu dài và phân phối qua CDN. Cloudinary trả về URL permanent có thể truy cập từ bất kỳ đâu.

**Step 7 - Save to Database**: Lưu metadata (URL, detections, timestamp, user...) vào MongoDB. Dữ liệu này dùng cho gallery, statistics, và audit log.

**Error Handling**: Mỗi bước được wrap trong try-catch để handle lỗi riêng biệt. Nếu Step 1-4 fail, không có ảnh nào được lưu. Nếu Step 6-7 fail, log error nhưng vẫn trả về detection results cho user.

#### 3.3.3.2. Tiền Xử Lý Ảnh (Preprocessing)

Preprocessing là bước chuẩn bị dữ liệu đầu vào cho model AI. Model được train với data ở format cụ thể, nên inference cũng phải dùng format tương tự để có kết quả chính xác.

```python
def preprocess_image(image_np):
    """
    Chuẩn bị ảnh cho model TFLite YOLO
    
    Args:
        image_np: NumPy array (H, W, 3) - BGR format
        
    Returns:
        input_data: NumPy array (1, 320, 320, 3) - RGB, normalized, batched
    """
    # 1. Resize về kích thước input của model (320x320)
    image_resized = cv2.resize(image_np, (INPUT_WIDTH, INPUT_HEIGHT))
    
    # 2. Convert BGR to RGB (OpenCV sử dụng BGR)
    image_rgb = cv2.cvtColor(image_resized, cv2.COLOR_BGR2RGB)
    
    # 3. Normalize pixel values về [0, 1]
    image_normalized = image_rgb.astype(np.float32) / 255.0
    
    # 4. Add batch dimension
    input_data = np.expand_dims(image_normalized, axis=0)
    
    return input_data
```

**Giải thích chi tiết:**

**1. Resize (320x320)**: 
Model YOLO được train với input cố định 320x320 pixels. Ảnh gốc từ camera là 640x480, cần resize để match. Chọn 320x320 thay vì 640x640 vì:
- Giảm computation: 320x320 = 102,400 pixels vs 640x640 = 409,600 pixels (4x ít hơn)
- Inference nhanh hơn đáng kể trên Raspberry Pi (~2s thay vì ~8s)
- Vẫn đủ độ phân giải để detect tôm có kích thước trung bình
- Trade-off hợp lý giữa accuracy và speed

OpenCV `cv2.resize()` mặc định dùng interpolation INTER_LINEAR (bilinear), smooth và nhanh. Aspect ratio không được giữ (640x480 → 320x320) nên ảnh bị stretch một chút, nhưng model đã được train với data tương tự nên không ảnh hưởng nhiều.

**2. Color Conversion (BGR → RGB)**:
OpenCV đọc ảnh theo format BGR (Blue-Green-Red) thay vì RGB chuẩn, vì lý do lịch sử từ thời early computer vision. Tuy nhiên, hầu hết deep learning frameworks (TensorFlow, PyTorch) expect RGB. Nếu không convert, model sẽ nhìn thấy màu sai (xanh dương thành đỏ và ngược lại), dẫn đến detection sai hoàn toàn.

`cv2.cvtColor()` thực hiện conversion nhanh bằng cách swap channel 0 và 2: `RGB[:,:,[2,1,0]]`.

**3. Normalization ([0, 255] → [0, 1])**:
Pixel values của ảnh RGB là integers từ 0-255 (uint8). Model neural network hoạt động tốt hơn với floating point values trong khoảng [0, 1] hoặc [-1, 1] vì:
- Activation functions (sigmoid, tanh, ReLU) được design cho range này
- Gradients ổn định hơn, không bị explode/vanish
- Training convergence nhanh hơn

Division bởi 255.0 (float) convert từ uint8 về float32 và normalize cùng lúc. Phải dùng 255.0 (không phải 255) để force float division.

**4. Batch Dimension**:
TensorFlow Lite model expect input shape `(batch_size, height, width, channels)`. Hiện tại chỉ detect 1 ảnh, batch_size = 1, nhưng vẫn phải có dimension này. `np.expand_dims(axis=0)` thêm dimension đầu tiên, biến shape từ `(320, 320, 3)` thành `(1, 320, 320, 3)`.

#### 3.3.3.3. Inference với TensorFlow Lite

Inference là bước chạy model AI để detect objects. TensorFlow Lite là phiên bản tối ưu của TensorFlow cho mobile và edge devices.

```python
def run_inference(input_data):
    """
    Chạy model inference với TFLite Interpreter
    
    Args:
        input_data: NumPy array (1, 320, 320, 3)
        
    Returns:
        dict: {boxes, classes, scores} - raw outputs from model
    """
    # Set input tensor
    interpreter.set_tensor(input_details[0]['index'], input_data)
    
    # Run inference
    interpreter.invoke()
    
    # Get output tensors
    boxes = interpreter.get_tensor(output_details[0]['index'])      # [1, N, 4]
    classes = interpreter.get_tensor(output_details[1]['index'])    # [1, N]
    scores = interpreter.get_tensor(output_details[2]['index'])     # [1, N]
    num_detections = int(interpreter.get_tensor(output_details[3]['index'])[0])
    
    return {
        'boxes': boxes[0][:num_detections],
        'classes': classes[0][:num_detections],
        'scores': scores[0][:num_detections]
    }
```

**Giải thích:**

**TFLite Interpreter**: Object được khởi tạo một lần khi server start:
```python
interpreter = Interpreter(model_path='models/best-fp16.tflite')
interpreter.allocate_tensors()  # Allocate memory for tensors
input_details = interpreter.get_input_details()    # Input tensor info
output_details = interpreter.get_output_details()  # Output tensor info
```

Interpreter là runtime engine chạy model. Không giống TensorFlow đầy đủ cần GPU, TFLite chỉ cần CPU và được optimize cho inference only (không training).

**Set Input Tensor**: Copy dữ liệu input vào buffer của model. `input_details[0]['index']` là ID của input tensor (thường là 0). Model có thể có nhiều inputs nhưng YOLO chỉ có 1.

**Invoke**: Chạy inference forward pass. Đây là bước tốn thời gian, với model FP16 trên RPi 4 mất ~1.8 giây. Trong thời gian này, CPU execute hàng triệu operations: convolutions, activations, pooling, etc.

**Get Output Tensors**: YOLO model có 4 outputs:
1. **boxes [1, N, 4]**: Bounding boxes với N là số detections tối đa (thường 100-200). Mỗi box có 4 values: `[y1, x1, y2, x2]` normalized về [0, 1]. Ví dụ: `[0.2, 0.3, 0.5, 0.7]` nghĩa là box từ 20% đến 50% height và 30% đến 70% width của ảnh.

2. **classes [1, N]**: Class ID cho mỗi detection. YOLO được train detect nhiều classes (person, car, dog...) nhưng model của chúng ta chỉ có 1 class "shrimp" = ID 0.

3. **scores [1, N]**: Confidence score cho mỗi detection, từ 0.0 đến 1.0. Score càng cao, model càng chắc chắn. Ví dụ: 0.92 = 92% confidence.

#### 3.3.3.4. Hậu Xử Lý (Post-processing)

```python
def post_process(detections, confidence_threshold=0.5):
    """
    Lọc và format kết quả detection
    """
    filtered = []
    
    for i in range(len(detections['scores'])):
        score = float(detections['scores'][i])
        
        # Chỉ giữ detection có confidence > threshold
        if score < confidence_threshold:
            continue
            
        # Convert normalized coordinates to pixel coordinates
        box = detections['boxes'][i]
        y1, x1, y2, x2 = box
        
        detection = {
            'className': 'shrimp',
            'confidence': score,
            'bbox': {
                'x': float(x1),
                'y': float(y1),
                'width': float(x2 - x1),
                'height': float(y2 - y1)
            }
        }
        
        filtered.append(detection)
    
    return filtered
```

#### 3.3.3.5. Vẽ Annotations

Visualization giúp user xác nhận kết quả detection một cách trực quan bằng cách vẽ bounding boxes và labels lên ảnh gốc.

```python
def draw_detections(image, detections):
    """
    Vẽ bounding boxes và labels lên ảnh
    
    Args:
        image: NumPy array - ảnh gốc
        detections: list - filtered detections
        
    Returns:
        annotated: NumPy array - ảnh đã vẽ annotations
    """
    annotated = image.copy()  # Không modify ảnh gốc
    h, w = image.shape[:2]
    
    for det in detections:
        bbox = det['bbox']
        conf = det['confidence']
        
        # Convert normalized coords to pixels
        x1 = int(bbox['x'] * w)
        y1 = int(bbox['y'] * h)
        x2 = int((bbox['x'] + bbox['width']) * w)
        y2 = int((bbox['y'] + bbox['height']) * h)
        
        # Draw rectangle (bounding box)
        cv2.rectangle(annotated, (x1, y1), (x2, y2), 
                     color=(0, 255, 0), thickness=2)
        
        # Draw label with confidence
        label = f"Shrimp {conf:.2f}"
        cv2.putText(annotated, label, (x1, y1-10),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
    
    return annotated
```

**Giải thích:**

**Copy image**: `annotated = image.copy()` tạo một bản sao để vẽ lên, không modify ảnh gốc. Quan trọng vì có thể cần ảnh gốc cho mục đích khác.

**Convert coordinates**: Bbox lưu normalized (0-1), phải convert về pixels để vẽ. Multiply với width/height và cast sang int vì OpenCV chỉ accept integer coordinates.

**Draw rectangle**: `cv2.rectangle()` vẽ hình chữ nhật:
- Points: (x1, y1) góc trên trái, (x2, y2) góc dưới phải
- Color: (0, 255, 0) là Green trong BGR format. Chọn xanh lá vì nổi bật trên background ao nước (xanh dương/nâu)
- Thickness: 2 pixels - đủ rõ mà không quá dày che mất object

**Draw label**: `cv2.putText()` vẽ text:
- Text: "Shrimp 0.92" với confidence format 2 chữ số thập phân
- Position: (x1, y1-10) phía trên bounding box 10 pixels để không overlap
- Font: HERSHEY_SIMPLEX - font rõ ràng, dễ đọc
- Scale: 0.5 - kích thước text vừa phải
- Color & Thickness: Giống với rectangle để nhất quán

**Optional enhancements** (có thể thêm sau):
- Background cho text: vẽ filled rectangle trước text để text dễ đọc hơn trên background phức tạp
- Màu sắc theo confidence: xanh lá (>0.7), vàng (0.5-0.7), đỏ (<0.5)
- ID cho mỗi detection: "Shrimp #1", "Shrimp #2" để tracking

### 3.3.4. Thiết Kế Giao Diện Người Dùng (Android App)

Giao diện người dùng là điểm tiếp xúc trực tiếp giữa hệ thống và người dùng cuối, quyết định trải nghiệm và tính khả dụng của ứng dụng. App được xây dựng bằng Jetpack Compose - framework UI declarative hiện đại của Android, với Material Design 3 guidelines.

#### 3.3.4.1. Cấu Trúc Navigation

Ứng dụng sử dụng Jetpack Navigation Component để quản lý điều hướng giữa các màn hình. Navigation được define bằng sealed class cho type-safety:

```kotlin
sealed class Route(val route: String) {
    object Login : Route("login")
    object Home : Route("home")
    object Camera : Route("camera")
    object Gallery : Route("gallery")
    object Profile : Route("profile")
    object Chart : Route("chart")
    object Admin : Route("admin")
}
```

**Sealed class benefits:**
- Type-safe: Compiler check tất cả possible routes
- Exhaustive when: Không miss case nào
- Easy refactoring: Rename route tự động update everywhere

**Navigation flow:**
```
App Launch
    ↓
Check Auth Status
    ↓
├─ Authenticated → Home (Camera Stream)
└─ Not Authenticated → Login
        ↓
        Login Success → Home
        
From Home:
├─ BottomBar → Camera | Gallery | Chart | Profile
├─ TopBar Menu → Settings | Admin (if admin)
└─ Tap Image → Image Detail (fullscreen)
```

**Implementation:**
```kotlin
// MainActivity.kt
NavHost(navController, startDestination = Route.Login.route) {
    composable(Route.Login.route) { LoginScreen(navController) }
    composable(Route.Home.route) { CameraStreamScreen(navController) }
    composable(Route.Gallery.route) { GalleryScreen(navController) }
    composable(Route.Chart.route) { ChartScreen(navController) }
    composable(Route.Profile.route) { ProfileScreen(navController) }
    composable(Route.Admin.route) { AdminScreen(navController) }
}
```

**Deep linking**: Có thể thêm deep links cho từng route để open app từ notifications hoặc external links:
```kotlin
composable(
    route = "image/{imageId}",
    deepLinks = listOf(navDeepLink { uriPattern = "myapp://image/{imageId}" })
) { backStackEntry ->
    val imageId = backStackEntry.arguments?.getString("imageId")
    ImageDetailScreen(imageId, navController)
}
```

#### 3.3.4.2. Component Hierarchy

Cây component được tổ chức theo hierarchy rõ ràng, mỗi màn hình là một Composable function chứa các Composable con:

```
MainActivity (ComponentActivity)
  └── NavHost
      ├── LoginScreen
      │   ├── GoogleSignInButton (Firebase Auth UI)
      │   └── PhoneSignInButton (Firebase Phone Auth)
      │
      ├── HomeScreen (Camera Stream)
      │   ├── TopBar (với title, icons)
      │   ├── CameraStreamView
      │   │   ├── AsyncImage (hiển thị MJPEG stream)
      │   │   └── CaptureButton (FAB - Floating Action Button)
      │   └── BottomNavBar (5 tabs)
      │
      ├── GalleryScreen
      │   ├── TopBar (với search icon)
      │   ├── LazyVerticalGrid (2 columns)
      │   │   └── ImageCard[] (repeated)
      │   │       ├── AsyncImage (thumbnail từ Cloudinary)
      │   │       └── InfoOverlay (số tôm, thời gian)
      │   └── BottomNavBar
      │
      ├── ProfileScreen
      │   ├── TopBar
      │   ├── UserInfoCard (email, avatar)
      │   ├── DeviceInfoCard (device model, ID)
      │   └── BottomNavBar
      │
      ├── ChartScreen
      │   ├── TopBar
      │   ├── StatisticsCard (tổng hợp số liệu)
      │   ├── LineChart (charting library)
      │   └── BottomNavBar
      │
      └── AdminScreen (chỉ admin)
          ├── TopBar
          ├── UserManagementSection
          │   ├── AddUserButton (Dialog trigger)
          │   └── UserList[] (LazyColumn)
          │       └── UserItem (email, delete button)
          └── BottomNavBar
```

**Design Principles:**

1. **Composition over inheritance**: Jetpack Compose favor composition - xây dựng UI phức tạp từ các Composable functions nhỏ, reusable.

2. **Single source of truth**: Mỗi state có một owner duy nhất (ViewModel hoặc remember), UI chỉ observe và react.

3. **Unidirectional data flow**: Data flow từ trên xuống (state → UI), events flow từ dưới lên (user action → callback).

4. **Reusability**: Các components như BottomNavBar, TopBar được reuse across multiple screens.

5. **Lazy loading**: Dùng LazyColumn, LazyVerticalGrid thay vì Column/Row thường để chỉ render items visible, optimize memory.

**Shared Components:**

```kotlin
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (Route) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Route.Home.route,
            onClick = { onNavigate(Route.Home) },
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Trang chủ") }
        )
        // ... other items
    }
}
```

#### 3.3.4.3. Màn Hình Chính (Camera Stream)

Màn hình Camera là màn hình chính của app, cho phép user xem live stream và chụp ảnh để nhận diện.

**Layout wireframe:**
```
┌─────────────────────────────────────┐
│  📷 Camera Stream       🔔  ⚙️  👤  │  ← TopAppBar (64dp height)
├─────────────────────────────────────┤
│                                     │
│                                     │
│         [Camera Stream View]        │  ← Main content area
│         (MJPEG frames displayed)    │     Aspect ratio 4:3
│                                     │     Background: black
│                                     │
│            ┌─────┐                  │
│            │  📷  │  ← FAB          │  ← Floating Action Button
│            └─────┘     (56dp)       │     Position: bottom center
│                                     │
├─────────────────────────────────────┤
│                                     │
│   Đã phát hiện: 3 con tôm 🦐       │  ← Status Card (optional)
│   Thời gian: 2.45s                  │     Visible sau khi detect
│                                     │
├─────────────────────────────────────┤
│   🏠    📷    📚    📊    👤        │  ← BottomNavigationBar (80dp)
└─────────────────────────────────────┘
```

**State Management:**

State của màn hình được quản lý bằng ViewModel với StateFlow:

```kotlin
data class CameraStreamState(
    val currentFrame: Bitmap? = null,          // Frame hiện tại từ stream
    val isLoading: Boolean = true,             // Đang load stream
    val isProcessing: Boolean = false,         // Đang xử lý detection
    val errorMessage: String = "",             // Lỗi nếu có
    val detectionCount: Int = 0,               // Số tôm phát hiện
    val processingTime: Float = 0f,            // Thời gian xử lý (giây)
    val detectedImageUrl: String? = null       // URL ảnh kết quả
)
```

**Các trạng thái UI:**

1. **Loading**: Hiển thị CircularProgressIndicator khi đang kết nối stream
   - `isLoading = true, currentFrame = null`
   - Message: "Đang kết nối camera..."

2. **Streaming**: Hiển thị live video stream
   - `isLoading = false, currentFrame != null`
   - Update currentFrame mỗi khi nhận frame mới (~30 FPS)

3. **Processing**: User đã nhấn capture, đang chờ kết quả detection
   - `isProcessing = true`
   - Overlay semi-transparent với progress indicator
   - Message: "Đang nhận diện tôm..."
   - Disable capture button để tránh spam requests

4. **Result**: Hiển thị kết quả detection
   - `isProcessing = false, detectionCount > 0`
   - Show status card với số tôm và thời gian
   - Có thể show ảnh result trong dialog hoặc navigate to gallery

5. **Error**: Khi có lỗi kết nối hoặc xử lý
   - `errorMessage != ""`
   - Show Snackbar hoặc AlertDialog với error message
   - Retry button

**Implementation highlights:**

```kotlin
@Composable
fun CameraStreamScreen(viewModel: CameraViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = { CameraTopBar() },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.captureAndDetect() },
                enabled = !state.isProcessing
            ) {
                Icon(Icons.Default.CameraAlt, "Capture")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            // Camera stream
            state.currentFrame?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Camera Stream",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: CircularProgressIndicator()
            
            // Processing overlay
            if (state.isProcessing) {
                ProcessingOverlay()
            }
            
            // Result card
            if (state.detectionCount > 0) {
                DetectionResultCard(
                    count = state.detectionCount,
                    time = state.processingTime
                )
            }
        }
    }
}
```

**Streaming logic:**

Streaming được handle trong coroutine của ViewModel:

```kotlin
fun startStreaming() {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$BASE_URL/blynk_feed")
                .build()
                
            client.newCall(request).execute().use { response ->
                val inputStream = response.body?.byteStream() ?: return@use
                val buffer = ByteArray(4096)
                
                while (isActive) {
                    // Read stream và parse MJPEG frames
                    val frame = readNextFrame(inputStream, buffer)
                    
                    // Decode to Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(frame, 0, frame.size)
                    
                    // Update state on Main thread
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(
                            currentFrame = bitmap,
                            isLoading = false
                        )}
                    }
                }
            }
        } catch (e: Exception) {
            _state.update { it.copy(
                isLoading = false,
                errorMessage = e.message ?: "Unknown error"
            )}
        }
    }
}
```

**Performance optimizations:**

1. **Bitmap reuse**: Reuse bitmap thay vì tạo mới mỗi frame → giảm GC pressure
2. **Decoding off main thread**: Decode JPEG trên IO dispatcher
3. **Skip frames**: Nếu processing chậm, skip frames thay vì queue → maintain realtime
4. **Downscale**: Có thể downscale bitmap trước khi display để save memory

**Capture and detect flow:**

```kotlin
fun captureAndDetect() {
    viewModelScope.launch {
        _state.update { it.copy(isProcessing = true) }
        
        try {
            // Get current frame
            val bitmap = _state.value.currentFrame ?: return@launch
            
            // Convert to Base64
            val base64 = bitmapToBase64(bitmap)
            
            // Call API
            val result = apiService.detectShrimp(
                DetectRequest(image = base64, source = "camera_stream")
            )
            
            // Update state with result
            _state.update { it.copy(
                isProcessing = false,
                detectionCount = result.detectionCount,
                processingTime = result.processingTime,
                detectedImageUrl = result.imageUrl
            )}
            
            // Show success message
            showSnackbar("Phát hiện ${result.detectionCount} con tôm")
            
        } catch (e: Exception) {
            _state.update { it.copy(
                isProcessing = false,
                errorMessage = e.message ?: "Detection failed"
            )}
        }
    }
}
```

#### 3.3.4.4. Màn Hình Thư Viện (Gallery)

Gallery screen hiển thị tất cả ảnh đã được detect trong quá khứ, cho phép user review lại history.

**Layout:**
```
┌─────────────────────────────────────┐
│  📷 Camera Stream       🔔  ⚙️  👤  │  ← TopBar
├─────────────────────────────────────┤
│                                     │
│                                     │
│         [Camera Stream]             │
│                                     │
│                                     │
│                                     │
│            ┌─────┐                  │
│            │  📷  │  ← Capture      │
│            └─────┘     Button       │
│                                     │
├─────────────────────────────────────┤
│                                     │
│   Đã phát hiện: 3 con tôm 🦐       │  ← Status
│   Thời gian: 2.45s                  │
│                                     │
├─────────────────────────────────────┤
│   🏠    📷    📚    📊    👤        │  ← BottomBar
└─────────────────────────────────────┘
```

**State Management:**
```kotlin
data class CameraStreamState(
    val currentFrame: Bitmap? = null,
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val errorMessage: String = "",
    val detectionCount: Int = 0,
    val processingTime: Float = 0f,
    val detectedImageUrl: String? = null
)
```

#### 3.3.4.4. Màn Hình Thư Viện (Gallery)

**Layout wireframe:**
```
┌─────────────────────────────────────┐
│  📚 Gallery             🔍  ⚙️  👤  │  ← TopBar với search icon
├─────────────────────────────────────┤
│  ┌────────┐  ┌────────┐  ┌────────┐│
│  │ Image1 │  │ Image2 │  │ Image3 ││  ← LazyVerticalGrid
│  │ 3 🦐   │  │ 5 🦐   │  │ 2 🦐   ││     GridCells.Fixed(2)
│  │ 10:30  │  │ 10:25  │  │ 10:20  ││     Spacing: 4.dp
│  └────────┘  └────────┘  └────────┘│
│  ┌────────┐  ┌────────┐  ┌────────┐│
│  │ Image4 │  │ Image5 │  │ Image6 ││
│  │ 4 🦐   │  │ 1 🦐   │  │ 6 🦐   ││
│  │ 10:15  │  │ 10:10  │  │ 10:05  ││
│  └────────┘  └────────┘  └────────┘│
│      ↓ Pull to load more ↓          │  ← Infinite scroll
├─────────────────────────────────────┤
│   🏠    📷    📚    📊    👤        │  ← BottomBar
└─────────────────────────────────────┘
```

**State & ViewModel:**

Gallery screen quản lý list of images với pagination để load dần dần, tránh lag:

```kotlin
class GalleryViewModel : ViewModel() {
    private val _images = MutableStateFlow<List<ShrimpImage>>(emptyList())
    val images: StateFlow<List<ShrimpImage>> = _images
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore
    
    private var currentPage = 0
    private val pageSize = 20
    
    fun loadImages() {
        if (_isLoading.value || !_hasMore.value) return  // Prevent duplicate calls
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = apiService.getShrimpImages(
                    limit = pageSize,
                    skip = currentPage * pageSize
                )
                
                // Append new images to existing list
                _images.value = _images.value + result.images
                
                // Check if more pages available
                _hasMore.value = (currentPage + 1) * pageSize < result.total
                currentPage++
                
            } catch (e: Exception) {
                // Show error
                showSnackbar(e.message ?: "Failed to load images")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        currentPage = 0
        _images.value = emptyList()
        _hasMore.value = true
        loadImages()
    }
}
```

**Giải thích:**

- **Infinite scroll**: User scroll đến cuối list → tự động trigger `loadImages()` để load page tiếp theo
- **Prevent duplicate**: Check `isLoading` và `hasMore` trước khi gọi API, tránh gọi nhiều lần đồng thời
- **Append strategy**: Append images mới vào list cũ thay vì replace, tạo hiệu ứng load more
- **Refresh**: Pull-to-refresh clear list và load lại từ đầu

**UI Implementation:**

```kotlin
@Composable
fun GalleryScreen(viewModel: GalleryViewModel = viewModel()) {
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refresh() }
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(images) { image ->
            ImageCard(
                image = image,
                onClick = { /* Navigate to detail */ }
            )
        }
        
        // Load more trigger
        item(span = { GridItemSpan(2) }) {
            if (hasMore && !isLoading) {
                LaunchedEffect(Unit) {
                    viewModel.loadImages()
                }
            }
            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ImageCard(image: ShrimpImage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)  // Square cards
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            // Thumbnail image từ Cloudinary với transformation
            AsyncImage(
                model = image.imageUrl.replace("/upload/", "/upload/w_300,h_300,c_fill/"),
                contentDescription = "Detection ${image.id}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.Gray)
            )
            
            // Overlay info ở bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "${image.detectionCount} 🦐",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTime(image.timestamp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
```

**Optimizations:**

- **Image transformation**: URL được modify để load thumbnail 300x300 thay vì full size, save bandwidth
- **Coil caching**: AsyncImage tự động cache ảnh, lần thứ 2 load ngay từ disk/memory
- **Lazy rendering**: LazyVerticalGrid chỉ render items visible + buffer một chút
- **Aspect ratio**: Cards có aspect ratio 1:1 (square) cho grid đẹp và consistent

#### 3.3.4.5. Màn Hình Thống Kê (Chart)

Chart screen visualize dữ liệu detection theo thời gian, giúp user phân tích xu hướng.

**Layout wireframe:**
```
┌─────────────────────────────────────┐
│  📊 Statistics          📅  ⚙️  👤  │  ← TopBar với date filter
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │  Tổng số ảnh: 150               ││
│  │  Tổng số tôm: 450               ││  ← Summary Card
│  │  Trung bình: 3 tôm/ảnh          ││     Material Card elevated
│  │  Ngày hoạt động: 15 ngày        ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │         Line Chart              ││  ← Chart component
│  │     (Số lượng theo ngày)        ││     Library: MPAndroidChart
│  │                                 ││     or Vico
│  │  50 ┤        ╭─╮                ││
│  │  40 ┤    ╭───╯ ╰─╮              ││
│  │  30 ┤╭───╯       ╰─╮            ││
│  │     └─────────────────           ││
│  │     Mon Tue Wed Thu Fri         ││
│  └─────────────────────────────────┘│
│                                     │
│  ◉ Ngày  ◉ Tuần  ◉ Tháng           │  ← Period selector chips
│                                     │
├─────────────────────────────────────┤
│   🏠    📷    📚    📊    👤        │  ← BottomBar
└─────────────────────────────────────┘
```

**Data Model:**

```kotlin
data class StatisticsData(
    val totalImages: Int,
    val totalShrimp: Int,
    val averagePerImage: Float,
    val activeDays: Int,
    val chartData: List<ChartPoint>
)

data class ChartPoint(
    val date: String,        // "2024-12-23"
    val count: Int,          // Số tôm detect trong ngày
    val imageCount: Int      // Số ảnh chụp trong ngày
)
```

**ViewModel:**

```kotlin
class ChartViewModel : ViewModel() {
    private val _statistics = MutableStateFlow<StatisticsData?>(null)
    val statistics: StateFlow<StatisticsData?> = _statistics
    
    private val _period = MutableStateFlow(Period.WEEK)  // DAY, WEEK, MONTH
    val period: StateFlow<Period> = _period
    
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val response = apiService.getStatistics(
                    period = _period.value.toString().lowercase()
                )
                
                _statistics.value = response
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun setPeriod(newPeriod: Period) {
        _period.value = newPeriod
        loadStatistics()  // Reload with new period
    }
}
```

**Chart Implementation:**

Có thể dùng library như Vico (Jetpack Compose native) hoặc MPAndroidChart (traditional View):

```kotlin
@Composable
fun StatisticsChart(data: List<ChartPoint>) {
    // Using Vico library (Compose-friendly)
    Chart(
        chart = lineChart(),
        model = entryModelOf(
            data.mapIndexed { index, point ->
                entryOf(index.toFloat(), point.count.toFloat())
            }
        ),
        startAxis = startAxis(),
        bottomAxis = bottomAxis(
            valueFormatter = { value, _ ->
                data.getOrNull(value.toInt())?.date ?: ""
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
```

**Aggregation Backend:**

Backend aggregate data theo period được request:

```python
@app.route('/api/statistics', methods=['GET'])
@requires_google_auth
def get_statistics():
    period = request.args.get('period', 'week')  # day, week, month
    user_email = g.user['email']
    
    # Calculate date range based on period
    now = datetime.now()
    if period == 'day':
        start_date = now - timedelta(days=7)
        group_format = '%Y-%m-%d'
    elif period == 'week':
        start_date = now - timedelta(weeks=4)
        group_format = '%Y-W%W'  # Week number
    else:  # month
        start_date = now - timedelta(days=365)
        group_format = '%Y-%m'
    
    # MongoDB aggregation pipeline
    pipeline = [
        {
            '$match': {
                'capturedFrom': user_email,
                'timestamp': {'$gte': int(start_date.timestamp() * 1000)}
            }
        },
        {
            '$group': {
                '_id': {
                    '$dateToString': {
                        'format': group_format,
                        'date': {'$toDate': '$timestamp'}
                    }
                },
                'totalShrimp': {'$sum': '$detectionCount'},
                'imageCount': {'$count': {}}
            }
        },
        {'$sort': {'_id': 1}}
    ]
    
    chart_data = list(db.detections.aggregate(pipeline))
    
    # Calculate totals
    total_images = db.detections.count_documents({'capturedFrom': user_email})
    total_shrimp = db.detections.aggregate([
        {'$match': {'capturedFrom': user_email}},
        {'$group': {'_id': None, 'total': {'$sum': '$detectionCount'}}}
    ])
    
    return jsonify({
        'totalImages': total_images,
        'totalShrimp': total_shrimp,
        'chartData': chart_data
    })
```

### 3.3.5. Thiết Kế Bảo Mật

Bảo mật là ưu tiên hàng đầu vì hệ thống có camera giám sát và dữ liệu cá nhân. Thiết kế áp dụng defense in depth - nhiều layers bảo mật chồng lên nhau.

#### 3.3.5.1. Authentication Flow

Quy trình xác thực đảm bảo chỉ người dùng hợp lệ mới truy cập được hệ thống:

#### 3.3.4.5. Màn Hình Thống Kê (Chart)

**Layout:**
```
┌─────────────────────────────────────┐
│  📊 Statistics          📅  ⚙️  👤  │  ← TopBar
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │  Tổng số ảnh: 150               ││
│  │  Tổng số tôm: 450               ││  ← Summary
│  │  Trung bình: 3 tôm/ảnh          ││     Card
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │         Line Chart              ││
│  │     (Số lượng theo ngày)        ││  ← Chart
│  │                                 ││
│  │  50 ┤        ╭─╮                ││
│  │  40 ┤    ╭───╯ ╰─╮              ││
│  │  30 ┤╭───╯       ╰─╮            ││
│  │     └─────────────────           ││
│  │     Mon Tue Wed Thu Fri         ││
│  └─────────────────────────────────┘│
│                                     │
├─────────────────────────────────────┤
│   🏠    📷    📚    📊    👤        │  ← BottomBar
└─────────────────────────────────────┘
```

### 3.3.5. Thiết Kế Bảo Mật

Bảo mật được thiết kế theo nguyên tắc "defense in depth" với nhiều lớp bảo vệ chồng lên nhau. Mỗi request phải pass qua nhiều checkpoints trước khi được xử lý.

#### 3.3.5.1. Authentication Flow

Quy trình xác thực đa lớp đảm bảo chỉ người dùng hợp lệ mới truy cập:

```
1. User Login (Firebase)
   - Google Sign-In hoặc Phone OTP
   ↓
2. Firebase Auth generates ID Token
   - JWT signed by Firebase, contains user info
   - Expiry: 1 hour
   ↓
3. App stores token securely
   - EncryptedSharedPreferences (AES-256)
   - Không bao giờ log token ra console
   ↓
4. Every API request includes token
   - Header: Authorization: <ID-Token>
   - hoặc X-Phone-Auth: <Phone-Number>
   ↓
5. Backend verifies token (Layer 1: Authentication)
   - Firebase Admin SDK verify signature
   - Check expiry time
   - Extract user email/phone
   ↓
6. Backend checks whitelist (Layer 2: Authorization)
   - Load permitted_emails.json
   - Check user email in list
   ↓
7. Backend checks device binding (Layer 3: Device Security)
   - Load permitted_devices.json
   - Verify device bound to this user
   ↓
8. Process request
   If any layer fails → Return 401/403
```

**Giải thích chi tiết:**

**Layer 1 - Firebase Authentication**: Firebase xác minh token có được issue bởi Firebase servers không, chưa bị revoked, và chưa expired. Đây là authentication cơ bản - verify "bạn là ai". Firebase Admin SDK thực hiện verification offline bằng public key caching, không cần call Firebase servers mỗi request nên rất nhanh (<1ms).

**Layer 2 - Whitelist Authorization**: Dù token hợp lệ, user vẫn phải nằm trong whitelist. Đây là authorization - verify "bạn có quyền không". Whitelist giúp admin control chặt chẽ ai được truy cập, thu hồi quyền ngay lập tức bằng cách remove khỏi file JSON. Critical vì ngay cả khi ai đó có Google account, họ không thể truy cập nếu admin chưa cấp quyền.

**Layer 3 - Device Binding**: Mỗi tài khoản chỉ được dùng trên một thiết bị cụ thể. Ngăn chặn việc chia sẻ tài khoản trái phép giữa nhiều người. Device ID được check và phải match với user trong binding table. Nếu device đã bind với user khác, request bị từ chối.

**Security benefits:**
- **Defense in depth**: Ba layers độc lập, nếu một layer bị bypass vẫn có hai layers khác
- **Centralized control**: Admin quản lý whitelist từ một nơi, changes có hiệu lực ngay
- **Audit trail**: Mọi request đều được log với user info và device info
- **Revocation**: Có thể revoke access tức thì bằng cách xóa khỏi whitelist hoặc unbind device

#### 3.3.5.2. Token Lifecycle

Token management đảm bảo tokens luôn valid và được refresh tự động khi cần:

```kotlin
object TokenManager {
    private const val PREFS_NAME = "auth"
    private const val TOKEN_KEY = "idToken"
    private const val TOKEN_EXPIRY_KEY = "tokenExpiry"
    
    suspend fun getValidToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(TOKEN_KEY, null)
        val expiry = prefs.getLong(TOKEN_EXPIRY_KEY, 0)
        
        // Check if token expired
        if (System.currentTimeMillis() > expiry) {
            return refreshToken(context)
        }
        
        return token
    }
    
    private suspend fun refreshToken(context: Context): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        
        // Force refresh token
        val result = user.getIdToken(true).await()
        val token = result.token ?: return null
        
        // Save new token with expiry
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putLong(TOKEN_EXPIRY_KEY, System.currentTimeMillis() + 3600000) // 1 hour
            .apply()
        
        return token
    }
    
    fun clearToken(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
```

**Giải thích:**

**Automatic Refresh**: `getValidToken()` được gọi trước mỗi API call. Nếu token sắp hết hạn (< 5 phút), tự động refresh token mới từ Firebase mà user không nhận biết. Điều này đảm bảo user experience seamless - không bị kick out đột ngột khi token expire.

**Expiry Tracking**: Lưu expiry time cùng với token để check local trước, không cần gọi backend mới biết token expired. Expiry được set 1 hour từ khi nhận token (Firebase default). Backend cũng check expiry nên attacker không thể fake expiry time.

**Secure Storage**: Dùng EncryptedSharedPreferences (part of Jetpack Security) để encrypt token trước khi lưu vào disk. Encryption key được manage bởi Android Keystore, một hardware-backed secure storage. Ngay cả khi device bị root, rất khó extract token.

**Clear on Logout**: Khi user logout, clear toàn bộ token khỏi storage và sign out khỏi Firebase. Token cũ vẫn valid cho đến hết expiry time nhưng app không còn giữ nên user phải login lại.

**Error Handling**: Nếu refresh fail (ví dụ: no internet, Firebase down, user revoked), return null và app navigate về login screen.

#### 3.3.5.3. Permission Levels

Hệ thống có hai roles với quyền hạn khác nhau:

| Role | Permissions | Identification |
|------|-------------|----------------|
| **Admin** | • Full access to all features<br>• View all users' images<br>• Manage whitelist (add/remove users)<br>• Manage device bindings<br>• System configuration<br>• View system logs<br>• Access admin panel | Email match với `ADMIN_EMAIL` env var |
| **User** | • View camera stream<br>• Capture and detect shrimp<br>• View own images only<br>• View own statistics<br>• Manage own profile<br>• Bind own device | Email trong `permitted_emails.json` |
| **Guest** | • No access<br>• Can see login screen only | Email không trong whitelist |

**Role Check Implementation:**

```python
def is_admin(email):
    """Check if user is admin"""
    return email == os.getenv('ADMIN_EMAIL')

def requires_admin(f):
    """Decorator for admin-only endpoints"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        email = g.user.get('email')
        if not is_admin(email):
            return jsonify({
                'success': False,
                'error': 'ADMIN_ONLY',
                'message': 'This endpoint requires admin privileges'
            }), 403
        return f(*args, **kwargs)
    return decorated_function

# Usage
@app.route('/api/admin/permitted-emails', methods=['POST'])
@requires_google_auth  # First check authentication
@requires_admin        # Then check admin role
def add_permitted_email():
    # Only admin can reach here
    pass
```

**Data Isolation**: User chỉ có thể query và view images của chính mình. Backend enforce bằng cách:
```python
query = {
    'capturedFrom': g.user['email']  # Filter by current user's email
}
# Admin có thể bỏ qua filter này để xem all images
if is_admin(g.user['email']):
    query = {}  # No filter for admin
```

**UI Conditional Rendering**: App ẩn/hiện features dựa trên role:
```kotlin
if (userRole == Role.ADMIN) {
    // Show admin menu item
    NavigationBarItem(
        icon = { Icon(Icons.Default.AdminPanelSettings, "Admin") },
        label = { Text("Admin") },
        onClick = { navController.navigate(Route.Admin) }
    )
}
```

#### 3.3.5.4. Device Binding Security

Device binding ngăn chặn account sharing và unauthorized device access:

```kotlin
object TokenManager {
    private const val PREFS_NAME = "auth"
    private const val TOKEN_KEY = "idToken"
    private const val TOKEN_EXPIRY_KEY = "tokenExpiry"
    
    suspend fun getValidToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(TOKEN_KEY, null)
        val expiry = prefs.getLong(TOKEN_EXPIRY_KEY, 0)
        
        // Check if token expired
        if (System.currentTimeMillis() > expiry) {
            return refreshToken(context)
        }
        
        return token
    }
    
    private suspend fun refreshToken(context: Context): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        val result = user.getIdToken(true).await()
        val token = result.token ?: return null
        
        // Save new token
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putLong(TOKEN_EXPIRY_KEY, System.currentTimeMillis() + 3600000) // 1 hour
            .apply()
        
        return token
    }
}
```

#### 3.3.5.3. Permission Levels

| Role | Permissions |
|------|-------------|
| **Admin** | - Full access to all features<br>- Manage user whitelist<br>- View all images<br>- System configuration |
| **User** | - View camera stream<br>- Capture and detect shrimp<br>- View own images<br>- View statistics |
| **Guest** | - No access (must be in whitelist) |

```python
def bind_device(email, device_id):
    """
    Liên kết thiết bị với tài khoản người dùng
    
    Args:
        email: User email hoặc phone
        device_id: Android device ID (Settings.Secure.ANDROID_ID)
        
    Returns:
        dict: {success, message}
    """
    permitted_devices = load_permitted_devices()
    
    # Check if device already bound to different user
    if device_id in permitted_devices:
        existing_email = permitted_devices[device_id].get('email')
        if existing_email != email:
            return {
                "success": False,
                "error": "DEVICE_BOUND",
                "message": f"Device already bound to {existing_email}",
                "requiresUnbind": True
            }
    
    # Bind device to user
    permitted_devices[device_id] = {
        "email": email,
        "bindTime": int(time.time() * 1000),
        "deviceModel": request.headers.get('X-Device-Model', 'Unknown'),
        "lastAccess": int(time.time() * 1000)
    }
    
    save_permitted_devices(permitted_devices)
    
    return {
        "success": True,
        "message": "Device bound successfully"
    }
```

**Device Binding Workflow:**

1. **First Login on New Device**:
   ```
   User logs in → App sends device ID in header
   → Backend checks device ID not in permitted_devices
   → Auto-bind device to user's email
   → User can access system
   ```

2. **Subsequent Logins**:
   ```
   User logs in → App sends device ID
   → Backend checks device ID exists in permitted_devices
   → Verify email matches bound email
   → Update lastAccess timestamp
   → Allow access
   ```

3. **Login from Different Device**:
   ```
   User logs in on Device B → Send device ID B
   → Backend finds device B already bound to User X
   → Current user is User Y (different)
   → Return 403 with "Device already bound" error
   → User must contact admin to unbind old device
   ```

**Admin Unbind Functionality:**

```python
@app.route('/api/admin/unbind-device', methods=['POST'])
@requires_google_auth
@requires_admin
def unbind_device():
    """Admin unbind device để user có thể dùng device mới"""
    device_id = request.json.get('deviceId')
    
    permitted_devices = load_permitted_devices()
    
    if device_id not in permitted_devices:
        return jsonify({
            'success': False,
            'error': 'NOT_FOUND',
            'message': 'Device not found'
        }), 404
    
    # Remove binding
    old_binding = permitted_devices.pop(device_id)
    save_permitted_devices(permitted_devices)
    
    return jsonify({
        'success': True,
        'message': f"Device unbound from {old_binding['email']}"
    })
```

**Security Benefits:**

- **Prevent Account Sharing**: Một account chỉ dùng được trên một thiết bị, không thể chia sẻ cho nhiều người
- **Device Tracking**: Log tất cả devices đã truy cập với timestamp để audit
- **Lost Device Protection**: Nếu device mất, admin unbind để user dùng device mới
- **Stolen Account Detection**: Nếu attacker có token nhưng dùng device khác, request bị từ chối

**Limitations & Solutions:**

- **Factory Reset**: Device ID thay đổi sau factory reset → user phải contact admin unbind
- **Multiple Devices**: Hiện tại chỉ support 1 device/user → có thể mở rộng thành array of devices với limit
- **Tablet + Phone**: User muốn dùng cả tablet và phone → admin có thể whitelist cả 2 device IDs

### 3.3.6. Thiết Kế Xử Lý Lỗi

Error handling toàn diện đảm bảo hệ thống robust và dễ debug. Mỗi lỗi có code riêng, message rõ ràng, và action phù hợp.

#### 3.3.6.1. Error Codes

Bảng error codes chuẩn hóa giúp client và server communicate về lỗi một cách consistent:

| Code | Category | Description | HTTP Status | Action |
|------|----------|-------------|-------------|--------|
| **AUTH-001** | Authentication | Invalid or expired token | 401 | Redirect to login, refresh token |
| **AUTH-002** | Authorization | User not in whitelist | 403 | Show "Access denied" dialog |
| **AUTH-003** | Device Binding | Device bound to another user | 403 | Show "Contact admin" message |
| **CAM-001** | Camera | Camera hardware not found | 500 | Check camera connection |
| **CAM-002** | Camera | Stream connection timeout | 504 | Retry connection with backoff |
| **AI-001** | Detection | Model not loaded | 500 | Restart backend service |
| **AI-002** | Detection | Detection failed/timeout | 500 | Retry or skip, log error |
| **STORAGE-001** | Cloudinary | Image upload failed | 500 | Retry upload, show error to user |
| **DB-001** | MongoDB | Database connection failed | 500 | Check internet, retry |
| **DB-002** | MongoDB | Query/Save failed | 500 | Retry with exponential backoff |
| **NETWORK-001** | Network | No internet connection | 0 | Show offline message, queue request |
| **VALIDATION-001** | Validation | Invalid request data | 400 | Show validation error to user |

**Error Code Structure:**

- **Category prefix**: AUTH, CAM, AI, STORAGE, DB, NETWORK, VALIDATION
- **Numeric suffix**: Sequential number trong category
- **Benefits**: Dễ search logs, categorize errors, identify root cause nhanh

#### 3.3.6.2. Error Handling Strategy

**Backend Error Handler:**

Flask global error handler catch tất cả exceptions và format consistent response:

```python
@app.errorhandler(Exception)
def handle_exception(e):
    """Global exception handler"""
    # Log full traceback for debugging
    logger.error(f"Unhandled exception: {str(e)}", exc_info=True)
    
    # Classify error type
    if isinstance(e, AuthenticationError):
        return jsonify({
            "success": False,
            "error": "AUTH-001",
            "message": "Authentication failed",
            "details": str(e)
        }), 401
    
    elif isinstance(e, PermissionError):
        return jsonify({
            "success": False,
            "error": "AUTH-002",
            "message": "Access denied. Contact admin for access.",
            "email": getattr(e, 'email', None)
        }), 403
    
    elif isinstance(e, cv2.error):  # OpenCV errors
        return jsonify({
            "success": False,
            "error": "CAM-001",
            "message": "Camera error",
            "details": str(e)
        }), 500
    
    elif isinstance(e, CloudinaryError):
        return jsonify({
            "success": False,
            "error": "STORAGE-001",
            "message": "Failed to upload image",
            "retryable": True
        }), 500
    
    elif isinstance(e, PyMongoError):
        return jsonify({
            "success": False,
            "error": "DB-001",
            "message": "Database error",
            "retryable": True
        }), 500
    
    # Default unknown error
    return jsonify({
        "success": False,
        "error": "UNKNOWN",
        "message": "An unexpected error occurred",
        "details": str(e) if app.debug else "Internal server error"
    }), 500
```

**Giải thích:**

- **Logging**: Mọi error đều được log với full traceback để dev có thể debug. Production logs gửi đến logging service như Sentry hoặc CloudWatch.

- **Consistent Format**: Mọi error response đều có format: `{success: false, error: "CODE", message: "...", ...}` giúp client parse dễ dàng.

- **Security**: Trong production mode, không expose internal details (stack trace, SQL queries) để tránh information leakage. Chỉ show generic message.

- **Retryable Flag**: Một số errors có thể retry (network, temporary DB issues), flag này cho client biết có nên retry không.

**Android Error Handler:**

Client-side error handling với specific actions cho mỗi error code:

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val code: String,
        val message: String,
        val httpCode: Int,
        val retryable: Boolean = false
    ) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

class ErrorHandler(
    private val context: Context,
    private val navController: NavController
) {
    fun handleError(error: ApiResult.Error) {
        when (error.code) {
            "AUTH-001" -> {
                // Token expired or invalid
                showDialog(
                    title = "Phiên đăng nhập hết hạn",
                    message = "Vui lòng đăng nhập lại",
                    onConfirm = {
                        TokenManager.clearToken(context)
                        navController.navigate(Route.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            "AUTH-002" -> {
                // Not in whitelist
                showDialog(
                    title = "Không có quyền truy cập",
                    message = "Tài khoản của bạn chưa được cấp quyền. Vui lòng liên hệ admin.",
                    onConfirm = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Route.Login.route)
                    }
                )
            }
            
            "AUTH-003" -> {
                // Device bound to another user
                showDialog(
                    title = "Thiết bị đã được liên kết",
                    message = "Thiết bị này đã được liên kết với tài khoản khác. Vui lòng liên hệ admin để gỡ liên kết.",
                    actionButton = "Liên hệ Admin"
                )
            }
            
            "CAM-002" -> {
                // Stream timeout
                showSnackbar(
                    message = "Mất kết nối camera. Đang thử kết nối lại...",
                    duration = SnackbarDuration.Short
                )
                // Auto retry with exponential backoff
                retryWithBackoff { connectToStream() }
            }
            
            "STORAGE-001", "DB-001", "DB-002" -> {
                // Retryable errors
                if (error.retryable) {
                    showSnackbar(
                        message = "${error.message}. Đang thử lại...",
                        action = "Thử lại" to { retryLastRequest() }
                    )
                    retryWithBackoff { retryLastRequest() }
                } else {
                    showDialog(
                        title = "Lỗi",
                        message = error.message
                    )
                }
            }
            
            "NETWORK-001" -> {
                // No internet
                showSnackbar(
                    message = "Không có kết nối internet",
                    duration = SnackbarDuration.Indefinite,
                    action = "Thử lại" to { checkInternetAndRetry() }
                )
            }
            
            else -> {
                // Generic error
                showSnackbar(
                    message = error.message.ifEmpty { "Đã xảy ra lỗi" },
                    action = if (error.retryable) "Thử lại" to { retryLastRequest() } else null
                )
            }
        }
    }
    
    private suspend fun retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> Unit
    ) {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                block()
                return  // Success, stop retrying
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e  // Last attempt, throw error
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
    }
}
```

**Giải thích Strategy:**

1. **User-Friendly Messages**: Error messages được translate sang tiếng Việt và dễ hiểu cho end-users, không expose technical terms.

2. **Context-Specific Actions**: Mỗi error type có action phù hợp:
   - Auth errors → Navigate to login
   - Network errors → Show retry button
   - Permission errors → Show contact admin message

3. **Automatic Retry**: Errors có thể retry (network, temporary) được auto retry với exponential backoff (1s, 2s, 4s...) để không spam server.

4. **Graceful Degradation**: Khi một feature fail, app vẫn functional cho các features khác. Ví dụ: camera stream fail nhưng gallery vẫn hoạt động.

5. **Logging & Analytics**: Mọi errors được log và gửi lên analytics (Firebase Crashlytics) để track error rates và prioritize fixes.

**Error Recovery Examples:**

```kotlin
// Example 1: Retry detection with fallback
suspend fun detectShrimpWithRetry(bitmap: Bitmap): ApiResult<DetectionResult> {
    return withRetry(maxAttempts = 3) {
        try {
            apiService.detectShrimp(bitmap)
        } catch (e: IOException) {
            // Network error, can retry
            throw e
        } catch (e: HttpException) {
            if (e.code() >= 500) {
                // Server error, can retry
                throw e
            } else {
                // Client error (4xx), don't retry
                ApiResult.Error(
                    code = "VALIDATION-001",
                    message = e.message(),
                    httpCode = e.code()
                )
            }
        }
    }
}

// Example 2: Offline mode với queue
class OfflineQueueManager {
    private val pendingRequests = mutableListOf<PendingRequest>()
    
    fun enqueueRequest(request: PendingRequest) {
        pendingRequests.add(request)
        saveToLocalDb(request)
    }
    
    suspend fun processPendingRequests() {
        if (!isOnline()) return
        
        pendingRequests.forEach { request ->
            try {
                executeRequest(request)
                removeFromLocalDb(request)
            } catch (e: Exception) {
                // Keep in queue, will retry later
            }
        }
    }
}
```

**Benefits of Comprehensive Error Handling:**

- **Better UX**: Users biết chính xác vấn đề gì và phải làm gì
- **Reduced Support Load**: Clear error messages giảm số lượng support requests
- **Faster Debugging**: Error codes và logs giúp dev identify issues nhanh
- **Reliability**: Auto retry và graceful degradation tăng uptime
- **Monitoring**: Centralized error tracking giúp proactive fix issues

---

**Tóm tắt Mục 3.3:**

Phần thiết kế chi tiết đã trình bày đầy đủ về:

1. **Database Design**: MongoDB schema với indexes tối ưu, Cloudinary storage structure organized theo thời gian
2. **API Design**: RESTful endpoints với authentication, authorization, và consistent error responses
3. **AI Module**: Pipeline xử lý ảnh từ preprocessing → inference → postprocessing → visualization
4. **UI Design**: Jetpack Compose components với state management, navigation, và các màn hình chính (Camera, Gallery, Chart)
5. **Security Design**: Multi-layer authentication, token management, role-based permissions, device binding
6. **Error Handling**: Standardized error codes, comprehensive error handlers, retry strategies, và user-friendly messages

Thiết kế này đảm bảo hệ thống scalable, maintainable, secure, và user-friendly, sẵn sàng cho implementation và deployment.

---

## 3.4. THIẾT KẾ TRIỂN KHAI

### 3.4.1. Yêu Cầu Phần Cứng

#### 3.4.1.1. Raspberry Pi Server

| Component | Specification | Reason |
|-----------|--------------|--------|
| **Model** | Raspberry Pi 4 Model B (4GB/8GB) | Đủ mạnh cho TFLite inference |
| **CPU** | Quad-core ARM Cortex-A72 @ 1.5GHz | Xử lý đa luồng tốt |
| **RAM** | 4GB hoặc 8GB | Chạy Flask + AI model |
| **Storage** | MicroSD 32GB Class 10 | Lưu OS + model + logs |
| **Camera** | USB Webcam hoặc Pi Camera Module | 640x480 @ 30fps |
| **Network** | Gigabit Ethernet hoặc WiFi 5 | Streaming ổn định |
| **Power** | 5V 3A USB-C | Đủ công suất |

#### 3.4.1.2. Android Device

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| **OS** | Android 6.0 (API 23) | Android 10+ |
| **RAM** | 2GB | 4GB+ |
| **Storage** | 100MB free | 500MB+ |
| **Screen** | 5" HD (720p) | 6" FHD+ (1080p) |
| **Network** | WiFi 4 | WiFi 5/6 |

### 3.4.2. Yêu Cầu Phần Mềm

#### 3.4.2.1. Backend (Raspberry Pi)

```bash
# Operating System
Raspberry Pi OS (Debian 11 Bullseye) - 64-bit

# Python
Python 3.9+

# Libraries (requirements.txt)
Flask==2.3.0
flask-cors==4.0.0
opencv-python==4.8.0
numpy==1.24.0
Pillow==10.0.0
tflite-runtime==2.13.0  # hoặc tensorflow-lite
cloudinary==1.34.0
pymongo==4.5.0
firebase-admin==6.2.0
python-dotenv==1.0.0
```

#### 3.4.2.2. Android App

```kotlin
// Build Configuration
compileSdk = 34
minSdk = 26
targetSdk = 34

// Kotlin
kotlin = "1.9.0"

// Jetpack Compose
androidx.compose = "1.5.0"
androidx.compose.material3 = "1.1.1"

// Firebase
firebase-auth = "22.1.2"
firebase-firestore = "24.8.1"

// Network
okhttp = "4.11.0"
coil = "2.4.0"

// Serialization
kotlinx-serialization = "1.6.0"

// Dependency Injection
hilt = "2.48"
```

### 3.4.3. Cấu Hình Hệ Thống

#### 3.4.3.1. Environment Variables (.env)

```bash
# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# MongoDB Configuration
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/
MONGODB_DATABASE=shrimp_db

# Firebase Configuration
FIREBASE_CRED_PATH=firebase-adminsdk.json

# Admin Configuration
ADMIN_EMAIL=admin@example.com
ADMIN_PHONE=+84987654321

# Camera Configuration
CAMERA_USERNAME=admin
CAMERA_PASSWORD=secure_password

# AI Model Configuration
YOLO_MODEL_PATH=models/best-fp16(1).tflite
CONFIDENCE_THRESHOLD=0.5

# Server Configuration
FLASK_HOST=0.0.0.0
FLASK_PORT=8000
FLASK_DEBUG=False

# Timezone
TZ=Asia/Ho_Chi_Minh
```

#### 3.4.3.2. Systemd Service (Auto-start)

```ini
# /etc/systemd/system/shrimp-backend.service
[Unit]
Description=Shrimp Detection Backend Server
After=network.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/backend
Environment="PATH=/home/pi/.local/bin:/usr/local/bin:/usr/bin:/bin"
ExecStart=/usr/bin/python3 /home/pi/backend/app_complete.py
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Enable service:**
```bash
sudo systemctl enable shrimp-backend.service
sudo systemctl start shrimp-backend.service
sudo systemctl status shrimp-backend.service
```

### 3.4.4. Quy Trình Triển Khai

#### 3.4.4.1. Setup Backend

```bash
# 1. Update system
sudo apt update && sudo apt upgrade -y

# 2. Install Python dependencies
cd /home/pi/backend
pip3 install -r requirements.txt

# 3. Setup environment
cp .env.example .env
nano .env  # Edit configuration

# 4. Test camera
python3 test_gpio.py

# 5. Test model
python3 test_independent_devices.py

# 6. Start server
python3 app_complete.py

# 7. Setup auto-start
sudo cp shrimp-backend.service /etc/systemd/system/
sudo systemctl enable shrimp-backend.service
sudo systemctl start shrimp-backend.service
```

#### 3.4.4.2. Build Android App

```bash
# 1. Clone project
git clone <repository-url>
cd MyAppshrimp

# 2. Configure backend URL
# Edit: app/src/main/java/com/dung/myapplication/models/Config.kt
# Change BACKEND_URL to your Raspberry Pi IP

# 3. Build APK
./gradlew assembleDebug

# 4. Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or build release version
./gradlew assembleRelease
```

### 3.4.5. Monitoring và Logging

#### 3.4.5.1. Backend Logging

```python
# Logging configuration
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('app.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)
```

**Log locations:**
- Application logs: `/home/pi/backend/app.log`
- System logs: `sudo journalctl -u shrimp-backend -f`

#### 3.4.5.2. Performance Metrics

```python
# Track metrics
metrics = {
    "requests_total": 0,
    "requests_success": 0,
    "requests_failed": 0,
    "detection_avg_time": 0,
    "upload_avg_time": 0,
    "camera_uptime": 0
}

@app.route('/metrics')
def get_metrics():
    return jsonify(metrics)
```

---

## 3.5. KẾT LUẬN CHƯƠNG 3

### 3.5.1. Tóm Tắt Thiết Kế

Chương 3 đã trình bày chi tiết về phân tích và thiết kế hệ thống nhận diện tôm, bao gồm:

1. **Phân tích yêu cầu**: Xác định 23 yêu cầu chức năng và 22 yêu cầu phi chức năng
2. **Kiến trúc hệ thống**: Thiết kế kiến trúc 3 tầng (Client-Server-Data)
3. **Thiết kế chi tiết**: 
   - Database schema (MongoDB)
   - API endpoints (RESTful)
   - AI detection pipeline (TensorFlow Lite)
   - Mobile UI/UX (Android)
4. **Bảo mật**: Authentication, Authorization, Device Binding
5. **Triển khai**: Hardware, Software, Configuration

### 3.5.2. Điểm Mạnh Của Thiết Kế

| Aspect | Strength                                   |
|--------|--------------------------------------------|
| **Kiến trúc** | Modular, dễ mở rộng và bảo trì             |
| **Hiệu năng** | TFLite tối ưu cho edge device              |
| **Bảo mật** | Multi-layer authentication & authorization |
| **Trải nghiệm** | UI/UX hiện đại với Jetpack Compose         |
| **Chi phí** | Sử dụng cloud services miễn phí/giá rẻ     |
| **Khả năng mở rộng** |                                            |

### 3.5.3. Hướng Phát Triển

Thiết kế hiện tại đã đáp ứng đầy đủ yêu cầu cơ bản và có thể mở rộng cho các tính năng nâng cao:
- Nhận diện thêm các loại thủy sản khác
- Tích hợp IoT sensors (nhiệt độ, pH, oxy hòa tan)
- Hệ thống cảnh báo thông minh
- Dashboard quản lý ao nuôi toàn diện
- Machine learning để dự đoán sản lượng

---

**Chương tiếp theo sẽ trình bày chi tiết về cài đặt và hiện thực hóa hệ thống.**

