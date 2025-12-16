package com.project.job.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.project.job.data.source.local.PreferencesManager

@SuppressLint("ClickableViewAccessibility")
fun View.addFadeClickEffect(
    fadeAlpha: Float = 0.5f,
    onClick: (() -> Unit)? = null
) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> v.alpha = fadeAlpha
            MotionEvent.ACTION_UP -> {
                v.alpha = 1f
                onClick?.invoke() // gọi callback click riêng
            }

            MotionEvent.ACTION_CANCEL -> v.alpha = 1f
        }
        true
    }
}


fun getFCMToken(context: android.content.Context) {
    val preferencesManager = PreferencesManager(context)
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w("FirebaseLogs", "Fetching FCM registration token failed", task.exception)
            return@addOnCompleteListener
        }
        val token = task.result
        token?.let {
            preferencesManager.saveFCMToken(it)
            Log.d("FirebaseLogs", "FCM Token: $it")
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthRequired


