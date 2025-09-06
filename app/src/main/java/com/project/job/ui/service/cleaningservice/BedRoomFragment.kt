package com.project.job.ui.service.cleaningservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentBedRoomBinding
import com.project.job.ui.service.cleaningservice.adapter.DetailServiceAdapter

class BedRoomFragment : Fragment() {
    private var _binding: FragmentBedRoomBinding? = null
    private val binding get() = _binding!!
    private val detailJob = arrayOf(
        "Lau bụi và lau tất cả các bề mặt",
        "Lau công tắc và tay cầm",
        "Lau sạch gương",
        "Sắp xếp lại giường cho gọn gàng (có thể thay khăn trải giường mới nếu bạn yêu cầu)",
        "Hút bụi và lau sàn"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBedRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewDetailServiceBedroom.adapter = DetailServiceAdapter(detailJob)
    }

}