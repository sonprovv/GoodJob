package com.project.job.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import androidx.core.graphics.drawable.IconCompat
import com.project.job.utils.Constant.Companion.MESSAGE_NOTIFICATION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


import java.util.*

/**
 * Helper class for showing notifications
 */
object NotificationHelper {
    private const val CHANNEL_ID = "chat_messages"
    private const val CHANNEL_NAME = "Chat Messages"
    private const val CHANNEL_DESCRIPTION = "Notifications for chat messages"

    private var notificationId = MESSAGE_NOTIFICATION_ID

    /**
     * Creates the notification channel for Android O and above
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a new message notification
     */
//    suspend fun showMessageNotification(
//        context: Context,
//        message: Message,
//        sender: User,
//        conversationId: String,
//        largeIcon: Bitmap? = null
//    ) {
//        withContext(Dispatchers.IO) {
//            try {
//                // Create the person for the sender
//                val senderPerson = Person.Builder()
//                    .setKey(sender.id)
//                    .setName(sender.name)
//                    .apply {
//                        sender.profileImageUrl.takeIf { it.isNotEmpty() }?.let { setIcon(loadIcon(context, it)) }
//                    }
//                    .build()
//
//                // Create the message style
//                val style = NotificationCompat.MessagingStyle(senderPerson)
//                    .addMessage(
//                        message.message,
//                        message.timestamp,
//                        senderPerson
//                    )
//                    .setConversationTitle(sender.name)
//                    .setGroupConversation(true)
//
//                // Create the notification
//                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setContentTitle(sender.name)
//                    .setContentText(message.message)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                    .setAutoCancel(true)
//                    .setShowWhen(true)
//                    .setWhen(message.timestamp)
//                    .setStyle(style)
//                    .setGroupSummary(true)
//                    .setGroup("group_chat_messages")
//                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                    .setVibrate(longArrayOf(0, 250, 250, 250))
//                    .setLights(0xff00ff00.toInt(), 300, 1000)
//                    .apply {
//                        largeIcon?.let { setLargeIcon(it) }
//                    }
//                    .build()
//
//                // Show the notification
//                with(NotificationManagerCompat.from(context)) {
//                    notify(getNextNotificationId(), notification)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    /**
     * Shows a new message notification with reply action
     */
//    suspend fun showMessageWithReply(
//        context: Context,
//        message: Message,
//        sender: User,
//        conversationId: String,
//        largeIcon: Bitmap? = null
//    ) {
//        withContext(Dispatchers.IO) {
//            try {
//                // Create the reply intent
//                val replyIntent = Intent(context, DirectReplyReceiver::class.java).apply {
//                    action = ACTION_REPLY
//                    putExtra(KEY_SENDER_ID, sender.id)
//                    putExtra(KEY_CONVERSATION_ID, conversationId)
//                }
//
//                // Create the reply pending intent
//                val replyPendingIntent = PendingIntent.getBroadcast(
//                    context,
//                    getNextNotificationId(),
//                    replyIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                )
//
//                // Create the mark as read intent
//                val markAsReadIntent = Intent(context, DirectReplyReceiver::class.java).apply {
//                    action = ACTION_MARK_AS_READ
//                    putExtra(KEY_CONVERSATION_ID, conversationId)
//                }
//
//                // Create the mark as read pending intent
//                val markAsReadPendingIntent = PendingIntent.getBroadcast(
//                    context,
//                    getNextNotificationId(),
//                    markAsReadIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                )
//
//                // Create the remote input for inline reply
//                val remoteInput = RemoteInput.Builder(KEY_MESSAGE)
//                    .setLabel(context.getString(R.string.reply_hint))
//                    .build()
//
//                // Create the reply action
//                val replyAction = NotificationCompat.Action.Builder(
//                    R.drawable.ic_reply,
//                    context.getString(R.string.reply),
//                    replyPendingIntent
//                )
//                    .addRemoteInput(remoteInput)
//                    .setAllowGeneratedReplies(true)
//                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
//                    .build()
//
//                // Create the mark as read action
//                val markAsReadAction = NotificationCompat.Action.Builder(
//                    R.drawable.ic_done_all_blue_24dp,
//                    context.getString(R.string.mark_as_read),
//                    markAsReadPendingIntent
//                ).build()
//
//                // Create the notification
//                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setContentTitle(sender.name)
//                    .setContentText(message.message)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                    .setAutoCancel(true)
//                    .setShowWhen(true)
//                    .setWhen(message.timestamp)
//                    .setStyle(NotificationCompat.BigTextStyle().bigText(message.message))
//                    .setGroupSummary(true)
//                    .setGroup("group_chat_messages")
//                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                    .setVibrate(longArrayOf(0, 250, 250, 250))
//                    .setLights(0xff00ff00.toInt(), 300, 1000)
//                    .addAction(replyAction)
//                    .addAction(markAsReadAction)
//                    .apply {
//                        largeIcon?.let { setLargeIcon(it) }
//                    }
//                    .build()
//
//                // Show the notification
//                with(NotificationManagerCompat.from(context)) {
//                    notify(conversationId.hashCode(), notification)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    /**
     * Cancels a notification by ID
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancels all notifications
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Loads an icon from a URL
     */
    private suspend fun loadIcon(context: Context, url: String): IconCompat? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeStream(java.net.URL(url).openStream())
                IconCompat.createWithBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Gets the next notification ID
     */
    private fun getNextNotificationId(): Int {
        notificationId++
        if (notificationId > MESSAGE_NOTIFICATION_ID + 100) {
            notificationId = MESSAGE_NOTIFICATION_ID
        }
        return notificationId
    }
}
