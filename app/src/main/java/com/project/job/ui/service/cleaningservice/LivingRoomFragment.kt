package com.project.job.ui.service.cleaningservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentLivingRoomBinding
import com.project.job.ui.service.cleaningservice.adapter.DetailServiceAdapter

class LivingRoomFragment : Fragment() {
    private var _binding: FragmentLivingRoomBinding? = null
    private val binding get() = _binding!!
    private val detailJob = arrayOf(
        "Quét bụi và lau tất cả các bề mặt",
        "Lau công tắc và tay cầm",
        "Đổ rác",
        "Quét và lau sàn"
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLivingRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewDetailServiceLivingroom.adapter = DetailServiceAdapter(detailJob)
    }


}