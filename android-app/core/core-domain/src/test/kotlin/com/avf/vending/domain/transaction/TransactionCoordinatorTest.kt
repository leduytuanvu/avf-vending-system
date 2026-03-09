package com.avf.vending.domain.transaction

import com.avf.vending.domain.model.DispenseOutcome
import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.model.Product
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.PaymentRepository
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.SyncRepository
import com.avf.vending.domain.repository.SyncTask
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.domain.usecase.UpdateStockUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TransactionCoordinatorTest {
    private val transactionRepository = FakeTransactionRepository()
    private val paymentRepository = FakePaymentRepository()
    private val productRepository = FakeProductRepository()
    private val syncRepository = FakeSyncRepository()
    private val coordinator = TransactionCoordinator(
        transactionRepository = transactionRepository,
        paymentRepository = paymentRepository,
        productRepository = productRepository,
        updateStockUseCase = UpdateStockUseCase(syncRepository),
        stateMachine = TransactionStateMachine(),
    )

    @Test
    fun `authorizePayment persists transaction before completed payment`() = runTest {
        val authorized = coordinator.authorizePayment(
            slotId = "A1",
            productId = "prod-1",
            amount = 15_000L,
            paymentMethod = PaymentMethod.CASH,
            change = 2_000L,
        )

        val transaction = transactionRepository.getById(authorized.transactionId)
        val payment = paymentRepository.getById(authorized.paymentId)

        assertNotNull(transaction)
        assertNotNull(payment)
        assertEquals(TransactionStatus.PAYMENT_SUCCESS, transaction?.status)
        assertEquals(PaymentStatus.COMPLETED, payment?.status)
        assertEquals(authorized.transactionId, payment?.transactionId)
    }

    @Test
    fun `authorizePayment marks transaction failed when payment persistence fails`() = runTest {
        paymentRepository.throwOnInsert = true

        var thrown: IllegalStateException? = null
        try {
            coordinator.authorizePayment(
                slotId = "A1",
                productId = "prod-1",
                amount = 15_000L,
                paymentMethod = PaymentMethod.CASH,
                change = 0L,
            )
        } catch (e: IllegalStateException) {
            thrown = e
        }

        assertNotNull(thrown)
        val failedTransaction = transactionRepository.transactions.values.single()
        assertEquals(TransactionStatus.FAILED, failedTransaction.status)
    }

    @Test
    fun `markDispensing and record success complete the transaction and enqueue stock sync`() = runTest {
        val authorized = coordinator.authorizePayment(
            slotId = "A1",
            productId = "prod-1",
            amount = 15_000L,
            paymentMethod = PaymentMethod.CASH,
            change = 0L,
        )

        coordinator.markDispensing(authorized.transactionId)
        coordinator.recordDispenseOutcome(
            authorized.transactionId,
            DispenseOutcome.Success(slotId = "A1"),
        )

        val completedTransaction = transactionRepository.getById(authorized.transactionId)
        assertEquals(TransactionStatus.COMPLETED, completedTransaction?.status)
        assertEquals(DispenseStatus.SUCCESS, completedTransaction?.dispenseStatus)
        assertEquals(listOf(Triple("slot_stock", "A1", 1)), syncRepository.enqueued)
    }

    @Test
    fun `recover interrupted transaction marks completed when delivery is confirmed`() = runTest {
        val authorized = coordinator.authorizePayment(
            slotId = "A1",
            productId = "prod-1",
            amount = 15_000L,
            paymentMethod = PaymentMethod.CASH,
            change = 0L,
        )
        coordinator.markDispensing(authorized.transactionId)

        coordinator.recoverInterruptedTransaction(
            transactionId = authorized.transactionId,
            delivered = true,
        )

        val reconciled = transactionRepository.getById(authorized.transactionId)
        assertEquals(TransactionStatus.COMPLETED, reconciled?.status)
        assertEquals(DispenseStatus.SUCCESS, reconciled?.dispenseStatus)
    }

    @Test
    fun `recover interrupted transaction marks refund required when delivery is not confirmed`() = runTest {
        val authorized = coordinator.authorizePayment(
            slotId = "A1",
            productId = "prod-1",
            amount = 15_000L,
            paymentMethod = PaymentMethod.CASH,
            change = 0L,
        )
        coordinator.markDispensing(authorized.transactionId)

        coordinator.recoverInterruptedTransaction(
            transactionId = authorized.transactionId,
            delivered = false,
        )

        val reconciled = transactionRepository.getById(authorized.transactionId)
        assertEquals(TransactionStatus.REFUND_REQUIRED, reconciled?.status)
        assertEquals(DispenseStatus.FAILED, reconciled?.dispenseStatus)
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
            transactions.values.filter { it.createdAt <= olderThanMs }
    }

    private class FakePaymentRepository : PaymentRepository {
        val payments = linkedMapOf<String, Payment>()
        var throwOnInsert = false

        override suspend fun insert(payment: Payment): String {
            if (throwOnInsert) error("insert failed")
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
        val enqueued = mutableListOf<Triple<String, String, Int>>()

        override suspend fun enqueue(entityType: String, entityId: String, priority: Int) {
            enqueued += Triple(entityType, entityId, priority)
        }

        override suspend fun dequeue(limit: Int): List<SyncTask> = emptyList()

        override suspend fun markCompleted(taskId: String) = Unit

        override suspend fun markFailed(taskId: String, retryAfterMs: Long) = Unit

        override suspend fun getPendingCount(): Int = enqueued.size

        override suspend fun resetStuckTasks() = Unit
    }
}
