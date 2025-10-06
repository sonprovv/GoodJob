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
import com.project.job.data.source.remote.api.response.PowersInfo
import com.project.job.databinding.FragmentServiceMaintenanceChildBinding
import com.project.job.ui.intro.MaintenanceIntroActivity
import com.project.job.ui.intro.MaintenanceWashingIntroActivity
import com.project.job.ui.service.maintenanceservice.adapter.PowerAdapter
import com.project.job.utils.addFadeClickEffect

class ServiceMaintenanceChildFragment : Fragment() {
    private var _binding: FragmentServiceMaintenanceChildBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PowerAdapter

    // Listener để thông báo cho activity khi có sự thay đổi
    private var priceChangedListener: OnPriceChangedListener? = null

    // Static listener để chia sẻ giữa tất cả các fragment instance
    companion object {
        private var sharedListener: OnPriceChangedListener? = null

        fun setSharedListener(listener: OnPriceChangedListener?) {
            sharedListener = listener
        }
    }

    // Method để set listener từ activity
    fun setPriceChangedListener(listener: OnPriceChangedListener) {
        this.priceChangedListener = listener
    }

    // Lưu trữ tên service hiện tại để sử dụng trong callback
    private lateinit var serviceName: String

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
        val serviceNameArg = arguments?.getString("serviceName").orEmpty()
        val powers = arguments?.getParcelableArrayList<PowersInfo>("powers") ?: emptyList()
        val maintenancePower = arguments?.getString("maintenance").orEmpty()

        // Lưu trữ tên service hiện tại để sử dụng trong callback
        serviceName = serviceNameArg
        // Load service image
        Glide.with(binding.root)
            .load(image.takeIf { it.isNotEmpty() })
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivService)

        // Setup RecyclerView with PowerAdapter
        setupPowerRecyclerView(powers, maintenancePower)

        // Setup intro navigation
        setupIntroNavigation()
    }

    private fun setupPowerRecyclerView(powers: List<PowersInfo>, maintenanceText: String) {
        adapter = PowerAdapter { selectedItems ->
            // Callback khi quantity thay đổi - thông báo cho activity với tất cả items hiện tại và tên service
            Log.d("ServiceMaintenanceChild", "All items: ${adapter.getAllItems()}")
            // Gửi tất cả items hiện tại và tên service hiện tại
            (priceChangedListener ?: sharedListener)?.onPriceChanged(adapter.getAllItems(), serviceName)
        }
        
        binding.rcvPower.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ServiceMaintenanceChildFragment.adapter
        }
        
        // Submit powers list to adapter with maintenance text
        adapter.submitList(powers, maintenanceText)
    }

    private fun setupIntroNavigation() {
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