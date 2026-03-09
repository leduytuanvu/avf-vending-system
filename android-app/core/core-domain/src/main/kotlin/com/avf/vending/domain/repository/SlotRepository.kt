package com.avf.vending.domain.repository

import com.avf.vending.domain.model.Slot
import kotlinx.coroutines.flow.Flow

interface SlotRepository {
    fun observeSlots(): Flow<List<Slot>>
    suspend fun getBySlotId(slotId: String): Slot?
    suspend fun upsertAll(slots: List<Slot>)
    suspend fun updateStock(slotId: String, delta: Int)
}
