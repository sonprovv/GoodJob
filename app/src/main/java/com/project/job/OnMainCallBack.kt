package com.project.job

import java.util.Objects

interface OnMainCallBack {
    fun showFragment(tag:String, data: Objects?, isBack:Boolean,viewID:Int)
}
