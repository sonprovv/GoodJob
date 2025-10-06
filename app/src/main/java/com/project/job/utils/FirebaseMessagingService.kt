package com.project.job.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log

import com.project.job.R
//import com.project.job.models.Message
//import com.project.job.receiver.DirectReplyReceiver
import android.app.ActivityManager
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.job.utils.Constant.Companion.ACTION_NEW_MESSAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import org.json.JSONObject

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FirebaseMsgService"
        private const val DEFAULT_NOTIFICATION_ID = 1001
        const val MESSAGE_NOTIFICATION_ID = 1002

        // Action IDs for notification actions
        const val ACTION_REPLY = "com.client.appbinh.ACTION_REPLY"
        const val ACTION_MARK_AS_READ = "com.client.appbinh.ACTION_MARK_AS_READ"

        // Intent extra keys
        const val KEY_MESSAGE = "message"
        const val KEY_SENDER_ID = "sender_id"
        const val KEY_SENDER_NAME = "sender_name"
        const val KEY_CONVERSATION_ID = "conversation_id"

        private var notificationId = DEFAULT_NOTIFICATION_ID
    }

//    private val notificationUtils by lazy { NotificationUtils }

//    private lateinit var appPreferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
//        appPreferences = AppPreferences(applicationContext)
        // Notification channel is created in App.kt, so this call is not needed here.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
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
        val title = data["title"] ?: getString(R.string.app_name)
        val messageText = data["message"] ?: ""
        val senderId = data[KEY_SENDER_ID] ?: ""
        val senderName = data[KEY_SENDER_NAME] ?: ""
        val conversationId = data[KEY_CONVERSATION_ID] ?: ""
        val profileImageUrl = data["profileImageUrl"] ?: ""
        val notificationType = data["notificationType"] ?: ""

        // Check if this is a chat notification
        if (notificationType == "Chat") {
            handleChatNotification(title, messageText, senderId, senderName, conversationId, data)
        } else {
            // Handle regular notifications
            Log.d(TAG, "Processing regular notification")
            handleRegularNotification(title, messageText, senderId, senderName, conversationId)
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (processInfo in appProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                processInfo.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun handleChatNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String,
        data: Map<String, String>
    ) {
        // Check if app is in foreground
        if (isAppInForeground()) {
            // App is in foreground, show in-app notification
            Log.d(TAG, "App is in foreground, showing in-app chat notification")

            // Broadcast the chat message to the activity
            val intent = Intent(ACTION_NEW_MESSAGE)
            intent.putExtra(KEY_MESSAGE, message)
            intent.putExtra(KEY_SENDER_ID, senderId)
            intent.putExtra(KEY_SENDER_NAME, senderName)
            intent.putExtra(KEY_CONVERSATION_ID, conversationId)
            intent.putExtra("notificationType", "Chat")
            sendBroadcast(intent)
        } else {
            Log.d(TAG, "App is in background, showing chat system notification")
            showChatNotification(title, message, senderId, senderName, conversationId)
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
            showBasicNotification(title, message, senderId, senderName, conversationId)
        }
    }

    private fun showBasicNotification(
        title: String,
        message: String,
        senderId: String,
        senderName: String,
        conversationId: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification builder
        val builder = NotificationUtils.getNotificationBuilder(this, title, message)
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
        conversationId: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open ChatDetailActivity
        val intent = Intent(this, com.project.job.ui.chat.detail.ChatDetailActivity::class.java).apply {
            putExtra(com.project.job.ui.chat.detail.ChatDetailActivity.EXTRA_RECEIVER_ID, senderId)
            putExtra(com.project.job.ui.chat.detail.ChatDetailActivity.EXTRA_PARTNER_NAME, senderName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification builder for chat
        val builder = NotificationUtils.getNotificationBuilder(this, title, message)
            .setContentTitle(if (senderName.isNotEmpty()) senderName else title)
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setLights(0xff2196F3.toInt(), 300, 1000) // Blue color for chat notifications

        // Show notification
        notificationManager.notify(notificationId++, builder.build())
    }

    private fun handleNotification(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {
        // Extract data from the notification
        val title = notification.title ?: getString(R.string.app_name)
        val message = notification.body ?: ""
        val senderId = data[KEY_SENDER_ID] ?: ""
        val senderName = data[KEY_SENDER_NAME] ?: ""
        val conversationId = data[KEY_CONVERSATION_ID] ?: ""
        val notificationType = data["notificationType"] ?: ""

        Log.d(TAG, "Handling notification: title=$title, message=$message, type=$notificationType")

        if (notificationType == "Chat") {
            handleChatNotification(title, message, senderId, senderName, conversationId, data)
        } else {
            // Show basic notification for notification payload
            showBasicNotification(title, message, senderId, senderName, conversationId)
        }
    }

    // Notification channel is now created in App.kt

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Save the token to shared preferences
//        appPreferences.fcmToken = token

        // If user is logged in, update the token in Firestore
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (!userId.isNullOrEmpty()) {
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    // Update token in Firestore
//                    val userRef = Firebase.firestore.collection("users").document(userId)
//                    userRef.update("fcmToken", token)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "FCM token updated in Firestore")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error updating FCM token in Firestore", e)
//                        }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error updating FCM token", e)
//                }
//            }
//        }
    }
}
