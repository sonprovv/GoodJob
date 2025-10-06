package com.project.job.ui.service.maintenanceservice.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.ui.service.maintenanceservice.OnPriceChangedListener
import com.project.job.ui.service.maintenanceservice.ServiceMaintenanceChildFragment

class TabLayoutMaintenanceAdapter(
    fragmentActivity: FragmentActivity,
    private val rooms: List<MaintenanceData>,
    private val priceChangedListener: OnPriceChangedListener? = null
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = rooms.size

    override fun createFragment(position: Int): Fragment {
        val room = rooms.getOrNull(position) ?: return ServiceMaintenanceChildFragment()
        return ServiceMaintenanceChildFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("powers", ArrayList(room.powers))
                putString("uid", room.uid)
                putString("serviceType", room.serviceType)
                putString("serviceName", room.serviceName)
                putString("image", room.image)
                putString("maintenance", room.maintenance)
                // Truyền listener thông qua Bundle (fragment sẽ tự nhận ra)
            }
        }
    }
}