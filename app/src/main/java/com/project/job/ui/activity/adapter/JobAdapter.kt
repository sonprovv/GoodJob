package com.project.job.ui.activity.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.ui.activity.jobdetail.JobDetailActivity

class JobAdapter(private var jobList: List<DataJobs> = emptyList()) : RecyclerView.Adapter<JobAdapter.viewHolder>() {

    private var isSelected = false

    fun setSelected(selected: Boolean) {
        isSelected = selected
        notifyDataSetChanged()
    }

    fun updateList(newList: List<DataJobs>) {
        jobList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_job_history,
            parent,
            false
        )
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(jobList[position])
        holder.itemView.setOnClickListener { 
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val intent = Intent(holder.itemView.context, JobDetailActivity::class.java)
                intent.putExtra("job", jobList[currentPosition])
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = jobList.size

    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAerviceTyoe = itemView.findViewById<TextView>(R.id.tv_service_tyoe)
        private val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
        private val tvDateCreate = itemView.findViewById<TextView>(R.id.tv_date_create)
        private val tvState = itemView.findViewById<TextView>(R.id.tv_state)
        private val tvTotalNumberPeople = itemView.findViewById<TextView>(R.id.tv_total_number_people)
        private val tvTotalPrice = itemView.findViewById<TextView>(R.id.tv_total_price)

        fun bind(job: DataJobs) {
            var typejob = "Dịch vụ: "
            if(job.serviceType == "CLEANING"){
                typejob += "Dọn dẹp"
            }
            else if(job.serviceType == "HEALTHCARE"){
                typejob += "Chăm sóc"
            }
            else if(job.serviceType == "MAINTENANCE"){
                typejob += "Bảo trì"
            }
            var status = ""
            if(job.status == "Processing"){
                status = "Đang xử lý"
                tvState.setTextColor(itemView.context.getColor(R.color.black))
            }
            else if(job.status == "Completed"){
                status = "Đã hoàn tất"
                tvState.setTextColor(itemView.context.getColor(R.color.xanh))
            }
            else if(job.status == "Active"){
                status = "Đang hoạt động"
                tvState.setTextColor(itemView.context.getColor(R.color.xanh))
            }
            tvAerviceTyoe.text = typejob
            tvLocation.text = job.location
            tvDateCreate.text = "${job.createdAt} - ${job.startTime}"
            tvState.text = status
            tvTotalNumberPeople.text = "${job.workerQuantity} người"
            tvTotalPrice.text = formatPrice(job.price)
        }

        private fun formatPrice(price: Int): CharSequence {
            return String.format("%,d", price) + " VND"
        }
    }
}