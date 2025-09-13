package com.project.job.ui.activity.jobdetail.calendar

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.project.job.databinding.DialogCalendarBinding

class CalendarDialog(
    context: Context,
    private val workingDays: List<String>
) : Dialog(context) {

    private lateinit var binding: DialogCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        binding = DialogCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDialog()
        setupCalendar()
    }

    private fun setupDialog() {
        // Set dialog properties
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupCalendar() {
        // Setup calendar with working days
        binding.dialogCalendarView.setWorkingDates(workingDays)
    }
}
