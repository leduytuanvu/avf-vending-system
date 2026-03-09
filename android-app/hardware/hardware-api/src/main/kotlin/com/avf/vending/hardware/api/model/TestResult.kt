package com.avf.vending.hardware.api.model

data class TestResult(
    val passed: Boolean,
    val message: String,
    val durationMs: Long,
)
