package com.avf.vending.repository.impl

import androidx.room.withTransaction
import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.local.database.VendingDatabase
import com.avf.vending.local.database.dao.SyncQueueDao
import com.avf.vending.local.database.dao.TransactionDao
import com.avf.vending.local.database.entity.SyncQueueEntity
import com.avf.vending.repository.mapper.TransactionMapper.toDomain
import com.avf.vending.repository.mapper.TransactionMapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val database: VendingDatabase,
    private val transactionDao: TransactionDao,
    private val syncQueueDao: SyncQueueDao,
) : TransactionRepository {

    override suspend fun insertPending(transaction: Transaction): String {
        transactionDao.insertPending(transaction.toEntity())
        return transaction.id
    }

    /**
     * Outbox pattern: atomically writes the transaction AND its sync-queue entry
     * in a single Room transaction. If the app crashes between the two writes,
     * the DB transaction is rolled back and nothing is lost.
     */
    override suspend fun insertWithOutbox(transaction: Transaction, syncPriority: Int): String {
        database.withTransaction {
            transactionDao.insertPending(transaction.toEntity())
            syncQueueDao.insert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "transaction",
                    entityId = transaction.id,
                    priority = syncPriority,
                    retryCount = 0,
                    status = SyncQueueEntity.STATUS_PENDING,
                    nextRetryAt = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
        return transaction.id
    }

    override suspend fun markSynced(id: String) {
        transactionDao.markSynced(id)
    }

    override suspend fun getById(id: String): Transaction? = transactionDao.getById(id)?.toDomain()

    override fun observeHistory(): Flow<List<Transaction>> =
        transactionDao.observeHistory().map { it.map { e -> e.toDomain() } }

    override suspend fun getPendingSync(): List<Transaction> =
        transactionDao.getPendingSync().map { it.toDomain() }

    override suspend fun updateStatus(id: String, syncStatus: SyncStatus) {
        transactionDao.updateSyncStatus(id, syncStatus.ordinal)
    }

    override suspend fun updateTransactionStatus(id: String, status: TransactionStatus) {
        transactionDao.updateTransactionStatus(id, status.name, System.currentTimeMillis())
    }

    override suspend fun updateDispenseStatus(id: String, dispenseStatus: DispenseStatus) {
        transactionDao.updateDispenseStatus(id, dispenseStatus.name, System.currentTimeMillis())
    }

    override suspend fun findInterruptedTransactions(olderThanMs: Long): List<Transaction> =
        transactionDao.findInterruptedTransactions(olderThanMs).map { it.toDomain() }
}
