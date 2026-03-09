package com.avf.vending.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN dispenseStatus TEXT NOT NULL DEFAULT 'NOT_STARTED'")
        db.execSQL("ALTER TABLE transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        // Backfill updatedAt from createdAt for existing rows
        db.execSQL("UPDATE transactions SET updatedAt = createdAt WHERE updatedAt = 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_status ON transactions(status)")
    }
}
