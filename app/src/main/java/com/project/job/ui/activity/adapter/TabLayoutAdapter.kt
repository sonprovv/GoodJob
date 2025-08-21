package com.project.job.ui.activity.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.job.ui.activity.monthlytab.MonthlyFragment
import com.project.job.ui.activity.scheduletab.ScheduleFragment
import com.project.job.ui.activity.upcomingtab.UpcomingFragment

class TabLayoutAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UpcomingFragment()
            1 -> ScheduleFragment()
            2 -> MonthlyFragment()
            else -> UpcomingFragment()
        }
    }
}