package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.DispenseJournalEntry
import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.model.Product
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.DispenseJournalRepository
import com.avf.vending.domain.repository.PaymentRepository
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.SyncRepository
import com.avf.vending.domain.repository.SyncTask
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.domain.transaction.TransactionCoordinator
import com.avf.vending.domain.transaction.TransactionStateMachine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReconcileTransactionsUseCaseTest {
    @Test
    fun `reconciliation completes delivered transactions and flags missing evidence for refund`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val paymentRepository = FakePaymentRepository()
        val productRepository = FakeProductRepository()
        val syncRepository = FakeSyncRepository()
        val journalRepository = FakeDispenseJournalRepository()
        val coordinator = TransactionCoordinator(
            transactionRepository = transactionRepository,
            paymentRepository = paymentRepository,
            productRepository = productRepository,
            updateStockUseCase = UpdateStockUseCase(syncRepository),
            stateMachine = TransactionStateMachine(),
        )
        val useCase = ReconcileTransactionsUseCase(
            transactionRepository = transactionRepository,
            dispenseJournalRepository = journalRepository,
            transactionCoordinator = coordinator,
        )

        val delivered = coordinator.authorizePayment("A1", "prod-1", 15_000L, PaymentMethod.CASH, 0L)
        coordinator.markDispensing(delivered.transactionId)
        journalRepository.entries[delivered.transactionId] = DispenseJournalEntry(
            dispenseId = "dispense-1",
            transactionId = delivered.transactionId,
            slotId = "A1",
            sensorTriggered = true,
            completed = false,
            createdAt = 1L,
        )

        val missingEvidence = coordinator.authorizePayment("A2", "prod-1", 15_000L, PaymentMethod.CASH, 0L)
        coordinator.markDispensing(missingEvidence.transactionId)

        useCase()

        assertEquals(TransactionStatus.COMPLETED, transactionRepository.getById(delivered.transactionId)?.status)
        assertEquals(TransactionStatus.REFUND_REQUIRED, transactionRepository.getById(missingEvidence.transactionId)?.status)
    }

    private class FakeTransactionRepository : TransactionRepository {
        val transactions = linkedMapOf<String, Transaction>()

        override suspend fun insertPending(transaction: Transaction): String {
            transactions[transaction.id] = transaction
            return transaction.id
        }

        override suspend fun insertWithOutbox(transaction: Transaction, syncPriority: Int): String {
            transactions[transaction.id] = transaction
            return transaction.id
        }

        override suspend fun markSynced(id: String) {
            updateStatus(id, SyncStatus.SYNCED)
        }

        override suspend fun getById(id: String): Transaction? = transactions[id]

        override fun observeHistory(): Flow<List<Transaction>> = flowOf(transactions.values.toList())

        override suspend fun getPendingSync(): List<Transaction> = transactions.values.toList()

        override suspend fun updateStatus(id: String, syncStatus: SyncStatus) {
            val current = requireNotNull(transactions[id])
            transactions[id] = current.copy(syncStatus = syncStatus)
        }

        override suspend fun updateTransactionStatus(id: String, status: TransactionStatus) {
            val current = requireNotNull(transactions[id])
            transactions[id] = current.copy(status = status)
        }

        override suspend fun updateDispenseStatus(id: String, dispenseStatus: DispenseStatus) {
            val current = requireNotNull(transactions[id])
            transactions[id] = current.copy(dispenseStatus = dispenseStatus)
        }

        override suspend fun findInterruptedTransactions(olderThanMs: Long): List<Transaction> =
            transactions.values.filter { it.status == TransactionStatus.DISPENSING || it.status == TransactionStatus.PAYMENT_SUCCESS }
    }

    private class FakePaymentRepository : PaymentRepository {
        private val payments = linkedMapOf<String, Payment>()

        override suspend fun insert(payment: Payment): String {
            payments[payment.id] = payment
            return payment.id
        }

        override suspend fun getById(id: String): Payment? = payments[id]

        override suspend fun updateStatus(id: String, status: PaymentStatus) {
            val current = requireNotNull(payments[id])
            payments[id] = current.copy(status = status)
        }

        override suspend fun getByTransactionId(transactionId: String): Payment? =
            payments.values.firstOrNull { it.transactionId == transactionId }
    }

    private class FakeProductRepository : ProductRepository {
        private val products = mapOf(
            "prod-1" to Product(
                id = "prod-1",
                name = "Coffee",
                price = 15_000L,
                imageUrl = "",
                categoryId = "drink",
            )
        )

        override fun observeActiveProducts(): Flow<List<Product>> = flowOf(products.values.toList())

        override suspend fun getProductById(id: String): Product? = products[id]

        override suspend fun getProductBySlotId(slotId: String): Product? = products["prod-1"]

        override suspend fun upsertAll(products: List<Product>) = Unit

        override suspend fun getChangedSince(timestamp: Long): List<Product> = emptyList()
    }

    private class FakeSyncRepository : SyncRepository {
        override suspend fun enqueue(entityType: String, entityId: String, priority: Int) = Unit

        override suspend fun dequeue(limit: Int): List<SyncTask> = emptyList()

        override suspend fun markCompleted(taskId: String) = Unit

        override suspend fun markFailed(taskId: String, retryAfterMs: Long) = Unit

        override suspend fun getPendingCount(): Int = 0

        override suspend fun resetStuckTasks() = Unit
    }

    private class FakeDispenseJournalRepository : DispenseJournalRepository {
        val entries = mutableMapOf<String, DispenseJournalEntry>()

        override suspend fun create(entry: DispenseJournalEntry) {
            entries[entry.transactionId] = entry
        }

        override suspend fun markSensorTriggered(dispenseId: String) = Unit

        override suspend fun markCompleted(dispenseId: String) = Unit

        override suspend fun getByTransactionId(transactionId: String): DispenseJournalEntry? = entries[transactionId]
    }
}
