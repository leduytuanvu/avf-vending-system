package com.avf.vending.domain.model

data class Category(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
)
