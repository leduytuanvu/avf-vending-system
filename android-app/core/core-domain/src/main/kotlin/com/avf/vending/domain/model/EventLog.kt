package com.avf.vending.domain.model

data class EventLog(
    val id: String,
    val traceId: String?,
    val screen: String,
    val action: String,
    val metadata: String = "",
    val timestamp: Long,
)
