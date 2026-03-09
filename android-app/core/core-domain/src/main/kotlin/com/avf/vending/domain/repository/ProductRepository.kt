package com.avf.vending.domain.repository

import com.avf.vending.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeActiveProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun getProductBySlotId(slotId: String): Product?
    suspend fun upsertAll(products: List<Product>)
    suspend fun getChangedSince(timestamp: Long): List<Product>
}
