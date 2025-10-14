package com.project.job.ui.chatbot

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.QueryJobs
import java.text.NumberFormat
import java.util.*

class JobAdapter(private val jobs: List<QueryJobs>) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_card, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount() = jobs.size

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvServiceType: TextView = itemView.findViewById(R.id.tvServiceType)
//        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvWorkingDays: TextView = itemView.findViewById(R.id.tvWorkingDays)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)

        fun bind(job: QueryJobs) {
            // Service Type
            tvServiceType.text = job.serviceType
            tvServiceType.setBackgroundColor(getServiceTypeColor(job.serviceType))

            // Status
//            tvStatus.text = job.status
//            tvStatus.setTextColor(getStatusColor(job.status))

            // Location
            tvLocation.text = job.location

            // Price
            val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            tvPrice.text = "${formatter.format(job.price.toLong())} VND"

            // Start Time
            tvStartTime.text = job.startTime

            // Working Days
            tvWorkingDays.text = job.listDays.joinToString(", ")

            // Created At
            tvCreatedAt.text = "Tạo: ${job.createdAt}"
        }

        private fun getServiceTypeColor(serviceType: String): Int {
            return when (serviceType) {
                "HEALTHCARE" -> Color.parseColor("#10B981") // Green
                "MAINTENANCE" -> Color.parseColor("#3B82F6") // Blue
                "CLEANING" -> Color.parseColor("#8B5CF6") // Purple
                else -> Color.parseColor("#6B7280") // Gray
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "Not Payment" -> Color.parseColor("#F59E0B") // Orange
                "Paid" -> Color.parseColor("#10B981") // Green
                "Completed" -> Color.parseColor("#6B7280") // Gray
                else -> Color.parseColor("#EF4444") // Red
            }
        }
    }
}
