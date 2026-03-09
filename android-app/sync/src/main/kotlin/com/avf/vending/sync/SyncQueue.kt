package com.avf.vending.sync

import com.avf.vending.domain.repository.SyncRepository
import com.avf.vending.domain.repository.SyncTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncQueue @Inject constructor(
    private val syncRepository: SyncRepository,
) {
    suspend fun enqueue(entityType: String, entityId: String, priority: Int = 0) =
        syncRepository.enqueue(entityType, entityId, priority)

    suspend fun dequeue(limit: Int = 10): List<SyncTask> = syncRepository.dequeue(limit)

    suspend fun markCompleted(taskId: String) = syncRepository.markCompleted(taskId)

    suspend fun markFailed(taskId: String, retryAfterMs: Long = 60_000) =
        syncRepository.markFailed(taskId, retryAfterMs)

    suspend fun pendingCount(): Int = syncRepository.getPendingCount()
}
