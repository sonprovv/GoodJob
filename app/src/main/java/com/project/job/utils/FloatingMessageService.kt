package com.project.job.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.project.job.MainActivity
import com.project.job.R
import com.project.job.databinding.FloatingMessageBinding

class FloatingMessageService : Service() {
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private val CHANNEL_ID = "FloatingMessageChannel"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "SHOW_FLOATING_MESSAGE") {
            val senderName = intent.getStringExtra("senderName") ?: ""
            val message = intent.getStringExtra("message") ?: ""
            showFloatingMessage(senderName, message)
        } else if (intent?.action == "HIDE_FLOATING_MESSAGE") {
            removeFloatingView()
        }
        return START_STICKY
    }

    private fun showFloatingMessage(senderName: String, message: String) {
        removeFloatingView()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = FloatingMessageBinding.inflate(inflater)
        floatingView = binding.root

        binding.tvSenderName.text = senderName
        binding.tvMessagePreview.text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP
        params.y = 100

        windowManager.addView(floatingView, params)

        // Auto-hide after 5 seconds
        floatingView?.postDelayed({
            removeFloatingView()
        }, 5000)

        // Make the view clickable
        var intentGet : Intent? = null
        val receiverId = intentGet?.getStringExtra("receiverId") ?: ""
        val receiverName = intentGet?.getStringExtra("senderName") ?: ""
        floatingView?.setOnClickListener {
            removeFloatingView()
            // Open chat activity
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_chat", true)
                putExtra("receiver_id", receiverId)
                putExtra("receiver_name", receiverName)
            }
            startActivity(intent)
        }
    }

    private fun removeFloatingView() {
        if (floatingView != null && floatingView?.windowToken != null) {
            windowManager.removeView(floatingView)
            floatingView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Floating Message Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App đang chạy")
            .setContentText("Nhấn để mở ứng dụng")
            .setSmallIcon(R.mipmap.ic_notification)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingView()
    }
}

// Helper extension function to show floating message
fun showFloatingMessage(context: android.content.Context, senderName: String, message: String) {
    val intent = Intent(context, FloatingMessageService::class.java).apply {
        action = "SHOW_FLOATING_MESSAGE"
        putExtra("senderName", senderName)
        putExtra("message", message)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}
