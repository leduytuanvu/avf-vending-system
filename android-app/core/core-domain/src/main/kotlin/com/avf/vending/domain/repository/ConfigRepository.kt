package com.avf.vending.domain.repository

interface ConfigRepository {
    suspend fun getMachineId(): String
    suspend fun getApiBaseUrl(): String
    suspend fun getIdleTimeoutMs(): Long
    suspend fun fetchAndApply(machineId: String)
    suspend fun refresh()
}
