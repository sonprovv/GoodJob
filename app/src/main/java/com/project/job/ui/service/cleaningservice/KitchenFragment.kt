package com.project.job.ui.service.cleaningservice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentKitchenBinding
import com.project.job.ui.service.cleaningservice.adapter.DetailServiceAdapter

class KitchenFragment : Fragment() {
    private var _binding: FragmentKitchenBinding? = null
    private val binding get() = _binding!!
    private val detailJob = arrayOf(
        "Rửa chén và xếp chén đĩa",
        "Lau bụi và lau tất cả các bề mặt",
        "Lau mặt ngoài của tủ bếp và thiết bị gia dụng",
        "Lau công tắc và tay cầm",
        "Cọ rửa bếp và mặt bàn",
        "Làm sạch bồn rửa",
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
        _binding = FragmentKitchenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewDetailServiceKitchen.adapter = DetailServiceAdapter(detailJob)
    }

}