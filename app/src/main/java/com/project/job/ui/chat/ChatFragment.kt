package com.project.job.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.project.job.base.BaseFragment
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.FragmentChatBinding
import com.project.job.ui.chat.detail.ChatDetailActivity
import com.project.job.ui.chat.taskerfavorite.TaskerFavoriteActivity
import com.project.job.ui.loading.LoadingDialog
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ChatFragment : BaseFragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // Use viewModels delegate for AndroidViewModel
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: ChatAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        // Initialize components
        loadingDialog = LoadingDialog(requireActivity())
        // viewModel is already initialized by viewModels delegate
        
        // Setup adapter with click listener
        adapter = ChatAdapter { conversation ->
            navigateToChatDetail(conversation.sender.id, conversation.sender.username, conversation.sender.avatar)
        }
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup click listeners
        setupClickListeners()
        
        // Observe ViewModel
        observeViewModel()
        
        // Load conversations
        viewModel.getConversations()

    }

    private fun setupRecyclerView() {
        binding.rcvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatFragment.adapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        // Navigate to favorite taskers
        binding.llFavTasker.addFadeClickEffect {
            val intent = Intent(requireContext(), TaskerFavoriteActivity::class.java)
            startActivityWithAnimation(intent)
        }
        
        // Click empty state card to go to favorite taskers
        binding.cardChatTaskerFavorite.setOnClickListener {
            val intent = Intent(requireContext(), TaskerFavoriteActivity::class.java)
            startActivityWithAnimation(intent)
        }
    }
    
    private fun navigateToChatDetail(receiverId: String, partnerName: String, partnerAvatar: String) {
        val userId = preferencesManager.getUserData()["user_id"] ?: ""
        val roomId = userId + "_" + receiverId
        val intent = Intent(requireContext(), ChatDetailActivity::class.java).apply {
            putExtra(ChatDetailActivity.EXTRA_RECEIVER_ID, receiverId)
            putExtra(ChatDetailActivity.EXTRA_PARTNER_NAME, partnerName)
            putExtra(ChatDetailActivity.EXTRA_PARTNER_AVATAR, partnerAvatar)
            putExtra(ChatDetailActivity.EXTRA_ROOM_ID, roomId) // Empty room ID
        }
        startActivityWithAnimation(intent)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                // Collect loading state
                viewModel.loading.collectLatest { isLoading ->
                    if (isLoading) {
                        loadingDialog.show()
                    } else {
                        loadingDialog.hide()
                    }
                }
            }
            
            launch {
                // Collect conversations
                viewModel.conversations.collectLatest { conversations ->
                    Log.d("ChatFragment", "Conversations received: ${conversations?.size} items")
                    if (conversations != null) {
                        Log.d("ChatFragment", "Submitting list to adapter: $conversations")
                        adapter.submitList(conversations)
                        updateEmptyState(conversations.isEmpty())
                        Log.d("ChatFragment", "Empty state: ${conversations.isEmpty()}, ll_chat_have_data visible: ${!conversations.isEmpty()}")
                    } else {
                        Log.d("ChatFragment", "Conversations is null")
                    }
                }
            }
            
            launch {
                // Collect error state
                viewModel.error.collectLatest { errorMessage ->
                    errorMessage?.let {
                        showError(it)
                    }
                }
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        Log.d("ChatFragment", "updateEmptyState: isEmpty=$isEmpty")
        binding.llLoginSuccessNoData.isVisible = isEmpty
        binding.llChatHaveData.isVisible = !isEmpty
        Log.d("ChatFragment", "After update - NoData visible: ${binding.llLoginSuccessNoData.isVisible}, HaveData visible: ${binding.llChatHaveData.isVisible}")
    }
    
    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Thử lại") {
                viewModel.getConversations()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}