package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String,
    val slotId: String,
    val productId: String,
    val amount: Long,
    val paymentMethod: String,
    val status: String,
    val dispenseStatus: String = "NOT_STARTED",
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val traceId: String = id,
    val idempotencyKey: String = id,
    val machineId: String = "",
)
