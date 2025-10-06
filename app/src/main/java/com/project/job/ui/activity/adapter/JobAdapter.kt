package com.project.job.ui.activity.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.ui.activity.jobdetail.JobDetailActivity
import com.project.job.ui.payment.PaymentQrFragment
import com.project.job.utils.addFadeClickEffect

class JobAdapter : RecyclerView.Adapter<JobAdapter.viewHolder>() {
    
    var jobList: List<DataJobs> = emptyList()
        private set

    private var isSelected = false

    fun setSelected(selected: Boolean) {
        isSelected = selected
        notifyDataSetChanged()
    }

    var healthcareServices: List<HealthcareService>? = null
        private set

    var maintenanceServices: List<MaintenanceData>? = null
        private set

    fun updateList(newList: List<DataJobs>, services: List<HealthcareService>? = null, maintenanceServices: List<MaintenanceData>? = null) {
        jobList = newList
        services?.let { healthcareServices = it }
        this.maintenanceServices = maintenanceServices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_job_activity,
            parent,
            false
        )
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(jobList[position])
        
        // Thêm hiệu ứng loang khi click vào item
        holder.itemView.addFadeClickEffect {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentJob = jobList[currentPosition]
                
                // Kiểm tra nếu status là "Not Payment" → hiển thị PaymentQrFragment
                if (currentJob.status == "Not Payment") {
                    val context = holder.itemView.context
                    if (context is FragmentActivity) {
                        val qrFragment = PaymentQrFragment(
                            uid = currentJob.user.uid,
                            jobID = currentJob.uid,
                            serviceType = currentJob.serviceType,
                            amount = currentJob.price
                        )
                        qrFragment.show(context.supportFragmentManager, "PaymentQrFragment")
                        Log.d("JobAdapter", "Showing PaymentQrFragment for Not Payment job")
                    }
                } else {
                    // Status không phải "Not Payment" → navigate to JobDetailActivity
                    val intent = Intent(holder.itemView.context, JobDetailActivity::class.java)
                    if(currentJob.serviceType == "HEALTHCARE" && !healthcareServices.isNullOrEmpty()){
                        Log.d("JobAdapter", "Passing ${healthcareServices?.size} healthcare services to detail")
                        intent.putParcelableArrayListExtra("healthcareServiceList", ArrayList(healthcareServices))
                    }
                    if(currentJob.serviceType == "MAINTENANCE" && !maintenanceServices.isNullOrEmpty()){
                        Log.d("JobAdapter", "Passing ${maintenanceServices?.size} maintenance services to detail")
                        intent.putParcelableArrayListExtra("maintenanceServiceList", ArrayList(maintenanceServices))
                    }
                    intent.putExtra("job", currentJob)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = jobList.size

    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvServiceType = itemView.findViewById<TextView>(R.id.tv_service_tyoe)
        private val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
        private val tvDateCreate = itemView.findViewById<TextView>(R.id.tv_date_create)
        private val tvState = itemView.findViewById<TextView>(R.id.tv_state)
        private val tvTotalNumberPeople = itemView.findViewById<TextView>(R.id.tv_total_number_people)
        private val tvTotalPrice = itemView.findViewById<TextView>(R.id.tv_total_price)
        private val llTotalPeople = itemView.findViewById<View>(R.id.ll_total_people)

        fun bind(job: DataJobs) {
            if(job.serviceType == "HEALTHCARE"){
                tvTotalNumberPeople.text = "${job.workerQuantity} người"
                llTotalPeople.visibility = View.VISIBLE
            }
            else{
                llTotalPeople.visibility = View.INVISIBLE
            }
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
                tvState.setTextColor(itemView.context.getColor(R.color.cam))
            }
            else if(job.status == "Completed"){
                status = "Đã hoàn tất"
                tvState.setTextColor(itemView.context.getColor(R.color.xanh))
            }
            else if(job.status == "Active"){
                status = "Đang hoạt động"
                tvState.setTextColor(itemView.context.getColor(R.color.xanh))
            }
            else if(job.status == "Closed") {
                status = "Đã đóng"
                tvState.setTextColor(itemView.context.getColor(R.color.red))
            }
            else if(job.status == "Hiring") {
                status = "Đang tuyển"
                tvState.setTextColor(itemView.context.getColor(R.color.cam))
            }
            else if(job.status == "Not Payment") {
                status = "Chưa thanh toán"
                tvState.setTextColor(itemView.context.getColor(R.color.red))
            }
            else if(job.status == "Cancel") {
                status = "Đã hủy"
                tvState.setTextColor(itemView.context.getColor(R.color.red))
            }
            tvServiceType.text = typejob
            tvLocation.text = job.location
            tvDateCreate.text = "${job.createdAt} - ${job.startTime}"
            tvState.text = status

            tvTotalPrice.text = formatPrice(job.price)
        }

        private fun formatPrice(price: Int): CharSequence {
            return String.format("%,d", price) + " VND"
        }
    }
}