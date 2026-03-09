package com.avf.vending.domain.model

data class PaymentSession(
    val id: String,
    val amount: Long,
    val method: PaymentMethod,
    val qrData: String? = null,
    val expiresAt: Long,
    val traceId: String = id,
)
