package com.example.facialcheckapp

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "facial_check_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveEnrollmentNumber(enrollmentNo: String) {
        sharedPreferences.edit().putString(KEY_ENROLLMENT_NO, enrollmentNo).apply()
    }
    
    fun getEnrollmentNumber(): String? {
        return sharedPreferences.getString(KEY_ENROLLMENT_NO, null)
    }
    
    fun saveFaceEmbedding(embedding: FloatArray) {
        val embeddingString = embedding.joinToString(",")
        sharedPreferences.edit().putString(KEY_FACE_EMBEDDING, embeddingString).apply()
    }
    
    fun getFaceEmbedding(): FloatArray? {
        val embeddingString = sharedPreferences.getString(KEY_FACE_EMBEDDING, null)
        return embeddingString?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }
    
    fun isLoggedIn(): Boolean {
        return getEnrollmentNumber() != null && getFaceEmbedding() != null
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    companion object {
        private const val KEY_ENROLLMENT_NO = "enrollment_number"
        private const val KEY_FACE_EMBEDDING = "face_embedding"
    }
}
