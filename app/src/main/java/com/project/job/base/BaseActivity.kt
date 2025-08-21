package com.project.job.base

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.project.job.OnMainCallBack
import java.lang.reflect.Constructor
import java.util.Objects

abstract class BaseActivity<V: ViewBinding,M: ViewModel>:
    AppCompatActivity(), View.OnClickListener, OnMainCallBack {

    lateinit var mbinding: V
    lateinit var viewmodel: M

    protected abstract fun getClassVM(): Class<M>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbinding = initViewBinding()
        viewmodel = ViewModelProvider(this)[getClassVM()]
        setContentView(mbinding.root)
        initView()
    }

    protected abstract fun initView()

    protected abstract fun initViewBinding(): V

    override fun onClick(v: View) {
        v.startAnimation(
            AnimationUtils.loadAnimation(
            this, androidx.appcompat.R.anim.abc_fade_in))
        clickView(v)
    }

    protected open fun clickView(v: View) {
    }

    override fun showFragment(tag: String, data: Objects?, isBack: Boolean, viewID:Int) {
        try {
            val clazz: Class<*> = Class.forName(tag)
            val constructor: Constructor<*> = clazz.getConstructor()
            val fragment = constructor.newInstance() as BaseFragment<*, *>
            fragment.mData = data
            fragment.setCallBack(this)
            val trans: FragmentTransaction = supportFragmentManager.beginTransaction()
            if (isBack) {
                trans.addToBackStack(null)
            }
            trans.replace(viewID, fragment, tag).commit()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}