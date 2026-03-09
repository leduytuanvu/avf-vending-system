package com.avf.vending.repository.mapper

import com.avf.vending.domain.model.*
import com.avf.vending.local.database.entity.PaymentEntity

object PaymentMapper {
    fun PaymentEntity.toDomain() = Payment(
        id = id, transactionId = transactionId, amount = amount, change = change,
        method = PaymentMethod.valueOf(method),
        status = PaymentStatus.valueOf(status),
        createdAt = createdAt,
        traceId = traceId,
        idempotencyKey = idempotencyKey,
    )

    fun Payment.toEntity() = PaymentEntity(
        id = id, transactionId = transactionId, amount = amount, change = change,
        method = method.name, status = status.name, createdAt = createdAt,
        traceId = traceId,
        idempotencyKey = idempotencyKey,
    )
}
