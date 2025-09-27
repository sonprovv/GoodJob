package com.project.job.utils

import android.content.Context
import android.util.Log
import com.project.job.data.source.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * TokenTestHelper - Utility class để test và debug tính năng auto refresh token
 */
class TokenTestHelper(private val context: Context) {
    
    private val tokenManager = TokenManager.getInstance(context)
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "TokenTestHelper"
    }
    
    /**
     * Test việc refresh token thủ công
     */
    fun testManualTokenRefresh() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "=== TESTING MANUAL TOKEN REFRESH ===")
                
                // Hiển thị trạng thái token hiện tại
                logCurrentTokenStatus()
                
                // Thử refresh token
                val refreshedToken = tokenManager.getCurrentFirebaseToken(forceRefresh = true)
                
                if (refreshedToken != null) {
                    Log.d(TAG, "✅ Manual token refresh successful")
                    Log.d(TAG, "New token: ${refreshedToken.take(20)}...")
                } else {
                    Log.e(TAG, "❌ Manual token refresh failed")
                }
                
                // Hiển thị trạng thái sau khi refresh
                logCurrentTokenStatus()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during manual token refresh test", e)
            }
        }
    }
    
    /**
     * Test việc kiểm tra token expiration
     */
    fun testTokenExpirationCheck() {
        Log.d(TAG, "=== TESTING TOKEN EXPIRATION CHECK ===")
        
        val isExpiringSoon = tokenManager.isTokenExpiringSoon()
        val expirationTime = preferencesManager.getTokenExpirationTime()
        val currentTime = System.currentTimeMillis()
        
        Log.d(TAG, "Current time: ${java.util.Date(currentTime)}")
        Log.d(TAG, "Token expiration time: ${if (expirationTime == 0L) "Not set" else java.util.Date(expirationTime)}")
        Log.d(TAG, "Is token expiring soon: $isExpiringSoon")
        
        if (expirationTime > 0L) {
            val timeUntilExpiration = expirationTime - currentTime
            Log.d(TAG, "Time until expiration: ${timeUntilExpiration / 1000} seconds")
        }
    }
    
    /**
     * Test việc lấy valid token (sẽ auto refresh nếu cần)
     */
    fun testGetValidToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "=== TESTING GET VALID TOKEN ===")
                
                logCurrentTokenStatus()
                
                val validToken = tokenManager.getValidToken()
                
                if (validToken != null) {
                    Log.d(TAG, "✅ Got valid token: ${validToken.take(20)}...")
                } else {
                    Log.e(TAG, "❌ Failed to get valid token")
                }
                
                logCurrentTokenStatus()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during get valid token test", e)
            }
        }
    }
    
    /**
     * Simulate token expiration để test auto refresh
     */
    fun simulateTokenExpiration() {
        Log.d(TAG, "=== SIMULATING TOKEN EXPIRATION ===")
        
        // Set expiration time to past (1 minute ago)
        val pastTime = System.currentTimeMillis() - (60 * 1000)
        preferencesManager.saveTokenExpirationTime(pastTime)
        
        Log.d(TAG, "Token expiration time set to: ${java.util.Date(pastTime)}")
        Log.d(TAG, "Token should now be considered expired")
        
        testTokenExpirationCheck()
    }
    
    /**
     * Log trạng thái hiện tại của tất cả tokens
     */
    fun logCurrentTokenStatus() {
        Log.d(TAG, "=== CURRENT TOKEN STATUS ===")
        Log.d(TAG, preferencesManager.getTokensInfo())
        
        val isFirebaseSignedIn = tokenManager.isFirebaseUserSignedIn()
        Log.d(TAG, "Firebase user signed in: $isFirebaseSignedIn")
        
        val isExpiringSoon = tokenManager.isTokenExpiringSoon()
        Log.d(TAG, "Token expiring soon: $isExpiringSoon")
    }
    
    /**
     * Clear tất cả token data để test từ đầu
     */
    fun clearAllTokens() {
        Log.d(TAG, "=== CLEARING ALL TOKENS ===")
        tokenManager.clearTokenData()
        Log.d(TAG, "All tokens cleared")
    }
    
    /**
     * Test complete flow: login -> get token -> simulate expiration -> auto refresh
     */
    fun testCompleteTokenFlow() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "=== TESTING COMPLETE TOKEN FLOW ===")
                
                // Step 1: Check current status
                Log.d(TAG, "Step 1: Initial status")
                logCurrentTokenStatus()
                
                // Step 2: Get valid token (should work if user is logged in)
                Log.d(TAG, "Step 2: Getting valid token")
                val token1 = tokenManager.getValidToken()
                Log.d(TAG, "Token 1: ${token1?.take(20) ?: "null"}")
                
                // Step 3: Simulate expiration
                Log.d(TAG, "Step 3: Simulating token expiration")
                simulateTokenExpiration()
                
                // Step 4: Get valid token again (should auto refresh)
                Log.d(TAG, "Step 4: Getting valid token after expiration")
                val token2 = tokenManager.getValidToken()
                Log.d(TAG, "Token 2: ${token2?.take(20) ?: "null"}")
                
                // Step 5: Compare tokens
                if (token1 != null && token2 != null && token1 != token2) {
                    Log.d(TAG, "✅ Token was successfully refreshed!")
                } else if (token1 == token2) {
                    Log.w(TAG, "⚠️ Token was not refreshed (same token)")
                } else {
                    Log.e(TAG, "❌ Token refresh test failed")
                }
                
                // Final status
                Log.d(TAG, "Final status:")
                logCurrentTokenStatus()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during complete token flow test", e)
            }
        }
    }
}
