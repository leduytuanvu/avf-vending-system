package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dispense_journal",
    indices = [Index("transactionId"), Index("createdAt")],
)
data class DispenseJournalEntity(
    @PrimaryKey val dispenseId: String,
    val transactionId: String,
    val slotId: String,
    val sensorTriggered: Boolean,
    val completed: Boolean,
    val createdAt: Long,
)
