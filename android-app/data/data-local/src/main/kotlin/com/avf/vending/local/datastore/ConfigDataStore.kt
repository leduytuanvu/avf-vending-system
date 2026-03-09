package com.avf.vending.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.configDataStore by preferencesDataStore(name = "machine_config")

@Singleton
class ConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val MACHINE_ID = stringPreferencesKey("machine_id")
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val MACHINE_TYPE = stringPreferencesKey("machine_type")
        val IDLE_TIMEOUT_MS = longPreferencesKey("idle_timeout_ms")
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
    }

    val machineId: Flow<String> = context.configDataStore.data
        .map { it[Keys.MACHINE_ID] ?: "" }

    val apiBaseUrl: Flow<String> = context.configDataStore.data
        .map { it[Keys.API_BASE_URL] ?: "" }

    val machineType: Flow<String> = context.configDataStore.data
        .map { it[Keys.MACHINE_TYPE] ?: "unknown" }

    val idleTimeoutMs: Flow<Long> = context.configDataStore.data
        .map { it[Keys.IDLE_TIMEOUT_MS] ?: 30_000L }

    val lastSyncAt: Flow<Long> = context.configDataStore.data
        .map { it[Keys.LAST_SYNC_AT] ?: 0L }

    suspend fun setMachineId(id: String) {
        context.configDataStore.edit { it[Keys.MACHINE_ID] = id }
    }

    suspend fun setApiBaseUrl(url: String) {
        context.configDataStore.edit { it[Keys.API_BASE_URL] = url }
    }

    suspend fun setMachineType(type: String) {
        context.configDataStore.edit { it[Keys.MACHINE_TYPE] = type }
    }

    suspend fun setIdleTimeoutMs(ms: Long) {
        context.configDataStore.edit { it[Keys.IDLE_TIMEOUT_MS] = ms }
    }

    suspend fun setLastSyncAt(timestamp: Long) {
        context.configDataStore.edit { it[Keys.LAST_SYNC_AT] = timestamp }
    }
}
