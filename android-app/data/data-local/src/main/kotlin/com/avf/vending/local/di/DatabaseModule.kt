package com.avf.vending.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.avf.vending.local.database.VendingDatabase
import com.avf.vending.local.database.dao.*
import com.avf.vending.local.database.dao.DispenseJournalDao
import com.avf.vending.local.database.migration.MIGRATION_1_2
import com.avf.vending.local.database.migration.MIGRATION_2_3
import com.avf.vending.local.database.migration.MIGRATION_3_4
import com.avf.vending.local.database.migration.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVendingDatabase(@ApplicationContext context: Context): VendingDatabase =
        Room.databaseBuilder(context, VendingDatabase::class.java, VendingDatabase.DATABASE_NAME)
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    @Provides fun provideProductDao(db: VendingDatabase): ProductDao = db.productDao()
    @Provides fun provideSlotDao(db: VendingDatabase): SlotDao = db.slotDao()
    @Provides fun provideCategoryDao(db: VendingDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideTransactionDao(db: VendingDatabase): TransactionDao = db.transactionDao()
    @Provides fun providePaymentDao(db: VendingDatabase): PaymentDao = db.paymentDao()
    @Provides fun provideSyncQueueDao(db: VendingDatabase): SyncQueueDao = db.syncQueueDao()
    @Provides fun provideErrorLogDao(db: VendingDatabase): ErrorLogDao = db.errorLogDao()
    @Provides fun provideEventLogDao(db: VendingDatabase): EventLogDao = db.eventLogDao()
    @Provides fun provideDispenseJournalDao(db: VendingDatabase): DispenseJournalDao = db.dispenseJournalDao()
}
