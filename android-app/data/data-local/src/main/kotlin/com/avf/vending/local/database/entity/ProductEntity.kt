package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products", indices = [Index("categoryId"), Index("isActive")])
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Long,
    val imageUrl: String,
    val categoryId: String,
    val isActive: Boolean,
    val updatedAt: Long,
)
