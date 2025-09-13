package com.project.job.ui.activity.jobdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.databinding.FragmentJobInfoBinding
import com.project.job.ui.activity.jobdetail.calendar.CalendarDialog

class JobInfoFragment : Fragment() {
    private var _binding: FragmentJobInfoBinding? = null
    private val binding get() = _binding!!
    private var dataJob: DataJobs? = null

    companion object {
        private const val ARG_DATA_JOB = "data_job"

        fun newInstance(dataJob: DataJobs): JobInfoFragment {
            val fragment = JobInfoFragment()
            val args = Bundle()
            args.putParcelable(ARG_DATA_JOB, dataJob)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dataJob = it.getParcelable(ARG_DATA_JOB)
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
            var typejob = ""
            if (job.serviceType == "CLEANING") {
                typejob += "Dọn dẹp"
            } else if (job.serviceType == "HEALTHCARE") {
                typejob += "Chăm sóc"
            } else if (job.serviceType == "MAINTENANCE") {
                typejob += "Bảo trì"
            }
            val listDays = job.listDays
            val serviceExtras = mutableListOf<String>()
            var totalTime = job.duration.workingHour

            if (job.isCooking){
                serviceExtras.add("Nấu ăn")
                totalTime += 1
            }
            if (job.isIroning) {
                serviceExtras.add("Ủi đồ")
                totalTime += 1
            }
            binding.tvDateCreate.text = "Ngày tạo: ${job.createdAt}"
            binding.tvTotalTime.text = totalTime.toString()
            binding.tvTotalPrice.text = formatPrice(job.price)
            binding.tvLocation.text = job.location
            binding.tvTotalNumber.text = job.workerQuantity.toString()
            binding.tvServiceType.text = typejob
            binding.tvFullName.text = job.user.username
            binding.tvPhone.text = job.user.tel
            binding.tvStartDate.text = listDays.firstOrNull().toString()
            binding.tvEndDate.text = listDays.lastOrNull().toString()
            binding.tvTotalRooms.text = job.services.size.toString()
            binding.tvTotalJobArea.text = job.duration.description
            binding.tvServiceExtras.text = serviceExtras.joinToString(", ")
            binding.tvTotalDay.text = listDays.size.toString()
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