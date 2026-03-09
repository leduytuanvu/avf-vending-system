package com.avf.vending.domain.repository

import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insertPending(transaction: Transaction): String
    /** Atomically inserts the transaction and a matching sync-queue outbox entry. */
    suspend fun insertWithOutbox(transaction: Transaction, syncPriority: Int = SyncPriority.CRITICAL): String
    suspend fun markSynced(id: String)
    suspend fun getById(id: String): Transaction?
    fun observeHistory(): Flow<List<Transaction>>
    suspend fun getPendingSync(): List<Transaction>
    suspend fun updateStatus(id: String, syncStatus: SyncStatus)
    suspend fun updateTransactionStatus(id: String, status: TransactionStatus)
    suspend fun updateDispenseStatus(id: String, dispenseStatus: DispenseStatus)
    /** Returns transactions stuck in PAYMENT_SUCCESS or DISPENSING, older than [olderThanMs]. */
    suspend fun findInterruptedTransactions(olderThanMs: Long): List<Transaction>
}
