package com.project.job.data.model

import java.util.Date

data class DayItem(
    val dayOfWeek: String,
    val date: String,
    val dateValue: Date,
    var isSelected: Boolean = false,
    val isToday: Boolean = false
) {
    fun isSelectable(): Boolean {
        // You can add logic here if some days shouldn't be selectable
        return true
    }
}
