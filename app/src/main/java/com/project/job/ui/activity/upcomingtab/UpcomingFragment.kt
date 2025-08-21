package com.project.job.ui.activity.upcomingtab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.databinding.FragmentUpcomingBinding
import com.project.job.ui.login.LoginFragment


class UpcomingFragment : Fragment() {
    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the upcoming tab view
        // You can add your logic here to display upcoming activities
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(parentFragmentManager, "LoginFragment")
        }
    }
}