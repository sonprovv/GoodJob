package com.project.job.ui.service.maintenanceservice

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.job.R
import com.project.job.databinding.FragmentSelectedItemBottomSheetBinding
import com.project.job.ui.service.maintenanceservice.adapter.SelectedItemAdapter

// Type alias để rút ngắn tên
private typealias PowerItem = com.project.job.ui.service.maintenanceservice.adapter.PowerItem
private typealias ServicePowerItem = com.project.job.ui.service.maintenanceservice.adapter.ServicePowerItem

class SelectedItemBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding : FragmentSelectedItemBottomSheetBinding ?= null
    private val binding get() = _binding!!
    private lateinit var adapter: SelectedItemAdapter

    // Interface để giao tiếp với activity
    interface OnNextButtonClickListener {
        fun onNextButtonClicked()
    }

    private var listener: OnNextButtonClickListener? = null

    fun setOnNextButtonClickListener(listener: OnNextButtonClickListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedItemBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                // Set background transparent cho bottom sheet container
                it.background = null

                // Thiết lập behavior
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        // Set background transparent cho window
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSelectedItems()

        // Đóng bottom sheet khi nhấn vào header
        binding.llBottomSheetDetail.setOnClickListener{
            dismiss()
        }

        // Xử lý nút "Tiếp theo" trong bottom sheet
        binding.cardViewButtonNext.setOnClickListener {
            handleNextButtonInBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        adapter = SelectedItemAdapter()
        binding.recyclerViewSelectedItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectedItemBottomSheetFragment.adapter
        }
    }

    private fun loadSelectedItems() {
        // Lấy danh sách selected items từ arguments
        val selectedItems = arguments?.getParcelableArrayList<ServicePowerItem>("selected_items")
            ?: emptyList()

        // Chỉ hiển thị các items có quantity > 0
        val itemsToShow = selectedItems.filter { it.powerItem.quantity > 0 }

        adapter.setData(itemsToShow)

        // Nếu không có items nào được chọn, đóng bottom sheet
        if (itemsToShow.isEmpty()) {
            dismiss()
        }

        // Hiển thị thông tin tổng quan
        updateTotalInfo()
    }

    private fun updateTotalInfo() {
        val totalSelectedCount = arguments?.getInt("total_selected_count") ?: 0
        val totalPrice = arguments?.getInt("total_price") ?: 0
        val totalHours = arguments?.getInt("total_hours") ?: 0

        // Hiển thị số lượng items đã chọn
        binding.tvTotalSelectedItems.text = totalSelectedCount.toString()

        // Hiển thị tổng giá với thông tin thời gian
        if (totalPrice > 0) {
            if (totalHours > 0) {
                binding.tvPrice.text = "${String.format("%,d", totalPrice)}đ/${totalHours}h"
            } else {
                binding.tvPrice.text = "${String.format("%,d", totalPrice)}đ"
            }
        } else {
            binding.tvPrice.text = "Chọn dịch vụ"
        }
    }

    private fun handleNextButtonInBottomSheet() {
        // Gọi callback để thông báo cho activity
        listener?.onNextButtonClicked()

        // Đóng bottom sheet
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}