package com.project.job

import android.app.Application
import com.project.job.data.network.RetrofitClient
import com.project.job.utils.NotificationHelper
import com.project.job.utils.NotificationUtils

class JobApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this)
        NotificationHelper.createNotificationChannel(this)
        // Initialize Retrofit with application context
        RetrofitClient.initialize(applicationContext)
    }
}
