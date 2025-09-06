package com.project.job.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputEditText

/**
 * Shows the soft keyboard for the view.
 */
fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Hides the soft keyboard from the window.
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Toggle password visibility for EditText with a drawable end icon.
 */
fun EditText.showPasswordToggle() {
    val isPasswordVisible = inputType and 
        (android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD or 
         android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD) == 0

    // Toggle input type
    inputType = if (isPasswordVisible) {
        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    } else {
        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    }

    // Move cursor to the end
    setSelection(text?.length ?: 0)

    // Toggle drawable
    val drawableRes = if (isPasswordVisible) {
        com.project.job.R.drawable.ic_visibility_off
    } else {
        com.project.job.R.drawable.ic_visibility
    }
    
    setCompoundDrawablesWithIntrinsicBounds(
        null, 
        null, 
        ContextCompat.getDrawable(context, drawableRes), 
        null
    )
}

/**
 * Extension function to set background color from resources
 */
fun View.setBackgroundColorRes(@ColorRes colorRes: Int) {
    setBackgroundColor(ContextCompat.getColor(context, colorRes))
}
