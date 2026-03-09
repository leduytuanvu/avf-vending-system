package com.avf.vending.domain.transaction

import com.avf.vending.domain.model.AppError
import com.avf.vending.domain.model.DispenseOutcome
import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.PaymentRepository
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.SyncPriority
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.domain.usecase.UpdateStockUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

data class AuthorizedTransaction(
    val transactionId: String,
    val paymentId: String,
)

class TransactionCoordinator @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository,
    private val productRepository: ProductRepository,
    private val updateStockUseCase: UpdateStockUseCase,
    private val stateMachine: TransactionStateMachine,
) {
    private val authMutex = Mutex()

    suspend fun authorizePayment(
        slotId: String,
        productId: String,
        amount: Long,
        paymentMethod: PaymentMethod,
        change: Long,
    ): AuthorizedTransaction = authMutex.withLock {
        productRepository.getProductById(productId)
            ?: throw AppError.ValidationError("Product not found: $productId")

        val now = System.currentTimeMillis()
        val transactionId = UUID.randomUUID().toString()
        val paymentId = UUID.randomUUID().toString()

        var currentStatus = stateMachine.transition(
            current = TransactionStatus.PENDING,
            event = TransactionLifecycleEvent.PaymentStarted,
        )

        val transaction = Transaction(
            id = transactionId,
            slotId = slotId,
            productId = productId,
            amount = amount,
            paymentMethod = paymentMethod,
            status = currentStatus,
            createdAt = now,
        )
        transactionRepository.insertWithOutbox(transaction, SyncPriority.CRITICAL)

        try {
            paymentRepository.insert(
                Payment(
                    id = paymentId,
                    transactionId = transactionId,
                    amount = amount,
                    change = change,
                    method = paymentMethod,
                    status = PaymentStatus.PENDING,
                    createdAt = now,
                    traceId = transactionId,
                )
            )

            currentStatus = stateMachine.transition(
                current = currentStatus,
                event = TransactionLifecycleEvent.PaymentAuthorized,
            )
            transactionRepository.updateTransactionStatus(transactionId, currentStatus)
            paymentRepository.updateStatus(paymentId, PaymentStatus.COMPLETED)
            return AuthorizedTransaction(transactionId = transactionId, paymentId = paymentId)
        } catch (e: Exception) {
            transactionRepository.updateTransactionStatus(
                transactionId,
                stateMachine.transition(
                    current = currentStatus,
                    event = TransactionLifecycleEvent.PaymentFailed,
                )
            )
            throw e
        }
    }

    suspend fun markDispensing(transactionId: String) {
        val transaction = requireTransaction(transactionId)
        transactionRepository.updateTransactionStatus(
            transactionId,
            stateMachine.transition(
                current = transaction.status,
                event = TransactionLifecycleEvent.DispenseStarted,
            )
        )
        transactionRepository.updateDispenseStatus(transactionId, DispenseStatus.DISPENSING)
    }

    suspend fun recordDispenseOutcome(transactionId: String, outcome: DispenseOutcome) {
        val transaction = requireTransaction(transactionId)
        when (outcome) {
            is DispenseOutcome.Success -> {
                transactionRepository.updateDispenseStatus(transactionId, DispenseStatus.SUCCESS)
                transactionRepository.updateTransactionStatus(
                    transactionId,
                    stateMachine.transition(transaction.status, TransactionLifecycleEvent.DispenseSucceeded)
                )
                updateStockUseCase(outcome.slotId, -1)
            }
            is DispenseOutcome.Failed -> {
                transactionRepository.updateDispenseStatus(transactionId, DispenseStatus.FAILED)
                transactionRepository.updateTransactionStatus(
                    transactionId,
                    stateMachine.transition(transaction.status, TransactionLifecycleEvent.DispenseFailed)
                )
            }
            is DispenseOutcome.Timeout -> {
                transactionRepository.updateDispenseStatus(transactionId, DispenseStatus.TIMEOUT)
                transactionRepository.updateTransactionStatus(
                    transactionId,
                    stateMachine.transition(transaction.status, TransactionLifecycleEvent.DispenseTimedOut)
                )
            }
        }
    }

    suspend fun recoverInterruptedTransaction(
        transactionId: String,
        delivered: Boolean,
    ) {
        val transaction = requireTransaction(transactionId)
        val event = if (delivered) {
            TransactionLifecycleEvent.RecoveredAsCompleted
        } else {
            TransactionLifecycleEvent.RecoveredAsRefundRequired
        }
        transactionRepository.updateTransactionStatus(
            transactionId,
            stateMachine.transition(transaction.status, event)
        )
        transactionRepository.updateDispenseStatus(
            transactionId,
            if (delivered) DispenseStatus.SUCCESS else DispenseStatus.FAILED,
        )
    }

    suspend fun markRefunded(transactionId: String) {
        val transaction = requireTransaction(transactionId)
        transactionRepository.updateTransactionStatus(
            transactionId,
            stateMachine.transition(transaction.status, TransactionLifecycleEvent.RefundCompleted)
        )
        paymentRepository.getByTransactionId(transactionId)
            ?.let { paymentRepository.updateStatus(it.id, PaymentStatus.REFUNDED) }
    }

    private suspend fun requireTransaction(transactionId: String): Transaction =
        requireNotNull(transactionRepository.getById(transactionId)) {
            "Transaction not found: $transactionId"
        }
}
