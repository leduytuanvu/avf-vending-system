package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.EventLogEntity

@Dao
interface EventLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventLog: EventLogEntity): Long

    @Query("SELECT * FROM event_logs WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<EventLogEntity>

    @Query("UPDATE event_logs SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String): Int

    @Query("DELETE FROM event_logs WHERE timestamp < :olderThan")
    suspend fun deleteBefore(olderThan: Long): Int

    /** Prune only synced events older than threshold to avoid losing unsynced data. */
    @Query("DELETE FROM event_logs WHERE synced = 1 AND timestamp < :olderThan")
    suspend fun deleteSyncedBefore(olderThan: Long): Int

    @Query("SELECT COUNT(*) FROM event_logs")
    suspend fun count(): Int
}