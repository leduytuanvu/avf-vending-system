package com.avf.vending.repository.impl

import com.avf.vending.domain.model.ErrorLog
import com.avf.vending.domain.model.EventLog
import com.avf.vending.domain.repository.ObservabilityRepository
import com.avf.vending.local.database.dao.ErrorLogDao
import com.avf.vending.local.database.dao.EventLogDao
import com.avf.vending.repository.mapper.ErrorLogMapper.toDomain
import com.avf.vending.repository.mapper.ErrorLogMapper.toEntity
import javax.inject.Inject

class ObservabilityRepositoryImpl @Inject constructor(
    private val errorLogDao: ErrorLogDao,
    private val eventLogDao: EventLogDao,
) : ObservabilityRepository {

    override suspend fun insertError(errorLog: ErrorLog) {
        errorLogDao.insert(errorLog.toEntity())
    }

    override suspend fun insertEvent(eventLog: EventLog) {
        eventLogDao.insert(eventLog.toEntity())
    }

    override suspend fun getUnSyncedErrors(limit: Int): List<ErrorLog> =
        errorLogDao.getUnsynced(limit).map { it.toDomain() }

    override suspend fun markErrorSynced(id: String) {
        errorLogDao.markSynced(id)
    }

    override suspend fun getUnSyncedEvents(limit: Int): List<EventLog> =
        eventLogDao.getUnsynced(limit).map { it.toDomain() }

    override suspend fun markEventSynced(id: String) {
        eventLogDao.markSynced(id)
    }

    override suspend fun pruneEventsOlderThan(olderThanMs: Long) {
        eventLogDao.deleteSyncedBefore(olderThanMs)
    }
}
