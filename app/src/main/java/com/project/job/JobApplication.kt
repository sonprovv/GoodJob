package com.project.job

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.local.PreferencesManager
import com.project.job.utils.FirebaseInstanceIdService
import com.project.job.utils.NotificationHelper
import com.project.job.utils.NotificationUtils
import com.project.job.utils.getFCMToken

class JobApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Set up FCM
        setupFCM()
        
        // Initialize other components
        NotificationUtils.createNotificationChannel(this)
        getFCMToken(applicationContext)
        
        // Initialize Retrofit with application context
        RetrofitClient.initialize(applicationContext)
        
        Log.d("FCM_DEBUG", "JobApplication initialized")
    }
    
    private fun setupFCM() {
        try {
            // Log FCM token
            FirebaseInstanceIdService.logToken()
            
            // Enable auto-init
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
            
            // Subscribe to debug topic
            FirebaseMessaging.getInstance().subscribeToTopic("debug")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM_DEBUG", "Subscribed to debug topic")
                    } else {
                        Log.e("FCM_DEBUG", "Failed to subscribe to debug topic", task.exception)
                    }
                }
                
        } catch (e: Exception) {
            Log.e("FCM_DEBUG", "Error initializing FCM", e)
        }
    }
}
