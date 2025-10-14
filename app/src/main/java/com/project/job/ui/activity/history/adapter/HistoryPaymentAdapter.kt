package com.project.job.ui.activity.history.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.PaymentData
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryPaymentAdapter : RecyclerView.Adapter<HistoryPaymentAdapter.ViewHolder>() {
    
    private var paymentList: List<PaymentData> = emptyList()
    private var onItemClickListener: ((PaymentData) -> Unit)? = null
    
    // Optional: Map to store job locations if you want to fetch them
    private var jobLocations: Map<String, String> = emptyMap()
    
    fun updateData(newPaymentList: List<PaymentData>) {
        paymentList = newPaymentList
        notifyDataSetChanged()
    }
    
    fun setOnItemClickListener(listener: (PaymentData) -> Unit) {
        onItemClickListener = listener
    }
    
    // Method to update job locations if you fetch them separately
    fun updateJobLocations(locations: Map<String, String>) {
        jobLocations = locations
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = paymentList[position]
        holder.bind(payment)
    }
    
    override fun getItemCount(): Int = paymentList.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvServiceName: TextView = itemView.findViewById(R.id.tv_service_name)
        private val tvServicePrice: TextView = itemView.findViewById(R.id.tv_sevice_price)
        private val tvUserTime: TextView = itemView.findViewById(R.id.tv_user_time)
        private val tvUserLocation: TextView = itemView.findViewById(R.id.tv_user_location)
        
        fun bind(payment: PaymentData) {
            // Set service name based on serviceType
            tvServiceName.text = when (payment.serviceType.uppercase()) {
                "CLEANING" -> "Dọn dẹp"
                "HEALTHCARE" -> "Chăm sóc"
                "MAINTENANCE" -> "Bảo trì"
                else -> "Khác"
            }
            
            // Format price with Vietnamese currency
            val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                .format(payment.amount) + " VND"
            tvServicePrice.text = formattedPrice
            
            // Format date from createdAt
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(payment.createdAt)
                tvUserTime.text = date?.let { outputFormat.format(it) } ?: payment.createdAt
            } catch (e: Exception) {
                // If parsing fails, show original date
                tvUserTime.text = payment.createdAt
            }
            
            // Set location - check if we have the actual location, otherwise show jobID
            val location = jobLocations[payment.jobID]
            if (location != null) {
                // Use actual location like JobAdapter: tvLocation.text = job.location
                tvUserLocation.text = location
            } else {
                // Fallback to jobID display
                tvUserLocation.text = ""
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onItemClickListener?.invoke(payment)
            }
        }
    }
}