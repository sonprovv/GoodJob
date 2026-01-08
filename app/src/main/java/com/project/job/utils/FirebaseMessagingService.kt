package com.project.job.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.util.Log
import com.project.job.R
import android.app.ActivityManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.job.ui.chat.detail.ChatDetailActivity
import com.project.job.utils.Constant.Companion.ACTION_NEW_MESSAGE

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_DEBUG"
        private const val DEFAULT_NOTIFICATION_ID = 1001

        const val KEY_MESSAGE = "message"
        const val KEY_SENDER_ID = "senderId"
        const val KEY_SENDER_NAME = "senderName"
        const val KEY_SENDER_AVT = "senderAvatar"
        const val KEY_CONVERSATION_ID = "chat_room_id"
        const val KEY_MESSAGE_TYPE = "messageType"
        const val KEY_IMAGE_URL = "imageUrl"

        private var notificationId = DEFAULT_NOTIFICATION_ID
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FirebaseMessagingService created")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            Log.d(TAG, "================================")
            Log.d(TAG, "🔥 FCM MESSAGE RECEIVED 🔥")
            Log.d(TAG, "================================")

            Log.d(TAG, "📌 From: ${remoteMessage.from}")
            Log.d(TAG, "📌 Message ID: ${remoteMessage.messageId}")

            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "\n📦 DATA PAYLOAD:")
                remoteMessage.data.forEach { (key, value) ->
                    Log.d(TAG, "   $key = $value")
                }
                handleDataMessage(remoteMessage.data)
            }

            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "\n📢 NOTIFICATION PAYLOAD:")
                Log.d(TAG, "   Title: ${notification.title}")
                Log.d(TAG, "   Body: ${notification.body}")
                handleNotification(notification, remoteMessage.data)
            }

            val isAppForeground = isAppInForeground()
            Log.d(TAG, "\n📱 APP STATE: ${if (isAppForeground) "FOREGROUND" else "BACKGROUND"}")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onMessageReceived: ${e.message}", e)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        try {
            Log.d(TAG, "=== handleDataMessage() ===")

            val title = data["title"] ?: data["gcm.notification.title"] ?: ""
            val messageText = data["message"] ?: data["body"] ?: data["content"] ?: ""
            val senderId = data["senderId"] ?: data["from"] ?: ""
            val senderName = data["senderName"] ?: data["name"] ?: title
            val conversationId = data["chat_room_id"] ?: data["roomId"] ?: ""
            val senderAvatar = data["senderAvatar"] ?: data["avatar"] ?: ""
            val messageType = data["messageType"] ?: "text"
            val imageUrl = data["imageUrl"] ?: data["image"] ?: data["photoUrl"] ?: ""
            val notificationType = data["type"] ?: ""

            Log.d(TAG, "Parsed - Name: $senderName, Type: $messageType, ImageUrl: $imageUrl")

            when {
                notificationType.equals("new_message", ignoreCase = true) ||
                        conversationId.isNotEmpty() -> {
                    handleChatNotification(
                        title = senderName.ifEmpty { title },
                        message = messageText,
                        senderId = senderId,
                        senderName = senderName,
                        conversationId = conversationId,
                        senderAvatar = senderAvatar,
                        messageType = messageType,
                        imageUrl = imageUrl,
                        data = data
                    )
                }
                else -> {
                    handleRegularNotification(title, messageText, senderId, senderName, conversationId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleDataMessage: ${e.message}", e)
        }
    }

    private fun handleNotification(notification: RemoteMessage.Notification, data: Map<String, String>) {
        val title = notification.title ?: getString(R.string.app_name)
        val message = notification.body ?: ""
        val senderId = data[KEY_SENDER_ID] ?: ""
        val senderName = data[KEY_SENDER_NAME] ?: ""
        val conversationId = data[KEY_CONVERSATION_ID] ?: ""
        val senderAvatar = data[KEY_SENDER_AVT] ?: ""
        val messageType = data[KEY_MESSAGE_TYPE] ?: "text"
        val imageUrl = data[KEY_IMAGE_URL] ?: ""

        if (data["type"] == "new_message") {
            handleChatNotification(
                title, message, senderId, senderName, conversationId,
                senderAvatar, messageType, imageUrl, data
            )
        } else {
            showBasicNotification(title, message, senderId, senderName, conversationId, "default")
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun getCurrentUserId(): String? {
        return try {
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPref.getString("user_id", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID: ${e.message}", e)
            null
        }
    }

    private fun handleChatNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String,
        senderAvatar: String,
        messageType: String = "text",
        imageUrl: String = "",
        data: Map<String, String>
    ) {
        try {
            Log.d(TAG, "=== handleChatNotification ===")
            Log.d(TAG, "Type: $messageType, Image: $imageUrl")

            val currentUserId = getCurrentUserId()
            if (currentUserId != null && senderId == currentUserId) {
                Log.d(TAG, "Own message, skipping notification")
                return
            }

            if (isAppInForeground()) {
                Log.d(TAG, "App in foreground, broadcasting")
                val intent = Intent(ACTION_NEW_MESSAGE).apply {
                    putExtra(KEY_MESSAGE, message)
                    putExtra(KEY_SENDER_ID, senderId)
                    putExtra(KEY_SENDER_NAME, senderName)
                    putExtra(KEY_CONVERSATION_ID, conversationId)
                    putExtra(KEY_SENDER_AVT, senderAvatar)
                    putExtra(KEY_MESSAGE_TYPE, messageType)
                    putExtra(KEY_IMAGE_URL, imageUrl)
                    data.forEach { (key, value) -> putExtra(key, value) }
                }
                sendBroadcast(intent)
            } else {
                Log.d(TAG, "App in background, showing notification")
                showCustomStyleNotification(
                    message = message,
                    senderId = senderId,
                    senderName = senderName,
                    conversationId = conversationId,
                    senderAvatar = senderAvatar,
                    messageType = messageType,
                    imageUrl = imageUrl
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleChatNotification: ${e.message}", e)
        }
    }

    private fun handleRegularNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String
    ) {
        if (isAppInForeground()) {
            val intent = Intent(ACTION_NEW_MESSAGE).apply {
                putExtra(KEY_MESSAGE, message)
                putExtra(KEY_SENDER_ID, senderId)
                putExtra(KEY_SENDER_NAME, senderName)
                putExtra(KEY_CONVERSATION_ID, conversationId)
            }
            sendBroadcast(intent)
        } else {
            showBasicNotification(title, message, senderId, senderName, conversationId, "default")
        }
    }

    private fun showBasicNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String,
        channelId: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationUtils.getNotificationBuilder(this, title, message, channelId)
            .setContentTitle(senderName.ifEmpty { title })
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))

        notificationManager.notify(notificationId++, builder.build())
    }

    private fun showCustomStyleNotification(
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String,
        senderAvatar: String,
        messageType: String,
        imageUrl: String
    ) {
        Log.d(TAG, "=== showTikTokStyleNotification ===")

        if (senderId.isEmpty()) {
            Log.e(TAG, "⚠️ SenderId is EMPTY!")
            return
        }

        val context = applicationContext
        val pkgName = context.packageName
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chat_messages"
        val notificationId = senderId.hashCode()

        // Tạo intent để mở ChatDetailActivity
        val contentIntent = Intent(context, ChatDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("roomId", conversationId)
            putExtra("receiverId", senderId)
            putExtra("partnerName", senderName)
            putExtra("partnerAvatar", senderAvatar)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Xác định nội dung hiển thị
        val displayMessage = when (messageType.lowercase()) {
            "image" -> "đã đăng bài"
            "video" -> "đã đăng video"
            else -> message
        }

        // Tạo custom layout
        val collapsedView = RemoteViews(pkgName, R.layout.notification_chat_message)
        val expandedView = RemoteViews(pkgName, R.layout.notification_chat_messenge_expanded)

        // Set dữ liệu cho collapsed view
        collapsedView.setTextViewText(R.id.notification_title, senderName)
        collapsedView.setTextViewText(R.id.notification_message, displayMessage)

        // Set dữ liệu cho expanded view
        expandedView.setTextViewText(R.id.notification_title, senderName)
        expandedView.setTextViewText(R.id.notification_message, displayMessage)

        // Tạo notification builder
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo_notification)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setColor(androidx.core.content.ContextCompat.getColor(context, R.color.cam))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        // Hiển thị notification ban đầu
        notificationManager.notify(notificationId, builder.build())

        // Load avatar và ảnh trong coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val avatarUrl = senderAvatar
                val contentImageUrl = imageUrl

                // Load avatar
                val avatarBitmap = if (avatarUrl.isNotEmpty() && avatarUrl.startsWith("http")) {
                    loadImageFromUrl(avatarUrl, 120, 120, true)
                } else null

                // Load ảnh tin nhắn/bài đăng
                val contentBitmap = if (contentImageUrl.isNotEmpty() && contentImageUrl.startsWith("http")) {
                    loadImageFromUrl(contentImageUrl, 1200, 800, false)
                } else null

                withContext(Dispatchers.Main) {
                    // Update collapsed view với avatar
                    if (avatarBitmap != null) {
                        collapsedView.setImageViewBitmap(R.id.notification_avatar, avatarBitmap)
                        expandedView.setImageViewBitmap(R.id.notification_avatar, avatarBitmap)
                    }

                    // Update expanded view với ảnh content
                    if (contentBitmap != null) {
                        expandedView.setImageViewBitmap(R.id.notification_image, contentBitmap)
                        expandedView.setViewVisibility(R.id.notification_image, android.view.View.VISIBLE)
                    } else {
                        expandedView.setViewVisibility(R.id.notification_image, android.view.View.GONE)
                    }

                    // Update notification
                    val updatedBuilder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_logo_notification)
                        .setCustomContentView(collapsedView)
                        .setCustomBigContentView(expandedView)
                        .setContentIntent(contentPendingIntent)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(longArrayOf(0, 250, 250, 250))
                        .setColor(androidx.core.content.ContextCompat.getColor(context, R.color.cam))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())

                    notificationManager.notify(notificationId, updatedBuilder.build())
                    Log.d(TAG, "✅ TikTok style notification updated with images")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading images: ${e.message}", e)
            }
        }
    }

    private suspend fun loadImageFromUrl(
        url: String,
        width: Int,
        height: Int,
        isCircle: Boolean
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val urlWithTimestamp = "${url}${if (url.contains('?')) '&' else '?'}t=${System.currentTimeMillis()}"

                val request = Glide.with(applicationContext)
                    .asBitmap()
                    .load(urlWithTimestamp)
                    .override(width, height)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)

                if (isCircle) {
                    request.circleCrop()
                }

                request.submit().get()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from $url: ${e.message}", e)
                null
            }
        }
    }
}