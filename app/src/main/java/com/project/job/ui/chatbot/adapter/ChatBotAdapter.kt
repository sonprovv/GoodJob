package com.project.job.ui.chatbot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.model.ChatMessage
import com.project.job.data.model.ChatMessageType
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.*

class ChatBotAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
        private const val TYPE_TYPING = 3
        private const val TYPE_JOB_LIST = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].isTyping -> TYPE_TYPING
            messages[position].isUser -> TYPE_USER
            messages[position].messageType == ChatMessageType.JOB_LIST -> TYPE_JOB_LIST
            else -> TYPE_AI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> UserMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_user, parent, false)
            )
            TYPE_TYPING -> TypingIndicatorViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_typing_indicator, parent, false)
            )
            TYPE_JOB_LIST -> JobListViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_job_list, parent, false)
            )
            else -> AIMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_ai, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserMessageViewHolder -> holder.bind(messages[position])
            is AIMessageViewHolder -> holder.bind(messages[position])
            is JobListViewHolder -> holder.bind(messages[position])
            is TypingIndicatorViewHolder -> holder.bind()
        }
    }

    override fun getItemCount() = messages.size

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: ChatMessage) {
            tvMessage.text = message.text
            tvTimestamp.text = dateFormat.format(Date(message.timestamp))
        }
    }

    class AIMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val markwon: Markwon = Markwon.create(itemView.context)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: ChatMessage) {
            markwon.setMarkdown(tvMessage, message.text)
            tvTimestamp.text = dateFormat.format(Date(message.timestamp))
        }
    }

    class TypingIndicatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            // Typing indicator animation can be added here
        }
    }

    class JobListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobListHeader: TextView = itemView.findViewById(R.id.tvJobListHeader)
        private val rvJobList: RecyclerView = itemView.findViewById(R.id.rvJobList)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val markwon: Markwon = Markwon.create(itemView.context)

        fun bind(message: ChatMessage) {
            // Set header text with markdown support (answer from API)
            markwon.setMarkdown(tvJobListHeader, message.text)
            
            // Setup RecyclerView for job list
            message.jobList?.let { jobs ->
                val jobAdapter = JobAdapter(jobs)
                rvJobList.apply {
                    adapter = jobAdapter
                    layoutManager = LinearLayoutManager(itemView.context)
                    isNestedScrollingEnabled = false
                }
            }

            // Set timestamp
            tvTimestamp.text = dateFormat.format(Date(message.timestamp))
        }
    }
}