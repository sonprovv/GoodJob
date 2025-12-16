package com.project.job.data.source.remote

/**
 * Map NetworkResult to another type
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(message)
    }
}


/**
 * Get data or null
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.Error -> null
    }
}
