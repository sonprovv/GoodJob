package com.project.job.utils

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T : Any> {
    /**
     * Represents a successful operation with a result of type [T]
     */
    data class Success<out T : Any>(val data: T) : Result<T>() {
        override fun isSuccess() = true
    }

    /**
     * Represents a failed operation with an error message
     */
    data class Error(val exception: Throwable? = null, val message: String? = null) : Result<Nothing>() {
        override fun isSuccess() = false
        
        constructor(message: String) : this(null, message)
        
        val errorMessage: String
            get() = message ?: exception?.message ?: "Unknown error"
    }

    /**
     * Represents a loading state
     */
    object Loading : Result<Nothing>() {
        override fun isSuccess() = false
    }

    /**
     * Returns true if this instance represents a successful outcome.
     */
    abstract fun isSuccess(): Boolean

    /**
     * Returns the encapsulated value if this instance represents [Success] or null if it is [Error] or [Loading].
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * Returns the encapsulated value if this instance represents [Success] or throws an exception if it is [Error] or [Loading].
     * @throws IllegalStateException if the result is not [Success]
     */
    @Throws(IllegalStateException::class)
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw IllegalStateException(errorMessage, exception)
        Loading -> throw IllegalStateException("Result is loading")
    }

    /**
     * Returns the encapsulated value if this instance represents [Success] or the result of [onFailure] function for the [error][Error] or [loading][Loading] state.
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Error) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onFailure(this)
        Loading -> onFailure(Error("Result is loading"))
    }
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or `null` if it is [Result.Error] or [Result.Loading].
 */
fun <T : Any> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or throws an exception if it is [Result.Error] or [Result.Loading].
 * @throws IllegalStateException if the result is not [Result.Success]
 */
fun <T : Any> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw IllegalStateException(errorMessage, exception)
    Result.Loading -> throw IllegalStateException("Result is loading")
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [Result.Success] or the original [Result.Error] or [Result.Loading] if it is not.
 */
inline fun <T : Any, R : Any> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        Result.Loading -> Result.Loading
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [Result.Success] or the original [Result.Error] or [Result.Loading] if it is not.
 */
inline fun <T : Any, R : Any> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> this
        Result.Loading -> Result.Loading
    }
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or the [defaultValue] if it is [Result.Error] or [Result.Loading].
 */
fun <T : Any> Result<T>.getOrDefault(defaultValue: T): T = when (this) {
    is Result.Success -> data
    else -> defaultValue
}

/**
 * Returns `true` if this instance represents [Result.Success] and its value is equal to the given [value].
 */
fun <T : Any> Result<T>.contains(value: T): Boolean {
    return this is Result.Success && data == value
}

/**
 * Executes the given [action] on the encapsulated value if this instance represents [Result.Success].
 * Returns the original [Result] unchanged.
 */
inline fun <T : Any> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes the given [action] on the encapsulated [Throwable] if this instance represents [Result.Error].
 * Returns the original [Result] unchanged.
 */
inline fun <T : Any> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) action(this)
    return this
}

/**
 * Executes the given [action] if this instance represents [Result.Loading].
 * Returns the original [Result] unchanged.
 */
inline fun <T : Any> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}
