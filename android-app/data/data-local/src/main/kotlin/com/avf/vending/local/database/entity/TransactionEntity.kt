package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions", indices = [Index("syncStatus"), Index("createdAt"), Index("status")])
data class TransactionEntity(
    @PrimaryKey val id: String,
    val slotId: String,
    val productId: String,
    val amount: Long,
    val paymentMethod: String,
    val status: String,
    val dispenseStatus: String = "NOT_STARTED",
    val syncStatus: Int, // 0=PENDING, 1=SYNCED, 2=CONFLICT, 3=FAILED
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val traceId: String = id,
    val idempotencyKey: String = id,
    val machineId: String = "",
)
