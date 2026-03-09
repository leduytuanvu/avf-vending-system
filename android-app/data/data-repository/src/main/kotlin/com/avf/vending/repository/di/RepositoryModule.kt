package com.avf.vending.repository.di

import com.avf.vending.domain.repository.*
import com.avf.vending.repository.impl.*
import com.avf.vending.repository.impl.DispenseJournalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds @Singleton
    abstract fun bindSlotRepository(impl: SlotRepositoryImpl): SlotRepository

    @Binds @Singleton
    abstract fun bindObservabilityRepository(impl: ObservabilityRepositoryImpl): ObservabilityRepository

    @Binds @Singleton
    abstract fun bindDispenseJournalRepository(impl: DispenseJournalRepositoryImpl): DispenseJournalRepository
}
