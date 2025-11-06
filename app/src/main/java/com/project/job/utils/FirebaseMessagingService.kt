package com.project.job.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log

import com.project.job.R
//import com.project.job.models.Message
//import com.project.job.receiver.DirectReplyReceiver
import android.app.ActivityManager
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit
//import com.project.job.models.User
//import com.project.job.utils.AppPreferences
//import com.project.job.utils.Constant.ACTION_NEW_MESSAGE
//import com.project.job.utils.Constants.KEY_CONVERSATION_ID
//import com.project.job.utils.Constants.KEY_MESSAGE
//import com.project.job.utils.Constants.KEY_SENDER_ID
//import com.project.job.utils.Constants.KEY_SENDER_NAME
//import com.project.job.utils.NotificationActionUtils
//import com.project.job.utils.NotificationHelper
//import com.project.job.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.job.ui.chat.detail.ChatDetailActivity
import com.project.job.utils.Constant.Companion.ACTION_NEW_MESSAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_DEBUG"
        private const val DEFAULT_NOTIFICATION_ID = 1001
        const val MESSAGE_NOTIFICATION_ID = 1002

        // Action IDs for notification actions
        const val ACTION_REPLY = "com.client.appbinh.ACTION_REPLY"
        const val ACTION_MARK_AS_READ = "com.client.appbinh.ACTION_MARK_AS_READ"

        // Intent extra keys
        const val KEY_MESSAGE = "message"
        const val KEY_SENDER_ID = "senderId"
        const val KEY_SENDER_NAME = "senderName"
        const val KEY_SENDER_AVT = "senderAvatar"
        const val KEY_CONVERSATION_ID = "chat_room_id"

        private var notificationId = DEFAULT_NOTIFICATION_ID
    }

//    private val notificationUtils by lazy { NotificationUtils }

//    private lateinit var appPreferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FirebaseMessagingService created")
//        appPreferences = AppPreferences(applicationContext)
        // Notification channel is created in App.kt, so this call is not needed here.
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        
        // If you want to send the token to your server, you can do it here
        // For example:
        // sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            Log.d(TAG, "================================")
            Log.d(TAG, "🔥 FCM MESSAGE RECEIVED 🔥")
            Log.d(TAG, "================================")
            
            // Basic message info
            Log.d(TAG, "📌 From: ${remoteMessage.from}")
            Log.d(TAG, "📌 Message ID: ${remoteMessage.messageId}")
            Log.d(TAG, "📌 Message Type: ${remoteMessage.messageType}")
            Log.d(TAG, "📌 Collapse Key: ${remoteMessage.collapseKey}")
            Log.d(TAG, "📌 TTL: ${remoteMessage.ttl} seconds")
            Log.d(TAG, "📌 Sent Time: ${Date(remoteMessage.sentTime)}")
            
            // Notification payload
            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "\n📢 NOTIFICATION PAYLOAD:")
                Log.d(TAG, "   Title: ${notification.title}")
                Log.d(TAG, "   Body: ${notification.body}")
                Log.d(TAG, "   Icon: ${notification.icon}")
                Log.d(TAG, "   Sound: ${notification.sound}")
                Log.d(TAG, "   Tag: ${notification.tag}")
                Log.d(TAG, "   Color: ${notification.color}")
                Log.d(TAG, "   Click Action: ${notification.clickAction}")
                // Removed problematic link access
            } ?: Log.d(TAG, "\nℹ️ No notification payload")
            
            // Data payload
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "\n📦 DATA PAYLOAD (${remoteMessage.data.size} items):")
                remoteMessage.data.forEach { (key, value) ->
                    Log.d(TAG, "   $key = $value")
                }
                
                // Log common chat-related fields specifically
                val commonFields = listOf(
                    "type", "messageType", "roomId", "conversationId", 
                    "senderId", "senderName", "senderAvatar", "avatar",
                    "content", "message", "title", "body"
                )
                
                Log.d(TAG, "\n🔍 EXTRACTED CHAT FIELDS:")
                commonFields.forEach { field ->
                    remoteMessage.data[field]?.let { value ->
                        Log.d(TAG, "   $field = $value")
                    }
                }
            } else {
                Log.d(TAG, "\nℹ️ No data payload")
            }
            
            // Check if app is in foreground
            val isAppForeground = isAppInForeground()
            Log.d(TAG, "\n📱 APP STATE: ${if (isAppForeground) "FOREGROUND" else "BACKGROUND"}")
            
            Log.d(TAG, "================================")
            Log.d(TAG, "PROCESSING MESSAGE...")
            Log.d(TAG, "================================")

            // Check if message contains a data payload
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Processing data payload")
                handleDataMessage(remoteMessage.data)
            } else {
                Log.d(TAG, "No data payload found")
            }

            // Check if message contains a notification payload
            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "Processing notification payload")
                Log.d(TAG, "Title: ${notification.title}")
                Log.d(TAG, "Body: ${notification.body}")
                Log.d(TAG, "Icon: ${notification.icon}")
                Log.d(TAG, "Sound: ${notification.sound}")
                Log.d(TAG, "Tag: ${notification.tag}")
                Log.d(TAG, "Color: ${notification.color}")
                Log.d(TAG, "Click Action: ${notification.clickAction}")
                
                handleNotification(notification, remoteMessage.data)
            } ?: run {
                Log.d(TAG, "No notification payload found")
            }
            
            // Log if we're in the foreground or background
            Log.d(TAG, "App in foreground: $isAppForeground")
            
            // If we have a notification but no data, we should still show a notification
            if (remoteMessage.notification != null && remoteMessage.data.isEmpty() && !isAppForeground) {
                Log.d(TAG, "Only notification payload, creating system notification")
                val notification = remoteMessage.notification!!
                showBasicNotification(
                    notification.title ?: getString(R.string.app_name),
                    notification.body ?: "",
                    "", "", "", "default"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMessageReceived: ${e.message}", e)
        }
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Data payload: ${remoteMessage.data}")
        Log.d(TAG, "Notification payload: ${remoteMessage.notification}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Processing data payload")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Processing notification payload: ${notification.body}")
            handleNotification(notification, remoteMessage.data)
        }
        
        // If no data and no notification, log warning
        if (remoteMessage.data.isEmpty() && remoteMessage.notification == null) {
            Log.w(TAG, "Message received but no data or notification payload found")
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        try {
            Log.d(TAG, "=== handleDataMessage() called ===")
            Log.d(TAG, "Full data payload: $data")
            
            // Extract data from the payload
            val title = data["title"] ?: data["gcm.notification.title"] ?: data["notification.title"] ?: ""
            val messageText = data["message"] ?: data["body"] ?: data["gcm.notification.body"] ?: data["notification.body"] ?: ""
            val senderId = data["senderId"] ?: data["from"] ?: ""
            val senderName = data["senderName"] ?: data["name"] ?: title ?: getString(R.string.app_name)
            val conversationId = data["chat_room_id"] ?: data["roomId"] ?: ""
            val profileImageUrl = data["senderAvatar"] ?: data["avatar"] ?: ""
            val notificationType = data["type"] ?: data["notificationType"] ?: ""
            val clickAction = data["click_action"] ?: data["clickAction"] ?: ""
            val messageType = data["messageType"] ?: "text"
            val content = data["content"] ?: messageText

            // Debug logging to check data payload
            Log.d(TAG, "=== Parsed Data ===")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Message: $messageText")
            Log.d(TAG, "Sender ID: '$senderId'")
            Log.d(TAG, "Sender Name: $senderName")
            Log.d(TAG, "Conversation ID: $conversationId")
            Log.d(TAG, "Profile Image URL: $profileImageUrl")
            Log.d(TAG, "Notification Type: $notificationType")
            Log.d(TAG, "Click Action: $clickAction")
            Log.d(TAG, "Message Type: $messageType")
            Log.d(TAG, "Content: $content")
            Log.d(TAG, "App in foreground: ${isAppInForeground()}")

            // Check if this is a chat notification
            when {
                notificationType.equals("new_message", ignoreCase = true) || 
                messageType.equals("text", ignoreCase = true) ||
                conversationId.isNotEmpty() -> {
                    Log.d(TAG, "Handling chat notification")
                    handleChatNotification(
                        title = senderName.ifEmpty { title },
                        message = content,
                        senderId = senderId,
                        senderName = senderName,
                        conversationId = conversationId,
                        data = data + ("senderAvatar" to profileImageUrl)
                    )
                }
                clickAction.isNotEmpty() -> {
                    Log.d(TAG, "Handling notification with click action: $clickAction")
                    handleRegularNotification(
                        title = title,
                        message = messageText,
                        senderId = senderId,
                        senderName = senderName,
                        conversationId = conversationId
                    )
                }
                else -> {
                    Log.d(TAG, "Handling regular notification")
                    handleRegularNotification(
                        title = title,
                        message = messageText,
                        senderId = senderId,
                        senderName = senderName,
                        conversationId = conversationId
                    )
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
        val notificationType = data["type"] ?: ""
        val senderAvatar = data["senderAvatar"] ?: ""

        Log.d(TAG, "Handling notification: title=$title, message=$message, type=$notificationType")

        if (notificationType == "new_message") {
            handleChatNotification(title, message, senderId, senderName, conversationId, data)
        } else {
            // Show basic notification for notification payload
            showBasicNotification(title, message, senderId, senderName, conversationId, "default")
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        
        Log.d(TAG, "Checking if app is in foreground. Package: $packageName")
        
        for (appProcess in appProcesses) {
            Log.d(TAG, "Process: ${appProcess.processName}, Importance: ${appProcess.importance}")
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && 
                appProcess.processName == packageName) {
                Log.d(TAG, "App is in foreground")
                return true
            }
        }
        
        Log.d(TAG, "App is in background")
        return false
    }
    
    /**
     * Get the current user's ID from SharedPreferences or your authentication system
     * @return The current user's ID or null if not logged in
     */
    private fun getCurrentUserId(): String? {
        try {
            // Try to get user ID from SharedPreferences
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", null)
            
            if (userId.isNullOrEmpty()) {
                Log.d(TAG, "No user ID found in SharedPreferences")
                // You can also try to get the user ID from your authentication system here
                // For example, if you're using Firebase Auth:
                // return FirebaseAuth.getInstance().currentUser?.uid
            } else {
                Log.d(TAG, "Found user ID in SharedPreferences: $userId")
            }
            
            return userId
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID: ${e.message}", e)
            return null
        }
    }

    private fun handleChatNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String,
        data: Map<String, String>
    ) {
        try {
            // Extract avatar from data payload with fallbacks
            val senderAvatar = data["senderAvatar"] ?: data["avatar"] ?: ""
            val roomId = data["roomId"] ?: conversationId
            val messageType = data["messageType"] ?: "text"
            val content = data["content"] ?: message
            
            // Log the received data for debugging
            Log.d(TAG, "=== Handle Chat Notification ===")
            Log.d(TAG, "- Title: $title")
            Log.d(TAG, "- Message: $message")
            Log.d(TAG, "- Sender ID: $senderId")
            Log.d(TAG, "- Sender Name: $senderName")
            Log.d(TAG, "- Conversation ID: $conversationId")
            Log.d(TAG, "- Room ID: $roomId")
            Log.d(TAG, "- Sender Avatar: $senderAvatar")
            Log.d(TAG, "- Message Type: $messageType")
            Log.d(TAG, "- Content: $content")
            Log.d(TAG, "- All data: $data")
            
            // Get current user ID to check if this is our own message
            val currentUserId = getCurrentUserId()
            if (currentUserId != null && senderId == currentUserId) {
                Log.d(TAG, "This is our own message, not showing notification")
                return
            }
            
            // Check if app is in foreground
            if (isAppInForeground()) {
                // App is in foreground, show in-app notification
                Log.d(TAG, "App is in foreground, showing in-app chat notification")

                // Broadcast the chat message to the activity
                val intent = Intent(ACTION_NEW_MESSAGE).apply {
                    putExtra(KEY_MESSAGE, content)
                    putExtra(KEY_SENDER_ID, senderId)
                    putExtra(KEY_SENDER_NAME, senderName)
                    putExtra(KEY_CONVERSATION_ID, conversationId)
                    putExtra("senderAvatar", senderAvatar)
                    putExtra("notificationType", "new_message")
                    putExtra("roomId", roomId)
                    putExtra("messageType", messageType)
                    // Add all data as extras
                    data.forEach { (key, value) ->
                        putExtra(key, value)
                    }
                }
                sendBroadcast(intent)
            } else {
                Log.d(TAG, "App is in background, showing chat system notification")
                showChatNotification(
                    title = senderName.ifEmpty { title },
                    message = content,
                    senderId = senderId,
                    senderName = senderName,
                    conversationId = roomId,
                    senderAvatar = senderAvatar
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleChatNotification: ${e.message}", e)
            // Try to show a basic notification if something goes wrong
            try {
                showBasicNotification(
                    title = senderName.ifEmpty { title },
                    message = message,
                    senderId = senderId,
                    senderName = senderName,
                    conversationId = conversationId,
                    channelId = "chat_messages"
                )
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to show basic notification: ${e2.message}", e2)
            }
        }
    }

    private fun handleRegularNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String
    ) {
        // Check if app is in foreground
        if (isAppInForeground()) {
            // App is in foreground, show in-app notification
            Log.d(TAG, "App is in foreground, showing in-app notification")

            // Broadcast the message to the activity
            val intent = Intent(ACTION_NEW_MESSAGE)
            intent.putExtra(KEY_MESSAGE, message)
            intent.putExtra(KEY_SENDER_ID, senderId)
            intent.putExtra(KEY_SENDER_NAME, senderName)
            intent.putExtra(KEY_CONVERSATION_ID, conversationId)
            sendBroadcast(intent)
        } else {
            // App is in background, show system notification
            Log.d(TAG, "App is in background, showing system notification")
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

        // Create notification builder
        val builder = NotificationUtils.getNotificationBuilder(this, title, message, channelId)
            .setContentTitle(if (senderName.isNotEmpty()) senderName else title)
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setLights(0xff00ff00.toInt(), 300, 1000)

        // Show notification
        notificationManager.notify(notificationId++, builder.build())
    }

    private fun showChatNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String,
        senderAvatar: String
    ) {
        Log.d(TAG, "=== showChatNotification ===")
        Log.d(TAG, "Title: '$title'")
        Log.d(TAG, "Message: '$message'")
        Log.d(TAG, "ConversationId: '$conversationId'")

        // Validate senderId before creating notification
        if (senderId.isEmpty()) {
            Log.e(TAG, "⚠️ SenderId is EMPTY! Cannot create notification properly")
            return
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chat_messages"
        val notificationId = System.currentTimeMillis().toInt()

        // Create an intent for when the notification is tapped
        Log.d("TAG", "Creating intent for ChatDetailActivity with roomId: $conversationId")
        val intent = Intent(this, ChatDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("roomId", conversationId)
            putExtra("receiverId", senderId)
            putExtra("partnerName", senderName)
            putExtra("partnerAvatar", senderAvatar)
        }

        Log.d(TAG, "Intent created: $intent")

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create a custom notification layout
        val notificationLayout = RemoteViews(packageName, R.layout.notification_chat)
        notificationLayout.setTextViewText(R.id.notification_title, senderName)
        notificationLayout.setTextViewText(R.id.notification_message, message)

        // Create the notification builder
        val builder = NotificationUtils.getNotificationBuilder(this, title, message, channelId)
            .setCustomContentView(notificationLayout)
            .setStyle(androidx.core.app.NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setLights(0xff2196F3.toInt(), 300, 1000)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(R.drawable.ic_logo_notification)

        // Set default avatar initially
        notificationLayout.setImageViewResource(
            R.id.notification_avatar,
            R.drawable.img_profile_picture_defaul
        )

        // Show notification immediately with default avatar
        notificationManager.notify(notificationId, builder.build())

        // Try to load the avatar in the background and update the notification
        if (senderAvatar.isNotEmpty() && senderAvatar.startsWith("http")) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        try {
                            val urlWithTimestamp = "${senderAvatar}${if (senderAvatar.contains('?')) '&' else '?'}t=${System.currentTimeMillis()}"
                            
                            // 1. First try with Glide's default settings (handles most cases)
                            try {
                                return@withContext Glide.with(applicationContext)
                                    .asBitmap()
                                    .load(urlWithTimestamp)
                                    .override(200, 200)
                                    .centerCrop()
                                    .submit()
                                    .get()
                            } catch (e: Exception) {
                                Log.w(TAG, "Glide standard load failed: ${e.message}")
                            }
                            
                            // 2. If that fails, try with a custom OkHttpClient that can handle larger files
                            try {
                                // Create a custom Glide module to handle larger files
                                val factory = OkHttpUrlLoader.Factory(OkHttpClient.Builder()
                                    .connectTimeout(15, TimeUnit.SECONDS)
                                    .readTimeout(15, TimeUnit.SECONDS)
                                    .writeTimeout(15, TimeUnit.SECONDS)
                                    .build())
                                
                                Glide.get(applicationContext).registry
                                    .replace(GlideUrl::class.java, InputStream::class.java, factory)
                                
                                return@withContext Glide.with(applicationContext)
                                    .asBitmap()
                                    .load(urlWithTimestamp)
                                    .override(200, 200)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .submit()
                                    .get()
                            } catch (e: Exception) {
                                Log.e(TAG, "Glide custom client load failed: ${e.message}")
                                return@withContext null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in image loading coroutine: ${e.message}")
                            null
                        }
                    }

                    withContext(Dispatchers.Main) {
                        try {
                            if (bitmap != null) {
                                notificationLayout.setImageViewBitmap(
                                    R.id.notification_avatar,
                                    bitmap
                                )
                                // Update the notification with the new bitmap
                                notificationManager.notify(notificationId, builder.build())
                                Log.d(TAG, "Notification updated with avatar image")
                            } else {
                                Log.d(TAG, "Using default avatar due to loading error")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating notification: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in avatar loading coroutine: ${e.message}")
                }
            }
        }
    }
}
