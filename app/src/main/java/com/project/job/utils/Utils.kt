package com.project.job.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

@SuppressLint("ClickableViewAccessibility")
fun View.addFadeClickEffect(
    fadeAlpha: Float = 0.5f,
    onClick: (() -> Unit)? = null
) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> v.alpha = fadeAlpha
            MotionEvent.ACTION_UP -> {
                v.alpha = 1f
                onClick?.invoke() // gọi callback click riêng
            }

            MotionEvent.ACTION_CANCEL -> v.alpha = 1f
        }
        true
    }
}

