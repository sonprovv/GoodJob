package com.project.job.ui.service.cleaningservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.base.BaseActivity
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.databinding.ActivitySelectServiceBinding
import com.project.job.ui.intro.CleaningIntroActivity
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.map.MapActivity
import com.project.job.ui.service.SelectTimeFragment
import com.project.job.ui.service.cleaningservice.adapter.DurationAdapter
import com.project.job.ui.service.cleaningservice.viewmodel.CleaningServiceViewModel
import com.project.job.utils.addFadeClickEffect
import com.project.job.utils.SelectedRoomManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectServiceActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectServiceBinding
    private lateinit var loadingDialog: LoadingDialog
    private val selectedExtraServices = mutableSetOf<Int>()
    private val EXTRA_SERVICE_FEE = 50000 // Extra service fee in VND
    private var currentBasePrice: Int = 0 // To store the base price without extra services
    private lateinit var viewModel: CleaningServiceViewModel
    private lateinit var durationAdapter: DurationAdapter
    private var cleaningService: List<CleaningService?> = emptyList()
    private var selectedDuration: CleaningDuration? = null
    private lateinit var preferencesManager: PreferencesManager
    private var totalHours = 0
    private var totalFee = 0

    // Location tạm cho job hiện tại (không update user profile)
    private var jobLocationAddress: String? = null
    private var jobLocationLatitude: Double = 0.0
    private var jobLocationLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        preferencesManager = PreferencesManager(this)

        // Handle location data from MapActivity
        handleLocationFromMap()

        viewModel = ViewModelProvider(this).get(CleaningServiceViewModel::class.java)
        viewModel.getServiceCleaning()

        // Set up radio button click listeners
        setupExtraServicesRadioGroup()

        // Xử lý hiển thị địa chỉ - loại bỏ tọa độ và chỉ hiển thị địa chỉ
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
//            location.contains(",") -> {
//                // Nếu chỉ có dấu phẩy thông thường, lấy phần sau dấu phẩy đầu tiên
//                location.substringAfter(",").trim()
//            }
            else -> location
        }
        
        binding.tvLocation.text = displayLocation

        // Handle back button click
        binding.ivBack.addFadeClickEffect {
            finish()
        }

        // Initialize duration adapter with callback
        durationAdapter = DurationAdapter().apply {
            onDurationSelected = { duration ->
                selectedDuration = duration
                updatePrice(duration.fee)
            }
        }
        binding.rcvDuration.adapter = durationAdapter

        // Then observe ViewModel
        observeViewModel()

        // Handle job detail card click
        binding.cardViewJobDetail.setOnClickListener {
            // Show CleaningServiceDetailFragment
            val cleaningServiceDetailFragment = CleaningServiceDetailFragment(cleaningService)
            Log.d("SelectServiceActivity", "Cleaning Service Data: $cleaningService")
            cleaningServiceDetailFragment.show(
                supportFragmentManager,
                "CleaningServiceDetailFragment"
            )
        }

        // Handle info button click
        binding.ivInfo.addFadeClickEffect {
            val intent = Intent(this, CleaningIntroActivity::class.java)
            startActivity(intent)
        }

        // Handle location header click
        binding.llContentHeader.addFadeClickEffect {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("source", "cleaning_service")
            }
            startActivity(intent)
        }

        // THÊM: Back stack listener để quản lý UI state
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // Delay để chờ fragment animation hoàn thành (400ms)
                binding.root.postDelayed({
                    // Ẩn fragment container với INVISIBLE để giữ layout
                    findViewById<View>(R.id.fragment_container)?.visibility = View.INVISIBLE
                    // Hiện lại Activity content ngay lập tức (không fade để mượt hơn)
                    findViewById<View>(R.id.activity_content)?.visibility = View.VISIBLE
                }, 400) // Match với animation duration
            }
        }

        binding.cardViewButtonNext.setOnClickListener {
            // Get selected extra services
            val extraServices = mutableListOf<String>()
            if (selectedExtraServices.contains(R.id.ll_extra_cooking)) {
                extraServices.add("Nấu ăn")
            }
            if (selectedExtraServices.contains(R.id.ll_extra_ironing)) {
                extraServices.add("Ủi đồ")
            }
            
            // Get selected rooms data
            val selectedRooms = SelectedRoomManager.getSelectedRooms()
            val selectedRoomNames = ArrayList(selectedRooms.map { it.serviceName })
            val selectedRoomCount = SelectedRoomManager.getSelectedRoomsCount()
            
            // Debug logging
            Log.d("SelectServiceActivity", "Selected rooms count: $selectedRoomCount")
            Log.d("SelectServiceActivity", "Selected room names: ${selectedRoomNames.joinToString(", ")}")
            Log.d("SelectServiceActivity", "Selected rooms: $selectedRooms")
            
            // Navigate to SelectTimeFragment using fragment transaction
            val fragment = SelectTimeFragment().apply {
                arguments = Bundle().apply {
                    putInt("totalHours", totalHours)
                    putInt("totalFee", totalFee)
                    putString("durationDescription", selectedDuration?.description ?: "")
                    putInt("durationWorkingHour", selectedDuration?.workingHour ?: 0)
                    putInt("durationFee", selectedDuration?.fee ?: 0)
                    putString("durationId", selectedDuration?.uid ?: "")
                    putStringArrayList("extraServices", ArrayList(extraServices))
                    putString("serviceType", "cleaning")
                    
                    // Truyền location đã chọn cho job này
                    val locationForJob = jobLocationAddress 
                        ?: preferencesManager.getUserData()["user_location"] 
                        ?: ""
                    putString("jobLocationAddress", locationForJob)
                    putDouble("jobLocationLatitude", jobLocationLatitude)
                    putDouble("jobLocationLongitude", jobLocationLongitude)
                }
            }

            // Sử dụng phương thức navigateToFragment từ BaseActivity nếu đã có, hoặc tự định nghĩa
            navigateToFragment(fragment, R.id.fragment_container, "SelectTimeFragment")
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
            Log.d("SelectServiceActivity", "Received location from map: $selectedAddress")

            // Lưu location tạm cho job này (KHÔNG update user profile)
            jobLocationAddress = selectedAddress
            
            // Lưu coordinates tạm
            val latitude = intent.getDoubleExtra("selected_latitude", 0.0)
            val longitude = intent.getDoubleExtra("selected_longitude", 0.0)
            if (latitude != 0.0 && longitude != 0.0) {
                jobLocationLatitude = latitude
                jobLocationLongitude = longitude
                Log.d("SelectServiceActivity", "Saved job location: Lat=$latitude, Lng=$longitude")
            }

            // Update UI với location cho job này
            binding.tvLocation.text = selectedAddress
            
            Log.d("SelectServiceActivity", "Job location updated (user profile NOT changed): $selectedAddress")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        binding.ivBack.isEnabled = false
                        binding.ivInfo.isEnabled = false
                        binding.llContentHeader.isEnabled = false
                        loadingDialog.show()
                        binding.cardViewJobDetail.isEnabled = false
                        binding.cardViewButtonNext.visibility = View.GONE
                        binding.llExtraCooking.isEnabled = false
                        binding.llExtraIroning.isEnabled = false
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.ivBack.isEnabled = true
                        binding.ivInfo.isEnabled = true
                        binding.llContentHeader.isEnabled = true
                        loadingDialog.hide()
                        binding.cardViewJobDetail.isEnabled = true
                        binding.cardViewButtonNext.visibility = View.VISIBLE
                        binding.llExtraCooking.isEnabled = true
                        binding.llExtraIroning.isEnabled = true
                    }
                }
            }
            launch {
                viewModel.durations.collectLatest { durations ->
                    durationAdapter.submitList(durations)
                    // The adapter will handle the first selection and callbacks
                }
            }
            launch {
                viewModel.cleaningdata.collectLatest { cleaningData ->
                    cleaningService = cleaningData
                }
            }
        }
    }

    private fun setupExtraServicesRadioGroup() {
        // Set click listeners for extra services
        binding.rgServiceExtras.findViewById<View>(R.id.ll_extra_cooking).setOnClickListener {
            handleExtraServiceClick(R.id.ll_extra_cooking, R.id.tv_pan)
        }
        binding.rgServiceExtras.findViewById<View>(R.id.ll_extra_ironing).setOnClickListener {
            handleExtraServiceClick(R.id.ll_extra_ironing, R.id.tv_iron)
        }
    }

    private fun handleExtraServiceClick(layoutId: Int, textViewId: Int) {
        if (selectedExtraServices.contains(layoutId)) {
            // Toggle off if already selected
            selectedExtraServices.remove(layoutId)
            updateExtraServiceAppearance(layoutId, false)
        } else {
            // Add to selected services
            selectedExtraServices.add(layoutId)
            updateExtraServiceAppearance(layoutId, true)
        }
        updateTotalPrice()
    }

    private fun updatePrice(price: Int) {
        currentBasePrice = price
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val extraServiceFee = selectedExtraServices.size * EXTRA_SERVICE_FEE
        val totalPrice = currentBasePrice + extraServiceFee

        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(totalPrice)
        val baseHours = selectedDuration?.workingHour ?: 1
        val totalHours = baseHours + selectedExtraServices.size // Add 1 hour per extra service
        this.totalHours = totalHours
        this.totalFee = totalPrice
        binding.tvPrice.text = "$formattedPrice VND/${totalHours}h"
    }

    private fun updateExtraServiceAppearance(layoutId: Int, isSelected: Boolean) {
        val backgroundRes = if (isSelected) R.drawable.bg_edt_orange else R.drawable.bg_edt_white
        val textColorRes = if (isSelected) R.color.cam else R.color.black

        when (layoutId) {
            R.id.ll_extra_cooking -> {
                binding.llExtraCooking.setBackgroundResource(backgroundRes)
                binding.tvPan.setTextColor(ContextCompat.getColor(this, textColorRes))
            }

            R.id.ll_extra_ironing -> {
                binding.llExtraIroning.setBackgroundResource(backgroundRes)
                binding.tvIron.setTextColor(ContextCompat.getColor(this, textColorRes))
            }
        }
    }

    // Override phương thức hideActivityContent nếu cần custom
    override fun hideActivityContent() {
        // Ẩn toàn bộ activity_content (bao gồm header, content và button)
        findViewById<View>(R.id.activity_content)?.visibility = View.GONE
    }

    // Override phương thức showActivityContent nếu cần custom
    override fun showActivityContent() {
        // Hiện lại toàn bộ activity_content
        findViewById<View>(R.id.activity_content)?.visibility = View.VISIBLE
    }
    
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}