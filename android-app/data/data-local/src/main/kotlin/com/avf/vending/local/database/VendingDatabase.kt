package com.avf.vending.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.avf.vending.local.database.converter.EnumConverters
import com.avf.vending.local.database.dao.*
import com.avf.vending.local.database.entity.*
import com.avf.vending.local.database.migration.MIGRATION_1_2
import com.avf.vending.local.database.migration.MIGRATION_2_3
import com.avf.vending.local.database.migration.MIGRATION_3_4
import com.avf.vending.local.database.migration.MIGRATION_4_5

@Database(
    entities = [
        ProductEntity::class,
        SlotEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        PaymentEntity::class,
        SyncQueueEntity::class,
        ErrorLogEntity::class,
        EventLogEntity::class,
        DispenseJournalEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(EnumConverters::class)
abstract class VendingDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun slotDao(): SlotDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun errorLogDao(): ErrorLogDao
    abstract fun eventLogDao(): EventLogDao
    abstract fun dispenseJournalDao(): DispenseJournalDao

    companion object {
        const val DATABASE_NAME = "vending.db"
    }
}
