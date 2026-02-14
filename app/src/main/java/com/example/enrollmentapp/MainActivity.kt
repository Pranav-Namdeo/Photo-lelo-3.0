package com.example.enrollmentapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var enrollmentNoInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var takeFacialDataButton: Button
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView
    
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 200
    
    private var faceEmbedding: FloatArray? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        enrollmentNoInput = findViewById(R.id.enrollmentNoInput)
        passwordInput = findViewById(R.id.passwordInput)
        takeFacialDataButton = findViewById(R.id.takeFacialDataButton)
        saveButton = findViewById(R.id.saveButton)
        statusText = findViewById(R.id.statusText)
        
        // Set up button listeners
        takeFacialDataButton.setOnClickListener {
            handleTakeFacialData()
        }
        
        saveButton.setOnClickListener {
            handleSave()
        }
    }
    
    private fun handleTakeFacialData() {
        if (checkCameraPermission()) {
            startCameraActivity()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun startCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            faceEmbedding = data?.getFloatArrayExtra("face_embedding")
            
            if (faceEmbedding != null) {
                statusText.text = "Facial data captured successfully! (${faceEmbedding!!.size} features)"
                Toast.makeText(this, "Face captured successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleSave() {
        val enrollmentNo = enrollmentNoInput.text.toString()
        val password = passwordInput.text.toString()
        
        if (enrollmentNo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (faceEmbedding == null) {
            Toast.makeText(this, "Please capture facial data first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Save enrollment data to database or server
        // Data includes: enrollmentNo, password, faceEmbedding
        
        Toast.makeText(this, "Enrollment saved successfully!", Toast.LENGTH_LONG).show()
        statusText.text = "Enrollment saved for: $enrollmentNo"
        
        // Clear form
        enrollmentNoInput.text.clear()
        passwordInput.text.clear()
        faceEmbedding = null
        statusText.text = "Ready to capture"
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
