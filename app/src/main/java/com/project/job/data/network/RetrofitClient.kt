package com.project.job.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.project.job.utils.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = Constant.BASE_URL
    private const val TAG = "RetrofitClient"
    
    // Application context
    private var _context: Context? = null
    val context: Context
        get() = _context ?: throw IllegalStateException("RetrofitClient not initialized. Call initialize() first.")
    
    fun initialize(context: Context) {
        _context = context.applicationContext
    }
    
    fun isInitialized(): Boolean = _context != null

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}