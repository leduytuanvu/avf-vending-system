package com.avf.vending.testing

import com.avf.vending.domain.model.DispenseJournalEntry
import com.avf.vending.domain.repository.DispenseJournalRepository

class FakeDispenseJournalRepository : DispenseJournalRepository {

    private val entries = mutableMapOf<String, DispenseJournalEntry>()

    override suspend fun create(entry: DispenseJournalEntry) {
        entries[entry.dispenseId] = entry
    }

    override suspend fun markSensorTriggered(dispenseId: String) {
        entries[dispenseId]?.let { entries[dispenseId] = it.copy(sensorTriggered = true) }
    }

    override suspend fun markCompleted(dispenseId: String) {
        entries[dispenseId]?.let { entries[dispenseId] = it.copy(completed = true) }
    }

    override suspend fun getByTransactionId(transactionId: String): DispenseJournalEntry? =
        entries.values
            .filter { it.transactionId == transactionId }
            .maxByOrNull { it.createdAt }
}
