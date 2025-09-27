package com.project.job.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.interceptor.AuthInterceptor
import com.project.job.utils.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = Constant.BASE_URL
    private const val TAG = "RetrofitClient"
    
    private var isInitialized = false
    private lateinit var tokenRepository: TokenRepository
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
            
            httpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(AuthInterceptor(tokenRepository = tokenRepository))
                .build()

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
        
    fun isInitialized(): Boolean = isInitialized
}