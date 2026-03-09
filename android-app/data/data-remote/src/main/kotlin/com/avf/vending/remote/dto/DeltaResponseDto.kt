package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeltaResponseDto<T>(
    val updated: List<T>,
    val deleted: List<String>,
    val timestamp: Long,
)
