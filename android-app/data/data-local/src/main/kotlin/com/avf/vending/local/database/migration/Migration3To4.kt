package com.avf.vending.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ── Fix 1: Clock Skew ──────────────────────────────────────────────────
        // Reset all PENDING sync tasks to run immediately after boot.
        // nextRetryAt was stored as wall-clock ms; after switching to monotonic
        // clock the stored values are meaningless across reboots, so set to 0
        // to make all tasks eligible immediately on the next sync pass.
        db.execSQL("UPDATE sync_queue SET nextRetryAt = 0 WHERE status = 'PENDING'")

        // ── Fix 3: Duplicate Dispense ─────────────────────────────────────────
        // Write-ahead journal for hardware dispense commands.
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS dispense_journal (
                dispenseId    TEXT    NOT NULL PRIMARY KEY,
                transactionId TEXT    NOT NULL,
                slotId        TEXT    NOT NULL,
                sensorTriggered INTEGER NOT NULL DEFAULT 0,
                completed     INTEGER NOT NULL DEFAULT 0,
                createdAt     INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_dispense_journal_transactionId ON dispense_journal(transactionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_dispense_journal_createdAt ON dispense_journal(createdAt)")
    }
}
