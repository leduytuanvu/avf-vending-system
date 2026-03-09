package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WalletStatusDto(
    val sessionId: String,
    val state: String, // "PENDING" | "PAID" | "EXPIRED"
    val transactionId: String? = null,
    val amount: Long? = null,
)
