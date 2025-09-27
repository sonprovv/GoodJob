package com.project.job.ui.service.cleaningservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.databinding.ActivitySelectServiceBinding
import com.project.job.ui.intro.CleaningIntroActivity
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.map.MapActivity
import com.project.job.ui.service.cleaningservice.adapter.DurationAdapter
import com.project.job.ui.service.cleaningservice.viewmodel.CleaningServiceViewModel
import com.project.job.utils.addFadeClickEffect
import com.project.job.utils.SelectedRoomManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectServiceActivity : AppCompatActivity() {
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
                }
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
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
    
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}