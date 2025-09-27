package com.project.job

import android.app.Application
import android.util.Log
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.local.PreferencesManager
import com.project.job.utils.NotificationHelper
import com.project.job.utils.NotificationUtils
import com.project.job.utils.getFCMToken

class JobApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this)
        getFCMToken(applicationContext)
        // Initialize Retrofit with application context
        RetrofitClient.initialize(applicationContext)
    }
}
