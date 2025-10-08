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
import kotlinx.coroutines.flow.collectLatest
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.request.ServicePowerInfo
import com.project.job.data.source.remote.api.request.PowersInfoQuantity
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.databinding.FragmentConfirmAndCheckoutBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.login.LoginFragment
import com.project.job.ui.login.LoginResultListener
import com.project.job.ui.payment.PaymentQrFragment
import com.project.job.ui.service.cleaningservice.viewmodel.CleaningServiceViewModel
import com.project.job.ui.service.healthcareservice.viewmodel.HealthCareViewModel
import com.project.job.ui.service.maintenanceservice.SelectServiceMaintenanceActivity
import com.project.job.ui.service.maintenanceservice.viewmodel.MaintenanceViewModel
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
    private lateinit var viewModelCleaning: CleaningServiceViewModel
    private lateinit var viewModelHealthCare: HealthCareViewModel
    private lateinit var viewModelMaintenance: MaintenanceViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var loadingDialog: LoadingDialog

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
        loadingDialog = LoadingDialog(requireActivity())
        viewModelCleaning = CleaningServiceViewModel()
        viewModelHealthCare = HealthCareViewModel()
        viewModelMaintenance = MaintenanceViewModel()

        preferencesManager = PreferencesManager(requireContext())

        // Register broadcast receiver
        registerUserDataReceiver()

        // Load and display user data
        loadUserData()

        // Set user info
        updateUserInfoDisplay()


        // Get data from arguments
        val serviceType = arguments?.getString("serviceType") ?: ""
        val selectedDates = arguments?.getStringArray("selectedDates")?.toList() ?: emptyList()
        val selectedTime = arguments?.getString("selectedTime") ?: ""
        val totalHours = arguments?.getInt("totalHours") ?: 0
        val totalFee = arguments?.getInt("totalFee") ?: 0
        val durationDescription = arguments?.getString("durationDescription") ?: ""
        val serviceExtras = arguments?.getString("serviceExtras") ?: "Không"
        val durationWorkingHour = arguments?.getInt("durationWorkingHour") ?: 0
        val durationFee = arguments?.getInt("durationFee") ?: 0
        val durationId = arguments?.getString("durationId") ?: ""

        val shiftId = arguments?.getString("selectedShiftId") ?: ""
        val shiftWorkingHour = arguments?.getInt("selectedShiftWorkingHour") ?: 0
        val shiftFee = arguments?.getInt("selectedShiftFee") ?: 0
        val numberOfBaby = arguments?.getInt("numberBaby", 0) ?: 0
        val numberOfAdult = arguments?.getInt("numberAdult", 0) ?: 0
        val numberOfElderly = arguments?.getInt("numberOld", 0) ?: 0
        val numberOfWorker = arguments?.getInt("numberWorker", 1) ?: 1

        // Get service IDs and names from arguments
        val babyServiceId = arguments?.getString("babyServiceId") ?: ""
        val adultServiceId = arguments?.getString("adultServiceId") ?: ""
        val elderlyServiceId = arguments?.getString("elderlyServiceId") ?: ""
        val babyServiceName = arguments?.getString("babyServiceName") ?: ""
        val adultServiceName = arguments?.getString("adultServiceName") ?: ""
        val elderlyServiceName = arguments?.getString("elderlyServiceName") ?: ""

        // Get selected services from SelectedServiceManager
//        selectedServices = SelectedRoomManager.getSelectedRooms()

        // Format the selected dates
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Format the total fee with VND
        val formattedPrice =
            NumberFormat.getNumberInstance(Locale("vi", "VN")).format(totalFee * selectedDates.size)

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
        binding.tvServiceExtras.text = serviceExtras

        // Update room count from Bundle data instead of SelectedServiceManager
//        binding.tvTotalRooms.text = "$selectedRoomCount phòng"

        // Debug logging for duration data
        Log.d("ConfirmCheckout", "Duration ID: $durationId")
        Log.d("ConfirmCheckout", "Duration Working Hour: $durationWorkingHour")
        Log.d("ConfirmCheckout", "Duration Fee: $durationFee")
        Log.d("ConfirmCheckout", "Duration Description: $durationDescription")

        Log.d("ConfirmCheckout", "Shift ID: $shiftId")
        Log.d("ConfirmCheckout", "Shift Working Hour: $shiftWorkingHour")
        Log.d("ConfirmCheckout", "Shift Fee: $shiftFee")
        Log.d("ConfirmCheckout", "Number of Baby: $numberOfBaby")
        Log.d("ConfirmCheckout", "Number of Adult: $numberOfAdult")
        Log.d("ConfirmCheckout", "Number of Elderly: $numberOfElderly")
        Log.d("ConfirmCheckout", "Number of Worker: $numberOfWorker")
        Log.d("ConfirmCheckout", "Baby Service Name: '$babyServiceName'")
        Log.d("ConfirmCheckout", "Adult Service Name: '$adultServiceName'")
        Log.d("ConfirmCheckout", "Elderly Service Name: '$elderlyServiceName'")
        Log.d("ConfirmCheckout", "Service Type: '$serviceType'")

        if (serviceType == "healthcare") {
            binding.llServiceExtras.visibility = View.GONE
            binding.tvTotalNumber.text = "$numberOfWorker"
            binding.llTotalNumber.visibility = View.VISIBLE

            // Create list of selected service names using actual service names
            val selectedServiceNames = mutableListOf<String>()
            Log.d(
                "ConfirmCheckout",
                "Checking baby: numberOfBaby=$numberOfBaby, babyServiceName='$babyServiceName'"
            )
            if (numberOfBaby > 0 && babyServiceName.isNotEmpty()) {
                selectedServiceNames.add(babyServiceName + " ($numberOfBaby)")
                Log.d("ConfirmCheckout", "Added baby service: $babyServiceName")
            }
            Log.d(
                "ConfirmCheckout",
                "Checking adult: numberOfAdult=$numberOfAdult, adultServiceName='$adultServiceName'"
            )
            if (numberOfAdult > 0 && adultServiceName.isNotEmpty()) {
                selectedServiceNames.add(adultServiceName + " ($numberOfAdult)")
                Log.d("ConfirmCheckout", "Added adult service: $adultServiceName")
            }
            Log.d(
                "ConfirmCheckout",
                "Checking elderly: numberOfElderly=$numberOfElderly, elderlyServiceName='$elderlyServiceName'"
            )
            if (numberOfElderly > 0 && elderlyServiceName.isNotEmpty()) {
                selectedServiceNames.add(elderlyServiceName + " ($numberOfElderly)")
                Log.d("ConfirmCheckout", "Added elderly service: $elderlyServiceName")
            }

            // If service names are empty, use fallback names
            if (selectedServiceNames.isEmpty()) {
                if (numberOfBaby > 0) selectedServiceNames.add("Trẻ em")
                if (numberOfAdult > 0) selectedServiceNames.add("Người khuyết tật")
                if (numberOfElderly > 0) selectedServiceNames.add("Người lớn tuổi")
            }

            // Format service area text
            val serviceAreaText = if (selectedServiceNames.isNotEmpty()) {
                "Chăm sóc " + selectedServiceNames.joinToString(", ")
            } else {
                "Chăm sóc"
            }
            binding.tvTotalJobArea.text = serviceAreaText

            Log.d("ConfirmCheckout", "Selected Service Names: $selectedServiceNames")
            Log.d("ConfirmCheckout", "Service Area Text: $serviceAreaText")
        } else if (serviceType == "cleaning") {
            binding.llServiceExtras.visibility = View.VISIBLE
            binding.tvTotalJobArea.text = durationDescription
            binding.llTotalNumber.visibility = View.GONE
        } else if (serviceType == "maintenance") {
            binding.llServiceExtras.visibility = View.GONE
            binding.llTotalNumber.visibility = View.GONE
            binding.tvTotalJobArea.text = durationDescription
            // Hiển thị thông tin maintenance với format phù hợp
            binding.tvTotalTime.text = "$selectedTime (${totalHours}h)"
        }


//        displaySelectedServices()


        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.cardViewChangrInfo.setOnClickListener {
            // Xử lý khi người dùng nhấn vào CardView "Thay đổi"
            val updateNameAndPhoneFragment = UpdateNameAndPhoneFragment()
            updateNameAndPhoneFragment.show(parentFragmentManager, "updateNameAndPhoneFragment")
        }
        binding.cardViewButtonPostJob.setOnClickListener {
            val uid = preferencesManager.getUserData()["user_id"] ?: ""

            if (uid == "") {
                // Hiển thị thông báo yêu cầu đăng nhập trước khi hiển thị LoginFragment
                android.widget.Toast.makeText(
                    requireContext(),
                    "Vui lòng đăng nhập để tiếp tục đăng công việc",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                // Hiển thị fragment đăng nhập (LoginFragment) với callback
                val loginFragment = LoginFragment()
                loginFragment.setLoginResultListener(object : LoginResultListener {
                    override fun onLoginSuccess() {
                        // Cập nhật lại thông tin người dùng sau khi đăng nhập thành công
                        loadUserData()

                        // Gửi broadcast thông báo cập nhật dữ liệu người dùng
                        val userData = preferencesManager.getUserData()
                        val userName = userData["user_name"] ?: "Người dùng"
                        val userPhone = userData["user_phone"] ?: ""
                        UserDataBroadcastManager.sendUserDataUpdatedBroadcast(
                            requireContext(),
                            userName,
                            userPhone
                        )
                    }
                })
                loginFragment.show(parentFragmentManager, "LoginFragment")
                return@setOnClickListener
            }


            val isCooking = serviceExtras.contains("Nấu ăn")
            val isIroning = serviceExtras.contains("Ủi đồ")
            val duration = CleaningDuration(
                uid = durationId,
                workingHour = durationWorkingHour,
                fee = durationFee,
                description = durationDescription
            )

//            val serviceSelect = selectedServices.map { service ->
//                service.copy(uid = service.uid.split("_").first())
//            }
            Log.d("ConfirmCheckout", "About to check service type conditions")
            if (serviceType == "cleaning") {
                Log.d("ConfirmCheckout", "Cleaning service type detected")
                viewModelCleaning.postServiceCleaning(
                    userID = uid,
                    startTime = selectedTime,
//                workerQuantity = numberOfPeople,
                    price = totalFee * selectedDates.size,
                    listDays = selectedDates,
                    duration = duration,
                    isCooking = isCooking,
                    isIroning = isIroning,
                    location = preferencesManager.getUserData()["user_location"] ?: ""
//                services = serviceSelect
                )
            } else if (serviceType == "healthcare") {
                Log.d("ConfirmCheckout", "Healthcare service type detected")
                val shift = ShiftInfo(
                    uid = shiftId,
                    workingHour = shiftWorkingHour,
                    fee = shiftFee
                )

                // Create services list based on quantities
                val services = mutableListOf<ServiceInfoHealthcare>()

                // Add baby service if quantity > 0
                if (numberOfBaby > 0 && babyServiceId.isNotEmpty()) {
                    services.add(
                        ServiceInfoHealthcare(
                            uid = babyServiceId,
                            quantity = numberOfBaby
                        )
                    )
                }

                // Add adult/disabled service if quantity > 0
                if (numberOfAdult > 0 && adultServiceId.isNotEmpty()) {
                    services.add(
                        ServiceInfoHealthcare(
                            uid = adultServiceId,
                            quantity = numberOfAdult
                        )
                    )
                }

                // Add elderly service if quantity > 0
                if (numberOfElderly > 0 && elderlyServiceId.isNotEmpty()) {
                    services.add(
                        ServiceInfoHealthcare(
                            uid = elderlyServiceId,
                            quantity = numberOfElderly
                        )
                    )
                }

                // Ensure we have at least one service
                if (services.isEmpty()) {
                    // Use elderly service as default if available, otherwise use any available service ID
                    val defaultServiceId = if (elderlyServiceId.isNotEmpty()) {
                        elderlyServiceId
                    } else if (adultServiceId.isNotEmpty()) {
                        adultServiceId
                    } else if (babyServiceId.isNotEmpty()) {
                        babyServiceId
                    } else {
                        "" // This should not happen if data is loaded properly
                    }

                    if (defaultServiceId.isNotEmpty()) {
                        services.add(
                            ServiceInfoHealthcare(
                                uid = defaultServiceId,
                                quantity = numberOfWorker
                            )
                        )
                    }
                }

                Log.d("ConfirmCheckout", "Final services list: $services")

                viewModelHealthCare.postServiceHealthcare(
                    userID = uid,
                    startTime = selectedTime,
                    price = totalFee * selectedDates.size,
                    listDays = selectedDates,
                    shift = shift,
                    services = services,
                    workerQuantity = numberOfWorker,
                    location = preferencesManager.getUserData()["user_location"] ?: ""
                )
            } else if (serviceType == "maintenance") {
                Log.d("ConfirmCheckout", "Maintenance service type detected")

                // Lấy thông tin chi tiết về các items đã chọn từ arguments
                val selectedServiceUids =
                    arguments?.getStringArrayList("selectedServiceUids") ?: arrayListOf()
                val selectedPowerUids =
                    arguments?.getStringArrayList("selectedPowerUids") ?: arrayListOf()
                val selectedQuantities =
                    arguments?.getIntegerArrayList("selectedQuantities") ?: arrayListOf()
                val selectedMaintenanceQuantities =
                    arguments?.getIntegerArrayList("selectedMaintenanceQuantities") ?: arrayListOf()

                // Tạo danh sách services cho maintenance dựa trên dữ liệu thực tế
                val services = mutableListOf<ServicePowerInfo>()

                // Với mỗi service UID, tạo một ServicePowerInfo với các power tương ứng
                selectedServiceUids.distinct().forEach { serviceUid ->
                    val powerItems = mutableListOf<PowersInfoQuantity>()

                    // Tìm tất cả power UID thuộc về service này
                    selectedServiceUids.indices.forEach { index ->
                        if (selectedServiceUids[index] == serviceUid) {
                            val powerUid = selectedPowerUids[index]
                            val quantity = selectedQuantities[index]

                            powerItems.add(
                                PowersInfoQuantity(
                                    uid = powerUid,
                                    quantity = quantity,
                                    quantityAction = selectedMaintenanceQuantities.getOrElse(index) { 0 }
                                )
                            )
                        }
                    }

                    if (powerItems.isNotEmpty()) {
                        val service = ServicePowerInfo(
                            uid = serviceUid,                      // UID từ API thực tế
                            powers = powerItems
                        )
                        services.add(service)
                    }
                }

//                // Nếu không có dữ liệu chi tiết, tạo service mặc định (fallback)
//                if (services.isEmpty()) {
//                    val defaultService = ServicePowerInfo(
//                        uid = durationId.ifEmpty { "maintenance_${System.currentTimeMillis()}" },
//                        power = listOf(
//                            PowersInfoQuantity(
//                                uid = durationId.ifEmpty { "maintenance_${System.currentTimeMillis()}" },
//                                quantity = totalHours,
//                                quantityAction = 0
//                            )
//                        )
//                    )
//                    services.add(defaultService)
//                }

                Log.d("ConfirmCheckout", "Maintenance services: $services")
                Log.d("ConfirmCheckout", "Service UIDs: ${selectedServiceUids.joinToString(", ")}")
                Log.d("ConfirmCheckout", "Power UIDs: ${selectedPowerUids.joinToString(", ")}")
                Log.d("ConfirmCheckout", "Quantities: ${selectedQuantities.joinToString(", ")}")
                Log.d(
                    "ConfirmCheckout",
                    "Maintenance quantities: ${selectedMaintenanceQuantities.joinToString(", ")}"
                )

                viewModelMaintenance.postServiceMaintenance(
                    userID = uid,
                    startTime = selectedTime,
                    price = totalFee,
                    listDays = selectedDates,
                    location = preferencesManager.getUserData()["user_location"] ?: "",
                    services = services
                )
            }
        }

        observeViewModel()

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading
            launch {
                viewModelCleaning.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        loadingDialog.show()
                        binding.cardViewButtonPostJob.isEnabled =
                            false // Vô hiệu hóa nút đăng nhập
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        loadingDialog.hide()
                        binding.cardViewButtonPostJob.isEnabled =
                            true // Kích hoạt lại nút đăng nhập
                    }
                }
            }
            launch {
                viewModelHealthCare.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        loadingDialog.show()
                        binding.cardViewButtonPostJob.isEnabled =
                            false // Vô hiệu hóa nút đăng nhập
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        loadingDialog.hide()
                        binding.cardViewButtonPostJob.isEnabled =
                            true // Kích hoạt lại nút đăng nhập
                    }
                }
            }
            launch {
                viewModelMaintenance.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        loadingDialog.show()
                        binding.cardViewButtonPostJob.isEnabled =
                            false // Vô hiệu hóa nút đăng nhập
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        loadingDialog.hide()
                        binding.cardViewButtonPostJob.isEnabled =
                            true // Kích hoạt lại nút đăng nhập
                    }
                }
            }
            launch {
                viewModelCleaning.new_job_cleaning.collectLatest { newJobCleaning ->
                    if (newJobCleaning != null) {
                        // Clear selected rooms data
                        SelectedRoomManager.clearAllRooms()

                        // Show success message
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Đăng công việc thành công!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        // Delay một chút để user thấy Toast trước khi dialog xuất hiện
                        binding.root.postDelayed({
                            val uid = newJobCleaning.userID
                            val jobID = newJobCleaning.uid
                            val serviceType = newJobCleaning.serviceType
                            val price = newJobCleaning.price * newJobCleaning.listDays.size * 5 / 100
                            // show qr fragment với callback để finish khi dismiss
                            val qrFragment = PaymentQrFragment(
                                uid = uid,
                                jobID = jobID,
                                serviceType = serviceType,
                                amount = price,
                                onDismissCallback = {
                                    // Finish activity khi user đóng QR dialog
                                    requireActivity().finish()
                                }
                            )
                            qrFragment.show(parentFragmentManager, "PaymentQrFragment")
                        }, 800) // Delay 800ms để user thấy Toast
                    }
                }
            }
            launch {
                viewModelHealthCare.new_job_healthcare.collectLatest { newJobHealthcare ->
                    if (newJobHealthcare != null) {
                        // Clear selected rooms data
                        SelectedRoomManager.clearAllRooms()

                        // Show success message
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Đăng công việc thành công!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        // Delay một chút để user thấy Toast trước khi dialog xuất hiện
                        binding.root.postDelayed({
                            val uid = newJobHealthcare.userID
                            val jobID = newJobHealthcare.uid
                            val serviceType = newJobHealthcare.serviceType
                            val price = newJobHealthcare.price * newJobHealthcare.listDays.size * 5 / 100
                            // show qr fragment với callback để finish khi dismiss
                            val qrFragment = PaymentQrFragment(
                                uid = uid,
                                jobID = jobID,
                                serviceType = serviceType,
                                amount = price,
                                onDismissCallback = {
                                    // Finish activity khi user đóng QR dialog
                                    requireActivity().finish()
                                }
                            )
                            qrFragment.show(parentFragmentManager, "PaymentQrFragment")
                        }, 800) // Delay 800ms để user thấy Toast
                    }
                }
            }

            launch {
                viewModelMaintenance.new_job_maintenance.collectLatest { newJob ->
                    if (newJob != null) {
                        // Clear selected rooms data
                        SelectedRoomManager.clearAllRooms()

                        // Show success message
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Đăng công việc thành công!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        // Delay một chút để user thấy Toast trước khi dialog xuất hiện
                        binding.root.postDelayed({
                            val uid = newJob.userID
                            val jobID = newJob.uid
                            val serviceType = newJob.serviceType
                            val price = newJob.price * newJob.listDays.size * 5 / 100
                            // show qr fragment với callback để finish khi dismiss
                            val qrFragment = PaymentQrFragment(
                                uid = uid,
                                jobID = jobID,
                                serviceType = serviceType,
                                amount = price,
                                onDismissCallback = {
                                    // Finish activity khi user đóng QR dialog
                                    requireActivity().finish()
                                }
                            )
                            qrFragment.show(parentFragmentManager, "PaymentQrFragment")
                        }, 800) // Delay 800ms để user thấy Toast
                    }
                }
            }
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