package com.avf.vending.testing

import com.avf.vending.domain.model.Product
import com.avf.vending.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductRepository : ProductRepository {
    private val products = MutableStateFlow<List<Product>>(emptyList())

    fun setProducts(list: List<Product>) { products.value = list }

    override fun observeActiveProducts(): Flow<List<Product>> = products
    override suspend fun getProductById(id: String) = products.value.find { it.id == id }
    override suspend fun getProductBySlotId(slotId: String) = null
    override suspend fun upsertAll(list: List<Product>) { products.value = list }
    override suspend fun getChangedSince(timestamp: Long) = products.value
}
