package com.project.job.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class FirebaseInstanceIdService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCM_DEBUG"
        
        fun logToken() {
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Current FCM token: ${task.result}")
                        // Get Firebase version from package info
                        val pkgInfo = FirebaseMessaging.getInstance().javaClass.`package`
                        Log.d(TAG, "FCM Package: ${pkgInfo?.name ?: "unknown"}")
                        Log.d(TAG, "FCM Version: ${pkgInfo?.implementationVersion ?: "unknown"}")
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.exception)
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting FCM token", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in logToken", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        
        // If you need to send the token to your server, you can do it here
        // For example:
        // sendTokenToServer(token)
        
        // Subscribe to topics if needed
        // FirebaseMessaging.getInstance().subscribeToTopic("your_topic")
    }
}
