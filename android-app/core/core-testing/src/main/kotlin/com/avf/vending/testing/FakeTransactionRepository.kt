package com.avf.vending.testing

import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class FakeTransactionRepository : TransactionRepository {
    private val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    override suspend fun insertPending(transaction: Transaction): String {
        val id = transaction.id.ifBlank { UUID.randomUUID().toString() }
        transactions.value = transactions.value + transaction.copy()
        return id
    }

    override suspend fun insertWithOutbox(transaction: Transaction, syncPriority: Int): String =
        insertPending(transaction)

    override suspend fun markSynced(id: String) {
        transactions.value = transactions.value.map {
            if (it.id == id) it.copy(syncStatus = SyncStatus.SYNCED) else it
        }
    }

    override suspend fun getById(id: String) = transactions.value.find { it.id == id }
    override fun observeHistory(): Flow<List<Transaction>> = transactions
    override suspend fun getPendingSync() = transactions.value.filter { it.syncStatus == SyncStatus.PENDING }
    override suspend fun updateStatus(id: String, syncStatus: SyncStatus) {
        transactions.value = transactions.value.map {
            if (it.id == id) it.copy(syncStatus = syncStatus) else it
        }
    }

    override suspend fun updateTransactionStatus(id: String, status: TransactionStatus) {
        transactions.value = transactions.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
    }

    override suspend fun updateDispenseStatus(id: String, dispenseStatus: DispenseStatus) {
        transactions.value = transactions.value.map {
            if (it.id == id) it.copy(dispenseStatus = dispenseStatus) else it
        }
    }

    override suspend fun findInterruptedTransactions(olderThanMs: Long): List<Transaction> =
        transactions.value.filter { tx ->
            (tx.status == TransactionStatus.PAYMENT_SUCCESS || tx.status == TransactionStatus.DISPENSING)
                && tx.updatedAt < olderThanMs
        }
}
