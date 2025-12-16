package com.project.job.data.model

import java.util.Date

data class DayItem(
    val dayOfWeek: String,
    val date: String,
    val dateValue: Date,
    var isSelected: Boolean = false,
    val isToday: Boolean = false
)
