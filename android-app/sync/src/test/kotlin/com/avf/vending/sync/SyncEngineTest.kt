package com.avf.vending.sync

import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.ErrorLog
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.ObservabilityRepository
import com.avf.vending.domain.repository.SyncRepository
import com.avf.vending.domain.repository.SyncTask
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.remote.api.TelemetryApiService
import com.avf.vending.remote.api.TransactionApiService
import com.avf.vending.remote.circuit.CircuitBreaker
import com.avf.vending.remote.circuit.CircuitBreakerConfig
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SyncEngineTest {
    @Test
    fun `runOnce uploads pending transaction batch and marks tasks complete`() = runTest {
        val syncRepository = FakeSyncRepository(
            initialTasks = mutableListOf(
                SyncTask(
                    id = "task-1",
                    entityType = "transaction",
                    entityId = "tx-1",
                    priority = 1,
                    retryCount = 0,
                )
            )
        )
        val transactionRepository = FakeTransactionRepository().apply {
            transactions["tx-1"] = transaction(id = "tx-1")
        }
        val transactionApi = FakeTransactionApiService()
        val engine = createEngine(
            syncRepository = syncRepository,
            transactionRepository = transactionRepository,
            transactionApi = transactionApi,
        )

        engine.runOnce()

        assertEquals(listOf("tx-1"), transactionApi.uploadedTransactionIds)
        assertEquals(listOf("task-1"), syncRepository.completedTaskIds)
        assertEquals(SyncStatus.SYNCED, transactionRepository.transactions.getValue("tx-1").syncStatus)
    }

    @Test
    fun `runOnce consolidates multiple error_log tasks into one sendErrors call`() = runTest {
        val telemetryApi = FakeTelemetryApiService()
        val observabilityRepo = FakeObservabilityRepository().apply {
            unsyncedErrors = mutableListOf(
                ErrorLog("err-1", null, "Tag", "msg", "stack", emptyList(), emptyMap(), 1L),
            )
        }
        val syncRepository = FakeSyncRepository(
            initialTasks = mutableListOf(
                SyncTask("task-1", "error_log", "err-1", 1, 0),
                SyncTask("task-2", "error_log", "err-1", 1, 0),
                SyncTask("task-3", "error_log", "err-1", 1, 0),
            ),
        )
        val engine = createEngine(
            syncRepository = syncRepository,
            transactionRepository = FakeTransactionRepository(),
            transactionApi = FakeTransactionApiService(),
            telemetryApi = telemetryApi,
            observabilityRepository = observabilityRepo,
        )

        engine.runOnce()

        assertEquals(1, telemetryApi.sendErrorsCallCount)
        assertEquals(listOf("task-1", "task-2", "task-3"), syncRepository.completedTaskIds)
    }

    @Test
    fun `runOnce skips network call when circuit breaker is open`() = runTest {
        val syncRepository = FakeSyncRepository(
            initialTasks = mutableListOf(
                SyncTask("task-1", "transaction", "tx-1", 1, 0),
                SyncTask("task-2", "transaction", "tx-2", 1, 0),
            )
        )
        val transactionRepository = FakeTransactionRepository().apply {
            transactions["tx-1"] = transaction(id = "tx-1")
            transactions["tx-2"] = transaction(id = "tx-2")
        }
        val transactionApi = FakeTransactionApiService().apply {
            throwOnUpload = IOException("network down")
        }
        val breaker = CircuitBreaker(CircuitBreakerConfig(failureThreshold = 1, resetTimeMs = 60_000L))
        val engine = createEngine(
            syncRepository = syncRepository,
            transactionRepository = transactionRepository,
            transactionApi = transactionApi,
            breaker = breaker,
        )

        engine.runOnce()
        engine.runOnce()

        assertEquals(1, transactionApi.callCount)
        assertTrue(syncRepository.failedTaskIds.containsAll(listOf("task-1", "task-2")))
        assertEquals(CircuitBreaker.State.OPEN, breaker.currentState())
    }

    private fun createEngine(
        syncRepository: FakeSyncRepository,
        transactionRepository: FakeTransactionRepository,
        transactionApi: FakeTransactionApiService,
        telemetryApi: TelemetryApiService = FakeTelemetryApiService(),
        observabilityRepository: ObservabilityRepository = FakeObservabilityRepository(),
        breaker: CircuitBreaker = CircuitBreaker(CircuitBreakerConfig()),
    ): SyncEngine = SyncEngine(
        syncQueue = SyncQueue(syncRepository),
        networkMonitor = mockk(relaxed = true),
        transactionApi = transactionApi,
        telemetryApi = telemetryApi,
        transactionRepository = transactionRepository,
        observabilityRepository = observabilityRepository,
        circuitBreaker = breaker,
    )

    private fun transaction(id: String) = Transaction(
        id = id,
        slotId = "A1",
        productId = "prod-1",
        amount = 15_000L,
        paymentMethod = PaymentMethod.CASH,
        status = TransactionStatus.PAYMENT_SUCCESS,
        dispenseStatus = DispenseStatus.NOT_STARTED,
        createdAt = 1L,
        syncStatus = SyncStatus.PENDING,
        traceId = id,
        idempotencyKey = id,
        machineId = "machine-1",
    )

    private class FakeTransactionApiService : TransactionApiService {
        var callCount = 0
        var throwOnUpload: Exception? = null
        val uploadedTransactionIds = mutableListOf<String>()

        override suspend fun uploadBatch(transactions: List<com.avf.vending.remote.dto.TransactionDto>) {
            callCount++
            throwOnUpload?.let { throw it }
            uploadedTransactionIds += transactions.map { it.id }
        }
    }

    private class FakeTelemetryApiService : TelemetryApiService {
        var sendErrorsCallCount = 0
        var sendEventsCallCount = 0

        override suspend fun sendErrors(payload: com.avf.vending.remote.dto.ErrorLogBatchDto) {
            sendErrorsCallCount++
        }

        override suspend fun sendEvents(payload: com.avf.vending.remote.dto.EventLogBatchDto) {
            sendEventsCallCount++
        }

        override suspend fun sendEvent(payload: Map<String, String>) = Unit
    }

    private class FakeTransactionRepository : TransactionRepository {
        val transactions = linkedMapOf<String, Transaction>()

        override suspend fun insertPending(transaction: Transaction): String = transaction.id

        override suspend fun insertWithOutbox(transaction: Transaction, syncPriority: Int): String = transaction.id

        override suspend fun markSynced(id: String) {
            val current = requireNotNull(transactions[id])
            transactions[id] = current.copy(syncStatus = SyncStatus.SYNCED)
        }

        override suspend fun getById(id: String): Transaction? = transactions[id]

        override fun observeHistory(): Flow<List<Transaction>> = flowOf(transactions.values.toList())

        override suspend fun getPendingSync(): List<Transaction> = transactions.values.toList()

        override suspend fun updateStatus(id: String, syncStatus: SyncStatus) {
            val current = requireNotNull(transactions[id])
            transactions[id] = current.copy(syncStatus = syncStatus)
        }

        override suspend fun updateTransactionStatus(id: String, status: TransactionStatus) = Unit

        override suspend fun updateDispenseStatus(id: String, dispenseStatus: DispenseStatus) = Unit

        override suspend fun findInterruptedTransactions(olderThanMs: Long): List<Transaction> = emptyList()
    }

    private class FakeObservabilityRepository : ObservabilityRepository {
        var unsyncedErrors = mutableListOf<ErrorLog>()
        var unsyncedEvents = mutableListOf<com.avf.vending.domain.model.EventLog>()
        var pruneEventsOlderThanCalled = false

        override suspend fun insertError(errorLog: ErrorLog) = Unit

        override suspend fun insertEvent(eventLog: com.avf.vending.domain.model.EventLog) = Unit

        override suspend fun getUnSyncedErrors(limit: Int): List<ErrorLog> = unsyncedErrors.take(limit)

        override suspend fun markErrorSynced(id: String) {
            unsyncedErrors.removeAll { it.id == id }
        }

        override suspend fun getUnSyncedEvents(limit: Int): List<com.avf.vending.domain.model.EventLog> =
            unsyncedEvents.take(limit)

        override suspend fun markEventSynced(id: String) {
            unsyncedEvents.removeAll { it.id == id }
        }

        override suspend fun pruneEventsOlderThan(olderThanMs: Long) {
            pruneEventsOlderThanCalled = true
        }
    }

    private class FakeSyncRepository(
        private val initialTasks: MutableList<SyncTask>,
    ) : SyncRepository {
        val completedTaskIds = mutableListOf<String>()
        val failedTaskIds = mutableListOf<String>()

        override suspend fun enqueue(entityType: String, entityId: String, priority: Int) = Unit

        override suspend fun dequeue(limit: Int): List<SyncTask> {
            val batch = initialTasks.take(limit)
            initialTasks.removeAll(batch.toSet())
            return batch
        }

        override suspend fun markCompleted(taskId: String) {
            completedTaskIds += taskId
        }

        override suspend fun markFailed(taskId: String, retryAfterMs: Long) {
            failedTaskIds += taskId
        }

        override suspend fun getPendingCount(): Int = initialTasks.size

        override suspend fun resetStuckTasks() = Unit
    }
}
