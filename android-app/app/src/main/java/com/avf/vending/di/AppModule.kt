package com.avf.vending.di

import com.avf.vending.BuildConfig
import com.avf.vending.common.di.ApplicationScope
import com.avf.vending.common.network.ApiConfig
import com.avf.vending.common.time.Clock
import com.avf.vending.common.time.SystemClock
import com.avf.vending.domain.repository.HardwareRepository
import com.avf.vending.hardware.api.validation.SensorTimingGuard
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds
    @Singleton
    abstract fun bindHardwareRepository(impl: DefaultHardwareRepository): HardwareRepository

    companion object {
        @Provides
        @Singleton
        fun provideApiConfig(): ApiConfig = object : ApiConfig {
            override val baseUrl: String get() = BuildConfig.API_BASE_URL
        }
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Provides the default [SensorTimingGuard] with production timing constants.
         * Override per machine type by providing a qualified binding in the flavor module.
         */
        @Provides
        @Singleton
        fun provideSensorTimingGuard(): SensorTimingGuard = SensorTimingGuard()
    }
}
