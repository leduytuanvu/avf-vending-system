package com.avf.vending.hardware.api.command

import java.util.UUID

data class HardwareCommand(
    val name: String,
    val busKey: String,
    val payloadDescription: String = "",
    val timeoutMs: Long = 2_000L,
    val maxRetries: Int = 0,
    val correlationId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
)
