package com.avf.vending.remote.circuit

data class CircuitBreakerConfig(
    val failureThreshold: Int = 5,
    val resetTimeMs: Long = 60_000,
)
