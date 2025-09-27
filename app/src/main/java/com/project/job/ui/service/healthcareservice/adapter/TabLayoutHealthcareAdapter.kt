package com.project.job.ui.service.healthcareservice.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.ui.service.healthcareservice.PeopleDetailFragment

class TabLayoutHealthcareAdapter(
    fragmentActivity: FragmentActivity,
    private val rooms: List<HealthcareService>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = rooms.size

    override fun createFragment(position: Int): Fragment {
        val room = rooms.getOrNull(position) ?: return PeopleDetailFragment()
        return PeopleDetailFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList("duties", ArrayList(room.duties))
                putStringArrayList("excludedTasks", ArrayList(room.excludedTasks))
                putString("uid", room.uid)
                putString("serviceType", room.serviceType)
                putString("serviceName", room.serviceName)
                putString("image", room.image)
            }
        }
    }
}