package com.project.job.ui.service.cleaningservice.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.model.DayItem
import java.util.*

class DayAdapter(
    private val onDaysSelected: (List<DayItem>) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private val items = mutableListOf<DayItem>()
    private val selectedItems = mutableListOf<DayItem>()

    fun setDays(days: List<DayItem>) {
        items.clear()
        val tempSelectedDates = selectedItems.map { it.dateValue.time }

        // Add all days and restore selection state
        items.addAll(days.map { day ->
            val isSelected = tempSelectedDates.contains(day.dateValue.time)
            day.copy(isSelected = isSelected)
        })

        // No default selection
        

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_of_week, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayOfWeek: TextView = itemView.findViewById(R.id.tv_day_of_week)
        private val day: TextView = itemView.findViewById(R.id.tv_day)
        private val dayOption: LinearLayout = itemView.findViewById(R.id.ll_day_option)
        private val currentDayIndicator: CardView = itemView.findViewById(R.id.card_view_current_day)

        fun bind(dayItem: DayItem) {
            // Format day of week (e.g., "T2", "T3", etc.)
            val shortDayOfWeek = when (dayItem.dayOfWeek) {
                "Thứ Hai" -> "T2"
                "Thứ Ba" -> "T3"
                "Thứ Tư" -> "T4"
                "Thứ Năm" -> "T5"
                "Thứ Sáu" -> "T6"
                "Thứ Bảy" -> "T7"
                "Chủ Nhật" -> "CN"
                else -> dayItem.dayOfWeek
            }

            dayOfWeek.text = shortDayOfWeek

            // Format day display - show day/month for first day of month, just day for others
            val dateParts = dayItem.date.split("/")
            val dayNumber = dateParts[0].toInt()
            val dayText = if (dayNumber == 1) dayItem.date else dayNumber.toString()
            day.text = dayText

            // Update selection state
            if (dayItem.isSelected) {
                dayOption.setBackgroundResource(R.drawable.bg_selected_day)
                dayOfWeek.setTextColor(itemView.context.getColor(R.color.white))
                day.setTextColor(itemView.context.getColor(R.color.white))
                currentDayIndicator.visibility = View.INVISIBLE
            } else {
                dayOption.setBackgroundResource(R.drawable.bg_edt_white)
                dayOfWeek.setTextColor(itemView.context.getColor(R.color.black))
                day.setTextColor(itemView.context.getColor(R.color.black))

                // Show current day indicator if today
                val today = Calendar.getInstance()
                val itemDate = Calendar.getInstance()
                itemDate.time = dayItem.dateValue

                val isToday = today.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)

                currentDayIndicator.visibility = if (isToday && !dayItem.isSelected) View.VISIBLE else View.INVISIBLE
            }

            itemView.setOnClickListener {
                // Toggle selection
                dayItem.isSelected = !dayItem.isSelected

                if (dayItem.isSelected) {
                    selectedItems.add(dayItem)
                } else {
                    selectedItems.removeIf { it.dateValue.time == dayItem.dateValue.time }
                }

                // Update UI
                notifyItemChanged(adapterPosition)

                // Notify listener with all selected days
                onDaysSelected(selectedItems.toList())
            }
        }
    }
}
