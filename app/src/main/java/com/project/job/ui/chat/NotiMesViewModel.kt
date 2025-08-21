package com.project.job.ui.chat

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.job.base.BaseViewModel
import com.project.job.data.model.CheckNotiMes
import com.project.job.data.repository.NotiMesRepo
import kotlinx.coroutines.launch

class NotiMesViewModel(application: Application) : BaseViewModel(application) {
    private val repository = NotiMesRepo()

    private val _checkResult = MutableLiveData<CheckNotiMes>()
    val checkResult: LiveData<CheckNotiMes> get() = _checkResult

    fun checkFirebase() {
        viewModelScope.launch {
            val response = repository.checkFirebase()
            if (response.isSuccessful) {
                _checkResult.postValue(response.body())
            }
        }
    }
}