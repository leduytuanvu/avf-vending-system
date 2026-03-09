package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.ErrorLogEntity

@Dao
interface ErrorLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(errorLog: ErrorLogEntity): Long

    @Query("SELECT * FROM error_logs WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<ErrorLogEntity>

    @Query("UPDATE error_logs SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String): Int

    /** Prune old synced entries to keep storage bounded (keep last 500 unsynced max). */
    @Query("DELETE FROM error_logs WHERE synced = 1 AND timestamp < :olderThan")
    suspend fun deleteSyncedBefore(olderThan: Long): Int
}
