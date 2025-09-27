package com.project.job.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.project.job.data.source.local.PreferencesManager
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TokenManager - Quản lý việc refresh Firebase ID token tự động
 * Sử dụng Firebase Auth để refresh token khi hết hạn
 */
class TokenManager(private val context: Context) {
    private val preferencesManager = PreferencesManager(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "TokenManager"
        private const val TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000L // 5 phút trước khi hết hạn
        
        @Volatile
        private var INSTANCE: TokenManager? = null
        
        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Lấy Firebase ID token hiện tại, tự động refresh nếu cần
     */
    suspend fun getCurrentFirebaseToken(forceRefresh: Boolean = false): String? {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    Log.w(TAG, "No Firebase user found")
                    return@withContext null
                }
                
                // Lấy token với force refresh nếu cần
                val tokenResult = currentUser.getIdToken(forceRefresh).await()
                val token = tokenResult.token
                
                if (token != null) {
                    Log.d(TAG, "Firebase token obtained successfully")
                    // Lưu token mới vào preferences
                    preferencesManager.saveAuthToken(token)
                    
                    // Lưu thời gian hết hạn (Firebase token thường có thời hạn 1 giờ)
                    val expirationTime = tokenResult.expirationTimestamp
                    preferencesManager.saveTokenExpirationTime(expirationTime)
                    
                    Log.d(TAG, "Token expires at: ${java.util.Date(expirationTime)}")
                } else {
                    Log.e(TAG, "Failed to get Firebase token")
                }
                
                token
            } catch (e: Exception) {
                Log.e(TAG, "Error getting Firebase token", e)
                null
            }
        }
    }
    
    /**
     * Kiểm tra xem token có sắp hết hạn không
     */
    fun isTokenExpiringSoon(): Boolean {
        val expirationTime = preferencesManager.getTokenExpirationTime()
        if (expirationTime == 0L) return true
        
        val currentTime = System.currentTimeMillis()
        val timeUntilExpiration = expirationTime - currentTime
        
        Log.d(TAG, "Time until token expiration: ${timeUntilExpiration / 1000} seconds")
        
        return timeUntilExpiration <= TOKEN_REFRESH_THRESHOLD
    }
    
    /**
     * Refresh token nếu cần thiết
     */
    suspend fun refreshTokenIfNeeded(): String? {
        return if (isTokenExpiringSoon()) {
            Log.d(TAG, "Token is expiring soon, refreshing...")
            getCurrentFirebaseToken(forceRefresh = true)
        } else {
            Log.d(TAG, "Token is still valid")
            preferencesManager.getAuthToken()
        }
    }
    
    /**
     * Lấy token hợp lệ cho API calls
     */
    suspend fun getValidToken(): String? {
        val currentToken = preferencesManager.getAuthToken()
        
        // Nếu không có token hoặc token sắp hết hạn, refresh
        return if (currentToken.isNullOrEmpty() || isTokenExpiringSoon()) {
            Log.d(TAG, "Getting fresh token...")
            getCurrentFirebaseToken(forceRefresh = true)
        } else {
            Log.d(TAG, "Using existing valid token")
            currentToken
        }
    }
    
    /**
     * Xóa tất cả token data
     */
    fun clearTokenData() {
        preferencesManager.clearAuthData()
        Log.d(TAG, "Token data cleared")
    }
    
    /**
     * Kiểm tra trạng thái đăng nhập Firebase
     */
    fun isFirebaseUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    /**
     * Đăng xuất Firebase
     */
    fun signOutFirebase() {
        firebaseAuth.signOut()
        clearTokenData()
        Log.d(TAG, "Firebase user signed out")
    }
}
