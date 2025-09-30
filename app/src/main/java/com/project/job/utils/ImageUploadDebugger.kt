package com.project.job.utils

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Utility class để debug image upload issues
 */
object ImageUploadDebugger {
    
    private const val TAG = "ImageUploadDebugger"
    
    /**
     * Validate file trước khi upload
     */
    fun validateImageFile(file: File): ValidationResult {
        val result = ValidationResult()
        
        // Check file exists
        if (!file.exists()) {
            result.isValid = false
            result.errors.add("File không tồn tại: ${file.absolutePath}")
            return result
        }
        
        // Check file size
        val fileSizeBytes = file.length()
        val fileSizeMB = fileSizeBytes / (1024 * 1024)
        
        Log.d(TAG, "File validation - Path: ${file.absolutePath}")
        Log.d(TAG, "File validation - Size: $fileSizeBytes bytes ($fileSizeMB MB)")
        Log.d(TAG, "File validation - Extension: ${file.extension}")
        Log.d(TAG, "File validation - Name: ${file.name}")
        
        if (fileSizeBytes == 0L) {
            result.isValid = false
            result.errors.add("File rỗng (0 bytes)")
        }
        
        // Check file size limit (10MB)
        if (fileSizeMB > 10) {
            result.isValid = false
            result.errors.add("File quá lớn: $fileSizeMB MB (tối đa 10MB)")
        }
        
        // Check file extension
        val validExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
        if (!validExtensions.contains(file.extension.lowercase())) {
            result.isValid = false
            result.errors.add("Định dạng file không hỗ trợ: ${file.extension}")
        }
        
        // Check if file is readable
        if (!file.canRead()) {
            result.isValid = false
            result.errors.add("Không thể đọc file")
        }
        
        return result
    }
    
    /**
     * Tạo MultipartBody.Part với các options khác nhau
     */
    fun createMultipartBody(file: File, fieldName: String = "image"): MultipartBody.Part? {
        return try {
            val mimeType = getMimeType(file)
            Log.d(TAG, "Creating multipart body - Field: $fieldName, MIME: $mimeType")
            
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating multipart body", e)
            null
        }
    }
    
    /**
     * Get MIME type based on file extension
     */
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            else -> "image/jpeg" // Default fallback
        }
    }
    
    /**
     * Log detailed information về request
     */
    fun logUploadAttempt(file: File, fieldName: String, endpoint: String) {
        Log.d(TAG, "=== UPLOAD ATTEMPT ===")
        Log.d(TAG, "Endpoint: $endpoint")
        Log.d(TAG, "Field name: $fieldName")
        Log.d(TAG, "File path: ${file.absolutePath}")
        Log.d(TAG, "File name: ${file.name}")
        Log.d(TAG, "File size: ${file.length()} bytes")
        Log.d(TAG, "File extension: ${file.extension}")
        Log.d(TAG, "MIME type: ${getMimeType(file)}")
        Log.d(TAG, "File exists: ${file.exists()}")
        Log.d(TAG, "File readable: ${file.canRead()}")
        Log.d(TAG, "=====================")
    }
    
    data class ValidationResult(
        var isValid: Boolean = true,
        val errors: MutableList<String> = mutableListOf()
    ) {
        fun getErrorMessage(): String {
            return errors.joinToString("\n")
        }
    }
}
