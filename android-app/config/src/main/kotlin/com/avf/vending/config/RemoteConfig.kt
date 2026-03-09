package com.avf.vending.config

import com.avf.vending.remote.api.ConfigApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfig @Inject constructor(
    private val configApi: ConfigApiService,
) {
    private var cache: Map<String, String> = emptyMap()
    private var machineId: String = ""

    suspend fun refresh(machineId: String) {
        this.machineId = machineId
        try {
            val config = configApi.getMachineConfig(machineId)
            cache = config.featureFlags.mapValues { it.value.toString() }
                .plus(mapOf(
                    "api_base_url" to config.apiBaseUrl,
                    "idle_timeout_seconds" to config.idleTimeoutSeconds.toString(),
                ))
        } catch (e: Exception) {
            // Keep stale cache on network error
        }
    }

    fun getString(key: String, default: String = ""): String = cache[key] ?: default

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean =
        cache[key]?.toBooleanStrictOrNull() ?: default

    fun getInt(key: String, default: Int = 0): Int =
        cache[key]?.toIntOrNull() ?: default
}
