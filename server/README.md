# Enrollment Server

Node.js server for the Enrollment App with facial recognition.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Install MongoDB:
   - Download from: https://www.mongodb.com/try/download/community
   - Or use MongoDB Atlas (cloud): https://www.mongodb.com/cloud/atlas

3. Configure environment:
   - Edit `.env` file with your MongoDB connection string

4. Start the server:
```bash
npm start
```

For development with auto-reload:
```bash
npm run dev
```

## API Endpoints

### Health Check
- `GET /` - Check if server is running

### Enrollment Operations
- `POST /api/enrollment` - Create new enrollment
- `GET /api/enrollment/:enrollmentNo` - Get enrollment by number
- `PUT /api/enrollment/:enrollmentNo` - Update enrollment
- `DELETE /api/enrollment/:enrollmentNo` - Delete enrollment
- `GET /api/enrollments` - Get all enrollments (admin)
- `POST /api/enrollment/verify` - Verify enrollment credentials

## Request Examples

### Create Enrollment
```json
POST /api/enrollment
{
  "enrollmentNo": "ENR001",
  "password": "securePassword123",
  "faceEmbedding": [0.123, 0.456, ..., 0.789]
}
```

### Verify Enrollment
```json
POST /api/enrollment/verify
{
  "enrollmentNo": "ENR001",
  "password": "securePassword123"
}
```

## Server URL
- Local: http://localhost:3000
- For Android emulator: http://10.0.2.2:3000
- For physical device: http://YOUR_COMPUTER_IP:3000
