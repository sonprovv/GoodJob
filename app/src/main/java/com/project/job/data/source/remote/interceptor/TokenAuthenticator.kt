package com.project.job.data.source.remote.interceptor
import com.project.job.data.network.ApiService
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // if we've already attempted to authenticate 3 times, give up
        if (responseCount(response) >= 2) {
            return null
        }

        // get the refresh token from DataStore
        // call refresh token api
        val refreshResponse = runBlocking {
            try {
                //userApi.refreshToken("Bearer ${tokenLocalImpl.getRefreshToken()}")
                apiService.refreshToken(RefreshTokenRequest("Bearer ${preferencesManager.getRefreshToken()}"))
            } catch (e: Exception) {
                null
            }
        }

        if (refreshResponse?.isSuccessful == false) return null

        runBlocking {
            // save new token to DataStore
            preferencesManager.saveAuthToken(refreshResponse?.body()?.data?.idToken ?: "")
            preferencesManager.saveRefreshToken(refreshResponse?.body()?.data?.refreshToken ?: "")
        }

        // use new access token to send request again
        refreshResponse?.body()?.data?.idToken?.let {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        }

        return null
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