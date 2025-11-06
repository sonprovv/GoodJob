package com.project.job.ui.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.job.data.source.remote.api.response.chat.MessageData
import com.project.job.databinding.ItemMessageBinding
import com.project.job.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChatMessagesAdapter(
    private val currentUserId: String
) : ListAdapter<MessageData, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    // Removed: private var messages, using ListAdapter's getItem() instead

    // ListAdapter provides submitList() automatically
    
    fun addMessage(message: MessageData) {
        val currentList = currentList.toMutableList()
        currentList.add(message)
        submitList(currentList)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        if(message.senderId == currentUserId) {
            return TYPE_SENT
        }
        return TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> SentMessageViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
            else -> ReceivedMessageViewHolder(ItemMessageBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageData) {
            binding.textMessage.text = message.message
            binding.textMessage.maxWidth = (binding.root.resources.displayMetrics.widthPixels * 0.7).toInt()
            binding.textTime.text = formatTimestamp(message.createdAt)
        }
        
        private fun formatTimestamp(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(timestamp)
                
                if (date != null) {
                    val now = System.currentTimeMillis()
                    val diff = now - date.time
                    
                    when {
                        diff < TimeUnit.MINUTES.toMillis(1) -> "vừa xong"
                        diff < TimeUnit.HOURS.toMillis(1) -> {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                            "${minutes} phút trước"
                        }
                        diff < TimeUnit.DAYS.toMillis(1) -> {
                            val hours = TimeUnit.MILLISECONDS.toHours(diff)
                            "${hours} giờ trước"
                        }
                        else -> {
                            val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                            dateFormat.format(date)
                        }
                    }
                } else {
                    "vừa xong"
                }
            } catch (e: Exception) {
                "vừa xong"
            }
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageData) {
            binding.textMessage.text = message.message
            binding.textMessage.maxWidth = (binding.root.resources.displayMetrics.widthPixels * 0.7).toInt()
            binding.textTime.text = formatTimestamp(message.createdAt)
        }
        
        private fun formatTimestamp(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(timestamp)
                
                if (date != null) {
                    val now = System.currentTimeMillis()
                    val diff = now - date.time
                    
                    when {
                        diff < TimeUnit.MINUTES.toMillis(1) -> "vừa xong"
                        diff < TimeUnit.HOURS.toMillis(1) -> {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                            "${minutes} phút trước"
                        }
                        diff < TimeUnit.DAYS.toMillis(1) -> {
                            val hours = TimeUnit.MILLISECONDS.toHours(diff)
                            "${hours} giờ trước"
                        }
                        else -> {
                            val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                            dateFormat.format(date)
                        }
                    }
                } else {
                    "vừa xong"
                }
            } catch (e: Exception) {
                "vừa xong"
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageData>() {
        override fun areItemsTheSame(oldItem: MessageData, newItem: MessageData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessageData, newItem: MessageData): Boolean {
            return oldItem == newItem
        }
    }
    
    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }
}
