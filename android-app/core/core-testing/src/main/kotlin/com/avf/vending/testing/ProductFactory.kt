package com.avf.vending.testing

import com.avf.vending.domain.model.Product
import java.util.UUID

object ProductFactory {
    fun create(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Product",
        price: Long = 10_000L,
        categoryId: String = "cat-1",
        isActive: Boolean = true,
    ) = Product(id = id, name = name, price = price, imageUrl = "", categoryId = categoryId, isActive = isActive)
}
