package com.avf.vending.di

import com.avf.vending.common.coroutine.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher

/**
 * General hardware dispatcher — kept for backwards compatibility.
 * Prefer [VendingDispatcher] or [BillDispatcher] for new hardware code.
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class HardwareDispatcher

/**
 * Single-token dispatcher for vending machine controller commands.
 * limitedParallelism(1) means at most one coroutine runs at a time on this
 * dispatcher, serialising all commands to the machine controller without
 * competing with Room / Retrofit on the shared IO thread pool.
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class VendingDispatcher

/**
 * Single-token dispatcher for bill acceptor commands.
 * Same rationale as [VendingDispatcher] but isolated so that bill polling
 * never blocks vending machine commands and vice-versa.
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class BillDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(
        @IoDispatcher io: CoroutineDispatcher,
        @DefaultDispatcher default: CoroutineDispatcher,
        @MainDispatcher main: CoroutineDispatcher,
        @HardwareDispatcher hardware: CoroutineDispatcher,
    ): DispatcherProvider = object : DispatcherProvider {
        override val io = io
        override val default = default
        override val main = main
        override val hardware = hardware
    }

    @Provides @IoDispatcher fun provideIo(): CoroutineDispatcher = Dispatchers.IO
    @Provides @DefaultDispatcher fun provideDefault(): CoroutineDispatcher = Dispatchers.Default
    @Provides @MainDispatcher fun provideMain(): CoroutineDispatcher = Dispatchers.Main

    // Legacy — unchanged so existing injection sites keep compiling.
    @Provides @HardwareDispatcher fun provideHardware(): CoroutineDispatcher = Dispatchers.IO

    // Each of these creates one "slot" inside the IO thread pool.
    // Coroutines dispatched here are serialised (max parallelism = 1) and do
    // NOT block threads — they suspend just like normal IO coroutines.
    @Provides @Singleton @VendingDispatcher
    fun provideVendingDispatcher(): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    @Provides @Singleton @BillDispatcher
    fun provideBillDispatcher(): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)
}
