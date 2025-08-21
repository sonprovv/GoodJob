package com.project.job.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Base ViewModel with common functionality and coroutine support
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application),
    CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * Launch a coroutine in the ViewModel scope
     */
    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(coroutineContext) { block() }
    }

    /**
     * Launch a coroutine in the IO context
     */
    protected fun launchIO(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(Dispatchers.IO) { block() }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}