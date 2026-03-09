package com.avf.vending.domain.usecase

import com.avf.vending.domain.repository.DispenseJournalRepository
import com.avf.vending.domain.repository.TransactionRepository
import com.avf.vending.domain.transaction.TransactionCoordinator
import javax.inject.Inject

/**
 * Startup reconciliation: finds transactions interrupted mid-flight
 * (payment succeeded but dispense was not confirmed) and resolves each one
 * using evidence from the [DispenseJournalRepository].
 *
 * Decision tree per interrupted transaction:
 * ┌─ No journal entry found         → REFUND_REQUIRED (no hardware command sent)
 * ├─ journal.completed = true       → COMPLETED (race — hardware did finish)
 * ├─ journal.sensorTriggered = true → COMPLETED (sensor saw the drop; product delivered)
 * └─ journal.sensorTriggered = false → REFUND_REQUIRED (command sent but no confirmation)
 *
 * "Sensor triggered" evidence prevents duplicate dispense: if the sensor saw the
 * product drop before the crash, we must NOT retry — the customer already received
 * their product.
 *
 * Called once from VendingApp.onCreate() before any UI is shown.
 */
class ReconcileTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val dispenseJournalRepository: DispenseJournalRepository,
    private val transactionCoordinator: TransactionCoordinator,
) {
    suspend operator fun invoke() {
        val cutoff = System.currentTimeMillis() - STALE_THRESHOLD_MS
        val interrupted = transactionRepository.findInterruptedTransactions(olderThanMs = cutoff)

        interrupted.forEach { tx ->
            val journal = dispenseJournalRepository.getByTransactionId(tx.id)
            val delivered = when {
                journal == null -> false
                journal.completed -> true
                journal.sensorTriggered -> true
                else -> false
            }
            transactionCoordinator.recoverInterruptedTransaction(tx.id, delivered)
        }
    }

    companion object {
        /** Transactions not updated for more than 5 minutes are considered stale. */
        private const val STALE_THRESHOLD_MS = 5 * 60 * 1_000L
    }
}
