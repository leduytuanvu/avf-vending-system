package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.AppError
import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.TransactionRepository
import javax.inject.Inject

class DispenseProductUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
) {
    /**
     * Validates the product, persists the transaction as PAYMENT_SUCCESS / NOT_STARTED,
     * then advances both statuses to DISPENSING before returning so the feature layer can
     * send the hardware command.  Stock is only decremented AFTER hardware confirms delivery
     * (via RecordDispenseResultUseCase).
     *
     * Returns the persisted transaction ID.
     */
    suspend operator fun invoke(transaction: Transaction): String {
        productRepository.getProductById(transaction.productId)
            ?: throw AppError.ValidationError("Product not found: ${transaction.productId}")

        val id = transactionRepository.insertPending(
            transaction.copy(
                status = TransactionStatus.PAYMENT_SUCCESS,
                dispenseStatus = DispenseStatus.NOT_STARTED,
            )
        )

        // Mark DISPENSING atomically so a crash here leaves the transaction in PAYMENT_SUCCESS
        // which startup reconciliation will catch.
        transactionRepository.updateTransactionStatus(id, TransactionStatus.DISPENSING)
        transactionRepository.updateDispenseStatus(id, DispenseStatus.DISPENSING)

        return id
    }
}
