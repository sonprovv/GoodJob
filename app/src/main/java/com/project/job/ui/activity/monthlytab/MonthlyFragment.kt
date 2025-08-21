package com.project.job.ui.activity.monthlytab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentMonthlyBinding
import com.project.job.ui.login.LoginFragment

class MonthlyFragment : Fragment() {
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMonthlyBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the monthly tab view
        // You can add your logic here to display monthly activities
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(parentFragmentManager, "LoginFragment")
        }
    }
}