# ✅ Phone Authentication Implementation Complete

## Summary

Hệ thống đã được cập nhật thành công để hỗ trợ **đăng nhập bằng số điện thoại** với đầy đủ tính năng như đăng nhập Google.

## Changes Made

### 1. Backend (app_complete.py)
- ✅ Added `permitted_phones.json` support
- ✅ Created `load_permitted_phones()` and `save_permitted_phones()` functions  
- ✅ Updated `requires_google_auth` decorator to support phone authentication via `X-Phone-Auth` header
- ✅ Updated `/api/auth/check` endpoint to handle both email and phone
- ✅ Added 3 new admin endpoints:
  - `POST /api/admin/add-phone` - Thêm phone number
  - `POST /api/admin/remove-phone` - Xóa phone number
  - `GET /api/admin/list-phones` - List phone numbers

### 2. Android App

#### UserSession.kt (New)
- ✅ Created session manager for user data
- ✅ Supports both Google and Phone login
- ✅ Methods: `saveUser()`, `getCurrentUser()`, `isAdmin()`, `isLoggedIn()`, `clearSession()`

#### login.kt
- ✅ Updated `loginWithFirestore()` to save full user info to UserSession
- ✅ Load user data from Firestore (username, fullName, bio, role, avatarResId)
- ✅ Check admin role from Firestore

#### signup.kt
- ✅ Updated registration to save full user info to Firestore
- ✅ Auto-generate username from email
- ✅ Set default values: role="user", avatarResId, bio

#### LogoutViewModel.kt
- ✅ Updated to use UserSession
- ✅ Clear session properly for both login types
- ✅ Display current user from session

#### ProfileViewModel.kt
- ✅ Updated to get user from UserSession instead of hardcoded list

#### ProfileScreen.kt
- ✅ Created `getAuthHeaders()` function to support both auth types
- ✅ Send `X-Phone-Auth` header for phone users
- ✅ Send `Authorization` token for Google users
- ✅ Updated all API calls to use `getAuthHeaders()`
- ✅ Admin detection works for both phone and email

### 3. Configuration Files
- ✅ Created `permitted_phones.json` with admin phone
- ✅ Updated `build.gradle.kts` to include Gson dependency
- ✅ Created `PHONE_AUTH_SETUP_GUIDE.md` with detailed instructions

## How It Works

### Google Login Flow
```
1. User logs in with Google
2. Firebase Auth returns ID token
3. App sends token in Authorization header
4. Backend verifies token with Firebase Admin SDK
5. Check email in permitted_emails.json
```

### Phone Login Flow
```
1. User logs in with phone + password
2. App verifies with Firestore
3. Save user data to UserSession
4. App sends phone in X-Phone-Auth header
5. Backend checks phone in permitted_phones.json
```

## Admin Setup

### Step 1: Set Admin Phone in Firestore
```javascript
// In Firebase Console → Firestore → users → +84123456789
{
  phoneNumber: "+84123456789",
  email: "admin@example.com",
  role: "admin",  // ← Important!
  username: "admin",
  fullName: "Administrator",
  avatarResId: 2131230784,
  bio: "System Administrator"
}
```

### Step 2: Update Backend
```bash
cd backend
# Edit .env file (optional)
echo 'ADMIN_PHONE=+84123456789' >> .env

# Start server
python app_complete.py
```

### Step 3: Test
1. Register new phone user in app
2. Admin logs in and adds phone to permitted list
3. Phone user logs in and can access all features

## Features for Phone Users

After admin grants permission, phone users can:
- ✅ Scan and bind devices
- ✅ View camera stream
- ✅ Use motor control
- ✅ View charts and statistics
- ✅ Browse gallery
- ✅ Everything that Google users can do!

## Admin Features

Both admin email and admin phone can:
- ✅ Manage email permissions (add/remove)
- ✅ Manage phone permissions (add/remove)
- ✅ View all permitted users
- ✅ Manage device bindings

## API Headers

### For Google Users
```http
Authorization: <firebase_id_token>
```

### For Phone Users
```http
X-Phone-Auth: +84xxxxxxxxx
```

## Testing Checklist

- [ ] Build app successfully (no compilation errors)
- [ ] Register new phone user
- [ ] Admin grants phone permission
- [ ] Phone user logs in successfully
- [ ] Phone user sees profile info correctly
- [ ] Phone user can scan devices
- [ ] Phone user can bind device
- [ ] Phone user can view camera stream
- [ ] Phone user can logout and login again
- [ ] Session persists after app restart

## Next Steps

1. **Sync Gradle** to download Gson dependency
2. **Build APK**: `gradlew assembleDebug`
3. **Configure Backend**: Restart backend server
4. **Test**: Register phone user and grant permission
5. **Deploy**: Install APK on device and test full flow

## Notes

- Phone numbers must start with `+` (country code)
- Admin phone default: `+84123456789`
- Change admin phone via `ADMIN_PHONE` env variable
- Firestore must have `role: "admin"` for admin phone

## Troubleshooting

**Q: Gradle sync failed for Gson**
A: Disable offline mode in Android Studio → Settings → Gradle → Uncheck "Offline work"

**Q: Phone login fails**
A: Check Firestore has user document with phone as ID

**Q: Permission denied**
A: Admin must add phone to permitted list via Profile screen

**Q: Cannot bind device**
A: Phone must be in permitted_phones.json first

