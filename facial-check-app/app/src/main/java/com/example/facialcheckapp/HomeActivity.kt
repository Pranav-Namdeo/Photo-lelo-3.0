package com.example.facialcheckapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {
    
    private lateinit var welcomeText: TextView
    private lateinit var enrollmentNoText: TextView
    private lateinit var faceVerificationButton: Button
    private lateinit var logoutButton: Button
    
    private lateinit var secureStorage: SecureStorage
    private lateinit var faceComparator: FaceComparator
    
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 200
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        secureStorage = SecureStorage(this)
        faceComparator = FaceComparator()
        
        // Check if logged in
        if (!secureStorage.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        // Initialize views
        welcomeText = findViewById(R.id.welcomeText)
        enrollmentNoText = findViewById(R.id.enrollmentNoText)
        faceVerificationButton = findViewById(R.id.faceVerificationButton)
        logoutButton = findViewById(R.id.logoutButton)
        
        // Display user info
        val enrollmentNo = secureStorage.getEnrollmentNumber()
        
        enrollmentNoText.text = "Enrollment No: $enrollmentNo"
        welcomeText.text = "Welcome, $enrollmentNo!"
        
        // Face verification button
        faceVerificationButton.setOnClickListener {
            handleFaceVerification()
        }
        
        // Logout button
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun handleFaceVerification() {
        if (checkCameraPermission()) {
            startVerificationCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun startVerificationCamera() {
        val intent = Intent(this, VerificationCameraActivity::class.java)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val capturedEmbedding = data?.getFloatArrayExtra("face_embedding")
            
            if (capturedEmbedding != null) {
                // Get stored enrollment embedding
                val storedEmbedding = secureStorage.getFaceEmbedding()
                
                if (storedEmbedding != null) {
                    // Perform offline face comparison
                    performFaceVerification(capturedEmbedding, storedEmbedding)
                } else {
                    Toast.makeText(
                        this,
                        "Error: No stored facial data found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun performFaceVerification(capturedEmbedding: FloatArray, storedEmbedding: FloatArray) {
        // Compare the two embeddings
        val result = faceComparator.compareFaces(storedEmbedding, capturedEmbedding)
        
        // Show result dialog
        showVerificationResult(result)
    }
    
    private fun showVerificationResult(result: FaceMatchResult) {
        val title = if (result.isMatch) "✓ Verification Successful" else "✗ Verification Failed"
        val titleColor = if (result.isMatch) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        
        val message = buildString {
            append(result.message)
            append("\n\n")
            append("Similarity: ${faceComparator.getSimilarityPercentage(result.similarity)}%\n")
            append("Distance: ${"%.4f".format(result.distance)}\n")
            append("\n")
            if (result.isMatch) {
                append("The captured face matches your enrolled face.")
            } else {
                append("The captured face does not match your enrolled face.")
            }
        }
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
        
        dialog.show()
        
        // Color the title
        dialog.window?.decorView?.findViewById<TextView>(
            resources.getIdentifier("alertTitle", "id", "android")
        )?.setTextColor(getColor(titleColor))
        
        // Show toast
        Toast.makeText(
            this,
            if (result.isMatch) "Face Verified ✓" else "Face Not Verified ✗",
            Toast.LENGTH_LONG
        ).show()
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
                startVerificationCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? Your facial data will be removed from this device.")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        secureStorage.clearAll()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
