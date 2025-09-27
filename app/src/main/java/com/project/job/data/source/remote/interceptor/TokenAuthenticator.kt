package com.project.job.data.source.remote.interceptor
import com.project.job.data.network.ApiService
import com.project.job.data.source.local.PreferencesManager
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
        val refreshReponse = runBlocking {
            try {
                //userApi.refreshToken("Bearer ${tokenLocalImpl.getRefreshToken()}")
                apiService.refreshToken("Bearer ${preferencesManager.getRefreshToken()}")
            } catch (e: Exception) {
                null
            }
        }

        if (refreshReponse?.isSuccessful == false) return null

        runBlocking {
            // save new token to DataStore
            preferencesManager.saveAuthToken(refreshReponse?.body()?.data?.token ?: "")
            preferencesManager.saveRefreshToken(refreshReponse?.body()?.data?.refreshToken ?: "")
        }

        // use new access token to send request again
        refreshReponse?.body()?.data?.token?.let {
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