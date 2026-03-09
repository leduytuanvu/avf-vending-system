package com.avf.vending.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPrefsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ADMIN_PIN_HASH = stringPreferencesKey("admin_pin_hash")
        val BRIGHTNESS = intPreferencesKey("brightness")
        val LANGUAGE = stringPreferencesKey("language")
        val RECEIPT_ENABLED = booleanPreferencesKey("receipt_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val adminPinHash: Flow<String?> = context.userPrefsDataStore.data
        .map { it[Keys.ADMIN_PIN_HASH] }

    val brightness: Flow<Int> = context.userPrefsDataStore.data
        .map { it[Keys.BRIGHTNESS] ?: 80 }

    val language: Flow<String> = context.userPrefsDataStore.data
        .map { it[Keys.LANGUAGE] ?: "vi" }

    val receiptEnabled: Flow<Boolean> = context.userPrefsDataStore.data
        .map { it[Keys.RECEIPT_ENABLED] ?: false }

    val soundEnabled: Flow<Boolean> = context.userPrefsDataStore.data
        .map { it[Keys.SOUND_ENABLED] ?: true }

    suspend fun setAdminPinHash(hash: String) {
        context.userPrefsDataStore.edit { it[Keys.ADMIN_PIN_HASH] = hash }
    }

    suspend fun setBrightness(value: Int) {
        context.userPrefsDataStore.edit { it[Keys.BRIGHTNESS] = value.coerceIn(0, 100) }
    }

    suspend fun setLanguage(lang: String) {
        context.userPrefsDataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setReceiptEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.RECEIPT_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }
}
