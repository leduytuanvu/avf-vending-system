package com.avf.vending.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
        val IDLE_TIMEOUT_MS = longPreferencesKey("idle_timeout_ms")
        val LAST_ACTIVITY_AT = longPreferencesKey("last_activity_at")
        val SESSION_COUNT = intPreferencesKey("session_count")
    }

    val lastSyncAt: Flow<Long> = context.sessionDataStore.data
        .map { it[Keys.LAST_SYNC_AT] ?: 0L }

    val idleTimeoutMs: Flow<Long> = context.sessionDataStore.data
        .map { it[Keys.IDLE_TIMEOUT_MS] ?: 30_000L }

    val lastActivityAt: Flow<Long> = context.sessionDataStore.data
        .map { it[Keys.LAST_ACTIVITY_AT] ?: 0L }

    val sessionCount: Flow<Int> = context.sessionDataStore.data
        .map { it[Keys.SESSION_COUNT] ?: 0 }

    suspend fun updateLastSyncAt(timestamp: Long) {
        context.sessionDataStore.edit { it[Keys.LAST_SYNC_AT] = timestamp }
    }

    suspend fun updateIdleTimeoutMs(ms: Long) {
        context.sessionDataStore.edit { it[Keys.IDLE_TIMEOUT_MS] = ms }
    }

    suspend fun updateLastActivityAt(timestamp: Long) {
        context.sessionDataStore.edit { it[Keys.LAST_ACTIVITY_AT] = timestamp }
    }

    suspend fun incrementSessionCount() {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.SESSION_COUNT] = (prefs[Keys.SESSION_COUNT] ?: 0) + 1
        }
    }
}
