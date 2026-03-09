package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "error_logs", indices = [Index("synced"), Index("timestamp")])
data class ErrorLogEntity(
    @PrimaryKey val id: String,
    val traceId: String?,
    val tag: String?,
    val message: String?,
    val stackTrace: String,
    /** JSON array of breadcrumb strings */
    val breadcrumbsJson: String,
    /** JSON object of extra key-value pairs */
    val extrasJson: String,
    val timestamp: Long,
    val synced: Boolean = false,
)
