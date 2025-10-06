package com.project.job.ui.activity.jobdetail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.ui.activity.jobdetail.JobInfoFragment
import com.project.job.ui.activity.jobdetail.WorkerOrderFragment

class TabLayoutAdapter(
    activity: FragmentActivity,
    private val dataJob: DataJobs,
    private val healthcareServiceList: List<HealthcareService>? = null,
    private val maintenanceServiceList: List<MaintenanceData>? = null
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> JobInfoFragment.newInstance(dataJob, healthcareServiceList, maintenanceServiceList)
            1 -> WorkerOrderFragment.newInstance(dataJob)
            else -> JobInfoFragment.newInstance(dataJob, healthcareServiceList, maintenanceServiceList)
        }
    }
}