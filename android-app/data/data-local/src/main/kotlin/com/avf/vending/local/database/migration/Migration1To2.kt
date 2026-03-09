package com.avf.vending.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add traceId, idempotencyKey, machineId to transactions
        db.execSQL("ALTER TABLE transactions ADD COLUMN traceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE transactions ADD COLUMN idempotencyKey TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE transactions ADD COLUMN machineId TEXT NOT NULL DEFAULT ''")
        // Backfill: use existing id as traceId and idempotencyKey
        db.execSQL("UPDATE transactions SET traceId = id, idempotencyKey = id WHERE traceId = ''")

        // Add traceId, idempotencyKey to payments
        db.execSQL("ALTER TABLE payments ADD COLUMN traceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE payments ADD COLUMN idempotencyKey TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE payments SET traceId = transactionId, idempotencyKey = id WHERE traceId = ''")

        // Add status column to sync_queue (default PENDING for all existing rows)
        db.execSQL("ALTER TABLE sync_queue ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_status ON sync_queue(status)")

        // Create error_logs table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS error_logs (
                id TEXT NOT NULL PRIMARY KEY,
                traceId TEXT,
                tag TEXT,
                message TEXT,
                stackTrace TEXT NOT NULL,
                breadcrumbsJson TEXT NOT NULL,
                extrasJson TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                synced INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_error_logs_synced ON error_logs(synced)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_error_logs_timestamp ON error_logs(timestamp)")

        // Create event_logs table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS event_logs (
                id TEXT NOT NULL PRIMARY KEY,
                traceId TEXT,
                screen TEXT NOT NULL,
                action TEXT NOT NULL,
                metadata TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_event_logs_timestamp ON event_logs(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_event_logs_traceId ON event_logs(traceId)")
    }
}
