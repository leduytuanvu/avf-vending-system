package com.avf.vending.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.strategyDataStore by preferencesDataStore(name = "strategy_config")

@Singleton
class StrategyDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LAST_SUCCESSFUL_STRATEGY_ID = stringPreferencesKey("last_successful_strategy_id")
        val FORCED_STRATEGY_ID = stringPreferencesKey("forced_strategy_id")
        val STRATEGY_CONFIG_JSON = stringPreferencesKey("strategy_config_json")
    }

    val lastSuccessfulStrategyId: Flow<String?> = context.strategyDataStore.data
        .map { it[Keys.LAST_SUCCESSFUL_STRATEGY_ID] }

    val forcedStrategyId: Flow<String?> = context.strategyDataStore.data
        .map { it[Keys.FORCED_STRATEGY_ID] }

    val strategyConfigJson: Flow<String?> = context.strategyDataStore.data
        .map { it[Keys.STRATEGY_CONFIG_JSON] }

    suspend fun saveLastSuccessfulStrategyId(id: String) {
        context.strategyDataStore.edit { it[Keys.LAST_SUCCESSFUL_STRATEGY_ID] = id }
    }

    suspend fun saveForcedStrategyId(id: String?) {
        context.strategyDataStore.edit { prefs ->
            if (id != null) prefs[Keys.FORCED_STRATEGY_ID] = id
            else prefs.remove(Keys.FORCED_STRATEGY_ID)
        }
    }

    suspend fun saveStrategyConfigJson(json: String) {
        context.strategyDataStore.edit { it[Keys.STRATEGY_CONFIG_JSON] = json }
    }
}
