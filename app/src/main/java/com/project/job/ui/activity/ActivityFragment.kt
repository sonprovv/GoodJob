package com.project.job.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.R
import com.project.job.databinding.FragmentActivityBinding
import com.project.job.ui.activity.history.HistoryActivity
import com.project.job.ui.activity.monthlytab.MonthlyFragment
import com.project.job.ui.activity.scheduletab.ScheduleFragment
import com.project.job.ui.activity.upcomingtab.UpcomingFragment
import java.time.Month

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up ViewPager2 with adapter
        val adapter = ViewPagerAdapter(this)
        binding.viewPagerActivity.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayoutActivity, binding.viewPagerActivity) { tab, position ->
            tab.text = when (position) {
                0 -> "Chờ làm"
                1 -> "Lặp lại"
                else -> "Gói tháng"
            }
        }.attach()

        binding.tvHistory.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    // ViewPager2 adapter to manage fragments
    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3 // Number of tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UpcomingFragment() // Replace with your fragment for Tab 1
                1 -> ScheduleFragment() // Replace with your fragment for Tab 2
                2 -> MonthlyFragment()
                else -> UpcomingFragment() // Fallback
            }
        }
    }
}