package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "event_logs", indices = [Index("timestamp"), Index("traceId"), Index("synced")])
data class EventLogEntity(
    @PrimaryKey val id: String,
    val traceId: String?,
    val screen: String,
    val action: String,
    val metadata: String,
    val timestamp: Long,
    val synced: Boolean = false,
)
