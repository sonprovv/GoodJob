package com.project.job.ui.service.cleaningservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.databinding.FragmentConfirmAndCheckoutBinding
import com.project.job.ui.service.cleaningservice.viewmodel.CleaningServiceViewModel
import com.project.job.utils.SelectedRoomManager
import com.project.job.utils.UserDataBroadcastManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ConfirmAndCheckoutFragment : Fragment() {
    private var _binding: FragmentConfirmAndCheckoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CleaningServiceViewModel
    private lateinit var preferencesManager: PreferencesManager
    private var selectedRoomNames: List<String> = emptyList()
    private var selectedServices: List<CleaningService> = emptyList()
    private var selectedRoomCount: Int = 0

    private val userDataUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == UserDataBroadcastManager.ACTION_USER_DATA_UPDATED) {
                val name = intent.getStringExtra(UserDataBroadcastManager.EXTRA_USER_NAME) ?: ""
                val phone = intent.getStringExtra(UserDataBroadcastManager.EXTRA_USER_PHONE) ?: ""
                updateUserDataInUI(name, phone)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmAndCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CleaningServiceViewModel()

        preferencesManager = PreferencesManager(requireContext())

        // Register broadcast receiver
        registerUserDataReceiver()

        // Load and display user data
        loadUserData()

        // Set user info
        updateUserInfoDisplay()


        // Get data from arguments
        val selectedDates = arguments?.getStringArray("selectedDates")?.toList() ?: emptyList()
        val selectedTime = arguments?.getString("selectedTime") ?: ""
        val totalHours = arguments?.getInt("totalHours") ?: 0
        val totalFee = arguments?.getInt("totalFee") ?: 0
        val durationDescription = arguments?.getString("durationDescription") ?: ""
        val serviceExtras = arguments?.getString("serviceExtras") ?: "Không"
        val numberOfPeople = arguments?.getInt("numberOfPeople", 1) ?: 1
        selectedRoomNames = arguments?.getStringArrayList("selectedRoomNames") ?: arrayListOf()
        selectedRoomCount = arguments?.getInt("selectedRoomCount") ?: 0
        val durationWorkingHour = arguments?.getInt("durationWorkingHour") ?: 0
        val durationFee = arguments?.getInt("durationFee") ?: 0
        val durationId = arguments?.getString("durationId") ?: ""

        // Get selected services from SelectedServiceManager
        selectedServices = SelectedRoomManager.getSelectedRooms()

        // Format the selected dates
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Format the total fee with VND
        val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(totalFee)

        // Update UI with the received data
        if (selectedDates.isNotEmpty()) {
            // Parse string dates to Date objects for sorting
            val parsedDates = selectedDates.mapNotNull { dateStr ->
                try {
                    inputFormat.parse(dateStr)
                } catch (e: Exception) {
                    null
                }
            }

            if (parsedDates.isNotEmpty()) {
                // Get the number of selected days
                val totalDays = parsedDates.size

                // Sort dates to get the first and last date
                val sortedDates = parsedDates.sorted()
                val startDate = displayFormat.format(sortedDates.first())
                val endDate = displayFormat.format(sortedDates.last())

                binding.tvTotalDay.text = "$totalDays ngày"
                binding.tvStartDate.text = startDate
                binding.tvEndDate.text = endDate
            } else {
                binding.tvTotalDay.text = "0 ngày"
                binding.tvStartDate.text = "--/--/----"
                binding.tvEndDate.text = "--/--/----"
            }
        } else {
            binding.tvTotalDay.text = "0 ngày"
            binding.tvStartDate.text = "--/--/----"
            binding.tvEndDate.text = "--/--/----"
        }

        // Update UI with the received data
        binding.tvTotalTime.text = "$selectedTime (${totalHours}h)"
        binding.tvTotalPrice.text = "$formattedPrice VND"
        binding.tvTotalJobArea.text = durationDescription
        binding.tvServiceExtras.text = serviceExtras
        binding.tvTotalNumber.text = "$numberOfPeople"

        // Update room count from Bundle data instead of SelectedServiceManager
        binding.tvTotalRooms.text = "$selectedRoomCount phòng"

        // Debug logging for duration data
        Log.d("ConfirmCheckout", "Duration ID: $durationId")
        Log.d("ConfirmCheckout", "Duration Working Hour: $durationWorkingHour")
        Log.d("ConfirmCheckout", "Duration Fee: $durationFee")
        Log.d("ConfirmCheckout", "Duration Description: $durationDescription")

        displaySelectedServices()


        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.cardViewChangrInfo.setOnClickListener {
            // Xử lý khi người dùng nhấn vào CardView "Thay đổi"
            val updateNameAndPhoneFragment = UpdateNameAndPhoneFragment()
            updateNameAndPhoneFragment.show(parentFragmentManager, "updateNameAndPhoneFragment")
        }
        binding.cardViewButtonPostJob.setOnClickListener {
            val token = preferencesManager.getAuthToken() ?: ""
            val uid = preferencesManager.getUserData()["user_id"] ?: ""
            val isCooking = serviceExtras.contains("Nấu ăn")
            val isIroning = serviceExtras.contains("Ủi đồ")
            val duration = CleaningDuration(
                uid = durationId,
                workingHour = durationWorkingHour,
                fee = durationFee,
                description = durationDescription
            )

            val serviceSelect = selectedServices.map { service ->
                service.copy(uid = service.uid.split("_").first())
            }

            viewModel.postServiceCleaning(
                token = token,
                userID = uid,
                startTime = selectedTime,
                workerQuantity = numberOfPeople,
                price = totalFee,
                listDays = selectedDates,
                duration = duration,
                isCooking = isCooking,
                isIroning = isIroning,
                location = preferencesManager.getUserData()["user_location"] ?: "",
                services = serviceSelect
            )
        }

        observeViewModel()

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        binding.flLoading.visibility = View.VISIBLE
                        binding.cardViewButtonPostJob.isEnabled =
                            false // Vô hiệu hóa nút đăng nhập
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.flLoading.visibility = View.GONE
                        binding.cardViewButtonPostJob.isEnabled =
                            true // Kích hoạt lại nút đăng nhập
                    }
                }
            }
            launch {
                viewModel.success_post.collectLatest { isSuccess ->
                    if (isSuccess) {
                        // Clear selected rooms data
                        SelectedRoomManager.clearAllRooms()

                        // Show success message
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Đăng công việc thành công!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Navigate back to SelectServiceActivity and finish all fragments
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun displaySelectedServices() {
        if (selectedServices.isNotEmpty()) {
            val serviceNames = selectedServices.joinToString(", ") { it.serviceName }
            val totalRooms = selectedServices.size
            binding.tvTotalRooms.text = "$totalRooms phòng"
            // You can display this information in your UI
            // For example, if you have TextViews to show selected services:
            // binding.tvSelectedRooms.text = "Phòng đã chọn: $serviceNames"
            // binding.tvTotalRooms.text = "Tổng số phòng: $totalRooms"

            // Log for debugging
            Log.d("ConfirmCheckout", "Selected services: $serviceNames")
            Log.d("ConfirmCheckout", "Total rooms from SelectedServiceManager: $totalRooms")
            Log.d("ConfirmCheckout", "Total rooms from Bundle: $selectedRoomCount")
            Log.d(
                "ConfirmCheckout",
                "Selected room names from Bundle: ${selectedRoomNames.joinToString(", ")}"
            )

            // Display detailed information about each selected service
            selectedServices.forEach { service ->
                Log.d("ConfirmCheckout", "Service: ${service.serviceName}")
                Log.d("ConfirmCheckout", "Tasks: ${service.tasks.joinToString(", ")}")
            }
        } else {
            Log.d("ConfirmCheckout", "No services selected")
        }
    }

    private fun registerUserDataReceiver() {
        val filter = IntentFilter(UserDataBroadcastManager.ACTION_USER_DATA_UPDATED)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(userDataUpdateReceiver, filter)
    }

    private fun loadUserData() {
        val userData = preferencesManager.getUserData()
        val location = userData["user_location"] ?: ""
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
        val fullName = userData["user_name"]
        val phone = userData["user_phone"]

        binding.tvLocation.text = displayLocation
        binding.tvFullName.text = fullName
        binding.tvPhone.text = phone
    }

    private fun updateUserInfoDisplay() {
        loadUserData()
    }

    private fun updateUserDataInUI(name: String, phone: String) {
        // Update UI with new user data
        binding.tvFullName.text = name
        binding.tvPhone.text = phone

        // Optionally show a toast or log
        Log.d("ConfirmCheckout", "User data updated: Name=$name, Phone=$phone")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister broadcast receiver
        try {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(userDataUpdateReceiver)
        } catch (e: Exception) {
            Log.e("ConfirmCheckout", "Error unregistering receiver", e)
        }
        _binding = null
    }
}