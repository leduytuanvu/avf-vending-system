package com.avf.vending.hardware.api.strategy

sealed class HealthStatus {
    object Healthy : HealthStatus()
    data class Degraded(val reason: String) : HealthStatus()
    data class Failed(val reason: String, val cause: Throwable? = null) : HealthStatus()
}
