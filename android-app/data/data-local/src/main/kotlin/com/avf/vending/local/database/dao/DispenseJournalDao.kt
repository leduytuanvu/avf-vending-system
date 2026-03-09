package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.DispenseJournalEntity

@Dao
interface DispenseJournalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: DispenseJournalEntity): Long

    @Query("UPDATE dispense_journal SET sensorTriggered = 1 WHERE dispenseId = :dispenseId")
    suspend fun markSensorTriggered(dispenseId: String): Int

    @Query("UPDATE dispense_journal SET completed = 1 WHERE dispenseId = :dispenseId")
    suspend fun markCompleted(dispenseId: String): Int

    /** Returns the most recent journal entry for a transaction. */
    @Query("""
        SELECT * FROM dispense_journal
        WHERE transactionId = :transactionId
        ORDER BY createdAt DESC
        LIMIT 1
    """)
    suspend fun getByTransactionId(transactionId: String): DispenseJournalEntity?
}
