package com.avf.vending.testing

import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import java.util.UUID

object TransactionFactory {
    fun create(
        id: String = UUID.randomUUID().toString(),
        slotId: String = "A1",
        productId: String = UUID.randomUUID().toString(),
        amount: Long = 10_000L,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        status: TransactionStatus = TransactionStatus.COMPLETED,
        syncStatus: SyncStatus = SyncStatus.PENDING,
    ) = Transaction(
        id = id,
        slotId = slotId,
        productId = productId,
        amount = amount,
        paymentMethod = paymentMethod,
        status = status,
        createdAt = System.currentTimeMillis(),
        syncStatus = syncStatus,
    )
}
