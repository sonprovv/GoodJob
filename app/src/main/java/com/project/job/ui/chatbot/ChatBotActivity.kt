package com.project.job.ui.chatbot

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.data.model.ChatMessage
import com.project.job.data.model.ChatMessageType
import com.project.job.databinding.ActivityChatBotBinding
import kotlinx.coroutines.launch

class ChatBotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBotBinding
    private lateinit var chatBotAdapter: ChatBotAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private val viewModel: ChatBotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        showWelcomeMessage()
        
        // Debug: Check if input layout is visible
        binding.inputLayout.post {
            android.util.Log.d("ChatBot", "Input layout height: ${binding.inputLayout.height}")
            android.util.Log.d("ChatBot", "Input layout visibility: ${binding.inputLayout.visibility}")
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            // Adjust padding for system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            
            // Handle keyboard visibility
            if (ime.bottom > 0) {
                // Keyboard is visible - scroll to bottom to show latest message
                binding.root.post {
                    scrollToBottom()
                }
            }
            
            insets
        }
    }

    private fun setupRecyclerView() {
        chatBotAdapter = ChatBotAdapter(chatMessages)
        binding.rvChatMessages.apply {
            adapter = chatBotAdapter
            layoutManager = LinearLayoutManager(this@ChatBotActivity)
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        // Send button click
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Send on Enter key
        binding.etMessageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        
        // Handle focus changes to ensure input is visible
        binding.etMessageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.root.post {
                    scrollToBottom()
                }
            }
        }

        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Clear chat
        binding.tvClear.setOnClickListener {
            clearChat()
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            // Add user message
            val userMessage = ChatMessage(messageText, true, System.currentTimeMillis())
            chatMessages.add(userMessage)
            chatBotAdapter.notifyItemInserted(chatMessages.size - 1)

            // Clear input
            binding.etMessageInput.text.clear()

            // Scroll to bottom
            scrollToBottom()

            // Show typing indicator
            showTypingIndicator()

            // Call actual API through ViewModel
            viewModel.chatBot(messageText)
        }
    }

    private fun showTypingIndicator() {
        val typingMessage = ChatMessage("Typing...", false, System.currentTimeMillis(), true)
        chatMessages.add(typingMessage)
        chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.loading.collect { isLoading ->
                // Loading state is handled by typing indicator
            }
        }

        lifecycleScope.launch {
            viewModel.success_change.collect { success ->
                if (success == true) {
                    hideTypingIndicator()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (!error.isNullOrEmpty()) {
                    hideTypingIndicator()
                    addErrorMessage(error)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.response_text.collect { text ->
                if (!text.isNullOrEmpty()) {
                    addInfoMessage(text)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.response_jobs.collect { jobs ->
                if (!jobs.isNullOrEmpty()) {
                    addJobListMessage(jobs)
                }
            }
        }
    }

    private fun hideTypingIndicator() {
        val typingIndex = chatMessages.indexOfFirst { it.isTyping }
        if (typingIndex != -1) {
            chatMessages.removeAt(typingIndex)
            chatBotAdapter.notifyItemRemoved(typingIndex)
        }
    }

    private fun addInfoMessage(text: String) {
        val aiMessage = ChatMessage(
            text = text,
            isUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = ChatMessageType.INFO
        )
        chatMessages.add(aiMessage)
        chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun addJobListMessage(jobs: List<com.project.job.data.source.remote.api.response.QueryJobs>) {
        val jobMessage = ChatMessage(
            text = "Danh sách công việc",
            isUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = ChatMessageType.JOB_LIST,
            jobList = jobs
        )
        chatMessages.add(jobMessage)
        chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun addErrorMessage(error: String) {
        val errorMessage = ChatMessage(
            text = "Xin lỗi, đã có lỗi xảy ra: $error",
            isUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = ChatMessageType.TEXT
        )
        chatMessages.add(errorMessage)
        chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }


    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            binding.rvChatMessages.post {
                binding.rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
            }
        }
    }

    private fun showWelcomeMessage() {
        Handler(Looper.getMainLooper()).postDelayed({
            val welcomeMessage = ChatMessage(
                "👋 Xin chào! Mình là **Trợ lý việc làm AI** của bạn. Mình có thể giúp bạn với:\n\n" +
                        "• Chiến lược tìm kiếm việc làm\n" +
                        "• Tối ưu hóa CV / hồ sơ xin việc\n" +
                        "• Mẹo phỏng vấn hiệu quả\n" +
                        "• Định hướng và tư vấn nghề nghiệp\n\n" +
                        "Hôm nay bạn muốn mình hỗ trợ về điều gì?",
                false,
                System.currentTimeMillis()
            )
            chatMessages.add(welcomeMessage)
            chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
            scrollToBottom()
        }, 500)
    }

    private fun clearChat() {
        chatMessages.clear()
        chatBotAdapter.notifyDataSetChanged()
        showWelcomeMessage()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}