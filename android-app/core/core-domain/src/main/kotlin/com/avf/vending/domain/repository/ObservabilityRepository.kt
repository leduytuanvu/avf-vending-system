package com.avf.vending.domain.repository

import com.avf.vending.domain.model.ErrorLog
import com.avf.vending.domain.model.EventLog

interface ObservabilityRepository {
    suspend fun insertError(errorLog: ErrorLog)
    suspend fun insertEvent(eventLog: EventLog)
    suspend fun getUnSyncedErrors(limit: Int = 50): List<ErrorLog>
    suspend fun markErrorSynced(id: String)
    suspend fun getUnSyncedEvents(limit: Int = 50): List<EventLog>
    suspend fun markEventSynced(id: String)
    suspend fun pruneEventsOlderThan(olderThanMs: Long)
}
