package com.example.facialcheckapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var enrollmentNoInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var apiService: ApiService
    private lateinit var secureStorage: SecureStorage
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize services
        apiService = ApiService(this)
        secureStorage = SecureStorage(this)
        
        // Check if already logged in
        if (secureStorage.isLoggedIn()) {
            navigateToHome()
            return
        }
        
        setContentView(R.layout.activity_login)
        
        // Initialize views
        enrollmentNoInput = findViewById(R.id.enrollmentNoInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)
        
        // Set up login button
        loginButton.setOnClickListener {
            handleLogin()
        }
    }
    
    private fun handleLogin() {
        val enrollmentNo = enrollmentNoInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        
        // Validation
        if (enrollmentNo.isEmpty()) {
            statusText.text = "Please enter enrollment number"
            statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            return
        }
        
        if (password.isEmpty()) {
            statusText.text = "Please enter password"
            statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            return
        }
        
        // Show loading
        loginButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        statusText.text = "Verifying credentials..."
        statusText.setTextColor(getColor(android.R.color.darker_gray))
        
        // Perform login
        lifecycleScope.launch {
            try {
                val response = apiService.verifyLogin(enrollmentNo, password)
                
                if (response.success && response.faceEmbedding != null) {
                    // Save credentials and facial data securely
                    secureStorage.saveEnrollmentNumber(enrollmentNo)
                    secureStorage.saveFaceEmbedding(response.faceEmbedding)
                    
                    statusText.text = "Login successful!"
                    statusText.setTextColor(getColor(android.R.color.holo_green_dark))
                    
                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome! Facial data saved securely.",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Navigate to home
                    navigateToHome()
                    
                } else {
                    statusText.text = response.message
                    statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                    
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${response.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                statusText.text = "Error: ${e.message}"
                statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                
                Toast.makeText(
                    this@LoginActivity,
                    "Network error occurred",
                    Toast.LENGTH_LONG
                ).show()
                
            } finally {
                loginButton.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
