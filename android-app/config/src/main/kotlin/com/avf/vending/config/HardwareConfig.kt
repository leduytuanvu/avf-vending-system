package com.avf.vending.config

data class HardwareConfig(
    val dispenseTimeoutMs: Long = 10_000L,
    val dispenseRetryCount: Int = 3,
    val dispenseRetryDelayMs: Long = 500L,
    val pollIntervalMs: Long = 2_000L,
    val billPollIntervalMs: Long = 500L,
    val motorTestDurationMs: Long = 3_000L,
    val temperatureWarningCelsius: Float = 45f,
    val temperatureCriticalCelsius: Float = 55f,
)
