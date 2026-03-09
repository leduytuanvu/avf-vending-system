package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WalletQRResponseDto(
    val sessionId: String,
    val qrString: String,
    val deepLink: String,
    val expiresAt: Long,
)
