package com.project.job.data.manager

import android.content.Context
import android.content.Intent
import android.util.Log
import com.project.job.data.repository.TokenRepository
import com.project.job.utils.TokenExpiredReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AuthenticationManager quản lý trạng thái đăng nhập của ứng dụng
 * Xử lý logout tự động khi token hết hạn
 */
class AuthenticationManager(
    private val context: Context,
    private val tokenRepository: TokenRepository
) {
    
    companion object {
        private const val TAG = "AuthenticationManager"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: AuthenticationManager? = null
        
        fun getInstance(context: Context, tokenRepository: TokenRepository): AuthenticationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthenticationManager(context.applicationContext, tokenRepository).also { INSTANCE = it }
            }
        }
    }
    
    // StateFlow để theo dõi trạng thái đăng nhập
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    // StateFlow để theo dõi trạng thái token expired
    private val _tokenExpired = MutableStateFlow(false)
    val tokenExpired: StateFlow<Boolean> = _tokenExpired.asStateFlow()
    
    init {
        // Kiểm tra trạng thái đăng nhập ban đầu
        checkAuthenticationStatus()
    }
    
    /**
     * Kiểm tra trạng thái đăng nhập hiện tại
     */
    private fun checkAuthenticationStatus() {
        try {
            // Sử dụng runBlocking trong init là không lý tưởng, 
            // nhưng cần thiết để khởi tạo trạng thái ban đầu
            kotlinx.coroutines.runBlocking {
                val hasToken = !tokenRepository.getAccessToken().isNullOrEmpty()
                _isAuthenticated.value = hasToken
                Log.d(TAG, "Authentication status checked: $hasToken")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking authentication status", e)
            _isAuthenticated.value = false
        }
    }
    
    /**
     * Đánh dấu user đã đăng nhập thành công
     */
    fun onLoginSuccess() {
        Log.d(TAG, "User logged in successfully")
        _isAuthenticated.value = true
        _tokenExpired.value = false
    }
    
    /**
     * Xử lý khi token hết hạn
     * Tự động logout và chuyển về màn hình đăng nhập
     */
    suspend fun onTokenExpired() {
        Log.w(TAG, "Token expired, logging out user")
        
        // Clear tất cả tokens
        tokenRepository.clearAuthTokens()
        
        // Update states
        _isAuthenticated.value = false
        _tokenExpired.value = true
        
        // Redirect to login screen
        redirectToLogin()
    }
    
    /**
     * Logout thủ công
     */
    suspend fun logout() {
        Log.d(TAG, "User logging out manually")
        
        // Clear tất cả tokens
        tokenRepository.clearAuthTokens()
        
        // Update states
        _isAuthenticated.value = false
        _tokenExpired.value = false
        
        // Redirect to login screen
        redirectToLogin()
    }
    
    /**
     * Chuyển hướng về màn hình đăng nhập
     */
    private fun redirectToLogin() {
        try {
            Log.d(TAG, "Attempting to redirect to login screen")
            
            // Sử dụng broadcast để thông báo cho UI layer
            val broadcastIntent = Intent(TokenExpiredReceiver.ACTION_TOKEN_EXPIRED).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(broadcastIntent)
            Log.d(TAG, "Token expired broadcast sent")
            
            // Fallback: Thử redirect trực tiếp nếu broadcast không hoạt động
            try {
                val loginIntent = Intent().apply {
                    setClassName(context, "com.project.job.ui.login.LoginActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(loginIntent)
                Log.d(TAG, "Direct redirect to login screen successful")
            } catch (directException: Exception) {
                Log.w(TAG, "Direct redirect failed, trying launcher intent", directException)
                
                // Fallback cuối cùng: Mở app launcher
                val launcherIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (launcherIntent != null) {
                    launcherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(launcherIntent)
                    Log.d(TAG, "Launcher intent redirect successful")
                } else {
                    Log.e(TAG, "No launcher intent available")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "All redirect methods failed", e)
        }
    }
    
    /**
     * Kiểm tra xem user có đang đăng nhập không
     */
    suspend fun isUserAuthenticated(): Boolean {
        val hasAccessToken = !tokenRepository.getAccessToken().isNullOrEmpty()
        val hasRefreshToken = !tokenRepository.getRefreshToken().isNullOrEmpty()
        
        val isAuthenticated = hasAccessToken || hasRefreshToken
        _isAuthenticated.value = isAuthenticated
        
        return isAuthenticated
    }
    
    /**
     * Reset token expired state
     */
    fun resetTokenExpiredState() {
        _tokenExpired.value = false
    }
    
    /**
     * Get current authentication info for debugging
     */
    suspend fun getAuthInfo(): String {
        val hasAccessToken = !tokenRepository.getAccessToken().isNullOrEmpty()
        val hasRefreshToken = !tokenRepository.getRefreshToken().isNullOrEmpty()
        
        return """
            Authentication Status:
            - Is Authenticated: ${_isAuthenticated.value}
            - Token Expired: ${_tokenExpired.value}
            - Has Access Token: $hasAccessToken
            - Has Refresh Token: $hasRefreshToken
        """.trimIndent()
    }
}
