package com.project.job.ui.service.cleaningservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentBathRoomBinding
import com.project.job.ui.service.cleaningservice.adapter.DetailServiceAdapter

class BathRoomFragment : Fragment() {
    private var _binding: FragmentBathRoomBinding? = null
    private val binding get() = _binding!!
    private val detailJob = arrayOf(
        "Làm sạch toilet",
        "Lau chùi vòi sen, bồn tắm và bồn rửa",
        "Làm sạch bên ngoài tủ, gương và đồ đạc",
        "Lau công tắc và tay cầm",
        "Sắp xếp ngăn nắp các vật dụng",
        "Đổ rác",
        "Quét và lau sàn"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBathRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewDetailServiceBathroom.adapter = DetailServiceAdapter(detailJob)
    }

}