package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val price: Long,
    val imageUrl: String,
    val categoryId: String,
    val isActive: Boolean,
    val updatedAt: Long,
)
