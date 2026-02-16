# Enrollment App - Complete Setup Guide

## Overview
This project consists of:
1. Android app (Kotlin) - Captures enrollment data with facial recognition
2. Node.js server - Stores enrollment data in MongoDB

---

## Server Setup

### Prerequisites
- Node.js (v14 or higher)
- MongoDB (local or cloud)

### Installation

1. **Install MongoDB:**
   - Download from: https://www.mongodb.com/try/download/community
   - Or use MongoDB Atlas (cloud): https://www.mongodb.com/cloud/atlas

2. **Install server dependencies:**
   ```bash
   cd server
   npm install
   ```

3. **Configure environment:**
   - Edit `server/.env` file
   - Set your MongoDB connection string:
     ```
     MONGODB_URI=mongodb://localhost:27017/enrollment_db
     ```

4. **Start the server:**
   ```bash
   npm start
   ```
   
   Server will run on: http://localhost:3000

---

## Android App Setup

### For Android Emulator:
- The app is already configured to use: `http://10.0.2.2:3000`
- No changes needed!

### For Physical Device:

1. **Find your computer's IP address:**
   - Windows: Open CMD and run `ipconfig`
   - Look for "IPv4 Address" (e.g., 192.168.1.100)

2. **Update app configuration:**
   - Open: `app/src/main/res/values/config.xml`
   - Change the server URL:
     ```xml
     <string name="server_base_url">http://YOUR_IP:3000/api</string>
     ```
   - Example: `http://192.168.1.100:3000/api`

3. **Rebuild and install:**
   ```bash
   .\gradlew.bat assembleDebug
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

4. **Important:** Make sure your phone and computer are on the same WiFi network!

---

## How to Use

1. **Start the server:**
   ```bash
   cd server
   npm start
   ```

2. **Open the app on your device**

3. **Enroll a person:**
   - Enter enrollment number (e.g., "ENR001")
   - Enter password
   - Tap "Take Facial Data"
   - Move your head slightly for liveness detection
   - Wait for 10 frames to be captured
   - Tap "Save"

4. **Data is saved to MongoDB!**

---

## API Endpoints

### Create Enrollment
```
POST http://localhost:3000/api/enrollment
Body: {
  "enrollmentNo": "ENR001",
  "password": "password123",
  "faceEmbedding": [0.123, 0.456, ...]
}
```

### Verify Enrollment
```
POST http://localhost:3000/api/enrollment/verify
Body: {
  "enrollmentNo": "ENR001",
  "password": "password123"
}
```

### Get Enrollment
```
GET http://localhost:3000/api/enrollment/ENR001
```

### Get All Enrollments
```
GET http://localhost:3000/api/enrollments
```

---

## Troubleshooting

### "Network error" in app:
- Check if server is running
- Verify the IP address in config.xml
- Make sure phone and computer are on same WiFi
- Check firewall settings

### "MongoDB connection error":
- Make sure MongoDB is running
- Check MONGODB_URI in .env file

### Server not starting:
- Check if port 3000 is already in use
- Run: `npm install` again

---

## Project Structure

```
.
├── app/                          # Android app
│   ├── src/main/
│   │   ├── java/.../
│   │   │   ├── MainActivity.kt
│   │   │   ├── CameraActivity.kt
│   │   │   ├── ApiService.kt
│   │   │   ├── LivenessDetector.kt
│   │   │   └── ...
│   │   ├── res/values/config.xml # Server URL configuration
│   │   └── assets/               # AI models
│   └── build.gradle.kts
│
├── server/                       # Node.js server
│   ├── server.js                # Main server file
│   ├── package.json
│   └── .env                     # Configuration
│
└── SETUP_GUIDE.md               # This file
```

---

## Features

✅ Face detection with MediaPipe
✅ Face recognition with MobileFaceNet
✅ Liveness detection (anti-spoofing)
✅ Secure password hashing (bcrypt)
✅ MongoDB database storage
✅ RESTful API
✅ Real-time camera preview

---

## Security Notes

- Passwords are hashed with bcrypt before storage
- Face embeddings are stored as 192-dimensional vectors
- Use HTTPS in production
- Add authentication/authorization for API endpoints
- Never commit .env file to git
