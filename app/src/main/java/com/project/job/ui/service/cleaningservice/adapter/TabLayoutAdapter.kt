package com.project.job.ui.service.cleaningservice.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.job.ui.service.cleaningservice.BathRoomFragment
import com.project.job.ui.service.cleaningservice.BedRoomFragment
import com.project.job.ui.service.cleaningservice.KitchenFragment
import com.project.job.ui.service.cleaningservice.LivingRoomFragment

class TabLayoutAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return BedRoomFragment()
            1 -> return BathRoomFragment()
            2 -> return KitchenFragment()
            3 -> return LivingRoomFragment()
            else -> return BedRoomFragment()
        }
    }
}