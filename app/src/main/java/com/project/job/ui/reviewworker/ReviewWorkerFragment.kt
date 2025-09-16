package com.project.job.ui.reviewworker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.databinding.FragmentReviewWorkerBinding

class ReviewWorkerFragment : Fragment() {
    private val TAG = "ReviewWorkerFragment"
    private var _binding: FragmentReviewWorkerBinding? = null
    private val binding get() = _binding!!
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
        val workerId = arguments?.getString("worker_id")
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
            requireActivity().supportFragmentManager.popBackStack()
        }

        Glide.with(requireContext())
            .load(workerAvatar)
            .placeholder(R.drawable.img_profile_picture_defaul) // Placeholder image
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivAvatar)
    }
}