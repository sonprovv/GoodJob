package com.project.job.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.project.job.data.manager.AuthenticationManager
import com.project.job.data.manager.TokenManagerIntegration
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.interceptor.AuthInterceptor
import com.project.job.data.source.remote.interceptor.TokenAuthenticator
import com.project.job.utils.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = Constant.BASE_URL
    private const val TAG = "RetrofitClient"
    
    private var isInitialized = false
    private lateinit var tokenRepository: TokenRepository
    @SuppressLint("StaticFieldLeak")
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var tokenManagerIntegration: TokenManagerIntegration
    private lateinit var httpClient: OkHttpClient
    private lateinit var _apiService: ApiService

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun initialize(context: Context) {
        if (!isInitialized) {
            val preferencesManager = PreferencesManager(context)
            tokenRepository = TokenRepository(preferencesManager)
            authenticationManager = AuthenticationManager.getInstance(context, tokenRepository)
            tokenManagerIntegration = TokenManagerIntegration.getInstance(
                tokenRepository, 
                authenticationManager
            )

            // Create API service first with basic client (for TokenAuthenticator)
            val basicClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(15, TimeUnit.SECONDS)   // Write timeout
                .build()

            _apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(basicClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            // Now create the full client with TokenAuthenticator
            httpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(AuthInterceptor(
                    tokenRepository = tokenRepository,
                    authenticationManager = authenticationManager
                ))
                .authenticator(TokenAuthenticator(
                    apiService = _apiService,
                    preferencesManager = preferencesManager,
                    context = context
                ))
                .connectTimeout(60, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(60, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(60, TimeUnit.SECONDS)   // Write timeout
                .build()

            // Recreate API service with the full client
            _apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
                
            isInitialized = true
            Log.d(TAG, "RetrofitClient initialized successfully")
        }
    }

    val apiService: ApiService
        get() {
            if (!isInitialized) {
                throw IllegalStateException("RetrofitClient must be initialized before use. Call initialize(context) first.")
            }
            return _apiService
        }
    
    val authManager: AuthenticationManager
        get() {
            if (!isInitialized) {
                throw IllegalStateException("RetrofitClient must be initialized before use. Call initialize(context) first.")
            }
            return authenticationManager
        }
    
    val tokenManager: TokenManagerIntegration
        get() {
            if (!isInitialized) {
                throw IllegalStateException("RetrofitClient must be initialized before use. Call initialize(context) first.")
            }
            return tokenManagerIntegration
        }
        
    fun isInitialized(): Boolean = isInitialized
}