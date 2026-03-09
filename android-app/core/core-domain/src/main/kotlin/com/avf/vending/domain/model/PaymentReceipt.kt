package com.avf.vending.domain.model

data class PaymentReceipt(
    val id: String,
    val amount: Long,
    val change: Long,
    val method: PaymentMethod,
    val timestamp: Long,
)
