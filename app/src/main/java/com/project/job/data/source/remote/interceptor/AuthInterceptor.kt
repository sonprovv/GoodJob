package com.project.job.data.source.remote.interceptor

import android.util.Log
import com.project.job.data.manager.AuthenticationManager
import com.project.job.data.manager.TokenManagerIntegration
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.remote.NetworkResult
import com.project.job.utils.AuthRequired
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import retrofit2.Invocation
import okhttp3.Response

// Interceptor để thêm Authorization header và tự động refresh token
class AuthInterceptor(
    private val tokenRepository: TokenRepository,
    private val authenticationManager: AuthenticationManager? = null
) : Interceptor {
    
    companion object {
        private const val TAG = "AuthInterceptor"
    }
    
    // TokenManagerIntegration để quản lý refresh token
    private val tokenManager by lazy {
        authenticationManager?.let { authManager ->
            TokenManagerIntegration.getInstance(tokenRepository, authManager)
        }
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val invocation = chain.request().tag(Invocation::class.java)
        val needAuth = invocation?.method()?.getAnnotation(AuthRequired::class.java) != null

        val newRequest = if (needAuth) {
            val builder = chain.request().newBuilder()

            val token = runBlocking {
                tokenManager?.getCurrentAccessToken() ?: tokenRepository.getAccessToken()
            }

            token?.let {
                builder.addHeader("Authorization", "Bearer $it")
                Log.d(TAG, "Added Authorization header with token")
            } ?: run {
                Log.w(TAG, "No access token available for authenticated request")
            }

            builder.build()
        } else {
            chain.request()
        }

        // Thực hiện request
        val response = chain.proceed(newRequest)
        
        // Kiểm tra nếu response là 401 (Unauthorized) và cần auth
        if (needAuth && response.code == 401) {
            Log.d(TAG, "Received 401 error, attempting to refresh token")
            
            // Đóng response cũ
            response.close()
            
            // Kiểm tra xem có refresh token không
            val hasRefreshToken = runBlocking {
                tokenManager?.hasRefreshToken() ?: false
            }
            
            if (!hasRefreshToken) {
                Log.e(TAG, "No refresh token available, clearing auth data")
                runBlocking {
                    tokenManager?.clearAllTokens() ?: tokenRepository.clearAuthTokens()
                }
                return response
            }
            
            // Thử refresh token sử dụng TokenManagerIntegration
            val refreshResult = runBlocking {
                tokenManager?.refreshTokenSafely() ?: NetworkResult.Error("TokenManager not available")
            }
            
            when (refreshResult) {
                is NetworkResult.Success -> {
                    val refreshData = refreshResult.data
                    if (refreshData != null && refreshData.success) {
                        Log.d(TAG, "Token refreshed successfully, retrying original request")
                        
                        // Tạo request mới với token mới
                        val newRequestWithRefreshedToken = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${refreshData.data.idToken}")
                            .build()
                        
                        // Thực hiện lại request với token mới
                        return chain.proceed(newRequestWithRefreshedToken)
                    } else {
                        Log.e(TAG, "Token refresh failed: ${refreshData?.message}")
                        runBlocking {
                            tokenManager?.clearAllTokens() ?: tokenRepository.clearAuthTokens()
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Token refresh error: ${refreshResult.message}")
                    runBlocking {
                        tokenManager?.clearAllTokens() ?: tokenRepository.clearAuthTokens()
                    }
                }
            }
        }

        return response
    }
}