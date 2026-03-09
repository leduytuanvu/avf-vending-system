package com.avf.vending.payment.api

import kotlinx.coroutines.flow.Flow

interface PaymentProcessor {
    val type: PaymentType
    fun startSession(requiredAmount: Long): Flow<PaymentEvent>
    suspend fun cancel()
    suspend fun confirm()
}
