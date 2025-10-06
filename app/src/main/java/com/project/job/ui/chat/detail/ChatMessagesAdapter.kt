package com.project.job.ui.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.databinding.ItemMessageBinding
import com.project.job.databinding.ItemMessageSentBinding

class ChatMessagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: List<MessageResponse> = emptyList()
    private val currentUserId = "current_user" // TODO: Get from auth token or user session

    fun submitList(newMessages: List<MessageResponse>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun addMessage(message: MessageResponse) {
        messages = messages + message
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> SentMessageViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
            else -> ReceivedMessageViewHolder(ItemMessageBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageResponse) {
            binding.textMessage.text = message.message
            binding.textTime.text = message.createdAt?.let { /* TODO: format timestamp */ it } ?: "now"
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageResponse) {
            binding.textMessage.text = message.message
            binding.textTime.text = message.createdAt?.let { /* TODO: format timestamp */ it } ?: "now"
        }
    }

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }
}
