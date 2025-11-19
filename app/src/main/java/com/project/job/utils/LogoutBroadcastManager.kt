package com.project.job.utils

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object LogoutBroadcastManager {
    const val ACTION_USER_LOGOUT = "com.project.job.ACTION_USER_LOGOUT"

    fun sendLogoutBroadcast(context: Context) {
        val intent = Intent(ACTION_USER_LOGOUT)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
