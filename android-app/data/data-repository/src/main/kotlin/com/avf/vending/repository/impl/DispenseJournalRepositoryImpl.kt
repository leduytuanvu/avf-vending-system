package com.avf.vending.repository.impl

import com.avf.vending.domain.model.DispenseJournalEntry
import com.avf.vending.domain.repository.DispenseJournalRepository
import com.avf.vending.local.database.dao.DispenseJournalDao
import com.avf.vending.local.database.entity.DispenseJournalEntity
import javax.inject.Inject

class DispenseJournalRepositoryImpl @Inject constructor(
    private val dao: DispenseJournalDao,
) : DispenseJournalRepository {

    override suspend fun create(entry: DispenseJournalEntry) {
        dao.insert(entry.toEntity())
    }

    override suspend fun markSensorTriggered(dispenseId: String) {
        dao.markSensorTriggered(dispenseId)
    }

    override suspend fun markCompleted(dispenseId: String) {
        dao.markCompleted(dispenseId)
    }

    override suspend fun getByTransactionId(transactionId: String): DispenseJournalEntry? =
        dao.getByTransactionId(transactionId)?.toDomain()

    private fun DispenseJournalEntry.toEntity() = DispenseJournalEntity(
        dispenseId = dispenseId,
        transactionId = transactionId,
        slotId = slotId,
        sensorTriggered = sensorTriggered,
        completed = completed,
        createdAt = createdAt,
    )

    private fun DispenseJournalEntity.toDomain() = DispenseJournalEntry(
        dispenseId = dispenseId,
        transactionId = transactionId,
        slotId = slotId,
        sensorTriggered = sensorTriggered,
        completed = completed,
        createdAt = createdAt,
    )
}
