package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: SyncQueueEntity): Long

    /** Returns candidates that are PENDING and eligible for retry, ordered by priority. */
    @Query("""
        SELECT * FROM sync_queue
        WHERE status = 'PENDING' AND nextRetryAt <= :now
        ORDER BY priority DESC, createdAt ASC
        LIMIT :limit
    """)
    suspend fun getPendingByPriority(now: Long, limit: Int): List<SyncQueueEntity>

    /**
     * Atomic claim: sets status to PROCESSING only if it is still PENDING.
     * Returns the number of rows updated (1 = claimed, 0 = already taken).
     */
    @Query("UPDATE sync_queue SET status = 'PROCESSING' WHERE id = :id AND status = 'PENDING'")
    suspend fun tryClaimTask(id: String): Int

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("""
        UPDATE sync_queue
        SET status = 'PENDING', retryCount = retryCount + 1, nextRetryAt = :nextRetryAt
        WHERE id = :id
    """)
    suspend fun markFailed(id: String, nextRetryAt: Long): Int

    /** Resets PROCESSING tasks back to PENDING (e.g. after a crash restart). */
    @Query("UPDATE sync_queue SET status = 'PENDING' WHERE status = 'PROCESSING'")
    suspend fun resetStuckProcessing(): Int

    /**
     * Called on startup after switching to monotonic clock.
     * Stored nextRetryAt values from a previous boot session are invalid because
     * SystemClock.elapsedRealtime() resets to 0 on each reboot.
     * Setting to 0 makes all pending tasks immediately eligible for the next sync pass.
     */
    @Query("UPDATE sync_queue SET nextRetryAt = 0 WHERE status = 'PENDING'")
    suspend fun resetPendingRetryAt(): Int

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    suspend fun count(): Int
}
