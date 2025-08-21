package com.project.job.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * A generic class that represents the state of a network operation.
 * This can be used to represent different states like loading, success, error, etc.
 */
sealed class NetworkState<out T> {
    /**
     * Represents a successful network operation with the result data
     */
    data class Success<out T>(val data: T) : NetworkState<T>()

    /**
     * Represents a failed network operation with an error message
     */
    data class Error(val message: String, val throwable: Throwable? = null) : NetworkState<Nothing>()

    /**
     * Represents a loading state of a network operation
     */
    object Loading : NetworkState<Nothing>()

    /**
     * Represents an idle state (initial state)
     */
    object Idle : NetworkState<Nothing>() {
        override fun toString() = "Idle"
    }

    /**
     * Returns true if the current state is [Success]
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if the current state is [Error]
     */
    fun isError(): Boolean = this is Error

    /**
     * Returns true if the current state is [Loading]
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Returns true if the current state is [Idle]
     */
    fun isIdle(): Boolean = this is Idle

    /**
     * Returns the data if the state is [Success], null otherwise
     */
    fun getDataOrNull(): T? = if (this is Success) data else null

    /**
     * Returns the error message if the state is [Error], null otherwise
     */
    fun getErrorMessageOrNull(): String? = if (this is Error) message else null

    /**
     * Returns the throwable if the state is [Error], null otherwise
     */
    fun getErrorOrNull(): Throwable? = if (this is Error) throwable else null

    companion object {
        /**
         * Creates a [Success] state with the given data
         */
        fun <T> success(data: T): NetworkState<T> = Success(data)

        /**
         * Creates an [Error] state with the given message and optional throwable
         */
        fun <T> error(message: String, throwable: Throwable? = null): NetworkState<T> = 
            Error(message, throwable)

        /**
         * Returns a [Loading] state
         */
        fun <T> loading(): NetworkState<T> = Loading

        /**
         * Returns an [Idle] state
         */
        fun <T> idle(): NetworkState<T> = Idle
    }
}

/**
 * A helper class to manage network state using LiveData
 */
class NetworkStateHelper<T> {
    private val _networkState = MutableLiveData<NetworkState<T>>(NetworkState.Idle)
    
    /**
     * The current network state as LiveData
     */
    val networkState: LiveData<NetworkState<T>> = _networkState
    
    /**
     * Updates the network state to [NetworkState.Loading]
     */
    fun setLoading() {
        _networkState.value = NetworkState.Loading
    }
    
    /**
     * Updates the network state to [NetworkState.Success] with the given data
     */
    fun setSuccess(data: T) {
        _networkState.value = NetworkState.Success(data)
    }
    
    /**
     * Updates the network state to [NetworkState.Error] with the given message and optional throwable
     */
    fun setError(message: String, throwable: Throwable? = null) {
        _networkState.value = NetworkState.Error(message, throwable)
    }
    
    /**
     * Updates the network state to [NetworkState.Idle]
     */
    fun setIdle() {
        _networkState.value = NetworkState.Idle
    }
    
    /**
     * Returns the current network state
     */
    fun getCurrentState(): NetworkState<T>? = _networkState.value
    
    /**
     * Returns true if the current state is [NetworkState.Loading]
     */
    fun isLoading(): Boolean = _networkState.value is NetworkState.Loading
    
    /**
     * Returns true if the current state is [NetworkState.Success]
     */
    fun isSuccess(): Boolean = _networkState.value is NetworkState.Success
    
    /**
     * Returns true if the current state is [NetworkState.Error]
     */
    fun isError(): Boolean = _networkState.value is NetworkState.Error
    
    /**
     * Returns true if the current state is [NetworkState.Idle]
     */
    fun isIdle(): Boolean = _networkState.value is NetworkState.Idle
    
    /**
     * Returns the data if the current state is [NetworkState.Success], null otherwise
     */
    fun getDataOrNull(): T? = (_networkState.value as? NetworkState.Success)?.data
    
    /**
     * Returns the error message if the current state is [NetworkState.Error], null otherwise
     */
    fun getErrorMessageOrNull(): String? = (_networkState.value as? NetworkState.Error)?.message
    
    /**
     * Returns the throwable if the current state is [NetworkState.Error], null otherwise
     */
    fun getErrorOrNull(): Throwable? = (_networkState.value as? NetworkState.Error)?.throwable
}
