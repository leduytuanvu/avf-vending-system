package com.avf.vending.domain.repository

interface SyncRepository {
    suspend fun enqueue(entityType: String, entityId: String, priority: Int = 0)
    suspend fun dequeue(limit: Int = 10): List<SyncTask>
    suspend fun markCompleted(taskId: String)
    suspend fun markFailed(taskId: String, retryAfterMs: Long = 60_000)
    suspend fun getPendingCount(): Int
    /** Resets PROCESSING tasks back to PENDING after a crash/restart. */
    suspend fun resetStuckTasks()
}

data class SyncTask(
    val id: String,
    val entityType: String,
    val entityId: String,
    val priority: Int,
    val retryCount: Int,
)
