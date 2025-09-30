package com.project.job.data.manager

import android.util.Log
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.RefreshTokenResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * ApiTokenManager quản lý việc refresh API token một cách tập trung
 * Tránh việc gọi refresh token đồng thời nhiều lần
 * Khác với utils/TokenManager (quản lý Firebase token)
 */
class ApiTokenManager(
    private val tokenRepository: TokenRepository,
    private val onTokenExpired: suspend () -> Unit = {}
) {
    
    companion object {
        private const val TAG = "ApiTokenManager"
    }
    
    // Mutex để đảm bảo chỉ có một refresh token request tại một thời điểm
    private val refreshMutex = Mutex()
    
    // Flag để track trạng thái refresh
    private var isRefreshing = false
    
    /**
     * Refresh access token một cách an toàn
     * Sử dụng mutex để tránh race condition
     */
    suspend fun refreshTokenSafely(): NetworkResult<RefreshTokenResponse?> {
        return refreshMutex.withLock {
            if (isRefreshing) {
                Log.d(TAG, "Token refresh already in progress, waiting...")
                // Nếu đang refresh, đợi và trả về kết quả hiện tại
                return@withLock NetworkResult.Error("Token refresh in progress")
            }
            
            try {
                isRefreshing = true
                Log.d(TAG, "Starting token refresh...")
                
                val refreshToken = tokenRepository.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token available")
                    return@withLock NetworkResult.Error("No refresh token available")
                }
                
                val result = tokenRepository.refreshAccessToken()
                
                when (result) {
                    is NetworkResult.Success -> {
                        val refreshData = result.data
                        Log.d(TAG, "Refresh response received: success=${refreshData?.success}, data=${refreshData?.data}")
                        
                        if (refreshData != null && refreshData.success) {
                            Log.d(TAG, "Token refreshed successfully")
                            
                            // Kiểm tra tokens không null trước khi lưu
                            val newAccessToken = refreshData.data.idToken
                            val newRefreshToken = refreshData.data.refreshToken
                            
                            Log.d(TAG, "New tokens - Access: ${newAccessToken?.length ?: 0} chars, Refresh: ${newRefreshToken?.length ?: 0} chars")
                            
                            if (!newAccessToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
                                // Lưu tokens mới
                                tokenRepository.saveAccessToken(newAccessToken)
                                tokenRepository.saveRefreshToken(newRefreshToken)
                                Log.d(TAG, "New tokens saved successfully")
                            } else {
                                Log.e(TAG, "Received null or empty tokens from refresh response - Access: '$newAccessToken', Refresh: '$newRefreshToken'")
                                tokenRepository.clearAuthTokens()
                                onTokenExpired()
                            }
                        } else {
                            Log.e(TAG, "Token refresh failed: ${refreshData?.message}")
                            // Clear tokens nếu refresh thất bại
                            tokenRepository.clearAuthTokens()
                            // Notify token expired
                            onTokenExpired()
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Token refresh error: ${result.message}")
                        // Clear tokens nếu có lỗi
                        tokenRepository.clearAuthTokens()
                        // Notify token expired
                        onTokenExpired()
                    }
                }
                
                return@withLock result
                
            } finally {
                isRefreshing = false
                Log.d(TAG, "Token refresh process completed")
            }
        }
    }
    
    /**
     * Kiểm tra xem có đang refresh token không
     */
    fun isRefreshingToken(): Boolean = isRefreshing
    
    /**
     * Lấy access token hiện tại
     */
    suspend fun getCurrentAccessToken(): String? {
        return tokenRepository.getAccessToken()
    }
    
    /**
     * Kiểm tra xem có refresh token không
     */
    suspend fun hasRefreshToken(): Boolean {
        return !tokenRepository.getRefreshToken().isNullOrEmpty()
    }
    
    /**
     * Clear tất cả tokens
     */
    suspend fun clearAllTokens() {
        Log.d(TAG, "Clearing all tokens")
        tokenRepository.clearAuthTokens()
    }
}
