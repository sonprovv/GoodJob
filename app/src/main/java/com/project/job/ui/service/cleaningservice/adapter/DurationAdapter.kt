package com.project.job.ui.service.cleaningservice.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.CleaningDuration

class DurationAdapter : RecyclerView.Adapter<DurationAdapter.DurationViewHolder>() {
    var onDurationSelected: ((CleaningDuration) -> Unit)? = null

    private val items = mutableListOf<CleaningDuration?>()
    private var selectedPosition = -1

    fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        if (previousSelected != -1) notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }

    fun submitList(newItems: List<CleaningDuration?>) {
        items.clear()
        // Sort by workingHour in ascending order (lowest hours first)
        val sortedList = newItems.sortedBy { it?.workingHour ?: 0 }
        items.addAll(sortedList)
        
        // If this is the first time loading or list was cleared
        if (selectedPosition == -1 && items.isNotEmpty()) {
            selectedPosition = 0
            // Notify about the first selection
            items[0]?.let { firstItem ->
                onDurationSelected?.invoke(firstItem)
            }
        } else if (items.isNotEmpty()) {
            // If we already have a selection, make sure it's still valid
            if (selectedPosition >= items.size) {
                selectedPosition = items.size - 1
            }
            // Notify about the current selection
            items[selectedPosition]?.let { selectedItem ->
                onDurationSelected?.invoke(selectedItem)
            }
        }
        
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DurationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_duration_option, parent, false)
        return DurationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DurationViewHolder, position: Int) {
        val item = items[position]
        val isSelected = position == selectedPosition
        
        holder.bind(item, isSelected)
        
        holder.itemView.setOnClickListener {
            if (selectedPosition != position) {
                setSelectedPosition(position)
                item?.let { duration -> onDurationSelected?.invoke(duration) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class DurationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_h)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_content)

        fun bind(item: CleaningDuration?, isSelected: Boolean) {
            item ?: return  // Skip if item is null
            tvDuration.text = "${item.workingHour} giờ"
            tvDescription.text = item.description

            // Update background based on selection
            val backgroundRes = if (isSelected) R.drawable.bg_selected_duration else R.drawable.bg_edt_white
            val textColorRes = if (isSelected) R.color.cam else R.color.black
            
            itemView.setBackgroundResource(backgroundRes)
            tvDuration.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))
        }
    }
}
