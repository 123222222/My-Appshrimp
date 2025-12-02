# 🔄 Hướng dẫn Update Ngrok URL

## ⚠️ Vấn đề

Ngrok free plan tạo URL mới mỗi khi restart:
```
Session 1: https://abc123.ngrok-free.dev
Session 2: https://xyz789.ngrok-free.dev  ← URL thay đổi!
```

→ Phải update URL trong app code và rebuild

## ✅ Giải pháp

### Option 1: Hardcode URL (Hiện tại)

**File cần update:**

1. **HomeScreen.kt** (dòng ~60)
```kotlin
val streamUrl = "https://YOUR-NEW-URL.ngrok-free.dev/blynk_feed"
```

2. **ShrimpApiService.kt** (dòng ~31)
```kotlin
private val BACKEND_URL = "https://YOUR-NEW-URL.ngrok-free.dev"
```

3. **ChartViewModel.kt** (dòng ~41)
```kotlin
private val BACKEND_URL = "https://YOUR-NEW-URL.ngrok-free.dev"
```

4. **GalleryViewModel.kt** (dòng ~41)
```kotlin
private val BACKEND_URL = "https://YOUR-NEW-URL.ngrok-free.dev"
```

5. **ProfileScreen.kt** (dòng ~60)
```kotlin
val backendUrl = "https://YOUR-NEW-URL.ngrok-free.dev"
```

**Steps:**
```bash
# 1. Lấy URL mới từ Ngrok
ssh pi@raspberry-pi-ip
# Xem terminal output của ngrok
# Copy URL mới

# 2. Update trong code Android
# Tìm và thay thế tất cả:
# Old: https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev
# New: https://YOUR-NEW-URL.ngrok-free.dev

# 3. Rebuild app
./gradlew assembleDebug

# 4. Install
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Config File (Đề xuất)

Tạo một file config để dễ update:

**1. Tạo Config.kt:**
```kotlin
// app/src/main/java/com/dung/myapplication/models/Config.kt
package com.dung.myapplication.models

object Config {
    // ⚠️ Update URL này khi Ngrok restart
    const val BACKEND_URL = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev"
    
    // Camera stream endpoint
    const val STREAM_URL = "$BACKEND_URL/blynk_feed"
}
```

**2. Update các file sử dụng:**

```kotlin
// HomeScreen.kt
val streamUrl = Config.STREAM_URL

// ShrimpApiService.kt
private val BACKEND_URL = Config.BACKEND_URL

// ChartViewModel.kt
private val BACKEND_URL = Config.BACKEND_URL

// GalleryViewModel.kt
private val BACKEND_URL = Config.BACKEND_URL

// ProfileScreen.kt
val backendUrl = Config.BACKEND_URL
```

**Lợi ích:**
- Chỉ cần update 1 chỗ
- Dễ quản lý
- Ít lỗi hơn

### Option 3: BuildConfig (Advanced)

Dùng Gradle để inject URL:

**1. Update build.gradle.kts:**
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "BACKEND_URL", "\"https://YOUR-URL.ngrok-free.dev\"")
    }
}
```

**2. Sử dụng:**
```kotlin
private val BACKEND_URL = BuildConfig.BACKEND_URL
```

**Update URL:**
```bash
# Chỉ cần update build.gradle.kts và rebuild
# Không cần đổi code
```

### Option 4: Remote Config (Best - Production)

Dùng Firebase Remote Config để update URL từ xa **không cần rebuild app**:

**Setup:**
```kotlin
// Fetch URL từ Firebase Remote Config
val remoteConfig = Firebase.remoteConfig
remoteConfig.fetchAndActivate().addOnCompleteListener {
    val backendUrl = remoteConfig.getString("backend_url")
    // Sử dụng URL này
}
```

**Update URL:**
1. Vào Firebase Console
2. Remote Config
3. Update parameter `backend_url`
4. Publish
5. App tự động fetch URL mới (không cần rebuild!)

### Option 5: Ngrok Static Domain (Paid - Recommended)

Mua Ngrok Pro plan ($8/month):
- Static domain: `https://myapp.ngrok.app` (không đổi)
- Không cần update code nữa!

**Setup:**
```bash
# Trên Raspberry Pi
ngrok http 8000 --domain=myapp.ngrok.app
```

## 🚀 Quick Update Script

Tạo script để update nhanh:

**update_ngrok_url.sh:**
```bash
#!/bin/bash

# Script tự động update Ngrok URL trong code Android

NEW_URL="$1"

if [ -z "$NEW_URL" ]; then
    echo "Usage: ./update_ngrok_url.sh <new-ngrok-url>"
    exit 1
fi

echo "Updating Ngrok URL to: $NEW_URL"

# Update HomeScreen.kt
sed -i "s|https://.*ngrok-free.dev/blynk_feed|$NEW_URL/blynk_feed|g" \
    app/src/main/java/com/dung/myapplication/mainUI/home/HomeScreen.kt

# Update ShrimpApiService.kt
sed -i "s|https://.*ngrok-free.dev|$NEW_URL|g" \
    app/src/main/java/com/dung/myapplication/utils/ShrimpApiService.kt

# Update ChartViewModel.kt
sed -i "s|https://.*ngrok-free.dev|$NEW_URL|g" \
    app/src/main/java/com/dung/myapplication/mainUI/chart/ChartViewModel.kt

# Update GalleryViewModel.kt
sed -i "s|https://.*ngrok-free.dev|$NEW_URL|g" \
    app/src/main/java/com/dung/myapplication/mainUI/gallery/GalleryViewModel.kt

# Update ProfileScreen.kt
sed -i "s|https://.*ngrok-free.dev|$NEW_URL|g" \
    app/src/main/java/com/dung/myapplication/mainUI/profile/ProfileScreen.kt

echo "✅ Updated all files!"
echo "Now rebuild the app:"
echo "  ./gradlew assembleDebug"
```

**Sử dụng:**
```bash
chmod +x update_ngrok_url.sh
./update_ngrok_url.sh https://new-url.ngrok-free.dev
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 📋 Checklist khi Ngrok restart

- [ ] SSH vào Raspberry Pi
- [ ] Chạy `ngrok http 8000`
- [ ] Copy URL mới từ terminal
- [ ] Update trong Config.kt (hoặc dùng script)
- [ ] Rebuild app: `./gradlew assembleDebug`
- [ ] Install: `adb install -r app/...apk`
- [ ] Test: Mở app → Home → Kiểm tra camera stream

## 💡 Tips

1. **Bookmark Ngrok Dashboard**: https://dashboard.ngrok.com/
   - Xem all active tunnels
   - Copy URL dễ dàng

2. **Keep Ngrok running**:
   ```bash
   # Chạy trong screen/tmux để không bị tắt khi SSH disconnect
   screen -S ngrok
   ngrok http 8000
   # Ctrl+A, D để detach
   ```

3. **Auto-restart Ngrok**:
   ```bash
   # systemd service để Ngrok tự start khi Pi boot
   sudo nano /etc/systemd/system/ngrok.service
   ```

4. **Monitor Ngrok**:
   ```bash
   # Check Ngrok có đang chạy không
   curl http://localhost:4040/api/tunnels
   ```

---

✅ Hiện tại code đã **hardcode Ngrok URL**, nên mỗi khi Ngrok restart phải update code và rebuild app.

🎯 **Khuyến nghị**: Implement **Option 2 (Config.kt)** hoặc **Option 4 (Firebase Remote Config)** để dễ quản lý hơn!

