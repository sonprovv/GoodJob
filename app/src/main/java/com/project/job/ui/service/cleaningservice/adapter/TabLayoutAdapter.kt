package com.project.job.ui.service.cleaningservice.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.ui.service.cleaningservice.RoomDetailFragment

class TabLayoutAdapter(
    fragmentActivity: FragmentActivity,
    private val rooms: List<CleaningService>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = rooms.size

    override fun createFragment(position: Int): Fragment {
        val room = rooms.getOrNull(position) ?: return RoomDetailFragment()
        return RoomDetailFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList("tasks", ArrayList(room.tasks))
                putString("uid", room.uid)
                putString("serviceType", room.serviceType)
                putString("serviceName", room.serviceName)
                putString("image", room.image)
            }
        }
    }
}