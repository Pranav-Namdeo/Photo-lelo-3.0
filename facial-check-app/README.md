# Facial Check App

Login-only app that connects to the Enrollment Server and stores facial data securely on device.

## Features

✅ Login with enrollment number and password (no registration)
✅ Verifies credentials against the server database
✅ Downloads and stores facial embedding data securely on device
✅ Uses Android EncryptedSharedPreferences for secure storage
✅ Facial data is encrypted and not visible to users
✅ Auto-login on subsequent app opens
✅ Logout clears all local data

## How It Works

1. **Login Screen:**
   - User enters enrollment number and password
   - App sends credentials to server for verification
   - Server validates and returns facial embedding data

2. **Secure Storage:**
   - Facial embedding (192 dimensions) is encrypted using AES256
   - Stored in EncryptedSharedPreferences (Android Security library)
   - Data is not accessible to other apps or users

3. **Home Screen:**
   - Shows welcome message with enrollment number
   - Confirms facial data is stored securely
   - Logout button to clear all data

## Configuration

Update server URL in: `app/src/main/res/values/config.xml`

```xml
<string name="server_base_url">http://YOUR_IP:3000/api</string>
```

## Build & Install

```bash
cd facial-check-app
.\gradlew.bat assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

## Security

- Passwords are never stored locally
- Facial embeddings are encrypted with AES256-GCM
- Uses Android Keystore for encryption keys
- Data is cleared on logout

## Server Connection

This app connects to the same Node.js server as the Enrollment App.

API Endpoint used:
- `POST /api/enrollment/verify` - Verify login credentials and get facial data
