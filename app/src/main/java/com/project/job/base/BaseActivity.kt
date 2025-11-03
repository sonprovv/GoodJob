package com.project.job.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.project.job.R
import java.lang.reflect.Constructor
import java.util.Objects
import androidx.fragment.app.Fragment
open class BaseActivity : AppCompatActivity() {

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    // Phương thức để chuyển sang Fragment với animation
    protected fun navigateToFragment(fragment: Fragment, containerId: Int, tag: String) {
        // Ẩn Activity content ngay lập tức
        findViewById<View>(R.id.activity_content)?.visibility = View.GONE

        // Hiện fragment container và đảm bảo layout complete trước khi animation
        val container = findViewById<View>(containerId)
        container.visibility = View.VISIBLE
        
        // Delay nhỏ 50ms để đảm bảo container đã layout và animation trigger đúng
        container.postDelayed({
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fragment_slide_in_right,
                    R.anim.fragment_slide_out_left,
                    R.anim.fragment_slide_in_left,
                    R.anim.fragment_slide_out_right
                )
                .replace(containerId, fragment, tag)
                .addToBackStack(tag)
                .commit()
        }, 50) // 50ms delay nhỏ để animation mượt
    }

    // Phương thức ẩn nội dung Activity
    protected open fun hideActivityContent() {
        // Mặc định ẩn view với id activity_content
        findViewById<View>(R.id.activity_content)?.visibility = View.GONE
    }

    // Phương thức hiện lại nội dung Activity
    protected open fun showActivityContent() {
        findViewById<View>(R.id.activity_content)?.visibility = View.VISIBLE
    }

    // Phương thức ẩn fragment container (sử dụng INVISIBLE để giữ layout)
    protected fun hideFragmentContainer(containerId: Int) {
        findViewById<View>(containerId)?.visibility = View.INVISIBLE
    }

    // Kiểm tra có Fragment đang hiển thị không
    protected fun isFragmentVisible(): Boolean {
        return supportFragmentManager.backStackEntryCount > 0
    }
}