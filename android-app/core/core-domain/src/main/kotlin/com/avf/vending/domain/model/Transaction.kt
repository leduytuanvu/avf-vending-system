package com.avf.vending.domain.model

data class Transaction(
    val id: String,
    val slotId: String,
    val productId: String,
    val amount: Long,
    val paymentMethod: PaymentMethod,
    val status: TransactionStatus,
    val dispenseStatus: DispenseStatus = DispenseStatus.NOT_STARTED,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val traceId: String = id,
    val idempotencyKey: String = id,
    val machineId: String = "",
)

enum class TransactionStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAYMENT_SUCCESS,
    DISPENSING,
    COMPLETED,
    FAILED,
    DISPENSE_FAILED,
    REFUND_REQUIRED,
    REFUNDED,
}
