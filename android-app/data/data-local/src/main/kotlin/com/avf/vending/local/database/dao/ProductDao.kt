package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1")
    fun observeActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductEntity?

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>): List<Long>

    @Query("SELECT * FROM products WHERE updatedAt > :timestamp")
    suspend fun getChangedSince(timestamp: Long): List<ProductEntity>
}