package com.avf.vending.local.cache

import com.avf.vending.domain.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductMemCache @Inject constructor() {
    private val ttlMs = 60_000L
    private var cachedAt: Long = 0
    private var cache: List<Product> = emptyList()

    fun get(): List<Product>? {
        val age = System.currentTimeMillis() - cachedAt
        return if (age < ttlMs && cache.isNotEmpty()) cache else null
    }

    fun put(products: List<Product>) {
        cache = products
        cachedAt = System.currentTimeMillis()
    }

    fun invalidate() {
        cachedAt = 0
        cache = emptyList()
    }
}
