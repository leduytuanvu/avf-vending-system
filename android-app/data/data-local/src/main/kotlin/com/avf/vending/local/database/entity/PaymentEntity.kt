package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val amount: Long,
    val change: Long,
    val method: String,
    val status: String,
    val createdAt: Long,
    val traceId: String = transactionId,
    val idempotencyKey: String = id,
)
