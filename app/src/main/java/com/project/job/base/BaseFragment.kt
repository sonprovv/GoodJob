package com.project.job.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.project.job.OnMainCallBack
import com.project.job.OnMainCallBack2
import java.lang.reflect.Constructor
import java.util.Objects

abstract class BaseFragment<B : ViewBinding,M: ViewModel>:
    Fragment(), View.OnClickListener, OnMainCallBack2 {
    private lateinit var mContext: Context
    lateinit var viewmodel: M
    protected lateinit var mbinding: B
    private var mCallBack: OnMainCallBack? = null
    private var mCallBack2: OnMainCallBack2? = null
    var mData: Objects? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mbinding = initViewBinding(inflater, container)
        viewmodel = ViewModelProvider(this)[getClassVM()]
        return mbinding.root
    }

    override fun onViewCreated
                (view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onClick(v: View) {
        v.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                androidx.appcompat.R.anim.abc_fade_in
            )
        )
        clickView(v)
    }

    protected abstract fun getClassVM(): Class<M>

    protected open fun clickView(v: View) {
    }

    protected abstract fun initView()

    protected abstract fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): B

    fun setmData(Data: Objects) {
        mData = Data
    }

    fun setCallBack(callBack: OnMainCallBack) {
        mCallBack = callBack
    }
    fun setCallBack2(callBack: OnMainCallBack2) {
        mCallBack2 = callBack
    }

    override fun showFragment
                (tag: String, data: Objects?, isBack: Boolean,viewID:Int) {
        try {
            val clazz: Class<*> = Class.forName(tag)
            val constructor: Constructor<*> = clazz.getConstructor()
            val fragment = constructor.newInstance() as BaseFragment<*, *>
            fragment.mData = data
            fragment.setCallBack2(this)
            val trans: FragmentTransaction = childFragmentManager.beginTransaction()
            if (isBack) {
                trans.addToBackStack(null)
            }
            trans.replace(viewID, fragment, tag).commit()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}