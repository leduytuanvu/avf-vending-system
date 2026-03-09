package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "slots", indices = [Index(value = ["slotId"], unique = true)])
data class SlotEntity(
    @PrimaryKey val id: String,
    val slotId: String,
    val row: String,
    val col: Int,
    val productId: String?,
    val stock: Int,
    val capacity: Int,
)
