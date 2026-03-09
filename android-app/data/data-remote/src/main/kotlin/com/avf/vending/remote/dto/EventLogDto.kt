package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventLogDto(
    val id: String,
    val traceId: String?,
    val screen: String,
    val action: String,
    val metadata: String,
    val timestamp: Long,
)

@Serializable
data class EventLogBatchDto(
    val events: List<EventLogDto>,
)
