const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const bcrypt = require('bcrypt');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));

// MongoDB Connection
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/enrollment_db';

mongoose.connect(MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
})
.then(() => console.log('✓ Connected to MongoDB'))
.catch(err => console.error('MongoDB connection error:', err));

// Enrollment Schema
const enrollmentSchema = new mongoose.Schema({
    enrollmentNo: {
        type: String,
        required: true,
        unique: true,
        index: true
    },
    password: {
        type: String,
        required: true
    },
    faceEmbedding: {
        type: [Number],
        required: true
    },
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
});

const Enrollment = mongoose.model('Enrollment', enrollmentSchema);

// Routes

// Health check
app.get('/', (req, res) => {
    res.json({ 
        status: 'ok', 
        message: 'Enrollment Server is running',
        timestamp: new Date().toISOString()
    });
});

// Create enrollment
app.post('/api/enrollment', async (req, res) => {
    try {
        const { enrollmentNo, password, faceEmbedding } = req.body;

        // Validation
        if (!enrollmentNo || !password || !faceEmbedding) {
            return res.status(400).json({ 
                success: false, 
                message: 'Missing required fields' 
            });
        }

        if (!Array.isArray(faceEmbedding) || faceEmbedding.length === 0) {
            return res.status(400).json({ 
                success: false, 
                message: 'Invalid face embedding data' 
            });
        }

        // Check if enrollment already exists
        const existingEnrollment = await Enrollment.findOne({ enrollmentNo });
        if (existingEnrollment) {
            return res.status(409).json({ 
                success: false, 
                message: 'Enrollment number already exists' 
            });
        }

        // Hash password
        const hashedPassword = await bcrypt.hash(password, 10);

        // Create new enrollment
        const enrollment = new Enrollment({
            enrollmentNo,
            password: hashedPassword,
            faceEmbedding
        });

        await enrollment.save();

        res.status(201).json({ 
            success: true, 
            message: 'Enrollment created successfully',
            data: {
                enrollmentNo: enrollment.enrollmentNo,
                createdAt: enrollment.createdAt
            }
        });

    } catch (error) {
        console.error('Error creating enrollment:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error',
            error: error.message 
        });
    }
});

// Get enrollment by number
app.get('/api/enrollment/:enrollmentNo', async (req, res) => {
    try {
        const { enrollmentNo } = req.params;

        const enrollment = await Enrollment.findOne({ enrollmentNo }).select('-password');

        if (!enrollment) {
            return res.status(404).json({ 
                success: false, 
                message: 'Enrollment not found' 
            });
        }

        res.json({ 
            success: true, 
            data: enrollment 
        });

    } catch (error) {
        console.error('Error fetching enrollment:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error',
            error: error.message 
        });
    }
});

// Update enrollment
app.put('/api/enrollment/:enrollmentNo', async (req, res) => {
    try {
        const { enrollmentNo } = req.params;
        const { password, faceEmbedding } = req.body;

        const enrollment = await Enrollment.findOne({ enrollmentNo });

        if (!enrollment) {
            return res.status(404).json({ 
                success: false, 
                message: 'Enrollment not found' 
            });
        }

        // Update fields
        if (password) {
            enrollment.password = await bcrypt.hash(password, 10);
        }
        if (faceEmbedding) {
            enrollment.faceEmbedding = faceEmbedding;
        }
        enrollment.updatedAt = Date.now();

        await enrollment.save();

        res.json({ 
            success: true, 
            message: 'Enrollment updated successfully' 
        });

    } catch (error) {
        console.error('Error updating enrollment:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error',
            error: error.message 
        });
    }
});

// Delete enrollment
app.delete('/api/enrollment/:enrollmentNo', async (req, res) => {
    try {
        const { enrollmentNo } = req.params;

        const result = await Enrollment.deleteOne({ enrollmentNo });

        if (result.deletedCount === 0) {
            return res.status(404).json({ 
                success: false, 
                message: 'Enrollment not found' 
            });
        }

        res.json({ 
            success: true, 
            message: 'Enrollment deleted successfully' 
        });

    } catch (error) {
        console.error('Error deleting enrollment:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error',
            error: error.message 
        });
    }
});

// Get all enrollments (for admin)
app.get('/api/enrollments', async (req, res) => {
    try {
        const enrollments = await Enrollment.find().select('-password -faceEmbedding');

        res.json({ 
            success: true, 
            count: enrollments.length,
            data: enrollments 
        });

    } catch (error) {
        console.error('Error fetching enrollments:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error',
            error: error.message 
        });
    }
});

// Verify enrollment (for authentication)
app.post('/api/enrollment/verify', async (req, res) => {
    try {
        const { enrollmentNo, password } = req.body;

        const enrollment = await Enrollment.findOne({ enrollmentNo });

        if (!enrollment) {
            return res.status(404).json({ 
                success: false, 
                message: 'Enrollment not found' 
            });
        }

        const isPasswordValid = await bcrypt.compare(password, enrollment.password);

        if (!isPasswordValid) {
            return res.status(401).json({ 
                success: false, 
                message: 'Invalid password' 
            });
        }

        res.json({ 
            success: true, 
            message: 'Verification successful',
            data: {
                enrollmentNo: enrollment.enrollmentNo,
                faceEmbedding: enrollment.faceEmbedding
            }
        });

    } catch (error) {
        console.error('Error verifying enrollment:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error',
            error: error.message 
        });
    }
});

// Start server
app.listen(PORT, () => {
    console.log(`✓ Server is running on port ${PORT}`);
    console.log(`✓ API endpoint: http://localhost:${PORT}/api`);
});
