package com.project.job.ui.service.cleaningservice

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.R
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.databinding.FragmentCleaningServiceDetailBinding
import com.project.job.ui.service.cleaningservice.adapter.TabLayoutAdapter

class CleaningServiceDetailFragment(private val cleaningService: List<CleaningService?>) : BottomSheetDialogFragment() {
    private var _binding: FragmentCleaningServiceDetailBinding? = null
    private val binding get() = _binding!!

    private val rooms = mutableListOf<CleaningService>()
    private var selectedDuration: CleaningDuration? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                it.background = null
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButton()
        updateRoomsFromCleaningServices()
        setupTabLayout()
    }

    private fun updateRoomsFromCleaningServices() {
        rooms.clear()
        cleaningService.forEach { service ->
            if (service != null) {
                rooms.add(
                    CleaningService(
                        uid = service.uid,
                        serviceName = service.serviceName,
                        serviceType = "CLEANING",
                        tasks = service.tasks,
                        image = service.image
                    )
                )
            }
        }
    }

    private fun setupButton() {
        binding.cardViewButtonClose.setOnClickListener {
            selectedDuration?.let { duration ->
                // Pass the selected duration back if needed
            }
            dismiss()
        }
    }

    private fun setupTabLayout() {
        if (rooms.isEmpty()) return

        // Configure TabLayout first
        binding.tabLayout.apply {
            // Cho phép scroll nếu có nhiều tab
            tabMode = if (rooms.size > 3) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL

            // Xóa các tab cũ nếu có
            removeAllTabs()
        }

        // Set up ViewPager2
        val adapter = TabLayoutAdapter(requireActivity(), rooms = rooms)
        binding.viewPager2.adapter = adapter

        // VÔ HIỆU HÓA SWIPE GESTURE
        binding.viewPager2.isUserInputEnabled = false

        // Connect TabLayout with ViewPager2 with proper text setup
        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            // Sử dụng custom view cho tab
            tab.customView = createTabView(rooms[position].serviceName)
        }
        tabLayoutMediator?.attach()

        // Setup tab selection listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabAppearance(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabAppearance(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle reselection if needed
            }
        })

        // Set initial selection after a short delay to ensure proper setup
        binding.tabLayout.post {
            if (binding.tabLayout.tabCount > 0) {
                val firstTab = binding.tabLayout.getTabAt(0)
                firstTab?.let {
                    updateTabAppearance(it, true)
                    // Update other tabs as unselected
                    for (i in 1 until binding.tabLayout.tabCount) {
                        binding.tabLayout.getTabAt(i)?.let { tab ->
                            updateTabAppearance(tab, false)
                        }
                    }
                }
            }
        }
    }

    private fun createTabView(text: String): View {
        return TextView(requireContext()).apply {
            this.text = text
            tooltipText = null

            // Text styling
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Design_Tab)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            isAllCaps = false

            // Padding - điều chỉnh để text hiển thị tốt hơn
            val horizontalPadding = 32.dpToPx()
            val verticalPadding = 16.dpToPx()
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

            // Layout parameters
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Minimum width để đảm bảo tab không quá nhỏ
            minWidth = 80.dpToPx()
        }
    }

    private fun updateTabAppearance(tab: TabLayout.Tab, isSelected: Boolean) {
        val textView = tab.customView as? TextView ?: return

        if (isSelected) {
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_background_selected)
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_background_unselected)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density + 0.5f).toInt()
    }

    override fun onDestroyView() {
        // Cleanup để tránh memory leak
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        _binding = null
        super.onDestroyView()
    }
}