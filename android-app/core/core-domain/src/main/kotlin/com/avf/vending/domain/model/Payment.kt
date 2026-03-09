package com.avf.vending.domain.model

data class Payment(
    val id: String,
    val transactionId: String,
    val amount: Long,
    val change: Long,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val createdAt: Long,
    val traceId: String = transactionId,
    val idempotencyKey: String = id,
)

enum class PaymentMethod { CASH, QR_WALLET, NFC, CARD }

enum class PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
