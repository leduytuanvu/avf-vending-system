package com.avf.vending.config

import com.avf.vending.domain.repository.ConfigRepository
import com.avf.vending.local.datastore.ConfigDataStore
import com.avf.vending.remote.api.ConfigApiService
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepositoryImpl @Inject constructor(
    private val configApiService: ConfigApiService,
    private val configDataStore: ConfigDataStore,
) : ConfigRepository {

    private val _config = MutableStateFlow(MachineConfig())

    /** Full config — inject ConfigRepositoryImpl directly when full MachineConfig is needed. */
    val config: StateFlow<MachineConfig> = _config.asStateFlow()

    override suspend fun getMachineId(): String = _config.value.machineId

    override suspend fun getApiBaseUrl(): String = _config.value.apiBaseUrl

    override suspend fun getIdleTimeoutMs(): Long = _config.value.ui.idleTimeoutMs

    override suspend fun fetchAndApply(machineId: String) {
        val dto = configApiService.getMachineConfig(machineId)
        val newConfig = MachineConfig(
            machineId = machineId,
            apiBaseUrl = dto.apiBaseUrl,
            updatedAt = System.currentTimeMillis(),
        )
        val validation = ConfigValidator.validate(newConfig)
        val applied = if (validation.isValid) newConfig else ConfigValidator.sanitize(newConfig)
        _config.value = applied
        configDataStore.setMachineId(applied.machineId)
        configDataStore.setApiBaseUrl(applied.apiBaseUrl)
    }

    override suspend fun refresh() = fetchAndApply(_config.value.machineId)
}
