package com.project.job.data.repository.implement

import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.RefreshTokenResponse

interface TokenRepositoryImpl {

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun getFcmToken(): String?

    suspend fun saveAccessToken(token: String?)

    suspend fun saveRefreshToken(token: String?)

    suspend fun saveFcmToken(fcmToken: String)

    suspend fun clearAuthTokens()

    suspend fun refreshAccessToken(): NetworkResult<RefreshTokenResponse?>
}