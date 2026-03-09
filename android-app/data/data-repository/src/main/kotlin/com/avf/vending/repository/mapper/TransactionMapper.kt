package com.avf.vending.repository.mapper

import com.avf.vending.domain.model.*
import com.avf.vending.local.database.entity.TransactionEntity

object TransactionMapper {
    fun TransactionEntity.toDomain() = Transaction(
        id = id, slotId = slotId, productId = productId, amount = amount,
        paymentMethod = PaymentMethod.valueOf(paymentMethod),
        status = TransactionStatus.valueOf(status),
        dispenseStatus = DispenseStatus.valueOf(dispenseStatus),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.entries[syncStatus],
        traceId = traceId,
        idempotencyKey = idempotencyKey,
        machineId = machineId,
    )

    fun Transaction.toEntity() = TransactionEntity(
        id = id, slotId = slotId, productId = productId, amount = amount,
        paymentMethod = paymentMethod.name,
        status = status.name,
        dispenseStatus = dispenseStatus.name,
        syncStatus = syncStatus.ordinal,
        createdAt = createdAt,
        updatedAt = updatedAt,
        traceId = traceId,
        idempotencyKey = idempotencyKey,
        machineId = machineId,
    )
}
