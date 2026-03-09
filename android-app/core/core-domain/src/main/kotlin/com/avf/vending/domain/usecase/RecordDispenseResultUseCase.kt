package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.DispenseOutcome
import com.avf.vending.domain.transaction.TransactionCoordinator
import javax.inject.Inject

/**
 * Records the hardware dispense result against the persisted transaction.
 *
 * - Success  → COMPLETED / SUCCESS + decrement stock
 * - Failed   → DISPENSE_FAILED / FAILED + mark REFUND_REQUIRED
 * - Timeout  → DISPENSE_FAILED / TIMEOUT + mark REFUND_REQUIRED
 *
 * This is the single point of truth for post-dispense state transitions.
 * Feature layer calls this immediately after receiving the DispenseResult
 * from the hardware driver.
 */
class RecordDispenseResultUseCase @Inject constructor(
    private val transactionCoordinator: TransactionCoordinator,
) {
    suspend operator fun invoke(transactionId: String, outcome: DispenseOutcome) =
        transactionCoordinator.recordDispenseOutcome(transactionId, outcome)
}
