package com.avf.vending.hardware.transport

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VendingDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BillDispatcher

@Module
@InstallIn(SingletonComponent::class)
object TransportDispatcherModule {
    @Provides
    @Singleton
    @VendingDispatcher
    fun provideVendingDispatcher(): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    @Provides
    @Singleton
    @BillDispatcher
    fun provideBillDispatcher(): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)
}
