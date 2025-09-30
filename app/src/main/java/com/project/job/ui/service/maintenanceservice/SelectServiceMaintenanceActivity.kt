package com.project.job.ui.service.maintenanceservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivitySelectServiceMaintenanceBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.map.MapActivity
import com.project.job.ui.service.maintenanceservice.adapter.TabLayoutMaintenanceAdapter
import com.project.job.ui.service.maintenanceservice.viewmodel.MaintenanceViewModel
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.launch

class SelectServiceMaintenanceActivity : AppCompatActivity() {
    private val TAG = "SelectServiceMaintenance"
    private lateinit var binding : ActivitySelectServiceMaintenanceBinding
    private lateinit var adapter: TabLayoutMaintenanceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel : MaintenanceViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectServiceMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        preferencesManager = PreferencesManager(this)

        viewModel = ViewModelProvider(this)[MaintenanceViewModel::class.java]
        viewModel.getMaintenanceService()


        // Handle location data from MapActivity
        handleLocationFromMap()
        val location = preferencesManager.getUserData()["user_location"] ?: ""
        val displayLocation = when {
            location.isEmpty() -> ""
            // Kiểm tra nếu chỉ có tọa độ không có địa chỉ
            location.matches(Regex("^\\d+(\\.\\d+)?,\\s*Lng:\\s*\\d+(\\.\\d+)?.*")) || // Format: 386665, Lng: 106,343867
                    location.matches(Regex("^\\d+(\\.\\d+)?,\\s*\\d+(\\.\\d+)?$")) -> { // Format: 20.123, 106.456
                "Chưa có địa chỉ cụ thể"
            }
            location.contains("°") && location.contains(",") -> {
                // Nếu có tọa độ kèm địa chỉ, lấy phần sau dấu phẩy đầu tiên
                val firstCommaIndex = location.indexOf(",")
                if (firstCommaIndex != -1 && firstCommaIndex < location.length - 1) {
                    location.substring(firstCommaIndex + 1).trim()
                } else {
                    location
                }
            }
            location.contains(",") -> {
                // Nếu chỉ có dấu phẩy thông thường, lấy phần sau dấu phẩy đầu tiên
                location.substringAfter(",").trim()
            }
            else -> location
        }

        binding.tvLocation.text = displayLocation

        // Handle back button click
        binding.ivBack.addFadeClickEffect {
            finish()
        }

        // Handle location header click
        binding.llContentHeader.addFadeClickEffect {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("source", "maintenance_service")
            }
            Log.d(TAG, "Navigating to MapActivity with source: maintenance_service")
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.loading.collect { isLoading ->
                        if (isLoading) loadingDialog.show() else loadingDialog.hide()
                    }
                }
                launch {
                    viewModel.maintenanceService.collect { list ->
                        val items = list.filterNotNull()
                        if (items.isNotEmpty()) {
                            setupTabs()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLocationFromMap()
    }

    private fun handleLocationFromMap() {
        val selectedAddress = intent.getStringExtra("selected_address")
        val locationSource = intent.getStringExtra("location_source")

        if (!selectedAddress.isNullOrEmpty() && locationSource == "map_selection") {
            Log.d(TAG, "Received location from map: $selectedAddress")

            // Update UI with new address
            binding.tvLocation.text = selectedAddress

            // Save to preferences
            preferencesManager.saveAddress(selectedAddress)

            // Save coordinates if available
            val latitude = intent.getDoubleExtra("selected_latitude", 0.0)
            val longitude = intent.getDoubleExtra("selected_longitude", 0.0)
            if (latitude != 0.0 && longitude != 0.0) {
                preferencesManager.saveLocationCoordinates(latitude, longitude)
            }
        }
    }

    private fun setupTabs() {
        val services = viewModel.maintenanceService.value.filterNotNull()
        binding.tabLayoutMaintenance.apply {
            // Cho phép scroll nếu có nhiều tab
            tabMode = if (services.size > 2) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL

            // Xóa các tab cũ nếu có
            removeAllTabs()
        }
        adapter = TabLayoutMaintenanceAdapter(this, services)
        binding.viewPagerMaintenance.adapter = adapter

        // VÔ HIỆU HÓA SWIPE GESTURE
        binding.viewPagerMaintenance.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayoutMaintenance, binding.viewPagerMaintenance) { tab, position ->
            val item = services[position]
            val textView = TextView(this).apply {
                text = item.serviceName
                setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(this@SelectServiceMaintenanceActivity, R.drawable.tab_background_unselected)
                setTextColor(ContextCompat.getColor(this@SelectServiceMaintenanceActivity, R.color.black))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            tab.customView = textView
        }.attach()

        binding.tabLayoutMaintenance.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabAppearance(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabAppearance(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // no-op
            }
        })

        // Initialize selected state for first tab
        binding.tabLayoutMaintenance.getTabAt(binding.tabLayoutMaintenance.selectedTabPosition)?.let { updateTabAppearance(it, true) }
    }

    private fun updateTabAppearance(tab: TabLayout.Tab, isSelected: Boolean) {
        val textView = tab.customView as? TextView ?: return

        if (isSelected) {
            textView.background = ContextCompat.getDrawable(this, R.drawable.tab_background_selected)
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            textView.background = ContextCompat.getDrawable(this, R.drawable.tab_background_unselected)
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density + 0.5f).toInt()
    }

}