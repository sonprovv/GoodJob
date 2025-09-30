package com.project.job.data.repository

import com.project.job.data.network.RetrofitClient
import com.project.job.data.repository.implement.TokenRepositoryImpl
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.RefreshTokenRequest
import com.project.job.data.source.remote.api.response.RefreshTokenResponse
import com.project.job.data.source.remote.safeApiCall

class TokenRepository(private val preferencesManager: PreferencesManager) : TokenRepositoryImpl {
    override suspend fun getAccessToken(): String? {
        return preferencesManager.getAuthToken()
    }

    override suspend fun getRefreshToken(): String? {
        return preferencesManager.getRefreshToken()
    }

    override suspend fun getFcmToken(): String? {
        return preferencesManager.getFCMToken()
    }

    override suspend fun saveAccessToken(token: String?) {
        preferencesManager.saveAuthToken(token)
    }

    override suspend fun saveRefreshToken(token: String?) {
        preferencesManager.saveRefreshToken(token)
    }

    override suspend fun saveFcmToken(fcmToken: String) {
        preferencesManager.saveFCMToken(fcmToken)
    }

    override suspend fun clearAuthTokens() {
        preferencesManager.clearAuthData()
    }

    override suspend fun refreshAccessToken(): NetworkResult<RefreshTokenResponse?> {
        val refreshToken = getRefreshToken()
        return if (refreshToken != null) {
            safeApiCall {
                RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
            }
        } else {
            NetworkResult.Error("No refresh token available")
        }
    }
}