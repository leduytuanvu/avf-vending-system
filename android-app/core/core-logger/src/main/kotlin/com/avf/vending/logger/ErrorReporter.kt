package com.avf.vending.logger

import com.avf.vending.domain.model.ErrorLog
import com.avf.vending.domain.repository.ObservabilityRepository
import com.avf.vending.domain.repository.SyncPriority
import com.avf.vending.domain.repository.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise error reporting pipeline.
 *
 * Flow:
 *   Exception → collect device context + breadcrumbs → save to local DB →
 *   enqueue sync task → SyncEngine uploads when online
 *
 * Guarantees:
 * - Non-blocking: always dispatched on IO
 * - Fail-safe: any internal exception is caught and ignored
 * - Never recursive-crash: errors in reporter itself are swallowed
 */
@Singleton
class ErrorReporter @Inject constructor(
    private val eventLogger: EventLogger,
    private val deviceContextProvider: DeviceContextProvider,
    private val observabilityRepository: ObservabilityRepository,
    private val syncRepository: SyncRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Reports an error with full context. Safe to call from any thread.
     *
     * @param error     The throwable to report.
     * @param traceId   Business trace ID to correlate with a transaction/payment flow.
     * @param tag       Optional module/class label (e.g. "PaymentOrchestrator").
     * @param extras    Caller-supplied key-value pairs (e.g. transactionId, machineId).
     *                  These are merged with auto-collected device context; caller values win
     *                  on key collision so they can override defaults.
     */
    fun report(
        error: Throwable,
        traceId: String? = null,
        tag: String? = null,
        extras: Map<String, String> = emptyMap(),
    ) {
        scope.launch {
            try {
                val resolvedTraceId = traceId ?: eventLogger.currentTraceId()
                // Device context is merged first; caller extras override any matching keys
                val fullExtras = deviceContextProvider.collect() + extras
                val errorLog = ErrorLog(
                    id = UUID.randomUUID().toString(),
                    traceId = resolvedTraceId,
                    tag = tag,
                    message = error.message,
                    stackTrace = error.stackTraceToString(),
                    breadcrumbs = eventLogger.recentBreadcrumbs(),
                    extras = fullExtras,
                    timestamp = System.currentTimeMillis(),
                )
                observabilityRepository.insertError(errorLog)
                syncRepository.enqueue("error_log", errorLog.id, SyncPriority.HIGH)
            } catch (_: Exception) {
                // Never crash the app from within the reporter
            }
        }
    }
}
