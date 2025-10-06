package com.project.job.ui.service.maintenanceservice.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServicePowerItem(
    val serviceName: String,
    val powerItem: PowerItem,
    val uid : String
) : Parcelable

class SelectedItemAdapter : RecyclerView.Adapter<SelectedItemAdapter.ViewHolder>() {
    private var selectedItems = listOf<ServicePowerItem>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNameService: TextView = itemView.findViewById(R.id.tv_name_service)
        val tvNameServiceDetail: TextView = itemView.findViewById(R.id.tv_name_service_detail)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        val tvQuantityChild: TextView = itemView.findViewById(R.id.tv_quantity_child)
        val llQuantityChild: View = itemView.findViewById(R.id.ll_quantity_child)
        val tvNameServiceChild: TextView = itemView.findViewById(R.id.tv_name_service_child)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_service_maintenance, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return selectedItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = selectedItems[position]

        // Hiển thị tên dịch vụ con (maintenanceName từ PowerItem)
        holder.tvNameServiceChild.text = "Thiết bị " + item.powerItem.maintenanceName

        // Hiển thị tên dịch vụ (serviceName từ tab)
        holder.tvNameService.text = item.serviceName

        // Hiển thị thông tin chi tiết (powerName từ PowerItem)
        holder.tvNameServiceDetail.text = item.powerItem.powerName

        // Hiển thị số lượng chính
        holder.tvQuantity.text = item.powerItem.quantity.toString()

        // Hiển thị số lượng maintenance (chỉ hiển thị nếu có)
        if (item.powerItem.isMaintenanceEnabled && item.powerItem.maintenanceQuantity!! > 0) {
            holder.tvQuantityChild.text = item.powerItem.maintenanceQuantity.toString()
            holder.llQuantityChild.visibility = View.VISIBLE
        } else {
            holder.tvQuantityChild.text = ""
            holder.llQuantityChild.visibility = View.GONE
        }
    }

    fun setData(items: List<ServicePowerItem>) {
        selectedItems = items
        notifyDataSetChanged()
    }
}