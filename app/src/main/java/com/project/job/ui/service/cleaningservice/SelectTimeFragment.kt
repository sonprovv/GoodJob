package com.project.job.ui.service.cleaningservice

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.project.job.R
import com.project.job.databinding.FragmentSelectTimeBinding
import com.project.job.ui.service.cleaningservice.adapter.DayAdapter
import com.project.job.data.model.DayItem
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout
import com.project.job.utils.SelectedRoomManager
import java.text.SimpleDateFormat
import java.util.*

class SelectTimeFragment : Fragment() {
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
    private var numberOfPeople = 1
    private var selectedRoomNames = arrayListOf<String>()
    private var selectedRoomCount = 0
    private var durationWorkingHour = 0
    private var durationFee = 0
    private var durationId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tvSelectedDates = binding.tvSelectedDates
        tvMonthYear = binding.tvMonthYear
        val totalHours = arguments?.getInt("totalHours") ?: 0
        val totalFee = arguments?.getInt("totalFee") ?: 0
        durationDescription = arguments?.getString("durationDescription") ?: ""
        durationWorkingHour = arguments?.getInt("durationWorkingHour") ?: 0
        durationFee = arguments?.getInt("durationFee") ?: 0
        durationId = arguments?.getString("durationId") ?: ""
        val extraServices = arguments?.getStringArrayList("extraServices") ?: arrayListOf()
        selectedRoomNames = arguments?.getStringArrayList("selectedRoomNames") ?: arrayListOf()
        selectedRoomCount = arguments?.getInt("selectedRoomCount") ?: 0
        val selectedRooms = SelectedRoomManager.getSelectedRooms()
        for (room in selectedRooms) {
            Log.d("SelectTimeFragment", "Received room: $room")
        }

        // Debug logging
        Log.d("SelectTimeFragment", "Received room count: $selectedRoomCount")
        Log.d("SelectTimeFragment", "Received room names: ${selectedRoomNames.joinToString(", ")}")

        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(totalFee)

        // Update price and duration description
        binding.tvPrice.text = "$formattedPrice VND/${totalHours}h"


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

        setupNumberOfPeopleInput()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            // Use activity's onBackPressed instead of Navigation Component
            activity?.onBackPressed()
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
                
                val fragment = ConfirmAndCheckoutFragment().apply {
                    arguments = Bundle().apply {
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
                        putStringArrayList("selectedRoomNames", selectedRoomNames)
                        putInt("selectedRoomCount", selectedRoomCount)
                        
                        // Debug logging before passing to ConfirmAndCheckoutFragment
                        Log.d("SelectTimeFragment", "Passing room count to Confirm: $selectedRoomCount")
                        Log.d("SelectTimeFragment", "Passing room names to Confirm: ${selectedRoomNames.joinToString(", ")}")
                        Log.d("SelectTimeFragment", "Passing duration info: id=$durationId, workingHour=$durationWorkingHour, fee=$durationFee")
                    }
                }
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit()

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

    private fun setupNumberOfPeopleInput() {
        binding.edtNumberOfPeople.apply {
            // Set default value
            setText(numberOfPeople.toString())
            
            // Handle focus change
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateAndSetPeopleCount()
                } else {
                    // Clear error when user starts editing
                    (parent.parent as? TextInputLayout)?.error = null
                }
            }
            
            // Handle text changes
            addTextChangedListener(object : TextWatcher {
                private var lastValid = "1"
                
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Keep track of last valid input
                    if (s?.toString()?.toIntOrNull() in 1..10) {
                        lastValid = s.toString()
                    }
                }
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.toString()?.let { input ->
                        if (input.isNotEmpty()) {
                            try {
                                val number = input.toInt()
                                if (number > 10) {
                                    (parent.parent as? TextInputLayout)?.error = "Tối đa 10 người"
                                    numberOfPeople = 10
                                } else if (number < 1) {
                                    (parent.parent as? TextInputLayout)?.error = "Tối thiểu 1 người"
                                    numberOfPeople = 1
                                } else {
                                    (parent.parent as? TextInputLayout)?.error = null
                                    numberOfPeople = number
                                }
                            } catch (e: NumberFormatException) {
                                (parent.parent as? TextInputLayout)?.error = "Vui lòng nhập số"
                                numberOfPeople = 1
                            }
                        } else {
                            (parent.parent as? TextInputLayout)?.error = null
                            numberOfPeople = 1
                        }
                    }
                }
                
                override fun afterTextChanged(s: Editable?) {
                    // This will be handled by the focus change
                }
            })
        }
    }
    
    private fun validateAndSetPeopleCount() {
        val input = binding.edtNumberOfPeople.text.toString().trim()
        val count = try {
            when {
                input.isEmpty() -> 1
                input.toInt() < 1 -> 1
                input.toInt() > 10 -> {
                    (binding.edtNumberOfPeople.parent.parent as? TextInputLayout)?.error = "Tối đa 10 người"
                    10
                }
                else -> input.toInt()
            }
        } catch (e: NumberFormatException) {
            1
        }
        
        // Update the numberOfPeople variable regardless of whether it changed
        numberOfPeople = count
        
        // Only update the UI if the displayed value is different
        if (input != count.toString()) {
            binding.edtNumberOfPeople.apply {
                removeTextChangedListener(null) // Remove all text watchers
                setText(count.toString())
                setSelection(text.length) // Move cursor to end
            }
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