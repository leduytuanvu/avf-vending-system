package com.avf.vending.sync

import com.avf.vending.domain.repository.ObservabilityRepository
import com.avf.vending.domain.repository.SyncTask
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.remote.api.TelemetryApiService
import com.avf.vending.remote.api.TransactionApiService
import com.avf.vending.remote.circuit.CircuitBreaker
import com.avf.vending.remote.dto.ErrorLogBatchDto
import com.avf.vending.remote.dto.ErrorLogDto
import com.avf.vending.remote.dto.EventLogBatchDto
import com.avf.vending.remote.dto.EventLogDto
import com.avf.vending.remote.dto.TransactionDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

@Singleton
class SyncEngine @Inject constructor(
    private val syncQueue: SyncQueue,
    private val networkMonitor: NetworkMonitor,
    private val transactionApi: TransactionApiService,
    private val telemetryApi: TelemetryApiService,
    private val transactionRepository: TransactionRepository,
    private val observabilityRepository: ObservabilityRepository,
    private val circuitBreaker: CircuitBreaker,
) {
    private val syncMutex = Mutex()

    @Volatile private var started = false

    /**
     * Called ONCE from the app lifecycle to subscribe the long-lived online→sync observer.
     * Idempotent: subsequent calls are no-ops.
     */
    fun start(scope: CoroutineScope) {
        if (started) return
        started = true
        networkMonitor.isOnline
            .onEach { online -> if (online) processPendingTasks() }
            .launchIn(scope)
    }

    /** Called by WorkManager workers and ConnectivityAwareSyncManager on reconnect. */
    suspend fun runOnce() = processPendingTasks()

    private suspend fun processPendingTasks() {
        if (!syncMutex.tryLock()) return
        try {
            // ── Observability: upload events + prune retention (once per sync run) ─────
            try {
                uploadEventLogs()
                observabilityRepository.pruneEventsOlderThan(
                    System.currentTimeMillis() - EVENT_LOG_RETENTION_MS,
                )
            } catch (_: Exception) {
                // Non-fatal; sync continues
            }

            var processed = 0
            do {
                val tasks = syncQueue.dequeue(limit = BATCH_SIZE)
                if (tasks.isEmpty()) break

                // ── Batch: all transaction tasks → one API call ──────────────────────
                val transactionTasks = tasks.filter { it.entityType == "transaction" }
                if (transactionTasks.isNotEmpty()) {
                    processTransactionBatch(transactionTasks)
                }

                // ── Batch: all error_log tasks → one uploadErrorLogs call ─────────────
                val errorLogTasks = tasks.filter { it.entityType == "error_log" }
                if (errorLogTasks.isNotEmpty()) {
                    processErrorLogBatch(errorLogTasks)
                }

                // ── Individual: all other entity types ───────────────────────────────
                tasks.filter { it.entityType !in listOf("transaction", "error_log") }
                    .forEach { task -> processSingleTask(task) }

                processed = tasks.size
            } while (processed == BATCH_SIZE)
        } finally {
            syncMutex.unlock()
        }
    }

    /**
     * Fetches all transactions for the given tasks, uploads them in a single batch call,
     * then marks each task completed or failed appropriately.
     */
    private suspend fun processTransactionBatch(tasks: List<SyncTask>) {
        // Build DTO list; skip tasks whose transaction is already gone from DB
        data class TaskDto(val taskId: String, val retryCount: Int, val dto: TransactionDto)

        val taskDtos = tasks.mapNotNull { task ->
            transactionRepository.getById(task.entityId)?.let { tx ->
                TaskDto(
                    taskId = task.id,
                    retryCount = task.retryCount,
                    dto = TransactionDto(
                        id = tx.id,
                        slotId = tx.slotId,
                        productId = tx.productId,
                        amount = tx.amount,
                        paymentMethod = tx.paymentMethod.name,
                        status = tx.status.name,
                        createdAt = tx.createdAt,
                        traceId = tx.traceId,
                        idempotencyKey = tx.idempotencyKey,
                        machineId = tx.machineId,
                    )
                )
            }
        }

        // Tasks whose entity no longer exists — already gone, mark completed
        val foundTaskIds = taskDtos.map { it.taskId }.toSet()
        tasks.filter { it.id !in foundTaskIds }.forEach { syncQueue.markCompleted(it.id) }

        if (taskDtos.isEmpty()) return

        try {
            withCircuitBreaker {
                // ONE API call for the entire batch — idempotencyKey deduplicates on the server
                transactionApi.uploadBatch(taskDtos.map { it.dto })
            }
            taskDtos.forEach { (taskId, _, dto) ->
                transactionRepository.markSynced(dto.id)
                syncQueue.markCompleted(taskId)
            }
        } catch (e: Exception) {
            // If the whole batch is rejected with a non-retriable status, dead-letter all
            val retriable = isRetriable(e)
            taskDtos.forEach { (taskId, retryCount, _) ->
                when {
                    !retriable || retryCount >= MAX_RETRIES -> syncQueue.markCompleted(taskId)
                    else -> syncQueue.markFailed(taskId, exponentialBackoffMs(retryCount))
                }
            }
        }
    }

    /**
     * Processes all error_log tasks with a single uploadErrorLogs() call.
     * Marks all tasks completed on success; on failure, marks failed with backoff or dead-letters.
     */
    private suspend fun processErrorLogBatch(tasks: List<SyncTask>) {
        try {
            uploadErrorLogs()
            tasks.forEach { syncQueue.markCompleted(it.id) }
        } catch (e: Exception) {
            val retriable = isRetriable(e)
            tasks.forEach { task ->
                when {
                    !retriable || task.retryCount >= MAX_RETRIES -> syncQueue.markCompleted(task.id)
                    else -> syncQueue.markFailed(task.id, exponentialBackoffMs(task.retryCount))
                }
            }
        }
    }

    private suspend fun processSingleTask(task: SyncTask) {
        try {
            // Future entity types (e.g. slot_stock) would be handled here
            syncQueue.markCompleted(task.id)
        } catch (e: Exception) {
            if (!isRetriable(e) || task.retryCount >= MAX_RETRIES) {
                syncQueue.markCompleted(task.id) // dead-letter
            } else {
                scheduleFailed(task)
            }
        }
    }

    private suspend fun scheduleFailed(task: SyncTask) =
        syncQueue.markFailed(task.id, exponentialBackoffMs(task.retryCount))

    private suspend fun uploadErrorLogs(): Boolean {
        val errors = observabilityRepository.getUnSyncedErrors(limit = 50)
        if (errors.isEmpty()) return true
        withCircuitBreaker {
            telemetryApi.sendErrors(
                ErrorLogBatchDto(
                    errors = errors.map { e ->
                        ErrorLogDto(
                            id = e.id,
                            traceId = e.traceId,
                            tag = e.tag,
                            message = e.message,
                            stackTrace = e.stackTrace,
                            breadcrumbs = e.breadcrumbs,
                            extras = e.extras,
                            timestamp = e.timestamp,
                        )
                    }
                )
            )
        }
        errors.forEach { observabilityRepository.markErrorSynced(it.id) }
        return true
    }

    private suspend fun uploadEventLogs() {
        val events = observabilityRepository.getUnSyncedEvents(limit = 50)
        if (events.isEmpty()) return
        withCircuitBreaker {
            telemetryApi.sendEvents(
                EventLogBatchDto(
                    events = events.map { e ->
                        EventLogDto(
                            id = e.id,
                            traceId = e.traceId,
                            screen = e.screen,
                            action = e.action,
                            metadata = e.metadata,
                            timestamp = e.timestamp,
                        )
                    },
                ),
            )
        }
        events.forEach { observabilityRepository.markEventSynced(it.id) }
    }

    private suspend fun <T> withCircuitBreaker(block: suspend () -> T): T {
        check(circuitBreaker.isAllowed()) { "Sync circuit breaker is open" }
        return try {
            block().also { circuitBreaker.recordSuccess() }
        } catch (e: Exception) {
            circuitBreaker.recordFailure()
            throw e
        }
    }

    companion object {
        private const val BATCH_SIZE = 20
        private const val MAX_RETRIES = 5
        private const val EVENT_LOG_RETENTION_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
        private const val BASE_BACKOFF_MS = 1_000L
        private const val MAX_BACKOFF_MS = 10 * 60 * 1_000L // 10 minutes

        /**
         * HTTP 4xx errors are client-side bugs (bad data, auth) — retrying will never help.
         * Network errors and 5xx are transient and should be retried with backoff.
         */
        fun isRetriable(e: Exception): Boolean =
            e !is HttpException || e.code() !in 400..499

        fun exponentialBackoffMs(retryCount: Int): Long =
            min(BASE_BACKOFF_MS * 2.0.pow(retryCount).toLong(), MAX_BACKOFF_MS)
    }
}
