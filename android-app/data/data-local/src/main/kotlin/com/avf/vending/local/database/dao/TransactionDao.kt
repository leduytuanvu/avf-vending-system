package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET syncStatus = 1 WHERE id = :id")
    suspend fun markSynced(id: String): Int

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeHistory(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE syncStatus = 0")
    suspend fun getPendingSync(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: Int): Int

    @Query("UPDATE transactions SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTransactionStatus(id: String, status: String, updatedAt: Long): Int

    @Query("UPDATE transactions SET dispenseStatus = :dispenseStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDispenseStatus(id: String, dispenseStatus: String, updatedAt: Long): Int

    @Query("""
        SELECT * FROM transactions
        WHERE status IN ('PAYMENT_SUCCESS', 'DISPENSING')
        AND updatedAt < :olderThanMs
    """)
    suspend fun findInterruptedTransactions(olderThanMs: Long): List<TransactionEntity>
}