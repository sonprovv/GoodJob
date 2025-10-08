package com.project.job.data.source.remote.interceptor

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.project.job.MainActivity
import com.project.job.data.network.ApiService
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.request.RefreshTokenRequest
import com.project.job.ui.dialog.SessionExpiredBottomSheet
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "TokenAuthenticator called for ${response.request.url}")

        // if we've already attempted to authenticate 3 times, give up
        if (responseCount(response) >= 2) {
            Log.w(TAG, "Maximum retry attempts reached, showing session expired dialog")
            showSessionExpiredDialog()
            return null
        }

        // get the refresh token from DataStore
        val refreshToken = preferencesManager.getRefreshToken()
        Log.d(TAG, "Refresh token from preferences: ${refreshToken?.take(10)}...") // Log first 10 chars for debugging
        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "No refresh token available, showing session expired dialog")
            showSessionExpiredDialog()
            return null
        }

        Log.d(TAG, "Attempting to refresh token...")

        // call refresh token api
        val refreshResponse = runBlocking {
            try {
                Log.d(TAG, "Calling refresh token API with refresh token: ${refreshToken.take(10)}...")
                apiService.refreshToken(RefreshTokenRequest(refreshToken))
            } catch (e: Exception) {
                Log.e(TAG, "Refresh token API call failed", e)
                null
            }
        }

        if (refreshResponse?.isSuccessful != true) {
            Log.e(TAG, "Refresh token API call was not successful: ${refreshResponse?.code()}")
            Log.e(TAG, "Refresh token response body: ${refreshResponse?.errorBody()?.string()}")
            showSessionExpiredDialog()
            return null
        }

        val responseBody = refreshResponse.body()
        if (responseBody?.success != true) {
            Log.e(TAG, "Refresh token response was not successful: ${responseBody?.message}")
            showSessionExpiredDialog()
            return null
        }

        val data = responseBody.data
        if (data.idToken.isNullOrEmpty() || data.refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "Refresh token response missing tokens")
            showSessionExpiredDialog()
            return null
        }

        Log.d(TAG, "Token refreshed successfully, saving new tokens")

        runBlocking {
            // save new token to DataStore
            preferencesManager.saveAuthToken(data.idToken)
            preferencesManager.saveRefreshToken(data.refreshToken)
        }

        Log.d(TAG, "Creating new request with refreshed token")

        // use new access token to send request again
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${data.idToken}")
            .build()
    }

    private fun showSessionExpiredDialog() {
        try {
            // Gửi broadcast để MainActivity hiển thị dialog
            val intent = Intent("com.project.job.SESSION_EXPIRED")
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            Log.d(TAG, "Local broadcast sent for session expired dialog")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send local broadcast for session expired dialog", e)
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }

        return count
    }
}
