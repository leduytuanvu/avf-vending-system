package com.avf.vending.remote.di

import com.avf.vending.common.network.ApiConfig
import com.avf.vending.remote.api.*
import com.avf.vending.remote.circuit.CircuitBreakerConfig
import com.avf.vending.remote.interceptor.AuthInterceptor
import com.avf.vending.remote.interceptor.LoggingInterceptor
import com.avf.vending.remote.interceptor.OfflineCacheInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        offlineCacheInterceptor: OfflineCacheInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(authInterceptor)
        .addInterceptor(offlineCacheInterceptor)
        .addNetworkInterceptor(LoggingInterceptor.create())
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json, apiConfig: ApiConfig): Retrofit =
        Retrofit.Builder()
            .baseUrl(apiConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApiService =
        retrofit.create(ProductApiService::class.java)

    @Provides @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

    @Provides @Singleton
    fun provideConfigApi(retrofit: Retrofit): ConfigApiService =
        retrofit.create(ConfigApiService::class.java)

    @Provides @Singleton
    fun provideWalletApi(retrofit: Retrofit): WalletApiService =
        retrofit.create(WalletApiService::class.java)

    @Provides @Singleton
    fun provideTelemetryApi(retrofit: Retrofit): TelemetryApiService =
        retrofit.create(TelemetryApiService::class.java)

    @Provides @Singleton
    fun provideCircuitBreakerConfig(): CircuitBreakerConfig = CircuitBreakerConfig()
}
