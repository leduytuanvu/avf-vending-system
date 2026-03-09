package com.avf.vending.remote.circuit

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class CircuitBreaker @Inject constructor(
    private val config: CircuitBreakerConfig,
) {
    enum class State { CLOSED, OPEN, HALF_OPEN }

    private val state = AtomicReference(State.CLOSED)
    private val failureCount = AtomicInteger(0)
    private val lastFailureTime = AtomicLong(0L)

    fun isAllowed(): Boolean {
        return when (state.get()) {
            State.CLOSED -> true
            State.OPEN -> {
                val elapsed = System.currentTimeMillis() - lastFailureTime.get()
                if (elapsed >= config.resetTimeMs) {
                    state.compareAndSet(State.OPEN, State.HALF_OPEN)
                    true
                } else false
            }
            State.HALF_OPEN -> true
        }
    }

    fun recordSuccess() {
        failureCount.set(0)
        state.set(State.CLOSED)
    }

    fun recordFailure() {
        val failures = failureCount.incrementAndGet()
        lastFailureTime.set(System.currentTimeMillis())
        if (failures >= config.failureThreshold) {
            state.set(State.OPEN)
        }
    }

    fun currentState(): State = state.get()
}
