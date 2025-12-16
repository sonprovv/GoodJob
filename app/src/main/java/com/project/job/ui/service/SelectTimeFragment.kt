package com.project.job.ui.service

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.project.job.R
import com.project.job.databinding.FragmentSelectTimeBinding
import com.project.job.ui.service.cleaningservice.adapter.DayAdapter
import com.project.job.data.model.DayItem
import com.project.job.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.*

class SelectTimeFragment : BaseFragment() {
    private var _binding: FragmentSelectTimeBinding? = null
    private val binding get() = _binding!!
    private var selectedHour = 0
    private var selectedMinute = 0
    private val days = mutableListOf<DayItem>()
    private val selectedDates = mutableListOf<Date>()
    private var dayAdapter: DayAdapter? = null
    private val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("'Tháng' M/yyyy", Locale.getDefault())
    private lateinit var tvSelectedDates: TextView
    private lateinit var tvMonthYear: TextView
    private var servicesText = ""
    private var durationDescription = ""
    private var serviceType = ""
    private var numberOfPeople = 1
    private var extraServices = arrayListOf<String>()
    private var durationWorkingHour = 0
    private var durationFee = 0
    private var durationId = ""

    private var shiftId =""
    private var shiftWorkingHour = 0
    private var shiftFee = 0
    private var numberOfWorker = 0
    private var numberOfBaby = 0
    private var numberOfAdult = 0
    private var numberOfElderly = 0
    
    // Service IDs and names
    private var babyServiceId = ""
    private var adultServiceId = ""
    private var elderlyServiceId = ""
    private var babyServiceName = ""
    private var adultServiceName = ""
    private var elderlyServiceName = ""
    // Dữ liệu maintenance services (lưu trữ để tránh mất dữ liệu)
    private var selectedServiceUids = arrayListOf<String>()
    private var selectedPowerUids = arrayListOf<String>()
    private var selectedQuantities = arrayListOf<Int>()
    private var selectedMaintenanceQuantities = arrayListOf<Int>()
    
    // Job location (lưu trữ để tránh mất dữ liệu)
    private var jobLocationAddress: String? = null
    private var jobLocationLatitude: Double = 0.0
    private var jobLocationLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thêm logging để debug việc nhận arguments
        Log.d("SelectTimeFragment", "Arguments received:")
        arguments?.keySet()?.forEach { key ->
            val value = arguments?.get(key)
            Log.d("SelectTimeFragment", "  $key: $value")
        }

        // Debug logging cụ thể cho maintenance data
        Log.d("SelectTimeFragment", "Service type: ${arguments?.getString("serviceType")}")
        Log.d("SelectTimeFragment", "Selected service UIDs: ${arguments?.getStringArrayList("selectedServiceUids")}")
        Log.d("SelectTimeFragment", "Selected power UIDs: ${arguments?.getStringArrayList("selectedPowerUids")}")
        Log.d("SelectTimeFragment", "Selected quantities: ${arguments?.getIntegerArrayList("selectedQuantities")}")
        Log.d("SelectTimeFragment", "Selected maintenance quantities: ${arguments?.getIntegerArrayList("selectedMaintenanceQuantities")}")

        // Thử đọc bằng cách khác để debug
        tvSelectedDates = binding.tvSelectedDates
        tvMonthYear = binding.tvMonthYear
        // Lưu trữ dữ liệu maintenance vào biến instance để tránh mất dữ liệu
        serviceType = arguments?.getString("serviceType") ?: ""
        selectedServiceUids = arguments?.getStringArrayList("selectedServiceUids") ?: arrayListOf()
        selectedPowerUids = arguments?.getStringArrayList("selectedPowerUids") ?: arrayListOf()
        selectedQuantities = arguments?.getIntegerArrayList("selectedQuantities") ?: arrayListOf()
        selectedMaintenanceQuantities = arguments?.getIntegerArrayList("selectedMaintenanceQuantities") ?: arrayListOf()

        val totalHours = arguments?.getInt("totalHours") ?: 0
        val totalFee = arguments?.getInt("totalFee") ?: 0
        durationDescription = arguments?.getString("durationDescription") ?: ""
        durationWorkingHour = arguments?.getInt("durationWorkingHour") ?: 0
        durationFee = arguments?.getInt("durationFee") ?: 0
        durationId = arguments?.getString("durationId") ?: ""

        shiftId = arguments?.getString("selectedShiftId") ?: ""
        shiftWorkingHour = arguments?.getInt("selectedShiftWorkingHour") ?: 0
        shiftFee = arguments?.getInt("selectedShiftFee") ?: 0
        numberOfBaby = arguments?.getInt("numberBaby", 0) ?: 0
        numberOfAdult = arguments?.getInt("numberAdult", 0) ?: 0
        numberOfElderly = arguments?.getInt("numberOld", 0) ?: 0
        numberOfWorker = arguments?.getInt("numberWorker", 1) ?: 1

        // Get service IDs and names from arguments
        babyServiceId = arguments?.getString("babyServiceId") ?: ""
        adultServiceId = arguments?.getString("adultServiceId") ?: ""
        elderlyServiceId = arguments?.getString("elderlyServiceId") ?: ""
        babyServiceName = arguments?.getString("babyServiceName") ?: ""
        adultServiceName = arguments?.getString("adultServiceName") ?: ""
        elderlyServiceName = arguments?.getString("elderlyServiceName") ?: ""
        
        // Lưu job location từ arguments
        jobLocationAddress = arguments?.getString("jobLocationAddress")
        jobLocationLatitude = arguments?.getDouble("jobLocationLatitude", 0.0) ?: 0.0
        jobLocationLongitude = arguments?.getDouble("jobLocationLongitude", 0.0) ?: 0.0
        
        Log.d("SelectTimeFragment", "📍 Job location loaded: $jobLocationAddress")
        Log.d("SelectTimeFragment", "📍 Coordinates: Lat=$jobLocationLatitude, Lng=$jobLocationLongitude")
        
        extraServices = arguments?.getStringArrayList("extraServices") ?: arrayListOf()

        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(totalFee)

        // Update price and duration description
        if(totalHours > 0) {
            binding.tvPrice.text = "$formattedPrice VND/${totalHours}h"
        } else {
            binding.tvPrice.text = "$formattedPrice VND"
        }



        // Update extra services display
        servicesText = "${extraServices.joinToString(", ")}"
        Log.d("SelectTimeFragment", "Extra Services: $servicesText")
        Log.d("SelectTimeFragment", "Duration Description: $durationDescription")

        // Set initial month/year display
        updateMonthYearDisplay()

        // Configure status bar (optional, depends on whether Fragment should handle this)
        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        // Set initial time to current time
        val calendar = Calendar.getInstance()
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)

        setupClickListeners()
        setupToolbar()
        setupDayRecyclerView()
        updateTimeDisplay()

    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            // Use activity's onBackPressed instead of Navigation Component
            navigateBackToActivity()
        }
    }

    private fun setupClickListeners() {
        binding.cardViewBtnSetupTime.setOnClickListener {
            showTimePickerDialog()
        }

        binding.cardViewButtonNext.setOnClickListener {
            if (selectedHour != 0 || selectedMinute != 0) {
                if (selectedDates.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Vui lòng chọn ít nhất một ngày",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Convert selected dates to string array
                val dateStrings = selectedDates.map { date ->
                    dateFormatDisplay.format(date)
                }

                // Create bundle with all data
                val timeString = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                val totalHours = arguments?.getInt("totalHours") ?: 0
                val totalFee = arguments?.getInt("totalFee") ?: 0
                
                Log.d("SelectTimeFragment", "🔥🔥🔥 Creating ConfirmAndCheckoutFragment 🔥🔥🔥")
                
                val fragment = ConfirmAndCheckoutFragment().apply {
                    arguments = Bundle().apply {
                        Log.d("SelectTimeFragment", "🔥 Preparing arguments bundle...")
                        putString("serviceType", serviceType)
                        putStringArray("selectedDates", dateStrings.toTypedArray())
                        putString("selectedTime", timeString)
                        putInt("totalHours", totalHours)
                        putInt("totalFee", totalFee)
                        putInt("numberOfPeople", numberOfPeople)
                        putString("durationDescription", durationDescription)
                        putInt("durationWorkingHour", durationWorkingHour)
                        putInt("durationFee", durationFee)
                        putString("durationId", durationId)
                        putString("serviceExtras", servicesText)

                        putString("selectedShiftId", shiftId)
                        putInt("selectedShiftWorkingHour", shiftWorkingHour)
                        putInt("selectedShiftFee", shiftFee)
                        putInt("numberBaby", numberOfBaby)
                        putInt("numberAdult", numberOfAdult)
                        putInt("numberOld", numberOfElderly)
                        putInt("numberWorker", numberOfWorker)
                        
                        // Pass service IDs and names to ConfirmAndCheckoutFragment
                        putString("babyServiceId", babyServiceId)
                        putString("adultServiceId", adultServiceId)
                        putString("elderlyServiceId", elderlyServiceId)
                        putString("babyServiceName", babyServiceName)
                        putString("adultServiceName", adultServiceName)
                        putString("elderlyServiceName", elderlyServiceName)

                        // Truyền location đã chọn cho job này (SỬ DỤNG BIẾN INSTANCE, KHÔNG ĐỌC LẠI TỪ ARGUMENTS!)
                        // Debug logging
                        Log.d("SelectTimeFragment", "==================== LOCATION DEBUG ====================")
                        Log.d("SelectTimeFragment", "Using job location from instance variable: $jobLocationAddress")
                        Log.d("SelectTimeFragment", "Using coordinates from instance: Lat=$jobLocationLatitude, Lng=$jobLocationLongitude")
                        Log.d("SelectTimeFragment", "Passing to ConfirmAndCheckoutFragment...")
                        Log.d("SelectTimeFragment", "=======================================================")
                        
                        putString("jobLocationAddress", jobLocationAddress)
                        putDouble("jobLocationLatitude", jobLocationLatitude)
                        putDouble("jobLocationLongitude", jobLocationLongitude)
                        
                        Log.d("SelectTimeFragment", "✅ Location data ADDED to bundle successfully!")
                        Log.d("SelectTimeFragment", "✅ jobLocationAddress in bundle: $jobLocationAddress")

                        // Truyền dữ liệu maintenance services nếu có
                        if (serviceType == "maintenance") {
                            Log.d("SelectTimeFragment", "Reading maintenance data for ConfirmAndCheckoutFragment:")
                            Log.d("SelectTimeFragment", "  Selected service UIDs: ${selectedServiceUids.joinToString(", ")}")
                            Log.d("SelectTimeFragment", "  Selected power UIDs: ${selectedPowerUids.joinToString(", ")}")
                            Log.d("SelectTimeFragment", "  Selected quantities: ${selectedQuantities.joinToString(", ")}")
                            Log.d("SelectTimeFragment", "  Selected maintenance quantities: ${selectedMaintenanceQuantities.joinToString(", ")}")

                            // Kiểm tra xem có dữ liệu không trước khi truyền
                            if (selectedServiceUids.isNotEmpty()) {
                                putStringArrayList("selectedServiceUids", selectedServiceUids)
                                putStringArrayList("selectedPowerUids", selectedPowerUids)
                                putIntegerArrayList("selectedQuantities", selectedQuantities)
                                putIntegerArrayList("selectedMaintenanceQuantities", selectedMaintenanceQuantities)
                                Log.d("SelectTimeFragment", "Maintenance data transmitted to ConfirmAndCheckoutFragment")
                            } else {
                                Log.d("SelectTimeFragment", "No maintenance data to transmit - selectedServiceUids is empty")
                            }
                        } else {
                            Log.d("SelectTimeFragment", "Service type is not maintenance: $serviceType")
                        }
                    }
                }

                // Post để đảm bảo animation hoạt động mượt khi back
                view?.post {
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.fragment_slide_in_right,
                            R.anim.fragment_slide_out_left,
                            R.anim.fragment_slide_in_left,
                            R.anim.fragment_slide_out_right
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn thời gian", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = CustomTimePickerDialog.newInstance()

        // Set initial time to current selected time
        timePickerDialog.setInitialTime(selectedHour, selectedMinute)

        timePickerDialog.setOnTimeSelectedListener { hour, minute ->
            selectedHour = hour
            selectedMinute = minute
            updateTimeDisplay()
        }
        timePickerDialog.show(childFragmentManager, CustomTimePickerDialog.TAG)
    }

    private fun setupDayRecyclerView() {
        // Only initialize the days list if it's empty
        if (days.isEmpty()) {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

            // Clear any existing items before adding new ones
            days.clear()

            // Add 30 days (5 rows x 6 columns)
            for (i in 0..29) {
                val currentDate = calendar.time
                val dayOfWeek = dayFormat.format(currentDate)
                val date = dateFormat.format(currentDate)

                days.add(
                    DayItem(
                        dayOfWeek = dayOfWeek,
                        date = date,
                        dateValue = currentDate,
                        isSelected = false // No default selection
                    )
                )

                calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to next day
            }
        }

        // Initialize adapter only once
        if (dayAdapter == null) {
            dayAdapter = DayAdapter { selectedDays ->
                selectedDates.clear()
                selectedDates.addAll(selectedDays.map { it.dateValue })
                updateTimeDisplay()
            }
        }

        // Set up the RecyclerView
        binding.rcvListDay.apply {
            if (adapter == null) {
                layoutManager = GridLayoutManager(requireContext(), 6)
                adapter = dayAdapter
            }
            dayAdapter?.setDays(days)
        }
    }
    private fun navigateBackToActivity() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        } else {
            requireActivity().onBackPressed()
        }
    }

    private fun updateMonthYearDisplay() {
        val calendar = Calendar.getInstance()
        // If we have selected dates, use the first one to determine month/year
        if (selectedDates.isNotEmpty()) {
            calendar.time = selectedDates.first()
        }
        tvMonthYear.text = monthYearFormat.format(calendar.time)
    }

    private fun updateTimeDisplay() {
        // Update the time display in the card_view_btn_setup_time
        val hourText = String.format(Locale.getDefault(), "%02d", selectedHour)
        val minuteText = String.format(Locale.getDefault(), "%02d", selectedMinute)

        // Update the TextViews in the card_view_btn_setup_time
        binding.cardViewBtnSetupTime.findViewById<TextView>(R.id.hourText)?.text = hourText
        binding.cardViewBtnSetupTime.findViewById<TextView>(R.id.minuteText)?.text = minuteText

        // Update selected dates display
        if (selectedDates.isEmpty()) {
            tvSelectedDates.text = "Chưa chọn ngày"
        } else {
            val sortedDates = selectedDates.sorted()
            val dateStrings = sortedDates.map { dateFormatDisplay.format(it) }
            tvSelectedDates.text = dateStrings.joinToString(", ")

            // Update month/year display based on first selected date
            updateMonthYearDisplay()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}