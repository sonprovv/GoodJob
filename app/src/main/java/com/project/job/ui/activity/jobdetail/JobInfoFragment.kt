package com.project.job.ui.activity.jobdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.databinding.FragmentJobInfoBinding
import com.project.job.ui.activity.jobdetail.calendar.CalendarDialog

class JobInfoFragment : Fragment() {
    private var _binding: FragmentJobInfoBinding? = null
    private val binding get() = _binding!!
    private var dataJob: DataJobs? = null
    private var healthcareServiceList: List<HealthcareService>? = null
    private var maintenanceServiceList: List<MaintenanceData>? = null

    companion object {
        private const val ARG_DATA_JOB = "data_job"
        private const val ARG_HEALTHCARE_SERVICE_LIST = "healthcare_service_list"
        private const val ARG_MAINTENANCE_SERVICE_LIST = "maintenance_service_list"

        fun newInstance(
            dataJob: DataJobs,
            healthcareServiceList: List<HealthcareService>? = null,
            maintenanceServiceList: List<MaintenanceData>? = null
        ): JobInfoFragment {
            val fragment = JobInfoFragment()
            val args = Bundle().apply {
                putParcelable(ARG_DATA_JOB, dataJob)
                if (!healthcareServiceList.isNullOrEmpty()) {
                    putParcelableArrayList(
                        ARG_HEALTHCARE_SERVICE_LIST,
                        ArrayList(healthcareServiceList)
                    )
                }
                if (!maintenanceServiceList.isNullOrEmpty()) {
                    putParcelableArrayList(
                        ARG_MAINTENANCE_SERVICE_LIST,
                        ArrayList(maintenanceServiceList)
                    )
                }
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dataJob = it.getParcelable(ARG_DATA_JOB)
            healthcareServiceList = it.getParcelableArrayList(ARG_HEALTHCARE_SERVICE_LIST)
            maintenanceServiceList = it.getParcelableArrayList(ARG_MAINTENANCE_SERVICE_LIST)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarToggle()

        // Use dataJob here to populate the UI
        dataJob?.let { job ->
            var tvAreaInfo = ""
            var typejob = ""
            if (job.serviceType == "CLEANING") {
                typejob += "Dọn dẹp"
                binding.tvTotalJobArea.text = job.duration?.description
                binding.llServiceExtras.visibility = View.VISIBLE
                val serviceExtras = mutableListOf<String>()
                var totalTime = job.duration?.workingHour ?: 0

                if (job.isCooking == true) {
                    serviceExtras.add("Nấu ăn")
                    totalTime += 1
                }
                if (job.isIroning == true) {
                    serviceExtras.add("Ủi đồ")
                    totalTime += 1
                }
                binding.tvTotalTime.text = job.startTime + " (" + totalTime.toString() + " h)"
                binding.tvServiceExtras.text = serviceExtras.joinToString(", ")

            } else if (job.serviceType == "HEALTHCARE") {
                typejob += "Chăm sóc"
                Log.d("JobInfoFragment", "Healthcare services count: ${job.services?.size ?: 0}")
                Log.d(
                    "JobInfoFragment",
                    "Available services: ${healthcareServiceList?.joinToString { it.uid + "-" + it.serviceName }}"
                )

                if (!healthcareServiceList.isNullOrEmpty() && !job.services.isNullOrEmpty()) {
                    job.services.forEach { serviceItem ->
                        Log.d("JobInfoFragment", "Looking for service with ID: ${serviceItem.uid}")
                        val service = healthcareServiceList!!.find { it.uid == serviceItem.uid }
                        Log.d(
                            "JobInfoFragment",
                            "Found service: ${service?.serviceName} with quantity: ${serviceItem.quantity}"
                        )
                        if (service != null) {
                            tvAreaInfo += "\n${service.serviceName} (${serviceItem.quantity ?: 0}) "
                        }
                    }
                }
                binding.tvTotalJobArea.text = tvAreaInfo
                binding.llServiceExtras.visibility = View.GONE
                binding.tvTotalTime.text =
                    job.startTime + " (" + job.shift?.workingHour.toString() + " h)"
            } else if (job.serviceType == "MAINTENANCE") {
                typejob += "Bảo trì"
                Log.d("JobInfoFragment", "Maintenance services count: ${job.services?.size ?: 0}")
                Log.d(
                    "JobInfoFragment",
                    "Available services: ${maintenanceServiceList?.joinToString { it.uid + "-" + it.serviceName }}"
                )
                if (!maintenanceServiceList.isNullOrEmpty() && !job.services.isNullOrEmpty()) {
                    var totalHourDependByQuantity = 0
                    job.services.forEach { serviceItem ->
                        Log.d("JobInfoFragment", "Looking for service with ID: ${serviceItem.uid}")
                        val service = maintenanceServiceList!!.find { it.uid == serviceItem.uid }
                        Log.d(
                            "JobInfoFragment",
                            "Found service: ${service?.serviceName} with powers: ${serviceItem.powers?.size ?: 0}"
                        )

                        if (service != null) {
                            var serviceInfo = "${service.serviceName}: "

                            // Hiển thị thông tin các power items
                            serviceItem.powers?.forEach { powerItem ->
                                // Tìm thông tin power từ đúng service trong maintenanceServiceList
                                val powerInfo = service.powers.find { it.uid == powerItem.uid }

                                if (powerInfo != null) {
                                    serviceInfo += "${powerInfo.name} x${powerItem.quantity}"

                                    // Thêm thông tin maintenance nếu có
                                    if (powerItem.quantityAction!! > 0) {
                                        serviceInfo += " (${service.maintenance} x${powerItem.quantityAction})"
                                    }

                                    serviceInfo += ", "

                                    // cộng số lượng quantity -> số giờ làm
                                    totalHourDependByQuantity += powerItem.quantity
                                }
                            }

                            // Xóa dấu phẩy cuối cùng nếu có
                            if (serviceInfo.endsWith(", ")) {
                                serviceInfo = serviceInfo.dropLast(2)
                            }

                            tvAreaInfo += "$serviceInfo "
                            binding.tvTotalJobArea.text = tvAreaInfo
                        }
                    }
                    binding.llServiceExtras.visibility = View.GONE
                    binding.tvTotalTime.text =
                        job.startTime + " (" + totalHourDependByQuantity.toString() + " h)"
                }
            }
            val listDays = job.listDays

            binding.tvDateCreate.text = "Ngày tạo: ${job.createdAt}"
            binding.tvTotalPrice.text = formatPrice(job.price)
            binding.tvLocation.text = job.location
            if (typejob == "Chăm sóc") {
                binding.tvTotalPeople.text = job.workerQuantity.toString()
                binding.llTotalPeople.visibility = View.VISIBLE
            } else {
                binding.llTotalPeople.visibility = View.GONE
            }
            binding.tvServiceType.text = typejob
            binding.tvFullName.text = job.user.username
            binding.tvPhone.text = job.user.tel
            binding.tvStartDate.text = listDays.firstOrNull().toString()
            binding.tvEndDate.text = listDays.lastOrNull().toString()
            binding.tvTotalDay.text = listDays.size.toString() + " ngày"
            // Setup calendar with highlighted days
            setupCalendarWithHighlightedDays(listDays)
        }
    }

    private fun setupCalendarToggle() {
        binding.ivCalendarToggle.setOnClickListener {
            // Show calendar dialog instead of inline calendar
            dataJob?.let { job ->
                val calendarDialog = CalendarDialog(requireContext(), job.listDays)
                calendarDialog.show()
            }
        }
    }

    private fun setupCalendarWithHighlightedDays(listDays: List<String>) {
        // Calendar is now handled by dialog, no need for inline setup
    }

    private fun formatPrice(price: Int): CharSequence {
        return String.format("%,d", price) + " VND"
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}