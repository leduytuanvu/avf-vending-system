package com.avf.vending.common.result

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(val error: Throwable) : AppResult<Nothing>()
}
