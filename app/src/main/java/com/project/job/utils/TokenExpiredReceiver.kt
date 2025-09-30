package com.project.job.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * BroadcastReceiver để lắng nghe token expired events
 * Tự động unregister khi lifecycle owner bị destroy
 */
class TokenExpiredReceiver(
    private val onTokenExpired: () -> Unit
) : BroadcastReceiver(), DefaultLifecycleObserver {
    
    companion object {
        const val ACTION_TOKEN_EXPIRED = "com.project.job.TOKEN_EXPIRED"
        private const val TAG = "TokenExpiredReceiver"
    }
    
    private var isRegistered = false
    private var context: Context? = null
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_TOKEN_EXPIRED) {
            Log.d(TAG, "Token expired broadcast received")
            onTokenExpired()
        }
    }
    
    /**
     * Register receiver với context và lifecycle owner
     */
    fun register(context: Context, lifecycleOwner: LifecycleOwner) {
        if (!isRegistered) {
            this.context = context
            val filter = IntentFilter(ACTION_TOKEN_EXPIRED)
            context.registerReceiver(this, filter)
            lifecycleOwner.lifecycle.addObserver(this)
            isRegistered = true
            Log.d(TAG, "TokenExpiredReceiver registered")
        }
    }
    
    /**
     * Unregister receiver
     */
    private fun unregister() {
        if (isRegistered && context != null) {
            try {
                context!!.unregisterReceiver(this)
                isRegistered = false
                Log.d(TAG, "TokenExpiredReceiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unregister()
    }
}

/**
 * Extension function để dễ dàng setup token expired listener trong Activity/Fragment
 */
fun LifecycleOwner.setupTokenExpiredListener(
    context: Context,
    onTokenExpired: () -> Unit
): TokenExpiredReceiver {
    val receiver = TokenExpiredReceiver(onTokenExpired)
    receiver.register(context, this)
    return receiver
}
