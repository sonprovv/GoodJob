package com.project.job.ui.service.healthcareservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.HealthcareShift
import com.project.job.databinding.ActivitySelectServiceHealthCareBinding
import com.project.job.ui.intro.CleaningIntroActivity
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.map.MapActivity
import com.project.job.ui.service.cleaningservice.SelectTimeFragment
import com.project.job.ui.service.healthcareservice.adapter.ShiftAdapter
import com.project.job.ui.service.healthcareservice.viewmodel.HealthCareViewModel
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

@Suppress("DEPRECATION")
class SelectServiceHealthCareActivity : AppCompatActivity() {
    private val TAG = "SelectServiceHealthCare"
    private lateinit var binding: ActivitySelectServiceHealthCareBinding
    private lateinit var viewModel: HealthCareViewModel
    private lateinit var loadingDialog: LoadingDialog
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

        loadingDialog = LoadingDialog(this)

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
            Log.d(
                "SelectServiceHealthCare",
                "Navigating to MapActivity with source: healthcare_service"
            )
            startActivity(intent)
        }

        // Add text change listeners for number inputs
        setupNumberInputListeners()

        binding.cardViewButtonNext.setOnClickListener {
            // Validate input before proceeding
            if (!validateServicesInput()) {
                Log.d(TAG, "Validation failed - some services are not filled")
                updateNextButtonState() // This will show error message
                return@setOnClickListener
            }

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

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view ->
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setupNumberInputListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateWorkerCount()
            }
        }

        binding.edtNumberOfPeopleBaby.addTextChangedListener(textWatcher)
        binding.edtNumberOfPeopleDisable.addTextChangedListener(textWatcher)
        binding.edtNumberOfPeopleOlder.addTextChangedListener(textWatcher)

        // Set up imeOptions listeners to hide keyboard when Done is pressed
        binding.edtNumberOfPeopleBaby.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                updateWorkerCount()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.edtNumberOfPeopleDisable.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                updateWorkerCount()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.edtNumberOfPeopleOlder.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                updateWorkerCount()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalPrice() {
        val workerCount = getCurrentWorkerCount()
        // Always update totalHours from the currently selected shift BEFORE calculating totalFee
        val baseHours = selectedShift?.workingHour ?: 1
        this.totalHours = baseHours
        totalFee = currentBasePrice * workerCount
        val formattedPrice =
            java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(totalFee)
        binding.tvPrice.text = "$formattedPrice VND/$totalHours giờ"

        // Update button state after price calculation
        updateNextButtonState()
    }

    private fun updateWorkerCount() {
        try {
            val babies = binding.edtNumberOfPeopleBaby.text.toString().toIntOrNull() ?: 0
            val disabled = binding.edtNumberOfPeopleDisable.text.toString().toIntOrNull() ?: 0
            val elderly = binding.edtNumberOfPeopleOlder.text.toString().toIntOrNull() ?: 0

            // Calculate total people
            val totalPeople = babies + disabled + elderly

            // Calculate required workers (total people / 3, rounded up)
            val requiredWorkers = if (totalPeople > 0) {
                (totalPeople + 2) / 3  // This is equivalent to Math.ceil(totalPeople / 3.0)
            } else {
                0  // No workers needed when no people are selected
            }

            // Get the current worker count before updating
            val currentWorkerCount = getCurrentWorkerCount()

            // Update the worker count field
            binding.edtNumberOfPeople.setText(requiredWorkers.toString())

            // If worker count changed, update the total price
            if (requiredWorkers != currentWorkerCount) {
                updateTotalPrice()
            } else {
                // Still update button state even if worker count didn't change
                updateNextButtonState()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating worker count: ${e.message}")
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
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.ivBack.isEnabled = true
                        binding.ivInfo.isEnabled = true
                        binding.llContentHeader.isEnabled = true
                        loadingDialog.hide()
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
                    Log.d(
                        "SelectServiceHealthCareActivity",
                        "Received shift data: ${shift.size} items"
                    )
                    shift.forEach { shiftItem ->
                        Log.d(
                            "SelectServiceHealthCareActivity",
                            "Shift: ${shiftItem?.workingHour}h - ${shiftItem?.fee} VND"
                        )
                    }
                    shiftAdapter.submitList(shift)
                }
            }
        }
    }

    private fun updatePrice(price: Int) {
        currentBasePrice = price
        updateTotalPrice()
    }

    private fun getCurrentWorkerCount(): Int {
        return binding.edtNumberOfPeople.text.toString().toIntOrNull() ?: 0
    }

    @SuppressLint("SetTextI18n")
    private fun validateServicesInput(): Boolean {
        val babyCount = binding.edtNumberOfPeopleBaby.text.toString().toIntOrNull() ?: 0
        val adultCount = binding.edtNumberOfPeopleDisable.text.toString().toIntOrNull() ?: 0
        val elderlyCount = binding.edtNumberOfPeopleOlder.text.toString().toIntOrNull() ?: 0

        // Check if at least one service has count >= 1
        return (babyCount >= 1) || (adultCount >= 1) || (elderlyCount >= 1)
    }

    private fun updateNextButtonState() {
        val isValid = validateServicesInput()

        if (isValid) {
            // Enable button and hide error message
            binding.cardViewButtonNext.isEnabled = true
            binding.cardViewButtonNext.alpha = 1.0f
            binding.tvErrorMessage.visibility = View.GONE
        } else {
            // Disable button and show error message
            binding.cardViewButtonNext.isEnabled = false
            binding.cardViewButtonNext.alpha = 0.5f
            binding.tvErrorMessage.visibility = View.VISIBLE
        }
    }
}