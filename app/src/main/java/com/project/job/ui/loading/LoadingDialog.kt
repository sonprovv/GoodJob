package com.project.job.ui.loading
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.project.job.R

class LoadingDialog(private val activity: Activity) {
    private var dialog: Dialog? = null

    fun show() {
        if (dialog == null) {
            dialog = Dialog(activity).apply {
                setContentView(R.layout.loading_animation)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
        dialog?.show()
    }

    fun hide() {
        dialog?.dismiss()
    }
}