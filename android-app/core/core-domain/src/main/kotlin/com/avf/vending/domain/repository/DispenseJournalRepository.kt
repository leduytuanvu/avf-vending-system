package com.avf.vending.domain.repository

import com.avf.vending.domain.model.DispenseJournalEntry

/**
 * Write-ahead journal for hardware dispense attempts.
 *
 * Every dispense command must create a journal entry BEFORE the hardware call.
 * This allows [ReconcileTransactionsUseCase] to determine — after a crash —
 * whether a product was actually dispensed, preventing duplicate dispenses.
 */
interface DispenseJournalRepository {
    /** Creates a new journal entry. Called before the hardware dispense command. */
    suspend fun create(entry: DispenseJournalEntry)

    /** Marks the sensor as having triggered within a valid timing window. */
    suspend fun markSensorTriggered(dispenseId: String)

    /** Marks the dispense as fully completed by hardware protocol. */
    suspend fun markCompleted(dispenseId: String)

    /** Returns the most recent journal entry for a transaction, or null if none. */
    suspend fun getByTransactionId(transactionId: String): DispenseJournalEntry?
}
