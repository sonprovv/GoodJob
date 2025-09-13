package com.project.job.ui.service.cleaningservice

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.project.job.R
import java.util.*

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

    private lateinit var npHour: NumberPicker
    private lateinit var npMinute: NumberPicker

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

        setupNumberPickers(view)
        setupClickListeners(view)
    }

    private fun setupNumberPickers(view: View) {
        // Setup Hour NumberPicker
        npHour = view.findViewById(R.id.np_hour)
        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.value = selectedHour
        npHour.setFormatter { String.format(Locale.getDefault(), "%02d", it) }
        npHour.setOnValueChangedListener { _, _, newVal ->
            selectedHour = newVal
        }

        // Setup Minute NumberPicker
        npMinute = view.findViewById(R.id.np_minute)
        npMinute.minValue = 0
        npMinute.maxValue = 59 // 0, 5, 10, ..., 55 (12 steps)
        npMinute.value = selectedMinute
        npMinute.setFormatter { String.format(Locale.getDefault(), "%02d", it) }
        npMinute.setOnValueChangedListener { _, _, newVal ->
            selectedMinute = newVal
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
        selectedMinute = minute // Round to nearest 5-minute increment
        if (::npHour.isInitialized && ::npMinute.isInitialized) {
            npHour.value = selectedHour
            npMinute.value = selectedMinute
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