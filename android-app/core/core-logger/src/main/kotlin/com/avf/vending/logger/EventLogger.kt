package com.avf.vending.logger

import com.avf.vending.domain.model.EventLog
import com.avf.vending.domain.repository.ObservabilityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records user-facing actions into an in-memory ring buffer for breadcrumb capture,
 * and asynchronously persists them to the local database for long-term audit.
 *
 * Thread-safe. Never throws — all DB errors are silently swallowed so this
 * class can never crash the application.
 */
@Singleton
class EventLogger @Inject constructor(
    private val observabilityRepository: ObservabilityRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Ring buffer: bounded in-memory breadcrumb store
    private val buffer = ArrayDeque<EventLog>(BUFFER_CAPACITY)

    fun log(screen: String, action: String, traceId: String? = null, metadata: String = "") {
        val event = EventLog(
            id = UUID.randomUUID().toString(),
            traceId = traceId,
            screen = screen,
            action = action,
            metadata = metadata,
            timestamp = System.currentTimeMillis(),
        )
        synchronized(buffer) {
            if (buffer.size >= BUFFER_CAPACITY) buffer.removeFirst()
            buffer.addLast(event)
        }
        scope.launch {
            try { observabilityRepository.insertEvent(event) } catch (_: Exception) {}
        }
    }

    /** Returns the most recent [count] events as breadcrumb strings, oldest first. */
    fun recentBreadcrumbs(count: Int = 50): List<String> = synchronized(buffer) {
        buffer.takeLast(count).map { "[${it.screen}] ${it.action}" }
    }

    /** Returns the last event's traceId for attaching to an ErrorReport. */
    fun currentTraceId(): String? = synchronized(buffer) { buffer.lastOrNull()?.traceId }

    companion object {
        private const val BUFFER_CAPACITY = 200
    }
}
