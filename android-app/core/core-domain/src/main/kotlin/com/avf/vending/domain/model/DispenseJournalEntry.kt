package com.avf.vending.domain.model

/**
 * Write-ahead log for a single hardware dispense attempt.
 *
 * Created BEFORE sending the hardware command so that a crash/reboot
 * mid-dispense leaves a durable record.  [ReconcileTransactionsUseCase]
 * reads this on startup to determine whether a product was actually
 * dispensed — preventing both lost-product (no retry when dispensed)
 * and duplicate-dispense (retry when not dispensed).
 *
 * @param sensorTriggered  True if the drop sensor fired within the valid
 *                         timing window (set after hardware command returns).
 * @param completed        True if the hardware protocol reported full success.
 */
data class DispenseJournalEntry(
    val dispenseId: String,
    val transactionId: String,
    val slotId: String,
    val sensorTriggered: Boolean,
    val completed: Boolean,
    val createdAt: Long,
)
