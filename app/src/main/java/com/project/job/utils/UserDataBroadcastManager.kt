package com.project.job.utils

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object UserDataBroadcastManager {
    const val ACTION_USER_DATA_UPDATED = "com.project.job.USER_DATA_UPDATED"
    const val EXTRA_USER_NAME = "user_name"
    const val EXTRA_USER_PHONE = "user_phone"
    
    fun sendUserDataUpdatedBroadcast(context: Context, name: String, phone: String) {
        val intent = Intent(ACTION_USER_DATA_UPDATED).apply {
            putExtra(EXTRA_USER_NAME, name)
            putExtra(EXTRA_USER_PHONE, phone)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
