package com.project.job.ui.chat.notificationtab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentNotificationMesBinding

class NotificationMesFragment : Fragment() {
    private var _binding: FragmentNotificationMesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationMesBinding.inflate(inflater, container, false)
        return binding.root
    }

}