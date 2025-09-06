package com.project.job.ui.service.cleaningservice

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment.STYLE_NORMAL
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.R
import com.project.job.databinding.FragmentCleaningServiceDetailBinding
import com.project.job.ui.service.cleaningservice.adapter.TabLayoutAdapter

class CleaningServiceDetailFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCleaningServiceDetailBinding ?= null
    private val binding get() = _binding!!
    private val tabTilte = arrayOf(
        "Phòng ngủ",
        "Phòng tắm",
        "Phòng bếp",
        "Phòng khách và khu vực chung"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCleaningServiceDetailBinding.inflate(inflater, container, false)
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
        setupTabLayout()
        binding.cardViewButtonClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupTabLayout() {
        binding.viewPager2.adapter = TabLayoutAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = tabTilte[position]
        }.attach()
        binding.viewPager2.isUserInputEnabled = false
        for (i in 0..3) {
            val textView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_title_detail_job, null) as TextView
            binding.tabLayout.getTabAt(i)?.customView = textView
            val tab = binding.tabLayout.getTabAt(i)
            tab?.view?.tooltipText = null
        }
    }

}