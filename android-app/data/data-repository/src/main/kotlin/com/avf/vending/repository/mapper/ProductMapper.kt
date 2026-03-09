package com.avf.vending.repository.mapper

import com.avf.vending.domain.model.Product
import com.avf.vending.local.database.entity.ProductEntity
import com.avf.vending.remote.dto.ProductDto

object ProductMapper {
    fun ProductEntity.toDomain() = Product(
        id = id, name = name, price = price, imageUrl = imageUrl,
        categoryId = categoryId, isActive = isActive,
    )

    fun Product.toEntity() = ProductEntity(
        id = id, name = name, price = price, imageUrl = imageUrl,
        categoryId = categoryId, isActive = isActive, updatedAt = System.currentTimeMillis(),
    )

    fun ProductDto.toEntity() = ProductEntity(
        id = id, name = name, price = price, imageUrl = imageUrl,
        categoryId = categoryId, isActive = isActive, updatedAt = updatedAt,
    )

    fun ProductDto.toDomain() = Product(
        id = id, name = name, price = price, imageUrl = imageUrl,
        categoryId = categoryId, isActive = isActive,
    )
}
