package com.project.job.ui.service.healthcareservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.HealthcareShift
import com.project.job.databinding.ActivitySelectServiceHealthCareBinding
import com.project.job.ui.intro.CleaningIntroActivity
import com.project.job.ui.map.MapActivity
import com.project.job.ui.service.cleaningservice.CleaningServiceDetailFragment
import com.project.job.ui.service.cleaningservice.SelectTimeFragment
import com.project.job.ui.service.cleaningservice.adapter.DurationAdapter
import com.project.job.utils.SelectedRoomManager
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectServiceHealthCareActivity : AppCompatActivity() {
    private val TAG = "SelectServiceHealthCare"
    private lateinit var binding: ActivitySelectServiceHealthCareBinding
    private lateinit var viewModel: HealthCareViewModel
    private var healthcareService: List<HealthcareService?> = emptyList()
    private var currentBasePrice: Int = 0 // To store the base price without extra services
    private var selectedShift: HealthcareShift? = null
    private lateinit var shiftAdapter: ShiftAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var totalHours = 0
    private var totalFee = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectServiceHealthCareBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        preferencesManager = PreferencesManager(this)
        
        // Handle location data from MapActivity
        handleLocationFromMap()

        viewModel = ViewModelProvider(this)[HealthCareViewModel::class.java]
        viewModel.getServiceHealthcare()

        // Initialize duration adapter with callback
        shiftAdapter = ShiftAdapter().apply {
            onShiftSelected = { shiftHealth ->
                selectedShift = shiftHealth
                updatePrice(shiftHealth.fee)
            }
        }
        binding.rcvDuration.adapter = shiftAdapter

        binding.cardViewJobDetail.setOnClickListener {
            // Show CleaningServiceDetailFragment
            val healthcareServiceDetailFragment = HealthcareServiceDetailFragment(healthcareService)
            Log.d(TAG, "Cleaning Service Data: $healthcareService")
            healthcareServiceDetailFragment.show(
                supportFragmentManager,
                "HealthcareServiceDetailFragment"
            )
        }

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

        // Handle info button click
        binding.ivInfo.addFadeClickEffect {
            val intent = Intent(this, CleaningIntroActivity::class.java)
            startActivity(intent)
        }

        // Handle location header click
        binding.llContentHeader.addFadeClickEffect {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("source", "healthcare_service")
            }
            Log.d("SelectServiceHealthCare", "Navigating to MapActivity with source: healthcare_service")
            startActivity(intent)
        }

        binding.cardViewButtonNext.setOnClickListener {
            val numberBaby = binding.edtNumberOfPeopleBaby.text.toString().toIntOrNull() ?: 0
            val numberAdult = binding.edtNumberOfPeopleDisable.text.toString().toIntOrNull() ?: 0
            val numberOld = binding.edtNumberOfPeopleOlder.text.toString().toIntOrNull() ?: 0
            val numberWorker = binding.edtNumberOfPeople.text.toString().toIntOrNull() ?: 1
            
            // Find service data from healthcareService list
            val babyService = healthcareService.find { it?.serviceName == "Trẻ em" }
            val adultService = healthcareService.find { it?.serviceName == "Người khuyết tật" }
            val elderlyService = healthcareService.find { it?.serviceName == "Người lớn tuổi" }
            
            // Navigate to SelectTimeFragment using fragment transaction
            val fragment = SelectTimeFragment().apply {
                arguments = Bundle().apply {
                    putInt("totalHours", totalHours)
                    putInt("totalFee", totalFee)
                    putInt("selectedWorkingHour", selectedShift?.workingHour ?: 0)
                    putInt("selectedFee", selectedShift?.fee ?: 0)
                    putString("selectedShiftId", selectedShift?.uid ?: "")
                    putInt("numberBaby", numberBaby)
                    putInt("numberAdult", numberAdult)
                    putInt("numberOld", numberOld)
                    putInt("numberWorker", numberWorker)
                    putString("serviceType", "healthcare")
                    // Pass service IDs and names
                    putString("babyServiceId", babyService?.uid ?: "")
                    putString("adultServiceId", adultService?.uid ?: "")
                    putString("elderlyServiceId", elderlyService?.uid ?: "")
                    putString("babyServiceName", babyService?.serviceName ?: "")
                    putString("adultServiceName", adultService?.serviceName ?: "")
                    putString("elderlyServiceName", elderlyService?.serviceName ?: "")
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Then observe ViewModel
        observeViewModel()
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
                        binding.lottieLoader.visibility = View.VISIBLE
                        binding.cardViewJobDetail.isEnabled = false
                        binding.cardViewButtonNext.visibility = View.GONE
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.ivBack.isEnabled = true
                        binding.ivInfo.isEnabled = true
                        binding.llContentHeader.isEnabled = true
                        binding.lottieLoader.visibility = View.GONE
                        binding.cardViewJobDetail.isEnabled = true
                        binding.cardViewButtonNext.visibility = View.VISIBLE
                    }
                }
            }
            launch {
                viewModel.healthcareService.collectLatest { healthcareData ->
                    healthcareService = healthcareData
                }
            }
            launch {
                viewModel.shift.collectLatest { shift ->
                    android.util.Log.d("SelectServiceHealthCareActivity", "Received shift data: ${shift?.size} items")
                    shift.forEach { shiftItem ->
                        android.util.Log.d("SelectServiceHealthCareActivity", "Shift: ${shiftItem?.workingHour}h - ${shiftItem?.fee} VND")
                    }
                    shiftAdapter.submitList(shift ?: emptyList())
                }
            }
        }
    }

    private fun updatePrice(price: Int) {
        currentBasePrice = price
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val totalPrice = currentBasePrice

        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(totalPrice)
        val baseHours = selectedShift?.workingHour ?: 1
        val totalHours = baseHours
        this.totalHours = totalHours
        this.totalFee = totalPrice
        binding.tvPrice.text = "$formattedPrice VND/${totalHours}h"
    }

}