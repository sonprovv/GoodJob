package com.project.job.ui.service.healthcareservice.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.HealthcareShift


class ShiftAdapter: RecyclerView.Adapter<ShiftAdapter.ViewHolder>() {
    var onShiftSelected: ((HealthcareShift) -> Unit)? = null

    private val items = mutableListOf<HealthcareShift?>()
    private var selectedPosition = -1

    fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        if (previousSelected != -1) notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }

    fun submitList(newItems: List<HealthcareShift?>) {
        items.clear()
        // Sort by workingHour in ascending order (lowest hours first)
        val sortedList = newItems.sortedBy { it?.workingHour ?: 0 }
        items.addAll(sortedList)

        // If this is the first time loading or list was cleared
        if (selectedPosition == -1 && items.isNotEmpty()) {
            selectedPosition = 0
            // Notify about the first selection
            items[0]?.let { firstItem ->
                onShiftSelected?.invoke(firstItem)
            }
        } else if (items.isNotEmpty()) {
            // If we already have a selection, make sure it's still valid
            if (selectedPosition >= items.size) {
                selectedPosition = items.size - 1
            }
            // Notify about the current selection
            items[selectedPosition]?.let { selectedItem ->
                onShiftSelected?.invoke(selectedItem)
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_of_week, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val isSelected = position == selectedPosition

        holder.bind(item, isSelected)

        holder.itemView.setOnClickListener {
            if (selectedPosition != position) {
                setSelectedPosition(position)
                item?.let { duration -> onShiftSelected?.invoke(duration) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_day_of_week)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_day)

        fun bind(item: HealthcareShift?, isSelected: Boolean) {
            item ?: return  // Skip if item is null
            tvDuration.text = "${item.workingHour} giờ"
            tvDescription.text = "${String.format("%,d", item.fee)} VND"

            // Update appearance based on selection - find the LinearLayout inside FrameLayout
            val linearLayout = itemView.findViewById<LinearLayout>(R.id.ll_day_option)
            if (isSelected) {
                linearLayout.setBackgroundResource(R.drawable.bg_selected_duration)
                tvDuration.setTextColor(ContextCompat.getColor(itemView.context, R.color.cam))
                tvDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.cam))
            } else {
                linearLayout.setBackgroundResource(R.drawable.bg_edt_white)
                tvDuration.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                tvDescription.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }
        }
    }
}