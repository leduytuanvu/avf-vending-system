package com.avf.vending.repository.impl

import com.avf.vending.domain.model.Product
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.local.cache.ProductMemCache
import com.avf.vending.local.database.dao.ProductDao
import com.avf.vending.repository.mapper.ProductMapper.toDomain
import com.avf.vending.repository.mapper.ProductMapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val memCache: ProductMemCache,
) : ProductRepository {

    override fun observeActiveProducts(): Flow<List<Product>> =
        productDao.observeActiveProducts().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProductById(id: String): Product? =
        productDao.getById(id)?.toDomain()

    override suspend fun getProductBySlotId(slotId: String): Product? = null // TODO: join via SlotDao

    override suspend fun upsertAll(products: List<Product>) {
        productDao.upsertAll(products.map { it.toEntity() })
        memCache.invalidate()
    }

    override suspend fun getChangedSince(timestamp: Long): List<Product> =
        productDao.getChangedSince(timestamp).map { it.toDomain() }
}
