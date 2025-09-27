package com.project.job.ui.service.cleaningservice

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.project.job.R

class CustomTimePickerDialog : DialogFragment() {

    companion object {
        const val TAG = "CustomTimePickerDialog"

        fun newInstance(): CustomTimePickerDialog {
            return CustomTimePickerDialog()
        }
    }

    private var onTimeSelectedListener: ((Int, Int) -> Unit)? = null

    // Current selected values
    private var selectedHour = 14
    private var selectedMinute = 0
    
    private lateinit var timePicker: TimePicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_setup_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        timePicker = view.findViewById(R.id.timePicker)
        setupTimePicker()
        setupClickListeners(view)
    }
    
    private fun setupTimePicker() {
        timePicker.setIs24HourView(true) // Set 24-hour format
        timePicker.hour = selectedHour
        timePicker.minute = selectedMinute
        
        // Set time change listener
        timePicker.setOnTimeChangedListener { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
        }
    }



    private fun setupClickListeners(view: View) {
        // Cancel button
        view.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }

        // OK button
        view.findViewById<TextView>(R.id.btn_ok).setOnClickListener {
            onTimeSelectedListener?.invoke(selectedHour, selectedMinute)
            dismiss()
        }
    }

    fun setOnTimeSelectedListener(listener: (Int, Int) -> Unit) {
        onTimeSelectedListener = listener
    }

    fun setInitialTime(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute
        if (::timePicker.isInitialized) {
            timePicker.hour = selectedHour
            timePicker.minute = selectedMinute
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
    }
}