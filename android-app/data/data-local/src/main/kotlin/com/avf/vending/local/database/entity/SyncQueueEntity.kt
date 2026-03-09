package com.avf.vending.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue", indices = [Index("priority"), Index("nextRetryAt"), Index("status")])
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val priority: Int,
    val retryCount: Int,
    val nextRetryAt: Long,
    val createdAt: Long,
    val status: String = STATUS_PENDING,
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_FAILED = "FAILED"
        const val MAX_RETRIES = 5
    }
}
