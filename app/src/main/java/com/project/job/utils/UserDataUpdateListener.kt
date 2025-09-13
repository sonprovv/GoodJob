package com.project.job.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

interface UserDataUpdateListener {
    fun onUserDataUpdated(name: String, phone: String)
    
    fun registerUserDataUpdateReceiver(context: Context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == UserDataBroadcastManager.ACTION_USER_DATA_UPDATED) {
                    val name = intent.getStringExtra(UserDataBroadcastManager.EXTRA_USER_NAME) ?: ""
                    val phone = intent.getStringExtra(UserDataBroadcastManager.EXTRA_USER_PHONE) ?: ""
                    onUserDataUpdated(name, phone)
                }
            }
        }
        
        val filter = IntentFilter(UserDataBroadcastManager.ACTION_USER_DATA_UPDATED)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
    }
    
    fun unregisterUserDataUpdateReceiver(context: Context, receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }
}
