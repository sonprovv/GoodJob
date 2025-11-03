package com.project.job.ui.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.job.R
import com.project.job.data.source.remote.api.response.chat.ConversationData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChatAdapter(
    private val onConversationClick: (ConversationData) -> Unit
) : ListAdapter<ConversationData, ChatAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        Log.d("ChatAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view, onConversationClick)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("ChatAdapter", "onBindViewHolder at position $position: ${item.sender.name}")
        holder.bind(item)
    }
    
    override fun submitList(list: List<ConversationData>?) {
        Log.d("ChatAdapter", "submitList called with ${list?.size} items")
        super.submitList(list)
    }

    class ConversationViewHolder(
        itemView: View,
        private val onConversationClick: (ConversationData) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)

        fun bind(conversation: ConversationData) {
            // Set name
            tvName.text = conversation.sender.name

            // Set last message (now it's a String, not an Object)
            val lastMessageText = conversation.lastMessage ?: "Chưa có tin nhắn"
            tvLastMessage.text = lastMessageText

            // Set timestamp (now it's a Long timestamp)
            tvTimestamp.text = formatTimestamp(conversation.lastMessageTime)

            // Load avatar
            if (conversation.sender.avatar.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(conversation.sender.avatar)
                    .placeholder(R.drawable.img_profile_picture_defaul)
                    .error(R.drawable.img_profile_picture_defaul)
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.img_profile_picture_defaul)
            }

            // Set click listener
            itemView.setOnClickListener {
                onConversationClick(conversation)
            }

            // Optional: Show unread count badge
            // TODO: Add badge view if needed
            // if (conversation.unreadCount > 0) {
            //     showUnreadBadge(conversation.unreadCount)
            // }
        }

        private fun formatTimestamp(timestamp: Long): String {
            return try {
                // timestamp is already in milliseconds
                val now = System.currentTimeMillis()
                val diff = now - timestamp
                
                when {
                    diff < TimeUnit.MINUTES.toMillis(1) -> "now"
                    diff < TimeUnit.HOURS.toMillis(1) -> {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                        " · ${minutes}m"
                    }
                    diff < TimeUnit.DAYS.toMillis(1) -> {
                        val hours = TimeUnit.MILLISECONDS.toHours(diff)
                        " · ${hours}h"
                    }
                    diff < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        " · ${days}d"
                    }
                    else -> {
                        // Show date format: dd/MM
                        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                        " · ${dateFormat.format(Date(timestamp))}"
                    }
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    class ConversationDiffCallback : DiffUtil.ItemCallback<ConversationData>() {
        override fun areItemsTheSame(oldItem: ConversationData, newItem: ConversationData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConversationData, newItem: ConversationData): Boolean {
            return oldItem == newItem
        }
    }
}