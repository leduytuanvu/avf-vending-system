package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.command.HardwareCommandQueue
import com.avf.vending.hardware.api.event.HardwareEventBus
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransportModule {
    @Binds
    @Singleton
    abstract fun bindHardwareCommandQueue(
        impl: DefaultHardwareCommandQueue,
    ): HardwareCommandQueue

    @Binds
    @Singleton
    abstract fun bindHardwareEventBus(
        impl: DefaultHardwareEventBus,
    ): HardwareEventBus
}
