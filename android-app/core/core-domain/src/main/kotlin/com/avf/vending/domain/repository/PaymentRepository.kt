package com.avf.vending.domain.repository

import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.model.PaymentStatus

interface PaymentRepository {
    suspend fun insert(payment: Payment): String
    suspend fun getById(id: String): Payment?
    suspend fun updateStatus(id: String, status: PaymentStatus)
    suspend fun getByTransactionId(transactionId: String): Payment?
}
