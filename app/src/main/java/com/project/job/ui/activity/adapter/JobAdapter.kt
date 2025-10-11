package com.project.job.ui.activity.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.ui.activity.jobdetail.JobDetailActivity
import com.project.job.ui.payment.PaymentQrFragment
import com.project.job.utils.addFadeClickEffect

class JobAdapter : RecyclerView.Adapter<JobAdapter.viewHolder>() {

    // JobAdapter với tính năng vuốt sang trái để huỷ bài đăng
    // Chỉ hiển thị cho các job có status "Not Payment" hoặc "Hiring"
    // Vuốt sang phải để xem chi tiết job (cho tất cả status)
    //
    // Cách sử dụng:
    // 1. Set adapter cho RecyclerView
    // 2. Gọi adapter.getItemTouchHelper().attachToRecyclerView(recyclerView)
    // 3. Set listener: adapter.setOnJobCancelListener { jobId, serviceType ->
    //        // Gọi API cancelJob ở đây
    //        viewModel.cancelJob(serviceType, jobId)
    //    }
    
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

    // Listener for cancel job action
    private var onJobCancelListener: OnJobCancelListener? = null

    fun setOnJobCancelListener(listener: OnJobCancelListener) {
        this.onJobCancelListener = listener
    }

    // Method to update job status without reloading entire list
    fun updateJobStatus(jobId: String, newStatus: String) {
        val position = jobList.indexOfFirst { it.uid == jobId }
        if (position != -1) {
            // Update the job status in the list
            val updatedJob = jobList[position].copy(status = newStatus)
            val mutableList = jobList.toMutableList()
            mutableList[position] = updatedJob
            jobList = mutableList
            
            // Only notify this specific item changed
            notifyItemChanged(position)
            Log.d("JobAdapter", "Updated job $jobId status to $newStatus at position $position")
        } else {
            Log.w("JobAdapter", "Job $jobId not found in list for status update")
        }
    }

    // ItemTouchHelper for swipe functionality
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT  // Enable both directions for debugging
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            Log.d("JobAdapter", "onSwiped called for position $position, direction: $direction")
            if (position != RecyclerView.NO_POSITION) {
                val job = jobList[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        // Swipe left - cancel job
                        if (job.status == "Not Payment" || job.status == "Hiring") {
                            Log.d("JobAdapter", "Calling cancel listener for job ${job.uid}")
                            onJobCancelListener?.onJobCancel(job.uid, job.serviceType)
                        }
                    }
                    ItemTouchHelper.RIGHT -> {
                        // Swipe right - some other action (optional)
                        Log.d("JobAdapter", "Right swipe for job ${job.uid}")
                        // You can add other actions here if needed
                    }
                }

                // No need to notify item changed - let the parent handle list updates
                // notifyItemChanged(position) // Removed to prevent unnecessary re-rendering
            }
        }

        @SuppressLint("ResourceType")
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val position = viewHolder.adapterPosition
            val job = jobList.getOrNull(position)

            Log.d("JobAdapter", "onChildDraw called for position $position, dX: $dX, status: ${job?.status}")

            // Only show swipe background for allowed statuses and when swiping left (dX < 0) or right (dX > 0)
            if (job != null && (job.status == "Not Payment" || job.status == "Hiring")) {
                if (dX < 0) { // Swiping left
                    Log.d("JobAdapter", "Drawing swipe background LEFT for job ${job.uid}")
                    val background = ColorDrawable(ContextCompat.getColor(recyclerView.context, R.color.red))
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    // Draw cancel icon and text
                    val paint = Paint()
                    paint.color = Color.WHITE
                    paint.textSize = 48f
                    paint.textAlign = Paint.Align.CENTER

                    val centerY = itemView.top + itemView.height / 2

                    c.drawText(
                        "Huỷ bài đăng",
                        itemView.right + dX / 2,
                        centerY + 16f,
                        paint
                    )
                } else if (dX > 0) { // Swiping right
                    Log.d("JobAdapter", "Drawing swipe background RIGHT for job ${job.uid}")
                    val background = ColorDrawable(ContextCompat.getColor(recyclerView.context, Color.BLUE))
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    background.draw(c)

                    // Draw some text for right swipe
                    val paint = Paint()
                    paint.color = Color.WHITE
                    paint.textSize = 48f
                    paint.textAlign = Paint.Align.CENTER

                    val centerY = itemView.top + itemView.height / 2

                    c.drawText(
                        "Action",
                        itemView.left + dX / 2,
                        centerY + 16f,
                        paint
                    )
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            val position = viewHolder.adapterPosition
            val job = jobList.getOrNull(position)
            Log.d("JobAdapter", "getSwipeThreshold called for position $position, status: ${job?.status}")

            // Only allow swipe for specific statuses
            return if (job != null && (job.status == "Not Payment" || job.status == "Hiring")) {
                Log.d("JobAdapter", "Swipe enabled for status: ${job.status}")
                0.3f // 30% of item width
            } else {
                Log.d("JobAdapter", "Swipe disabled for status: ${job?.status}")
                Float.MAX_VALUE // Disable swipe for other statuses
            }
        }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_job_activity,
            parent,
            false
        )
        return viewHolder(view)
    }

    fun getItemTouchHelper(): ItemTouchHelper {
        return itemTouchHelper
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(jobList[position])

        // Set click listener manually instead of using addFadeClickEffect to avoid blocking swipe
        holder.itemView.addFadeClickEffect {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentJob = jobList[currentPosition]

                when (currentJob.status) {
                    "Not Payment" -> {
                        // Hiển thị PaymentQrFragment cho status "Not Payment"
                        val context = holder.itemView.context
                        if (context is FragmentActivity) {
                            val qrFragment = PaymentQrFragment(
                                uid = currentJob.user.uid,
                                jobID = currentJob.uid,
                                serviceType = currentJob.serviceType,
                                amount = currentJob.price * 5 / 100
                            )
                            qrFragment.show(context.supportFragmentManager, "PaymentQrFragment")
                            Log.d("JobAdapter", "Showing PaymentQrFragment for Not Payment job")
                        }
                    }
                    "Hiring" -> {
                        // Navigate to JobDetailActivity for Hiring status too
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
                    else -> {
                        // Các status khác → navigate to JobDetailActivity
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