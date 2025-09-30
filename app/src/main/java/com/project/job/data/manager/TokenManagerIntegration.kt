package com.project.job.data.manager

import android.content.Context
import android.util.Log
import com.project.job.data.repository.TokenRepository

/**
 * TokenManagerIntegration - Quản lý API Token một cách tập trung
 * 
 * Quản lý API Access/Refresh Token từ backend
 */
class TokenManagerIntegration(
    private val tokenRepository: TokenRepository,
    private val authenticationManager: AuthenticationManager
) {
    
    companion object {
        private const val TAG = "TokenManagerIntegration"
        
        @Volatile
        private var INSTANCE: TokenManagerIntegration? = null
        
        fun getInstance(
            tokenRepository: TokenRepository,
            authenticationManager: AuthenticationManager
        ): TokenManagerIntegration {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManagerIntegration(
                    tokenRepository,
                    authenticationManager
                ).also { INSTANCE = it }
            }
        }
    }
    
    // API Token Manager (cho backend API)
    private val apiTokenManager = ApiTokenManager(tokenRepository) {
        // Callback khi API token expired
        Log.w(TAG, "API token expired, triggering logout")
        kotlinx.coroutines.runBlocking {
            authenticationManager.onTokenExpired()
        }
    }
    
    /**
     * Lấy API access token hợp lệ (cho backend API calls)
     */
    suspend fun getCurrentAccessToken(): String? {
        return try {
            val token = apiTokenManager.getCurrentAccessToken()
            Log.d(TAG, "API token obtained: ${token != null}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting API token", e)
            null
        }
    }
    
    /**
     * Refresh API token một cách an toàn
     */
    suspend fun refreshTokenSafely() = apiTokenManager.refreshTokenSafely()
    
    /**
     * Kiểm tra có API refresh token không
     */
    suspend fun hasRefreshToken(): Boolean {
        return apiTokenManager.hasRefreshToken()
    }
    
    /**
     * Kiểm tra xem có đang refresh token không
     */
    fun isRefreshingToken(): Boolean {
        return apiTokenManager.isRefreshingToken()
    }
    
    /**
     * Clear tất cả API tokens
     */
    suspend fun clearAllTokens() {
        Log.d(TAG, "Clearing all API tokens")
        apiTokenManager.clearAllTokens()
        Log.d(TAG, "All API tokens cleared successfully")
    }
    
    /**
     * Kiểm tra trạng thái authentication
     */
    suspend fun getAuthenticationStatus(): AuthStatus {
        val hasApiToken = getCurrentAccessToken() != null
        val hasRefreshToken = hasRefreshToken()
        
        return AuthStatus(
            hasApiAccessToken = hasApiToken,
            hasApiRefreshToken = hasRefreshToken,
            isAuthenticated = hasApiToken || hasRefreshToken
        )
    }
    
    /**
     * Debug info cho API token manager
     */
    suspend fun getDebugInfo(): String {
        val authStatus = getAuthenticationStatus()
        val apiToken = getCurrentAccessToken()
        
        return """
            API Token Manager Debug Info:
            ============================
            Has API Access Token: ${authStatus.hasApiAccessToken}
            Has API Refresh Token: ${authStatus.hasApiRefreshToken}
            Is Authenticated: ${authStatus.isAuthenticated}
            Is Refreshing Token: ${isRefreshingToken()}
            
            API Token Length: ${apiToken?.length ?: 0}
        """.trimIndent()
    }
}

/**
 * Data class để represent trạng thái authentication
 */
data class AuthStatus(
    val hasApiAccessToken: Boolean,
    val hasApiRefreshToken: Boolean,
    val isAuthenticated: Boolean
)
