package com.avf.vending.remote.api

import com.avf.vending.remote.dto.ConfigDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ConfigApiService {
    @GET("machines/{machineId}/config")
    suspend fun getMachineConfig(@Path("machineId") machineId: String): ConfigDto
}
