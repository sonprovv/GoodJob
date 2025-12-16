package com.project.job.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Hides the soft keyboard from the window.
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

