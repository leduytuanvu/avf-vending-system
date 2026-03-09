package com.avf.vending.repository.impl

import com.avf.vending.common.time.MonotonicClock
import com.avf.vending.domain.repository.SyncRepository
import com.avf.vending.domain.repository.SyncTask
import com.avf.vending.local.database.dao.SyncQueueDao
import com.avf.vending.local.database.entity.SyncQueueEntity
import java.util.UUID
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val monotonicClock: MonotonicClock,
) : SyncRepository {

    override suspend fun enqueue(entityType: String, entityId: String, priority: Int) {
        syncQueueDao.insert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = entityType,
                entityId = entityId,
                priority = priority,
                retryCount = 0,
                status = SyncQueueEntity.STATUS_PENDING,
                // Use monotonic clock so nextRetryAt is immune to NTP/wall-clock skew
                nextRetryAt = monotonicClock.elapsed(),
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    /**
     * Atomically claims tasks to prevent two concurrent workers from processing
     * the same item. Only tasks that win the tryClaimTask race are returned.
     */
    override suspend fun dequeue(limit: Int): List<SyncTask> {
        val candidates = syncQueueDao.getPendingByPriority(monotonicClock.elapsed(), limit)
        return candidates.mapNotNull { candidate ->
            val claimed = syncQueueDao.tryClaimTask(candidate.id)
            if (claimed > 0) SyncTask(candidate.id, candidate.entityType, candidate.entityId, candidate.priority, candidate.retryCount)
            else null
        }
    }

    override suspend fun markCompleted(taskId: String) {
        syncQueueDao.delete(taskId)
    }

    override suspend fun markFailed(taskId: String, retryAfterMs: Long) {
        // Schedule retry relative to monotonic elapsed time — immune to clock skew
        syncQueueDao.markFailed(taskId, monotonicClock.elapsed() + retryAfterMs)
    }

    override suspend fun getPendingCount(): Int = syncQueueDao.count()

    override suspend fun resetStuckTasks() {
        syncQueueDao.resetStuckProcessing()
        // Reset nextRetryAt for all PENDING tasks to 0 so they run immediately
        // after a reboot (monotonic clock restarts at 0, old stored values are invalid)
        syncQueueDao.resetPendingRetryAt()
    }
}
