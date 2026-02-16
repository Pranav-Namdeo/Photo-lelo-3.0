package com.example.facialcheckapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiService(private val context: Context) {
    
    companion object {
        private const val TAG = "ApiService"
    }
    
    private val baseUrl: String
        get() = context.getString(R.string.server_base_url)
    
    suspend fun verifyLogin(
        enrollmentNo: String,
        password: String
    ): LoginResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/enrollment/verify")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val jsonObject = JSONObject()
                jsonObject.put("enrollmentNo", enrollmentNo)
                jsonObject.put("password", password)
                
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonObject.toString())
                writer.flush()
                writer.close()
                
                val responseCode = connection.responseCode
                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                val responseJson = JSONObject(response.toString())
                val success = responseJson.optBoolean("success", false)
                val message = responseJson.optString("message", "Unknown error")
                
                var faceEmbedding: FloatArray? = null
                
                if (success) {
                    val dataObject = responseJson.optJSONObject("data")
                    if (dataObject != null) {
                        val embeddingArray = dataObject.optJSONArray("faceEmbedding")
                        if (embeddingArray != null) {
                            faceEmbedding = FloatArray(embeddingArray.length())
                            for (i in 0 until embeddingArray.length()) {
                                faceEmbedding[i] = embeddingArray.getDouble(i).toFloat()
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Login response: success=$success, message=$message, embedding size=${faceEmbedding?.size}")
                
                LoginResponse(success, message, responseCode, faceEmbedding)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying login", e)
                LoginResponse(false, "Network error: ${e.message}", 0, null)
            }
        }
    }
}

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val faceEmbedding: FloatArray?
)
