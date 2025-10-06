package com.project.job.ui.service.maintenanceservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.project.job.ui.service.maintenanceservice.adapter.PowerItem
import com.project.job.ui.service.maintenanceservice.adapter.ServicePowerItem
import com.project.job.ui.service.maintenanceservice.adapter.TabLayoutMaintenanceAdapter
import com.project.job.ui.service.maintenanceservice.viewmodel.MaintenanceViewModel
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.launch

//// Type alias để rút ngắn tên
//private typealias PowerItem = com.project.job.ui.service.maintenanceservice.adapter.PowerItem
//private typealias ServicePowerItem = com.project.job.ui.service.maintenanceservice.adapter.ServicePowerItem

interface OnPriceChangedListener {
    fun onPriceChanged(
        selectedItems: List<com.project.job.ui.service.maintenanceservice.adapter.PowerItem>,
        currentServiceName: String
    )
}

class SelectServiceMaintenanceActivity : AppCompatActivity(), OnPriceChangedListener {
    private val TAG = "SelectServiceMaintenance"
    private lateinit var binding: ActivitySelectServiceMaintenanceBinding
    private lateinit var adapter: TabLayoutMaintenanceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: MaintenanceViewModel

    // Danh sách tất cả ServicePowerItem được chọn từ tất cả các fragment
    private val allSelectedItems =
        mutableListOf<com.project.job.ui.service.maintenanceservice.adapter.ServicePowerItem>()

    // Mapping giữa tên service và UID của service từ API
    private val serviceUidMap = mutableMapOf<String, String>()

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
        // Khởi tạo hiển thị giá và bottom sheet detail mặc định
        updateTotalPriceDisplay()
        updateBottomSheetDetailVisibility()

        // Thêm click listener cho bottom sheet detail
        binding.llBottomSheetDetail.addFadeClickEffect {
            showSelectedItemsBottomSheet()
        }

        // Thêm click listener cho nút "Tiếp theo"
        binding.cardViewButtonNext.addFadeClickEffect {
            handleNextButtonClick()
        }
    }

    private fun handleNextButtonClick() {
        val selectedItemsData = allSelectedItems.filter { it.powerItem.quantity > 0 }

        if (selectedItemsData.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một thiết bị", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPrice = calculateTotalPrice()
        val totalHours = calculateTotalHours()

        // Tạo danh sách tên services và mô tả (giữ nguyên để hiển thị trong extraServices)
        val serviceNames = selectedItemsData.map { it.serviceName }.distinct()
        val serviceDescriptions = selectedItemsData.map { item ->
            val powerDescription = "${item.powerItem.powerName} x${item.powerItem.quantity}"

            // Thêm thông tin maintenance nếu có
            val maintenanceDescription = if (item.powerItem.isMaintenanceEnabled &&
                                            (item.powerItem.maintenanceQuantity ?: 0) > 0) {
                " (${item.powerItem.maintenanceName} x${item.powerItem.maintenanceQuantity})"
            } else {
                ""
            }

            powerDescription + maintenanceDescription
        }

        // Thu thập thông tin chi tiết về các items đã chọn
        // Service UIDs: UID của service từ API (Pntsvw5ILpxwdO7e1Gyg, O45WyERwfZsJxywdbDHR, v.v.)
        val serviceUids = selectedItemsData.map { it.uid }
        // Power UIDs: UID của power item (dưới 2HP, trên 2HP, v.v.)
        val powerUids = selectedItemsData.map { it.powerItem.uid }
        val quantities = selectedItemsData.map { it.powerItem.quantity }
        val maintenanceQuantities = selectedItemsData.map { it.powerItem.maintenanceQuantity }

        // Điều hướng sang SelectTimeFragment với dữ liệu maintenance chi tiết
        navigateToSelectTimeFragment(
            serviceNames = serviceNames,
            serviceDescriptions = serviceDescriptions,
            totalHours = totalHours,
            totalPrice = totalPrice,
            selectedItems = selectedItemsData,
            serviceUids = serviceUids,
            powerUids = powerUids,
            quantities = quantities,
            maintenanceQuantities = maintenanceQuantities
        )
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
                            // Lưu service UID mapping khi nhận được data từ API
                            updateServiceUidMap(items)
                            setupTabs()
                            // Cập nhật lại hiển thị giá và bottom sheet detail khi có dữ liệu mới
                            updateTotalPriceDisplay()
                            updateBottomSheetDetailVisibility()
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

        // Set shared listener cho tất cả các fragment
        ServiceMaintenanceChildFragment.setSharedListener(this)

        binding.tabLayoutMaintenance.apply {
            // Cho phép scroll nếu có nhiều tab
            tabMode = if (services.size > 2) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL

            // Xóa các tab cũ nếu có
            removeAllTabs()
        }
        adapter = TabLayoutMaintenanceAdapter(this, services, this)
        binding.viewPagerMaintenance.adapter = adapter

        // VÔ HIỆU HÓA SWIPE GESTURE
        binding.viewPagerMaintenance.isUserInputEnabled = false

        TabLayoutMediator(
            binding.tabLayoutMaintenance,
            binding.viewPagerMaintenance
        ) { tab, position ->
            val item = services[position]
            val textView = TextView(this).apply {
                text = item.serviceName
                setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(
                    this@SelectServiceMaintenanceActivity,
                    R.drawable.tab_background_unselected
                )
                setTextColor(
                    ContextCompat.getColor(
                        this@SelectServiceMaintenanceActivity,
                        R.color.black
                    )
                )
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            tab.customView = textView
        }.attach()

        binding.tabLayoutMaintenance.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
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
        binding.tabLayoutMaintenance.getTabAt(binding.tabLayoutMaintenance.selectedTabPosition)
            ?.let { updateTabAppearance(it, true) }
    }

    private fun updateTabAppearance(tab: TabLayout.Tab, isSelected: Boolean) {
        val textView = tab.customView as? TextView ?: return

        if (isSelected) {
            textView.background =
                ContextCompat.getDrawable(this, R.drawable.tab_background_selected)
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            textView.background =
                ContextCompat.getDrawable(this, R.drawable.tab_background_unselected)
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density + 0.5f).toInt()
    }

    // Hàm tính tổng số giờ từ tất cả các ServicePowerItem được chọn
    private fun calculateTotalHours(): Int {
        var totalHours = 0
        allSelectedItems.forEach { servicePowerItem ->
            val item = servicePowerItem.powerItem
            // Tổng số giờ = tổng quantity (vì 1 quantity = 1 giờ)
            if (item.quantity > 0) {
                totalHours += item.quantity
            }
        }
        return totalHours
    }

    // Hàm tính tổng giá từ tất cả các ServicePowerItem được chọn
    private fun calculateTotalPrice(): Int {
        var totalPrice = 0
        allSelectedItems.forEach { servicePowerItem ->
            val item = servicePowerItem.powerItem
            // Tính giá cho số lượng chính (chỉ khi có quantity > 0)
            if (item.quantity > 0) {
                val price = item.price ?: 0
                totalPrice += price * item.quantity
            }

            // Tính giá cho maintenance (chỉ khi bật maintenance và có quantityMaintenance > 0)
            if (item.isMaintenanceEnabled && (item.maintenanceQuantity ?: 0) > 0) {
                val maintenancePrice = item.priceAction ?: 0
                totalPrice += maintenancePrice * (item.maintenanceQuantity ?: 0)
            }
        }
        return totalPrice
    }

    // Hàm cập nhật hiển thị tổng giá trên nút với thông tin thời gian
    private fun updateTotalPriceDisplay() {
        val totalPrice = calculateTotalPrice()
        val totalHours = calculateTotalHours()
        val hasSelectedItems = allSelectedItems.any { it.powerItem.quantity > 0 }

        if (hasSelectedItems && totalPrice > 0) {
            if (totalHours > 0) {
                binding.tvPrice.text = "${String.format("%,d", totalPrice)}đ/${totalHours}h"
            } else {
                binding.tvPrice.text = "${String.format("%,d", totalPrice)}đ"
            }
            binding.tvPrice.visibility = View.VISIBLE
            binding.llBottomSheetBaoGia.visibility = View.VISIBLE
        } else {
            binding.tvPrice.text = "Chọn dịch vụ"
            binding.tvPrice.visibility = View.VISIBLE
            binding.llBottomSheetBaoGia.visibility = View.GONE
        }
    }

    // Hàm cập nhật hiển thị bottom sheet detail
    private fun updateBottomSheetDetailVisibility() {
        val totalSelectedItems = allSelectedItems.count { it.powerItem.quantity > 0 }

        if (totalSelectedItems > 0) {
            binding.llBottomSheetBaoGia.visibility = View.VISIBLE
            binding.llBottomSheetDetail.visibility = View.VISIBLE
            binding.tvTotalSelectedItems.text = totalSelectedItems.toString()
        } else {
            binding.llBottomSheetBaoGia.visibility = View.GONE
        }
    }

    // Hàm cập nhật mapping giữa tên service và UID từ API response
    private fun updateServiceUidMap(services: List<com.project.job.data.source.remote.api.response.MaintenanceData>) {
        serviceUidMap.clear()
        services.forEach { service ->
            serviceUidMap[service.serviceName] = service.uid
        }
    }

    private fun setupBottomSheetDetailClickListener() {
        binding.llBottomSheetDetail.addFadeClickEffect {
            showSelectedItemsBottomSheet()
        }
    }

    private fun showSelectedItemsBottomSheet() {
        val bottomSheetFragment = SelectedItemBottomSheetFragment().apply {
            arguments = Bundle().apply {
                // Truyền dữ liệu selected items
                val selectedItemsData = allSelectedItems.filter { it.powerItem.quantity > 0 }
                putParcelableArrayList("selected_items", ArrayList(selectedItemsData))

                // Truyền thông tin tổng quan
                val totalSelectedCount = selectedItemsData.size
                val totalPrice = calculateTotalPrice()
                val totalHours = calculateTotalHours()
                putInt("total_selected_count", totalSelectedCount)
                putInt("total_price", totalPrice)
                putInt("total_hours", totalHours)
            }

            // Thiết lập listener để nhận callback từ bottom sheet
            setOnNextButtonClickListener(object : SelectedItemBottomSheetFragment.OnNextButtonClickListener {
                override fun onNextButtonClicked() {
                    // Đóng bottom sheet và thực hiện hành động tương tự như nhấn nút "Tiếp theo" của activity
                    handleNextButtonClick()
                }
            })
        }
        bottomSheetFragment.show(supportFragmentManager, "SelectedItemBottomSheet")
    }

    override fun onPriceChanged(selectedItems: List<PowerItem>, currentServiceName: String) {
        // Tổng hợp tất cả items từ các fragment, loại bỏ duplicate theo uid
        val allItemsMap =
            mutableMapOf<String, com.project.job.ui.service.maintenanceservice.adapter.ServicePowerItem>()

        // Thêm items hiện tại
        allSelectedItems.forEach { item ->
            allItemsMap[item.powerItem.uid] = item
        }

        // Cập nhật với items mới từ fragment
        selectedItems.forEach { powerItem ->
            // Lấy service UID từ mapping, fallback về tên service nếu không tìm thấy
            val serviceUid = serviceUidMap[currentServiceName] ?: currentServiceName
            allItemsMap[powerItem.uid] = ServicePowerItem(currentServiceName, powerItem, serviceUid)
        }

        // Cập nhật danh sách tổng
        allSelectedItems.clear()
        allSelectedItems.addAll(allItemsMap.values)

        // Cập nhật lại hiển thị giá và bottom sheet detail khi có thay đổi
        updateTotalPriceDisplay()
        updateBottomSheetDetailVisibility()
    }

    private fun navigateToSelectTimeFragment(
        serviceNames: List<String>,
        serviceDescriptions: List<String>,
        totalHours: Int,
        totalPrice: Int,
        selectedItems: List<ServicePowerItem>,
        serviceUids: List<String>,
        powerUids: List<String>,
        quantities: List<Int>,
        maintenanceQuantities: List<Int?>
    ) {
        try {
            // Group các items theo service name để tạo description mới
            val groupedItems = selectedItems.groupBy { it.serviceName }

            val formattedDescriptions = groupedItems.map { (serviceName, items) ->
                val itemDescriptions = items.map { item ->
                    val powerDescription = "${item.powerItem.powerName} x${item.powerItem.quantity}"
                    
                    // Thêm thông tin maintenance nếu có
                    val maintenanceDescription = if (item.powerItem.isMaintenanceEnabled && 
                                                    (item.powerItem.maintenanceQuantity ?: 0) > 0) {
                        " (${item.powerItem.maintenanceName} x${item.powerItem.maintenanceQuantity})"
                    } else {
                        ""
                    }
                    
                    powerDescription + maintenanceDescription
                }.joinToString(", ")
                "$serviceName: $itemDescriptions"
            }

            val finalDescription = formattedDescriptions.joinToString("\n")

            // Tạo fragment mới
            val selectTimeFragment =
                com.project.job.ui.service.cleaningservice.SelectTimeFragment().apply {
                    arguments = Bundle().apply {
                        // Thông tin cơ bản về service
                        putString("serviceType", "maintenance")
                        putInt("totalHours", totalHours)
                        putInt("totalFee", totalPrice)

                        // Thông tin về duration với format mới
                        putString("durationDescription", finalDescription)
                        putInt("durationWorkingHour", totalHours)
                        putInt("durationFee", totalPrice)
                        putString("durationId", "maintenance_${System.currentTimeMillis()}")

                        // Thông tin về các service được chọn (cho cleaning service format)
                        val serviceExtras = serviceDescriptions.joinToString(", ")
                        putStringArrayList("extraServices", ArrayList(listOf(serviceExtras)))

                        // Thông tin về shift (có thể điều chỉnh theo nhu cầu)
                        putString("selectedShiftId", "maintenance_shift")
                        putInt("selectedShiftWorkingHour", totalHours)
                        putInt("selectedShiftFee", totalPrice)

                        // Thông tin chi tiết về các items đã chọn để tạo ServicePowerInfo chính xác
                        putStringArrayList("selectedServiceUids", ArrayList(serviceUids))
                        putStringArrayList("selectedPowerUids", ArrayList(powerUids))
                        putIntegerArrayList("selectedQuantities", ArrayList(quantities))
                        putIntegerArrayList(
                            "selectedMaintenanceQuantities",
                            ArrayList(maintenanceQuantities.map { it ?: 0 })
                        )

                        // Debug logging
                        Log.d(TAG, "Navigating to SelectTimeFragment with:")
                        Log.d(TAG, "  - Service names: ${serviceNames.joinToString(", ")}")
                        Log.d(TAG, "  - Total hours: $totalHours")
                        Log.d(TAG, "  - Total price: $totalPrice")
                        Log.d(TAG, "  - Selected items count: ${selectedItems.size}")
                        Log.d(TAG, "  - Service UIDs: ${serviceUids.joinToString(", ")}")
                        Log.d(TAG, "  - Power UIDs: ${powerUids.joinToString(", ")}")
                        Log.d(TAG, "  - Quantities: ${quantities.joinToString(", ")}")
                        Log.d(TAG, "  - Maintenance quantities: ${maintenanceQuantities.map { it ?: 0 }.joinToString(", ")}")
                        Log.d(TAG, "  - Formatted description: $finalDescription")
                    }
                }

            // Điều hướng sang SelectTimeFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, selectTimeFragment)
                .addToBackStack("SelectTimeFragment")
                .commit()

            Log.d(TAG, "Fragment transaction committed")

        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to SelectTimeFragment", e)
            Toast.makeText(this, "Có lỗi xảy ra khi chuyển trang", Toast.LENGTH_SHORT).show()
        }
    }
}