package com.avf.vending.common.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen

fun <T> Flow<T>.retryWithBackoff(
    maxRetries: Long = 3,
    initialDelayMs: Long = 500,
    factor: Double = 2.0,
): Flow<T> = retryWhen { cause, attempt ->
    if (attempt < maxRetries) {
        delay((initialDelayMs * Math.pow(factor, attempt.toDouble())).toLong())
        true
    } else {
        false
    }
}

fun <T> Flow<T>.throttleFirst(windowDurationMs: Long): Flow<T> = flow {
    var lastEmitTime = 0L
    collect { value ->
        val now = System.currentTimeMillis()
        if (now - lastEmitTime >= windowDurationMs) {
            lastEmitTime = now
            emit(value)
        }
    }
}
