# CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG

## 3.1 PHÂN TÍCH YÊU CẦU CỦA HỆ THỐNG

### 3.1.1 Về chức năng hệ thống

#### 3.1.1.1 Chức năng xác thực và quản lý người dùng

Hệ thống triển khai cơ chế xác thực đa phương thức thông qua Firebase Authentication, một dịch vụ xác thực được cung cấp bởi Google Cloud Platform. Firebase Authentication hỗ trợ nhiều phương thức đăng nhập khác nhau, trong đó hệ thống này tập trung vào hai phương thức chính: đăng nhập bằng tài khoản Google (OAuth 2.0) và xác thực số điện thoại (Phone Authentication với OTP).

Đối với phương thức Google Sign-In, người dùng chỉ cần nhấn vào nút "Sign in with Google" trên màn hình đăng nhập của ứng dụng Android. Hệ thống sẽ mở Google Account Chooser, cho phép người dùng chọn tài khoản Google đã đăng nhập trên thiết bị hoặc đăng nhập tài khoản mới. Sau khi người dùng chọn tài khoản và cấp quyền, Firebase SDK tự động xử lý toàn bộ quy trình OAuth, bao gồm việc trao đổi authorization code và nhận access token từ Google servers.

Phương thức Phone Authentication hoạt động theo cơ chế gửi mã OTP (One-Time Password) qua tin nhắn SMS đến số điện thoại của người dùng. Người dùng nhập số điện thoại ở định dạng quốc tế (ví dụ: +84987648717), Firebase gửi mã OTP 6 chữ số, người dùng nhập mã này vào ứng dụng để xác thực. Firebase tự động verify mã OTP với backend của họ và trả về kết quả xác thực cho ứng dụng.

Sau khi xác thực thành công bằng bất kỳ phương thức nào, Firebase Authentication sẽ tạo một User object chứa thông tin người dùng (uid, email hoặc phoneNumber, displayName, photoURL) và quan trọng nhất là ID Token. ID Token này là một JWT (JSON Web Token) có chữ ký số của Firebase, chứa claims về người dùng như user_id, email, phone_number và thời gian hết hạn (mặc định 1 giờ). Token được mã hóa và ký bằng private key của Firebase, đảm bảo không thể giả mạo.

Ứng dụng Android lưu ID Token này vào SharedPreferences với MODE_PRIVATE để bảo mật. Khi gọi bất kỳ API nào đến backend, ứng dụng sẽ đính kèm token này vào HTTP header với format `Authorization: <token>` (đối với Google login) hoặc `X-Phone-Auth: <phone_number>` (đối với Phone login). Backend nhận request, extract token từ header và thực hiện verification.

Quá trình verification trên backend được thực hiện bởi Firebase Admin SDK. Backend gọi hàm `firebase_auth.verify_id_token(token)` để verify chữ ký số và decode token. Firebase Admin SDK tự động tải public key từ Google servers, verify chữ ký RSA, kiểm tra thời gian hết hạn và các claims khác. Nếu token hợp lệ, hàm trả về decoded token chứa thông tin người dùng. Nếu token không hợp lệ (giả mạo, hết hạn hoặc bị revoke), hàm throw exception và backend trả về 401 Unauthorized.

Sau khi verify token thành công, backend extract email hoặc số điện thoại từ decoded token. Tiếp theo, backend kiểm tra email/phone này có nằm trong danh sách whitelist hay không. Whitelist được lưu trong hai file JSON: `permitted_emails.json` chứa danh sách email được phép và `permitted_phones.json` chứa danh sách số điện thoại được phép. Cấu trúc file JSON đơn giản là một array các string:

```json
// permitted_emails.json
[
  "hodung15032003@gmail.com",
  "user1@example.com",
  "user2@example.com"
]

// permitted_phones.json
[
  "+84987648717",
  "+84901234567",
  "+84912345678"
]
```

Nếu email hoặc số điện thoại của người dùng không có trong whitelist, backend trả về response 403 Forbidden với message "Email not permitted" hoặc "Phone number not permitted". Ứng dụng Android hiển thị dialog thông báo cho người dùng biết rằng tài khoản chưa được cấp quyền và cần liên hệ Admin để được thêm vào danh sách.

Hệ thống phân quyền dựa trên role-based access control (RBAC) với hai vai trò chính: Admin và User thường. Admin được xác định dựa trên environment variable ADMIN_EMAIL và ADMIN_PHONE được set trong file `.env` trên server. Khi backend kiểm tra quyền, nó so sánh email hoặc phone của người dùng với giá trị ADMIN_EMAIL/ADMIN_PHONE. Nếu khớp, người dùng được coi là Admin và có full access vào tất cả tính năng.

Admin có các quyền đặc biệt bao gồm:
1. Truy cập Admin Panel trong ứng dụng để quản lý người dùng
2. Thêm email hoặc số điện thoại mới vào whitelist qua API POST `/api/admin/add-email` và `/api/admin/add-phone`
3. Xóa email hoặc số điện thoại khỏi whitelist qua API POST `/api/admin/remove-email` và `/api/admin/remove-phone`
4. Xem danh sách tất cả người dùng được phép qua API GET `/api/admin/list-emails` và `/api/admin/list-phones`
5. Truy cập vào tất cả các thiết bị trong hệ thống
6. Điều khiển GPIO và các thiết bị IoT
7. Xóa bất kỳ ảnh nào trong gallery

User thường chỉ có các quyền cơ bản:
1. Xem camera stream của thiết bị mà họ đã bind
2. Chụp ảnh và sử dụng AI để nhận diện tôm
3. Xem gallery các ảnh mà họ đã chụp
4. Xem thống kê và biểu đồ
5. Quản lý (bind/unbind) thiết bị của riêng mình
6. Cấu hình WiFi cho thiết bị của mình
7. Xem và chỉnh sửa profile cá nhân

Mỗi người dùng có một màn hình Profile riêng trong ứng dụng, hiển thị đầy đủ thông tin cá nhân bao gồm:
- Avatar (lấy từ Google account hoặc placeholder nếu dùng phone login)
- Tên hiển thị (displayName từ Firebase hoặc số điện thoại)
- Email hoặc số điện thoại đã đăng ký
- Trạng thái kết nối thiết bị (Device ID, IP address nếu đã bind)
- Nút "Scan Devices" để quét thiết bị mới
- Nút "Unbind Device" nếu đã bind thiết bị
- Nút "WiFi Configuration" để cấu hình mạng cho Raspberry Pi
- Nút "Logout" để đăng xuất

Đối với Admin, màn hình Profile còn hiển thị thêm Admin Panel với các chức năng:
- Tab "Permitted Emails" hiển thị danh sách email được phép, có nút Add và Delete cho mỗi email
- Tab "Permitted Phones" hiển thị danh sách số điện thoại được phép, có nút Add và Delete
- Form nhập email/phone mới với validation (kiểm tra format email hợp lệ, số điện thoại bắt đầu bằng +)
- Confirmation dialog trước khi xóa để tránh xóa nhầm

Backend implement decorator pattern để protect các endpoint. Decorator `@requires_google_auth` được wrap quanh mọi API endpoint cần authentication. Decorator này thực hiện các bước sau:
1. Extract token từ `request.headers.get('Authorization')` hoặc phone từ `request.headers.get('X-Phone-Auth')`
2. Nếu có phone, check trong permitted_phones.json; nếu có token, verify bằng Firebase Admin SDK
3. Extract email hoặc phone từ token đã decode
4. Check email/phone trong whitelist
5. Nếu pass tất cả checks, set `request.user_email` và cho phép request tiếp tục
6. Nếu fail bất kỳ check nào, return 401 hoặc 403 với error message

Decorator cho phép code reuse và giảm duplicate code, thay vì phải copy-paste logic authentication vào mỗi endpoint. Ví dụ:

```python
@app.route('/api/detect-shrimp', methods=['POST'])
@requires_google_auth
def detect_shrimp():
    # request.user_email đã available ở đây, đã được verify
    email = request.user_email
    logger.info(f"User {email} detecting shrimp")
    # ... xử lý detection
```

Token có thời gian sống (TTL - Time To Live) là 1 giờ. Sau 1 giờ, token hết hạn và backend sẽ reject request với lỗi "Token expired". Ứng dụng Android tự động detect lỗi này và gọi `user.getIdToken(true)` để force refresh token mới từ Firebase. Firebase SDK tự động gửi refresh token (có TTL dài hơn, thường vài tháng) lên server để đổi lấy ID token mới. Quá trình này transparent với người dùng, không cần đăng nhập lại.

Khi người dùng logout, ứng dụng Android thực hiện các bước:
1. Gọi `FirebaseAuth.getInstance().signOut()` để sign out khỏi Firebase
2. Clear ID Token và refresh token khỏi SharedPreferences
3. Clear tất cả user data khỏi cache
4. Navigate về LoginScreen
5. Backend không cần endpoint logout vì sử dụng stateless authentication, mỗi request độc lập

Hệ thống cũng xử lý các edge cases như:
- User bị admin xóa khỏi whitelist trong khi đang sử dụng app: Request tiếp theo sẽ bị reject 403, app hiển thị dialog "Account access revoked, please contact admin"
- Firebase service down: App fallback sang cached data và hiển thị offline mode
- Token bị compromised: Admin có thể revoke token từ Firebase Console, user phải đăng nhập lại
- Multiple devices cùng login: Mỗi device có token riêng, logout trên device này không ảnh hưởng device khác

#### 3.1.1.2 Chức năng quản lý thiết bị độc lập

Hệ thống áp dụng mô hình "Independent Device Binding", một kiến trúc độc đáo cho phép mỗi người dùng độc lập kết nối và quản lý thiết bị Raspberry Pi của riêng mình mà không ảnh hưởng đến các người dùng khác. Đây là một cải tiến quan trọng so với mô hình traditional shared device, giải quyết vấn đề xung đột khi nhiều người dùng cùng muốn sử dụng một thiết bị.

Trong mô hình cũ (shared device), một thiết bị chỉ có thể được bind với một người dùng tại một thời điểm. Data structure lưu dạng `{device_id: owner_email}`, nghĩa là device_id "raspberrypi-001" chỉ thuộc về một email duy nhất. Khi user khác muốn kết nối cùng thiết bị, họ phải chờ owner hiện tại unbind trước. Điều này gây bất tiện trong môi trường nhiều người dùng như trang trại nuôi tôm có nhiều nhân viên giám sát.

Mô hình mới (independent binding) đảo ngược data structure thành `{user_email: {device_id, ip, last_updated}}`. Mỗi user có một entry riêng trong dictionary, chứa thông tin thiết bị mà user đó đã bind. Nhiều user có thể có entry với cùng device_id, nghĩa là nhiều người cùng kết nối đến một thiết bị vật lý. Khi user A unbind thiết bị, chỉ entry của A bị xóa, entry của user B, C vẫn còn nguyên.

Ví dụ cụ thể về data structure trong `permitted_devices.json`:

```json
{
  "hodung15032003@gmail.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1704067200
  },
  "user1@example.com": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1704067300
  },
  "user2@example.com": {
    "device_id": "raspberrypi-002",
    "ip": "192.168.1.101",
    "last_updated": 1704067400
  },
  "+84987648717": {
    "device_id": "raspberrypi-001",
    "ip": "192.168.1.100",
    "last_updated": 1704067500
  }
}
```

Trong ví dụ này, admin và user1 cùng bind vào raspberrypi-001, user2 bind vào raspberrypi-002, và một phone user cũng bind vào raspberrypi-001. Tất cả đều độc lập, không ai ảnh hưởng đến ai.

Quá trình device discovery được thực hiện thông qua giao thức UDP broadcast. UDP (User Datagram Protocol) là connectionless protocol, phù hợp cho discovery vì không cần establish connection trước. Broadcast cho phép gửi message đến tất cả devices trong cùng subnet mà không cần biết trước IP của chúng.

Chi tiết protocol:
1. Raspberry Pi khởi động background thread chạy UDP server lắng nghe trên port 37020
2. Server bind đến address 0.0.0.0:37020 để nhận broadcast packets
3. Khi nhận được packet chứa string "DISCOVER_SHRIMP_DEVICE", server parse request
4. Server gửi reply packet chứa format "SHRIMP_DEVICE:{device_id}:{local_ip}"
5. Device_id được lấy từ hostname của Raspberry Pi hoặc config file
6. Local_ip được lấy bằng cách query network interface

Ở phía Android app:
1. User nhấn nút "Scan Devices" trong Profile screen
2. App tạo DatagramSocket và enable broadcast option
3. App tạo DatagramPacket chứa string "DISCOVER_SHRIMP_DEVICE"
4. App gửi packet đến broadcast address 255.255.255.255:37020
5. App set socket timeout 5 giây và loop receive packets
6. Với mỗi packet nhận được, parse format "SHRIMP_DEVICE:{device_id}:{ip}"
7. Add device vào discovered list, deduplicate nếu nhận duplicate reply
8. Hiển thị list devices trong dialog cho user chọn

Code minh họa trong Kotlin:

```kotlin
suspend fun discoverDevices(): List<Device> = withContext(Dispatchers.IO) {
    val devices = mutableListOf<Device>()
    val socket = DatagramSocket()
    socket.broadcast = true
    socket.soTimeout = 5000
    
    val sendData = "DISCOVER_SHRIMP_DEVICE".toByteArray()
    val sendPacket = DatagramPacket(
        sendData, sendData.size,
        InetAddress.getByName("255.255.255.255"), 37020
    )
    socket.send(sendPacket)
    
    val receiveBuffer = ByteArray(1024)
    while (true) {
        try {
            val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
            socket.receive(receivePacket)
            val response = String(receivePacket.data, 0, receivePacket.length)
            if (response.startsWith("SHRIMP_DEVICE:")) {
                val parts = response.split(":")
                devices.add(Device(parts[1], parts[2]))
            }
        } catch (e: SocketTimeoutException) {
            break
        }
    }
    socket.close()
    devices.distinctBy { it.deviceId }
}
```

Sau khi user chọn device từ danh sách discovered, quá trình binding diễn ra:

1. App gọi API POST `/api/devices/bind` với request body:
```json
{
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

2. Request header chứa Firebase ID Token: `Authorization: <token>`

3. Backend receive request, decorator `@requires_google_auth` verify token và extract email

4. Backend load `permitted_devices.json` vào memory as Python dict

5. Backend check xem `email` đã có trong dict chưa:
   - Nếu chưa có: Tạo entry mới `{email: {device_id, ip, timestamp}}`
   - Nếu đã có với cùng device_id: Update IP và timestamp (case device đổi IP)
   - Nếu đã có với device_id khác: Overwrite (user đang switch sang device khác)

6. Backend save dict về `permitted_devices.json` với `json.dump()`

7. Backend return success response:
```json
{
  "success": true,
  "message": "Device bound successfully",
  "device_id": "raspberrypi-001",
  "device_ip": "192.168.1.100"
}
```

8. App nhận response, save device info vào SharedPreferences:
```kotlin
prefs.edit()
    .putString("rasp_ip", deviceIp)
    .putString("rasp_device_id", deviceId)
    .apply()
```

9. App navigate về HomeScreen, HomeScreen detect có device_ip và bắt đầu load camera stream

Backend cũng implement auto-migration từ old format sang new format. Khi hệ thống upgrade từ version cũ, file `permitted_devices.json` có thể còn format cũ `{device_id: email}`. Function `load_permitted_devices()` detect old format bằng cách check value type:

```python
def load_permitted_devices():
    with open(PERMITTED_DEVICES_PATH, 'r') as f:
        data = json.load(f)
    
    # Detect old format
    needs_migration = False
    for key, value in data.items():
        if isinstance(value, str):  # Old: {device_id: email}
            needs_migration = True
            break
    
    if needs_migration:
        # Migrate to new format
        new_data = {}
        for device_id, email in data.items():
            new_data[email] = {
                'device_id': device_id,
                'ip': None,  # IP unknown in old format
                'last_updated': time.time()
            }
        save_permitted_devices(new_data)
        return new_data
    
    return data
```

Migration này đảm bảo backward compatibility, không làm mất dữ liệu khi upgrade system.

Unbind process ngược lại với bind:

1. User nhấn "Unbind Device" trong Profile screen
2. App show confirmation dialog "Are you sure you want to unbind this device?"
3. Nếu user confirm, app gọi API POST `/api/devices/unbind`:
```json
{
  "device_id": "raspberrypi-001"
}
```

4. Backend verify user có bind device này không bằng cách check:
```python
if email not in permitted_devices:
    return {"error": "You don't have any device bound"}, 404

user_device = permitted_devices[email]
if user_device['device_id'] != device_id:
    return {"error": "Device mismatch"}, 400
```

5. Backend xóa entry của user: `del permitted_devices[email]`

6. Backend save lại file và return success

7. App clear SharedPreferences và update UI

Điểm quan trọng: Chỉ entry của user này bị xóa, entries của users khác với cùng device_id vẫn còn. Họ vẫn có thể sử dụng device bình thường.

Device status check được thực hiện khi app launch. HomeScreen có `LaunchedEffect(Unit)` block chạy một lần khi screen mount:

```kotlin
LaunchedEffect(Unit) {
    // Check local SharedPreferences first
    val localDeviceId = prefs.getString("rasp_device_id", null)
    val localIp = prefs.getString("rasp_ip", null)
    
    if (localDeviceId != null && localIp != null) {
        // User đã bind device trước đó, use local data
        raspDeviceId = localDeviceId
        raspIp = localIp
    } else {
        // No local data, check backend
        val response = apiService.getMyDevice()
        if (response.bound) {
            // Backend có data, sync xuống local
            raspDeviceId = response.device_id
            raspIp = response.device_ip
            // Save to SharedPreferences
            prefs.edit()
                .putString("rasp_device_id", response.device_id)
                .putString("rasp_ip", response.device_ip)
                .apply()
        } else {
            // User chưa bind device, show message
            showMessage("Please bind a device in Profile screen")
        }
    }
}
```

Backend endpoint `/api/devices/my-device` implement như sau:

```python
@app.route('/api/devices/my-device', methods=['GET'])
@requires_google_auth
def get_my_device():
    email = request.user_email
    permitted_devices = load_permitted_devices()
    
    if email in permitted_devices:
        device_info = permitted_devices[email]
        return {
            "success": True,
            "bound": True,
            "device_id": device_info['device_id'],
            "device_ip": device_info['ip']
        }
    else:
        return {
            "success": True,
            "bound": False,
            "message": "No device bound to this account"
        }
```

Hệ thống cũng hỗ trợ remote WiFi configuration cho Raspberry Pi. Khi user nhấn "WiFi Configuration" trong Profile, app hiển thị form:
- Dropdown chọn WiFi network (scan các SSID available)
- TextField nhập WiFi password
- Button "Connect"

App gọi API POST `/api/wifi/configure` với:
```json
{
  "ssid": "HomeWiFi",
  "password": "mypassword123",
  "device_id": "raspberrypi-001"
}
```

Backend verify user có quyền configure device này (phải đã bind), sau đó:
1. Write WiFi credentials vào `/etc/wpa_supplicant/wpa_supplicant.conf`
2. Restart networking service: `sudo systemctl restart networking`
3. Wait 10 giây để Raspberry Pi connect WiFi mới
4. Get new IP address và update vào permitted_devices.json
5. Return new IP cho app

Tính năng này rất hữu ích khi deploy Raspberry Pi ở ao nuôi xa, không cần phải truy cập vật lý để đổi WiFi.

#### 3.1.1.3 Chức năng truyền hình camera trực tiếp

Hệ thống cung cấp chức năng streaming camera real-time với độ trễ thấp sử dụng giao thức MJPEG (Motion JPEG). Backend sử dụng OpenCV để capture frames từ camera USB hoặc CSI camera trên Raspberry Pi, với tốc độ 30 frames mỗi giây. Mỗi frame được encode thành định dạng JPEG với chất lượng 80% để cân bằng giữa chất lượng hình ảnh và băng thông.

Camera stream được bảo vệ bởi cơ chế xác thực, yêu cầu người dùng phải có token hợp lệ để truy cập. Endpoint `/blynk_feed` trả về multipart/x-mixed-replace response, cho phép browser và ứng dụng Android hiển thị video liên tục. Hệ thống tối ưu hóa latency bằng cách thiết lập buffer size = 1 và tắt buffering của nginx thông qua header `X-Accel-Buffering: no`.

Ứng dụng Android sử dụng OkHttp client để kết nối đến camera stream, đọc dữ liệu theo từng frame và decode thành Bitmap để hiển thị trong Compose UI. Hệ thống tự động xử lý lỗi kết nối và hiển thị thông báo khi không thể truy cập camera. Camera stream hoạt động độc lập với việc device binding, người dùng có quyền truy cập vào bất kỳ thiết bị nào họ đã kết nối.

#### 3.1.1.4 Chức năng nhận diện tôm bằng AI

Hệ thống tích hợp mô hình YOLO (You Only Look Once) phiên bản TFLite để nhận diện tôm trên thiết bị edge. Mô hình được tối ưu hóa với định dạng FP16 (16-bit floating point) để cân bằng giữa tốc độ và độ chính xác. Input của mô hình là ảnh với kích thước 320x320 pixels, được chuẩn hóa về khoảng [0, 1] trước khi đưa vào inference.

Khi người dùng chụp ảnh từ camera stream, ảnh được encode base64 và gửi đến endpoint `/api/detect-shrimp` trên backend. Backend decode ảnh, chạy inference với TFLite interpreter, và parse kết quả detection. Hệ thống áp dụng confidence threshold 60% để lọc các detection có độ tin cậy thấp, và sử dụng Non-Maximum Suppression (NMS) để loại bỏ các bounding box trùng lặp.

Mỗi detection bao gồm thông tin về className, confidence score, và bounding box (x, y, width, height). Hệ thống tự động tính toán chiều dài tôm (cm) dựa trên tỷ lệ pixel-to-cm với hệ số PIXEL_TO_CM_RATIO = 0.05, và ước tính khối lượng tôm (gram) theo công thức sinh học W = 0.0065 × L^3.1, phù hợp với loài tôm thẻ chân trắng (Litopenaeus vannamei).

Kết quả nhận diện được vẽ lên ảnh gốc với bounding box màu xanh lá, hiển thị thông tin className, confidence, chiều dài và khối lượng ở giữa bounding box với background semi-transparent. Ảnh đã được chú thích được upload lên Cloudinary và metadata được lưu vào MongoDB để tra cứu sau này.

#### 3.1.1.5 Chức năng lưu trữ và quản lý ảnh

Hệ thống sử dụng kiến trúc phân tán cho việc lưu trữ ảnh và metadata. Ảnh nhận diện được upload lên Cloudinary, một dịch vụ cloud storage chuyên dụng cho media, cung cấp CDN global để tăng tốc độ tải ảnh. Cloudinary tự động tối ưu hóa ảnh, tạo thumbnail và cung cấp URL secure để truy cập.

Metadata của mỗi ảnh bao gồm imageUrl, cloudinaryUrl, danh sách detections, timestamp, capturedFrom và inferenceTime được lưu vào MongoDB collection "detections". MongoDB được chọn vì tính linh hoạt của document database, phù hợp với cấu trúc dữ liệu JSON của detection results. Mỗi document có một ObjectId duy nhất làm khóa chính.

Ứng dụng Android cung cấp Gallery screen hiển thị grid layout các ảnh đã chụp, sử dụng LazyVerticalGrid để lazy loading và tối ưu performance. Người dùng có thể xem chi tiết từng ảnh bao gồm số lượng tôm phát hiện, thời gian chụp và inference time. Hệ thống hỗ trợ xóa ảnh thông qua API DELETE `/api/shrimp-images/{id}`, xóa cả metadata trên MongoDB nhưng giữ nguyên file trên Cloudinary để tránh orphaned data.

API GET `/api/shrimp-images` trả về danh sách 100 ảnh gần nhất được sắp xếp theo timestamp giảm dần. Ứng dụng sử dụng Coil library để load ảnh từ URL với disk cache, giảm số lần request đến server. Gallery cũng hiển thị thống kê tổng số ảnh và tổng số tôm phát hiện được.

#### 3.1.1.6 Chức năng thống kê và biểu đồ

Hệ thống cung cấp Chart screen để hiển thị thống kê dựa trên dữ liệu detection history. Biểu đồ cột (Bar Chart) thể hiện số lượng tôm phát hiện theo thời gian, cho phép người dùng phân tích xu hướng tăng trưởng. Biểu đồ được vẽ bằng Canvas API của Jetpack Compose, với animation smooth khi dữ liệu thay đổi.

Thống kê bao gồm các chỉ số: tổng số ảnh chụp, tổng số tôm phát hiện, trung bình tôm mỗi ảnh, chiều dài trung bình và khối lượng trung bình. Dữ liệu được aggregate từ MongoDB collection, có thể lọc theo khoảng thời gian (ngày, tuần, tháng). ViewModel quản lý state của Chart screen và xử lý logic tính toán thống kê.

Hệ thống cũng hỗ trợ hiển thị distribution chart để thấy phân bố kích thước tôm, giúp người nuôi đánh giá chất lượng đàn tôm. Export dữ liệu thống kê ra file CSV hoặc PDF để báo cáo cũng được tích hợp trong roadmap phát triển tương lai.

#### 3.1.1.7 Chức năng điều khiển GPIO (Motor Control)

Hệ thống tích hợp điều khiển GPIO trên Raspberry Pi để tự động hóa các thiết bị trong ao nuôi như máy sục khí, máy bơm nước và hệ thống cho ăn. Backend sử dụng thư viện RPi.GPIO để điều khiển các chân GPIO, hỗ trợ 3 motor với pin 17, 27 và 22.

Chế độ điều khiển bao gồm Manual Mode và Auto Mode. Manual Mode cho phép người dùng bật/tắt từng motor theo ý muốn thông qua API POST `/api/gpio/manual/control`. Auto Mode hoạt động dựa trên lịch trình (schedule) được cấu hình cho từng motor, bao gồm thời gian bắt đầu, kết thúc và các ngày trong tuần.

Auto Mode chạy trong một background thread riêng, kiểm tra lịch trình mỗi giây và tự động bật/tắt motor khi đến thời gian. Hệ thống ngăn chặn manual control khi auto mode đang active để tránh xung đột. Tất cả thay đổi trạng thái GPIO được log chi tiết để audit trail.

API GET `/api/gpio/status` trả về trạng thái hiện tại của tất cả GPIO pins, bao gồm trạng thái ON/OFF và mode (manual/auto). Ứng dụng Android hiển thị Control Panel với switch button cho từng motor và calendar picker để cấu hình schedule. Timezone được set về Asia/Ho_Chi_Minh để đảm bảo lịch trình hoạt động chính xác.

### 3.1.2 Về rủi ro và bảo mật của hệ thống

#### 3.1.2.1 Rủi ro về xác thực và phân quyền

Hệ thống đối mặt với rủi ro token hijacking, khi kẻ tấn công có thể đánh cắp Firebase ID Token từ network traffic hoặc memory của thiết bị. Để giảm thiểu, hệ thống bắt buộc sử dụng HTTPS/TLS 1.3 cho mọi communication, mã hóa end-to-end từ ứng dụng đến backend. Token có thời gian sống giới hạn (1 giờ), sau đó ứng dụng tự động refresh token mới từ Firebase.

Rủi ro privilege escalation xảy ra khi user thường cố gắng truy cập các endpoint chỉ dành cho admin. Backend kiểm tra role của user bằng cách so sánh email trong token với ADMIN_EMAIL trong environment variable. Mọi endpoint admin đều có decorator `@requires_google_auth` và kiểm tra bổ sung `if request.user_email != ADMIN_EMAIL`. Error message được thiết kế generic để không lộ thông tin hệ thống.

Whitelist-based access control tạo rủi ro khi file `permitted_emails.json` bị corrupt hoặc sai format. Hệ thống implement exception handling để load file an toàn, backup file corrupt với timestamp và khởi tạo lại với admin email mặc định. File được lưu với permission 600 (chỉ owner đọc/ghi) trên Linux để ngăn user khác truy cập.

Rủi ro session fixation được ngăn chặn bằng cách không lưu session state trên backend, mọi request đều stateless và phải có valid token. Ứng dụng Android lưu token trong SharedPreferences với MODE_PRIVATE và clear token khi logout. Firebase Auth tự động invalidate token khi user đổi password hoặc revoke access.

#### 3.1.2.2 Rủi ro về truyền dữ liệu

Camera stream truyền dữ liệu video không nén hoàn toàn qua HTTP, tạo rủi ro eavesdropping. Kẻ tấn công trên cùng mạng WiFi có thể sniff traffic và xem camera feed. Giải pháp là enforce HTTPS cho camera stream endpoint và sử dụng Ngrok tunnel để mã hóa traffic từ Raspberry Pi ra internet.

Ảnh chụp được encode base64 trước khi gửi lên server, tăng kích thước payload lên 33%. Với ảnh 1920x1080 JPEG (~500KB), payload có thể lên đến 700KB, gây tốn băng thông và tăng latency. Hệ thống tối ưu bằng cách resize ảnh xuống 1280x720 trước khi encode, giảm payload còn ~300KB, đủ chất lượng cho AI detection.

Man-in-the-middle attack có thể xảy ra nếu certificate không được verify đúng. OkHttp client trong Android app mặc định verify SSL certificate, nhưng cần cẩn thận với self-signed certificate trong development. Production environment bắt buộc sử dụng valid certificate từ Let's Encrypt hoặc Cloudflare.

Rate limiting chưa được implement, tạo rủi ro DDoS attack. Kẻ tấn công có thể spam request đến endpoint detect-shrimp, làm quá tải CPU và memory của Raspberry Pi. Giải pháp đề xuất là sử dụng Flask-Limiter với limit 10 requests/minute per user cho detection endpoint và 1 concurrent connection per user cho camera stream.

#### 3.1.2.3 Rủi ro về lưu trữ dữ liệu

MongoDB connection string chứa username và password được lưu trong environment variable, có rủi ro lộ khi file `.env` bị commit lên Git. Hệ thống sử dụng `.gitignore` để exclude file `.env` và `.env.example` chỉ chứa placeholder. Production database nên enable authentication và IP whitelist để chỉ cho phép Raspberry Pi IP truy cập.

Cloudinary API key và secret được lưu trong `.env` file, nếu bị lộ, kẻ tấn công có thể upload ảnh spam hoặc xóa ảnh hiện có. Giải pháp là rotate API key định kỳ mỗi 3 tháng và enable signed upload với timestamp expiration. Cloudinary cũng hỗ trợ resource type restriction để chỉ cho phép upload image type.

Backup dữ liệu detection history chưa được tự động hóa, rủi ro mất dữ liệu khi MongoDB server gặp sự cố. Đề xuất sử dụng MongoDB Atlas với automatic backup daily và point-in-time recovery. Export dữ liệu ra JSON file hàng tuần và lưu trên Google Drive hoặc S3.

Personal data của user (email, phone) được lưu plain text trong `permitted_emails.json`, vi phạm GDPR nếu triển khai ở EU. Giải pháp là hash email trước khi lưu, hoặc migrate sang database với encryption at rest. Tuy nhiên, với scope khóa luận và deployment trong nước, plain text được chấp nhận để đơn giản hóa.

#### 3.1.2.4 Rủi ro về mô hình AI

Model poisoning attack xảy ra khi kẻ tấn công inject ảnh độc hại vào training data, làm model học sai. Hệ thống sử dụng pre-trained YOLOv8 model từ Ultralytics, đã được train trên dataset chuẩn. Custom training cần validate ảnh input trước khi thêm vào dataset, kiểm tra file header và virus scan.

Adversarial examples là ảnh được craft đặc biệt để fool model, ví dụ thêm noise không nhìn thấy nhưng làm model classify sai. Với aquaculture application, rủi ro này thấp vì không có attacker motivation. Tuy nhiên, có thể implement input validation để reject ảnh có entropy quá cao hoặc histogram bất thường.

Model inference time (~1-1.5 giây trên Raspberry Pi 4) có thể bị chậm hơn nếu CPU bị overload bởi background process. Hệ thống monitor CPU usage và kill non-essential process khi inference. Cân nhắc upgrade lên Raspberry Pi 5 hoặc sử dụng Coral TPU accelerator để giảm inference time xuống <100ms.

Overfitting của model dẫn đến accuracy cao trên training data nhưng thấp trên real-world data. Giải pháp là augment training data với rotation, flip, brightness adjustment để tăng diversity. Regular evaluation trên validation set và thu thập feedback từ user để retrain model khi accuracy giảm.

#### 3.1.2.5 Rủi ro về thiết bị IoT

Raspberry Pi chạy SSH service (port 22) để remote management, có rủi ro brute-force attack. Hệ thống disable password authentication và chỉ cho phép SSH key authentication. IP được restrict trong router firewall, chỉ cho phép connection từ admin IP. Fail2ban được cài đặt để auto-ban IP sau 3 failed login attempts.

GPIO pins điều khiển motor có thể bị malicious control nếu API không được bảo vệ đúng. Tất cả GPIO endpoints đều require authentication token và chỉ admin mới có quyền control. Log mọi GPIO state change với timestamp và user email để audit trail. Emergency stop button trong UI để ngắt tất cả motor nếu xảy ra sự cố.

SD card trên Raspberry Pi có tuổi thọ giới hạn (write cycles), có thể corrupt file system sau vài năm sử dụng. Giải pháp là mount `/var/log` và `/tmp` vào RAM disk để giảm write IO. Regular backup SD card image hàng tháng. Production deployment nên migrate sang USB SSD để tăng reliability.

Camera USB có thể bị disconnect ngẫu nhiên do cable lỏng hoặc power issue. Backend detect camera disconnect trong `generate_frames()` function và tự động retry reconnect mỗi 5 giây. Hiển thị placeholder "Camera Disconnected" trong stream thay vì crash server. Monitor camera status qua `/health` endpoint.

#### 3.1.2.6 Rủi ro về network và connectivity

WiFi connection không ổn định trong môi trường ao nuôi (xa router, nhiều interference) dẫn đến mất kết nối Raspberry Pi. Giải pháp là sử dụng WiFi extender hoặc mesh network để tăng coverage. Cân nhắc dùng PoE (Power over Ethernet) kết hợp Ethernet cable để ổn định hơn WiFi.

Ngrok tunnel có thể disconnect nếu Raspberry Pi reboot hoặc mất internet tạm thời. Systemd service được cấu hình để auto-restart Ngrok process khi crash. Ngrok agent tự động reconnect khi internet trở lại. Tuy nhiên, public URL có thể thay đổi với free plan, cần upgrade lên paid plan để có fixed subdomain.

Bandwidth hạn chế của 4G/LTE connection (nếu không có broadband) ảnh hưởng camera stream quality. Camera stream ở 30 fps, 640x480, JPEG quality 80% tiêu tốn khoảng 2-3 Mbps. Giảm frame rate xuống 15 fps hoặc resolution xuống 480p để tiết kiệm bandwidth. Implement adaptive bitrate streaming dựa trên network condition.

MQTT hoặc WebSocket cho real-time notification chưa được implement, phải polling API định kỳ để check new detections. Tốn battery trên mobile app và tăng server load. Đề xuất tích hợp Firebase Cloud Messaging (FCM) để push notification khi có detection mới, tiết kiệm battery và real-time hơn.

### 3.1.3 Đề xuất phương án thiết kế

#### 3.1.3.1 Kiến trúc tổng thể hệ thống

Hệ thống được thiết kế theo kiến trúc Client-Server ba tầng (3-tier architecture) với tách biệt rõ ràng giữa Presentation Layer, Application Layer và Data Layer. Presentation Layer là ứng dụng Android được xây dựng bằng Kotlin và Jetpack Compose, cung cấp giao diện người dùng hiện đại với Material Design 3.

Application Layer là Flask backend server chạy trên Raspberry Pi, xử lý business logic bao gồm authentication, AI detection, camera streaming và GPIO control. Backend expose RESTful API endpoints với JSON format để ứng dụng Android gọi. Mọi endpoint đều stateless, không lưu session state, phù hợp với mobile app có thể disconnect bất kỳ lúc nào.

Data Layer bao gồm ba thành phần: Firebase Authentication cho user credentials và token management, MongoDB Atlas cho detection metadata và user permissions, Cloudinary cho binary image storage. Kiến trúc này phân tán load và tối ưu hóa từng loại dữ liệu với công nghệ phù hợp.

Communication giữa các layer sử dụng HTTPS/TLS với REST API cho synchronous request-response (capture, detect, control) và MJPEG streaming cho camera feed. WebSocket hoặc Server-Sent Events có thể được thêm vào trong tương lai cho real-time notification. Ngrok tunnel cung cấp secure connection từ internet đến Raspberry Pi behind NAT.

Horizontal scaling có thể đạt được bằng cách deploy nhiều Raspberry Pi instances, mỗi instance serve một camera và có device_id riêng. MongoDB và Cloudinary đã support sharding và CDN nên scale tốt. Backend có thể migrate lên cloud (AWS EC2, Google Compute Engine) với multiple instances behind load balancer nếu cần serve nhiều concurrent users.

#### 3.1.3.2 Thiết kế module xác thực

Module xác thực sử dụng Firebase Authentication làm Identity Provider (IdP), hỗ trợ OAuth 2.0 flow cho Google Sign-In và Phone Authentication với OTP. Ứng dụng Android tích hợp Firebase Auth SDK, trigger sign-in UI và nhận User object sau khi xác thực thành công.

User object chứa uid (unique identifier), email hoặc phone number và providerData (Google, Phone). Ứng dụng gọi `user.getIdToken(true)` để lấy fresh ID Token (JWT) và lưu vào SharedPreferences. Token này được refresh tự động bởi SDK khi gần hết hạn (1 giờ).

Backend triển khai decorator `@requires_google_auth` để wrap mọi protected endpoint. Decorator extract token từ header `Authorization: <token>` hoặc `X-Phone-Auth: <phone>`, verify với Firebase Admin SDK, extract email/phone từ decoded token và check trong whitelist. Nếu verification fail, return 401 Unauthorized; nếu không trong whitelist, return 403 Forbidden.

Admin management được implement qua Admin Panel trong Profile screen của app. Chỉ user có email khớp với ADMIN_EMAIL environment variable mới thấy panel này. Panel hiển thị danh sách permitted emails/phones và cung cấp form để add/remove. API `/api/admin/add-email` và `/api/admin/remove-email` có check `request.user_email == ADMIN_EMAIL` trước khi thực hiện.

Session management không sử dụng server-side session vì stateless API design. Mỗi request phải có token hợp lệ. Logout được handle bởi ứng dụng Android: clear SharedPreferences, sign out Firebase Auth (`FirebaseAuth.getInstance().signOut()`) và navigate về Login screen. Backend không maintain session state nên không cần logout endpoint.

#### 3.1.3.3 Thiết kế module quản lý thiết bị

Module quản lý thiết bị triển khai mô hình "Independent Device Binding" với data structure `{email: {device_id, ip, last_updated}}` lưu trong file JSON. Mỗi user có một entry riêng, cho phép bind một device tại một thời điểm nhưng nhiều user có thể bind cùng một device_id vật lý.

Device discovery sử dụng UDP broadcast trên port 37020. Raspberry Pi chạy background thread lắng nghe broadcast message "DISCOVER_SHRIMP_DEVICE", reply lại với "SHRIMP_DEVICE:{device_id}:{ip}". Android app gửi broadcast packet đến 255.255.255.255:37020, lắng nghe reply trong 5 giây và hiển thị danh sách discovered devices.

Bind process: User chọn device từ danh sách, app gọi POST `/api/devices/bind` với body `{device_id, device_ip}` và token header. Backend kiểm tra user đã bind device nào chưa, nếu chưa thì tạo entry mới, nếu đã bind device khác thì overwrite (switch device), nếu đã bind cùng device thì update IP. Save vào `permitted_devices.json` và return success.

Unbind process: User nhấn "Unbind Device" trong Profile screen, app gọi POST `/api/devices/unbind` với `{device_id}`. Backend verify user có bind device này không, nếu có thì xóa entry của user đó khỏi dictionary, không ảnh hưởng entry của user khác. Return success và app clear local SharedPreferences.

Device status check: Khi app launch, HomeScreen gọi GET `/api/devices/my-device` để lấy thông tin device đã bind. Backend tìm entry theo `request.user_email`, return `{bound: true, device_id, device_ip}` nếu có hoặc `{bound: false}` nếu chưa bind. App lưu vào SharedPreferences và sử dụng IP để construct camera stream URL.

#### 3.1.3.4 Thiết kế module AI detection

Module AI detection sử dụng YOLOv8 model được convert sang TFLite format với FP16 quantization. Model file `best-fp16.tflite` được load bằng TFLite Runtime hoặc TensorFlow Lite interpreter, allocate tensors một lần khi Flask app start để giảm latency.

Input preprocessing pipeline: Ảnh từ app được base64 decode → PIL Image → numpy array → BGR color space (OpenCV format) → resize đến model input size (320x320) → normalize [0,1] → expand dimension thành batch size 1 → set vào interpreter input tensor. Pipeline này được optimize để minimize copy operation.

Inference execution: Gọi `interpreter.invoke()` để chạy model forward pass, mất khoảng 1-1.5 giây trên Raspberry Pi 4. Output tensor có shape [1, N, 6] với N là max detections (thường 100-300), mỗi detection có [x, y, w, h, confidence, class_id]. Parser extract detections có confidence >= 0.6 và apply NMS với IoU threshold 0.6.

Size estimation module: Bounding box width và height (pixels) được convert sang cm bằng công thức `length_cm = max(width, height) * PIXEL_TO_CM_RATIO`. PIXEL_TO_CM_RATIO được calibrate dựa trên camera height và FOV. Weight estimation sử dụng allometric equation `W = 0.0065 * L^3.1` cho tôm thẻ chân trắng, trong đó W là gram và L là cm.

Result annotation: Ảnh gốc được vẽ bounding box màu xanh với OpenCV `cv2.rectangle()`. Label text hiển thị className, confidence, length và weight được vẽ centered trong box với `cv2.putText()`. Semi-transparent background được tạo bằng `cv2.addWeighted()` để label dễ đọc. Annotated image encode JPEG quality 90% và upload Cloudinary.

#### 3.1.3.5 Thiết kế module camera streaming

Module camera streaming implement MJPEG protocol với multipart/x-mixed-replace content type. Generator function `generate_frames()` chạy trong vòng lặp vô hạn, liên tục capture frame từ camera, encode JPEG và yield bytes với boundary delimiter.

Camera initialization: Quét các video device `/dev/video0-14`, thử open với OpenCV `cv2.VideoCapture(i, cv2.CAP_V4L2)`. Set codec fourcc MJPEG, resolution 640x480, FPS 30 và buffer size 1. Test capture một frame để verify camera hoạt động. Nếu thành công thì break, nếu không tìm thấy camera nào thì log warning và trả về placeholder image.

Frame capture optimization: Sử dụng threading lock `camera_lock` để serialize access đến camera object. Gọi `camera.grab()` để discard old frames trong buffer, sau đó `camera.retrieve()` để get latest frame. Này giảm latency xuống dưới 100ms so với `camera.read()` thông thường. Encode JPEG với quality 80% và optimization flag.

Streaming endpoint `/blynk_feed` protected bởi `@requires_google_auth` decorator. Return Flask Response object với `generate_frames()` generator và mimetype multipart. Add headers `Cache-Control: no-cache` để tắt browser cache, `Connection: close` để tránh connection pooling, `X-Accel-Buffering: no` để tắt nginx buffering.

Client side (Android): OkHttp Request với GET method đến stream URL và auth header. Execute request, check response successful, get InputStream. Đọc stream trong coroutine loop, tìm JPEG header (0xFF 0xD8) và footer (0xFF 0xD9) để extract frame. Decode frame thành Bitmap bằng BitmapFactory. Update mutableState để trigger Compose recomposition và hiển thị frame trong Image composable.

#### 3.1.3.6 Thiết kế module storage

Module storage sử dụng hybrid approach với binary data (images) trên Cloudinary và metadata trên MongoDB. Cloudinary upload được trigger trong detection endpoint sau khi annotate image. Upload function nhận BytesIO buffer, gửi đến Cloudinary API với folder "shrimp-detections" và resource_type "image". Cloudinary return JSON response chứa public_id, version, secure_url và other metadata.

MongoDB document schema được thiết kế với các fields: `_id` (ObjectId), `imageUrl` (HTTP URL từ Cloudinary), `cloudinaryUrl` (HTTPS URL với CDN), `detections` (array of objects), `timestamp` (milliseconds since epoch), `capturedFrom` (string), `inferenceTime` (seconds). Index được tạo trên field `timestamp` descending để optimize query recent images.

Gallery API endpoint `/api/shrimp-images` query MongoDB với `collection.find().sort('timestamp', -1).limit(100)`, convert ObjectId sang string và return JSON array. Android app parse JSON với kotlinx.serialization, map thành data class `ShrimpImage` và lưu trong ViewModel state. LazyVerticalGrid composable render images với Coil AsyncImage, tự động cache đến disk.

Image detail screen fetch chi tiết một image qua GET `/api/shrimp-images/{id}`. Backend query MongoDB bằng ObjectId, return full document. Screen hiển thị large image từ cloudinaryUrl, danh sách detections với confidence bar, timestamp formatted theo timezone, và inference time. Delete button gọi DELETE endpoint, backend xóa document từ MongoDB nhưng giữ image trên Cloudinary để avoid broken links nếu URL được share.

Cleanup strategy chưa được implement trong current version. Future work: Cloudinary admin API có thể delete image by public_id. Implement scheduled job chạy hàng tháng, query MongoDB find images older than 6 months, delete từ Cloudinary bằng API, sau đó delete MongoDB documents. Hoặc implement lifecycle policy trong Cloudinary dashboard để auto-delete after certain period.

#### 3.1.3.7 Thiết kế module GPIO control

Module GPIO control sử dụng RPi.GPIO library với BCM pin numbering. GPIO pins 17, 27, 22 được map tới motor1, motor2, motor3. Setup mode OUTPUT với initial state LOW (OFF). Wrapper try-except để handle environment không có GPIO (development laptop) mà không crash app.

Manual control API `/api/gpio/manual/control` nhận JSON `{motor_id, state}` với state là 0 (OFF) hoặc 1 (ON). Check motor_id valid trong GPIO_PINS dict. Check auto_mode_active, nếu true thì return 403 với message "Cannot manual control while auto mode active". Nếu GPIO available thì gọi `GPIO.output(pin, GPIO.HIGH/LOW)`. Log state change với timestamp và user email.

Auto mode schedule data structure: `{motor_id: {enabled, start_time, end_time, days}}` với start_time/end_time là string "HH:MM" và days là array of weekday names. API POST `/api/gpio/auto/schedule` để update schedule của một motor. Validate time format và days valid. Save vào in-memory dict (future: persist to file or database).

Auto mode worker thread: Chạy trong vòng lặp while auto_mode_active. Mỗi giây check schedule của từng motor bằng function `check_schedule()`. Function lấy current time và weekday, check nằm trong time range và day list không. Nếu có thì return True (should be ON), nếu không thì False (should be OFF). So sánh với current GPIO state, nếu khác thì output new state và log.

Auto mode start API: Check ít nhất một schedule enabled và có days configured. Turn off tất cả manual controls trước. Set `auto_mode_active = True` và start worker thread với `daemon=True`. Auto mode stop API: Set `auto_mode_active = False`, worker thread sẽ tự exit vòng lặp, turn off tất cả GPIO pins. Return JSON với schedules hiện tại để app UI sync state.

---

*Tài liệu này được tạo tự động từ phân tích source code hệ thống nhận diện tôm.*  
*Ngày tạo: 29/12/2025*  
*Phiên bản: 1.0*

