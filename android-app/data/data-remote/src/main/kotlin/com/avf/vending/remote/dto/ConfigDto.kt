package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConfigDto(
    val machineId: String,
    val apiBaseUrl: String,
    val idleTimeoutSeconds: Int,
    val featureFlags: Map<String, Boolean> = emptyMap(),
)
