package com.avf.vending.hardware.transport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class DriverConnectionWatchdog(
    private val scope: CoroutineScope,
    private val reconnect: suspend () -> Boolean,
    private val maxAttempts: Int = 5,
    private val initialBackoffMs: Long = 1_000L,
    private val maxBackoffMs: Long = 10_000L,
) {
    private val reconnecting = AtomicBoolean(false)
    private var reconnectJob: Job? = null

    fun scheduleReconnect() {
        if (!reconnecting.compareAndSet(false, true)) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            var backoffMs = initialBackoffMs
            repeat(maxAttempts) {
                delay(backoffMs)
                if (reconnect()) {
                    reconnecting.set(false)
                    return@launch
                }
                backoffMs = min(backoffMs * 2, maxBackoffMs)
            }
            reconnecting.set(false)
        }
    }

    fun cancel() {
        reconnectJob?.cancel()
        reconnecting.set(false)
    }
}
