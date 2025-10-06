package com.project.job.ui.chat.detail

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.data.repository.ChatRepository
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.databinding.ActivityChatDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private val repo = ChatRepository()
    private lateinit var adapter: ChatMessagesAdapter

    private val receiverId: String by lazy { intent.getStringExtra(EXTRA_RECEIVER_ID) ?: "" }
    private val partnerName: String? by lazy { intent.getStringExtra(EXTRA_PARTNER_NAME) }
    private val partnerAvatar: String? by lazy { intent.getStringExtra(EXTRA_PARTNER_AVATAR) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadMessages()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = partnerName ?: "Chat"
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ChatMessagesAdapter()
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.recyclerMessages.adapter = adapter

        binding.buttonSend.setOnClickListener { sendCurrentInput() }
        binding.inputMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentInput(); true
            } else false
        }
    }

    private fun sendCurrentInput() {
        val text = binding.inputMessage.text?.toString()?.trim().orEmpty()
        if (text.isEmpty() || receiverId.isEmpty()) return
        lifecycleScope.launch {
            setLoading(true)
            val result = withContext(Dispatchers.IO) {
                repo.sendMessage(SendMessageRequest(receiverId, text))
            }
            result.onSuccess { response ->
                adapter.addMessage(response.data)
                binding.inputMessage.setText("")
                scrollToBottom()
            }.onFailure {
                // TODO: show error feedback
            }
            setLoading(false)
        }
    }

    private fun loadMessages() {
        if (receiverId.isEmpty()) return
        lifecycleScope.launch {
            setLoading(true)
            val result = withContext(Dispatchers.IO) { repo.getMessages(receiverId, 50) }
            result.onSuccess { response ->
                adapter.submitList(response.data ?: emptyList())
                scrollToBottom()
            }.onFailure {
                // TODO: show error feedback
            }
            setLoading(false)
        }
    }

    private fun scrollToBottom() {
        val count = adapter.itemCount
        if (count > 0) binding.recyclerMessages.scrollToPosition(count - 1)
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.buttonSend.isEnabled = !loading
        binding.inputMessage.isEnabled = !loading
    }

    companion object {
        const val EXTRA_RECEIVER_ID = "receiverId"
        const val EXTRA_PARTNER_NAME = "partnerName"
        const val EXTRA_PARTNER_AVATAR = "partnerAvatar"
    }
}
