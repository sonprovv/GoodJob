package com.project.job.network

import android.content.Context
import android.util.Log
import com.project.job.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * AuthInterceptor - Tự động thêm Authorization header và refresh token khi cần
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    
    private val tokenManager = TokenManager.getInstance(context)
    
    companion object {
        private const val TAG = "AuthInterceptor"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Lấy token hợp lệ (tự động refresh nếu cần)
        val token = runBlocking {
            try {
                tokenManager.getValidToken()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting valid token", e)
                null
            }
        }
        
        // Tạo request với Authorization header
        val requestWithAuth = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .build()
        } else {
            Log.w(TAG, "No valid token available, proceeding without Authorization header")
            originalRequest
        }
        
        // Thực hiện request
        val response = chain.proceed(requestWithAuth)
        
        // Nếu nhận được 401 Unauthorized, thử refresh token và retry
        if (response.code == 401 && !token.isNullOrEmpty()) {
            Log.w(TAG, "Received 401 Unauthorized, attempting to refresh token")
            
            response.close() // Đóng response cũ
            
            // Thử refresh token
            val refreshedToken = runBlocking {
                try {
                    tokenManager.getCurrentFirebaseToken(forceRefresh = true)
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing token", e)
                    null
                }
            }
            
            if (!refreshedToken.isNullOrEmpty()) {
                Log.d(TAG, "Token refreshed successfully, retrying request")
                
                // Retry request với token mới
                val retryRequest = originalRequest.newBuilder()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + refreshedToken)
                    .build()
                
                return chain.proceed(retryRequest)
            } else {
                Log.e(TAG, "Failed to refresh token, user may need to re-login")
                // Có thể trigger logout hoặc redirect to login
                // tokenManager.signOutFirebase()
            }
        }
        
        return response
    }
}

/**
 * TokenAuthenticator - Xử lý authentication challenges (401 responses)
 * Sử dụng cùng với AuthInterceptor để có cơ chế retry mạnh mẽ hơn
 */
class TokenAuthenticator(private val context: Context) : okhttp3.Authenticator {
    
    private val tokenManager = TokenManager.getInstance(context)
    
    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
    
    override fun authenticate(route: okhttp3.Route?, response: Response): okhttp3.Request? {
        Log.w(TAG, "Authentication challenge received, attempting token refresh")
        
        // Kiểm tra xem đã thử refresh token chưa để tránh infinite loop
        val currentToken = response.request.header(AUTHORIZATION_HEADER)
        
        val refreshedToken = runBlocking {
            try {
                tokenManager.getCurrentFirebaseToken(forceRefresh = true)
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing token in authenticator", e)
                null
            }
        }
        
        return if (!refreshedToken.isNullOrEmpty() && 
                   currentToken != BEARER_PREFIX + refreshedToken) {
            Log.d(TAG, "Creating retry request with refreshed token")
            response.request.newBuilder()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + refreshedToken)
                .build()
        } else {
            Log.e(TAG, "Cannot refresh token or token unchanged, giving up")
            // Trả về null để dừng retry
            null
        }
    }
}
