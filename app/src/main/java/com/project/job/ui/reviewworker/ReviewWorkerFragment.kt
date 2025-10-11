package com.project.job.ui.reviewworker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.ExtendedReview
import com.project.job.databinding.FragmentReviewWorkerBinding
import com.project.job.ui.activity.jobdetail.viewmodel.JobDetailViewModel
import com.project.job.ui.notification.NotificationActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReviewWorkerFragment : Fragment() {
    private val TAG = "ReviewWorkerFragment"
    private var _binding: FragmentReviewWorkerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: JobDetailViewModel
    private lateinit var preferencesManager: PreferencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewWorkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        viewModel = JobDetailViewModel()

        val workerId = arguments?.getString("worker_id") ?: ""
        val workerName = arguments?.getString("worker_name")
        val workerPhone = arguments?.getString("worker_phone")
        val workerLocation = arguments?.getString("worker_location")
        val workerAvatar = arguments?.getString("worker_avatar")
        val workerDescription = arguments?.getString("worker_description")
        val workerGender = arguments?.getString("worker_gender")
        val workerStatus = arguments?.getString("worker_status")
        val workerBirthdate = arguments?.getString("worker_birthdate")
        val workerEmail = arguments?.getString("worker_email")

        // Use the retrieved data (e.g., display it in the UI)
        binding.tvName.text = workerName
        binding.tvPhone.text = workerPhone
        binding.tvAddress.text = workerLocation
        binding.tvDescription.text = workerDescription
        binding.tvGender.text = workerGender
        binding.tvBirth.text = workerBirthdate
        binding.tvEmail.text = workerEmail

        binding.ivClose.setOnClickListener {
            // Show lại TabLayout và ViewPager2 trước khi đóng fragment
            val activity = requireActivity() as? com.project.job.ui.activity.jobdetail.JobDetailActivity
            activity?.let { jobDetailActivity ->
                jobDetailActivity.binding.tabLayoutActivity.visibility = View.VISIBLE
                jobDetailActivity.binding.viewPagerActivity.visibility = View.VISIBLE
            }

            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.tvViewReview.setOnClickListener{
            // Navigate to ReviewerActivity with all necessary data
            val intent = ReviewerActivity.newIntent(
                context = requireContext(),
                workerId = workerId,
                workerName = workerName ?: getString(R.string.worker_reviews),
                serviceType = ReviewerActivity.SERVICE_TYPE_CLEANING, // Default to HEALTHCARE service type
                workerAvatar = workerAvatar,
                workerPhone = workerPhone,
                workerLocation = workerLocation,
                workerDescription = workerDescription,
                workerGender = workerGender,
                workerBirthdate = workerBirthdate,
                workerEmail = workerEmail
            )
            startActivity(intent)
        }

        Glide.with(requireContext())
            .load(workerAvatar)
            .placeholder(R.drawable.img_profile_picture_defaul) // Placeholder image
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivAvatar)
        // Get all service types reviews
        viewModel.getReviewWorker(workerId)

        observeViewModel()
    }
    private fun observeViewModel() {
        lifecycleScope.launch {
            // Quan sát serviceRatings thay vì userReview
            viewModel.serviceRatings.collectLatest { ratings ->
                displayExperienceRatings(ratings)
            }
        }
    }
    
    @SuppressLint("DefaultLocale")
    private fun displayExperienceRatings(ratings: Map<String, Double>) {
        val cleaningRating = ratings["CLEANING"] ?: 0.0
        val healthcareRating = ratings["HEALTHCARE"] ?: 0.0
        val maintenanceRating = ratings["MAINTENANCE"] ?: 0.0

        val experienceText = buildString {
            if (cleaningRating > 0) append(
                "- Dọn dẹp: ${
                    String.format(
                        "%.1f",
                        cleaningRating
                    )
                } ⭐\n"
            )
            if (healthcareRating > 0) append(
                "- Chăm sóc: ${
                    String.format(
                        "%.1f",
                        healthcareRating
                    )
                } ⭐\n"
            )
            if (maintenanceRating > 0) append(
                "- Bảo trì: ${
                    String.format(
                        "%.1f",
                        maintenanceRating
                    )
                } ⭐"
            )
        }
        binding.tvExperience.text = experienceText.ifEmpty { "Chưa có đánh giá nào" }
    }
        
    override fun onDestroy() {
        super.onDestroy()

        // Show lại TabLayout và ViewPager2 khi fragment bị destroy (bao gồm cả back button)
        val activity = requireActivity() as? com.project.job.ui.activity.jobdetail.JobDetailActivity
        activity?.let { jobDetailActivity ->
            // Sử dụng post để đảm bảo UI được cập nhật sau khi fragment bị remove khỏi view hierarchy
            jobDetailActivity.binding.root.post {
                jobDetailActivity.binding.tabLayoutActivity.visibility = View.VISIBLE
                jobDetailActivity.binding.viewPagerActivity.visibility = View.VISIBLE
            }
        }
    }
}