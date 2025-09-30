package com.project.job.ui.service.maintenanceservice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.databinding.FragmentServiceMaintenanceChildBinding
import com.project.job.ui.intro.MaintenanceIntroActivity
import com.project.job.ui.intro.MaintenanceWashingIntroActivity
import com.project.job.ui.service.maintenanceservice.adapter.PowerAdapter
import com.project.job.utils.addFadeClickEffect

class ServiceMaintenanceChildFragment : Fragment() {
    private var _binding: FragmentServiceMaintenanceChildBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PowerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceMaintenanceChildBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val image = arguments?.getString("image").orEmpty()
        val serviceName = arguments?.getString("serviceName").orEmpty()
        val powers = arguments?.getStringArrayList("powers") ?: arrayListOf()
        val maintenancePower = arguments?.getString("maintenance").orEmpty()
        // Load service image
        Glide.with(binding.root)
            .load(image.takeIf { it.isNotEmpty() })
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivService)

        // Setup RecyclerView with PowerAdapter
        setupPowerRecyclerView(powers, maintenancePower)

        // Setup intro navigation
        setupIntroNavigation(serviceName)
    }

    private fun setupPowerRecyclerView(powers: List<String>, maintenanceText: String) {
        adapter = PowerAdapter { selectedItems ->
            // Callback khi quantity thay đổi
            Log.d("ServiceMaintenanceChild", "Selected items: $selectedItems")
            // TODO: Update price or other UI based on selected items
        }
        
        binding.rcvPower.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ServiceMaintenanceChildFragment.adapter
        }
        
        // Submit powers list to adapter with maintenance text
        adapter.submitList(powers, maintenanceText)
    }

    private fun setupIntroNavigation(serviceName: String) {
        when (serviceName) {
            "Máy giặt" -> {
                binding.cardViewJobDetail.addFadeClickEffect {
                    val intent = Intent(requireContext(), MaintenanceWashingIntroActivity::class.java)
                    startActivity(intent)
                }
            }
            "Điều hòa" -> {
                binding.cardViewJobDetail.addFadeClickEffect {
                    val intent = Intent(requireContext(), MaintenanceIntroActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}