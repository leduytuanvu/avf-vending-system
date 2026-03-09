package com.avf.vending.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Long, // in VND (smallest unit)
    val imageUrl: String,
    val categoryId: String,
    val isActive: Boolean = true,
)
