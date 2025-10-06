package com.project.job.ui.activity.jobdetail.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.project.job.R
import java.text.SimpleDateFormat
import java.util.*

class CustomCalendarGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridView(context, attrs, defStyleAttr) {

    private var workingDates = mutableSetOf<String>()
    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Format từ API
    private val internalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format nội bộ
    private val displayFormat = SimpleDateFormat("d", Locale.getDefault())
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var calendarAdapter: CalendarAdapter? = null

    init {
        numColumns = 7
        setupCalendar()
    }

    fun setWorkingDates(dates: List<String>) {
        workingDates.clear()
        
        // Convert input dates to internal format and store
        val convertedDates = mutableListOf<Date>()
        dates.forEach { dateString ->
            try {
                val date = inputDateFormat.parse(dateString) // Parse dd/MM/yyyy
                date?.let {
                    workingDates.add(internalDateFormat.format(it)) // Store as yyyy-MM-dd
                    convertedDates.add(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Tính toán ngày bắt đầu và kết thúc từ danh sách ngày làm việc
        if (convertedDates.isNotEmpty()) {
            val sortedDates = convertedDates.sorted()
            try {
                val firstDate = sortedDates.first()
                val lastDate = sortedDates.last()
                
                startDate = Calendar.getInstance().apply { 
                    time = firstDate 
                    set(Calendar.DAY_OF_MONTH, 1) // Bắt đầu từ đầu tháng
                }
                endDate = Calendar.getInstance().apply { 
                    time = lastDate 
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) // Kết thúc cuối tháng
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        calendarAdapter?.generateCalendarDays()
        calendarAdapter?.notifyDataSetChanged()
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter()
        calendarAdapter?.generateCalendarDays() // Generate initial calendar
        adapter = calendarAdapter
    }

    fun setMonth(year: Int, month: Int) {
        // This method is no longer needed as we display date range
    }

    private inner class CalendarAdapter : BaseAdapter() {
        private val daysInMonth = mutableListOf<CalendarDay>()

        init {
            // Don't generate calendar days in init - wait for data
        }

        fun generateCalendarDays() {
            daysInMonth.clear()
            
            if (startDate == null || endDate == null) {
                // If no date range, show current month as fallback
                val currentCal = Calendar.getInstance()
                currentCal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK) - 1
                val daysInCurrentMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                // Add empty days for previous month
                for (i in 0 until firstDayOfWeek) {
                    daysInMonth.add(CalendarDay("", false))
                }
                
                // Add days of current month
                for (day in 1..daysInCurrentMonth) {
                    currentCal.set(Calendar.DAY_OF_MONTH, day)
                    val dateString = internalDateFormat.format(currentCal.time)
                    val isWorkingDay = workingDates.contains(dateString)
                    daysInMonth.add(CalendarDay(day.toString(), true, isWorkingDay))
                }
                return
            }
            
            val calendar = startDate!!.clone() as Calendar
            val endCalendar = endDate!!.clone() as Calendar
            
            // Tính toán ngày đầu tiên của tuần chứa startDate
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            
            // Add empty days for previous month if needed
            for (i in 0 until firstDayOfWeek) {
                daysInMonth.add(CalendarDay("", false))
            }
            
            // Add all days from start date to end date
            while (calendar.timeInMillis <= endCalendar.timeInMillis) {
                val dateString = internalDateFormat.format(calendar.time)
                val isWorkingDay = workingDates.contains(dateString)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                
                daysInMonth.add(CalendarDay(dayOfMonth.toString(), true, isWorkingDay))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            // Add empty days to complete the last week if needed
            val remainingDays = 7 - (daysInMonth.size % 7)
            if (remainingDays < 7) {
                for (i in 0 until remainingDays) {
                    daysInMonth.add(CalendarDay("", false))
                }
            }
        }

        override fun getCount(): Int = daysInMonth.size

        override fun getItem(position: Int): CalendarDay = daysInMonth[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_calendar_day, parent, false)
            
            val dayText = view.findViewById<TextView>(R.id.tv_day)
            val calendarDay = getItem(position)
            
            dayText.text = calendarDay.day
            dayText.isEnabled = false
            if (calendarDay.isCurrentMonth) {
                dayText.setTextColor(ContextCompat.getColor(context, R.color.black))
                if (calendarDay.isWorkingDay) {
                    // Đánh dấu màu cam cho ngày làm việc
                    dayText.setBackgroundResource(R.drawable.bg_working_day)
                    dayText.setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    dayText.background = null
                }
            } else {
                dayText.setTextColor(ContextCompat.getColor(context, R.color.gray_light))
                dayText.background = null
            }
            
            return view
        }

        override fun notifyDataSetChanged() {
            generateCalendarDays()
            super.notifyDataSetChanged()
        }
    }

    private data class CalendarDay(
        val day: String,
        val isCurrentMonth: Boolean = true,
        val isWorkingDay: Boolean = false
    )
}
