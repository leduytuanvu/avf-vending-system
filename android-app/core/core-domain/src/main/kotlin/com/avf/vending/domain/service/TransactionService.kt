package com.avf.vending.domain.service

import com.avf.vending.domain.model.Transaction
import com.avf.vending.domain.model.TransactionStatus
import com.avf.vending.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Domain service for transaction queries.
 *
 * Replaces the anemic [GetTransactionHistoryUseCase] (a pure repository
 * delegate) and adds [observeRefundRequired] — a derived stream that the
 * admin screen needs to show pending refunds.  Having this derivation in the
 * domain layer keeps the filtering logic out of the ViewModel and makes it
 * testable independently of UI.
 */
class TransactionService @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    /** Full transaction history ordered by creation time descending. */
    fun observeHistory(): Flow<List<Transaction>> =
        transactionRepository.observeHistory()

    /**
     * Reactive stream of transactions that require operator refund action.
     *
     * These are transactions where payment was collected but product delivery
     * could not be confirmed (hardware failure, crash mid-dispense, sensor
     * timeout).  The admin screen subscribes to this to surface alerts and
     * allow manual refund processing.
     */
    fun observeRefundRequired(): Flow<List<Transaction>> =
        transactionRepository.observeHistory().map { list ->
            list.filter { it.status == TransactionStatus.REFUND_REQUIRED }
        }

    /** Looks up a single transaction by ID. Returns null if not found. */
    suspend fun getById(id: String): Transaction? =
        transactionRepository.getById(id)
}
