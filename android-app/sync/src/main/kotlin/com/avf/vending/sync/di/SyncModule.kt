package com.avf.vending.sync.di

import com.avf.vending.common.time.MonotonicClock
import com.avf.vending.sync.SystemMonotonicClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds @Singleton
    abstract fun bindMonotonicClock(impl: SystemMonotonicClock): MonotonicClock
}
