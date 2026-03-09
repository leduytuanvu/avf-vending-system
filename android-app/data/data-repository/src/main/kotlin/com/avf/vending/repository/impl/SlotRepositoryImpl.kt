package com.avf.vending.repository.impl

import com.avf.vending.domain.model.Slot
import com.avf.vending.domain.repository.SlotRepository
import com.avf.vending.local.database.dao.SlotDao
import com.avf.vending.repository.mapper.SlotMapper.toDomain
import com.avf.vending.repository.mapper.SlotMapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SlotRepositoryImpl @Inject constructor(
    private val slotDao: SlotDao,
) : SlotRepository {

    override fun observeSlots(): Flow<List<Slot>> =
        slotDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getBySlotId(slotId: String): Slot? =
        slotDao.getBySlotId(slotId)?.toDomain()

    override suspend fun upsertAll(slots: List<Slot>) {
        slotDao.upsertAll(slots.map { it.toEntity() })
    }

    override suspend fun updateStock(slotId: String, delta: Int) {
        slotDao.updateStock(slotId, delta)
    }
}
