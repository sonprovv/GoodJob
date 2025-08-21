package com.project.job.ui.chat.messagetab

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentMessageBinding
import com.project.job.ui.chat.taskerfavorite.TaskerFavoriteActivity

class MessageFragment : Fragment() {
   private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up any additional UI components or listeners here
        binding.cardChatTaskerFavorite.setOnClickListener {
            val intent = Intent(requireContext(), TaskerFavoriteActivity::class.java)
            startActivity(intent)
        }
    }
}