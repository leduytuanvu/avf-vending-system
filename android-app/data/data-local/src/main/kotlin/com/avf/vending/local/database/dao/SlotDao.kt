package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.SlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotDao {
    @Query("SELECT * FROM slots")
    fun observeAll(): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE slotId = :slotId LIMIT 1")
    suspend fun getBySlotId(slotId: String): SlotEntity?

    @Upsert
    suspend fun upsertAll(slots: List<SlotEntity>): List<Long>

    @Query("UPDATE slots SET stock = stock + :delta WHERE slotId = :slotId")
    suspend fun updateStock(slotId: String, delta: Int): Int
}