package com.avf.vending.domain.model

data class ErrorLog(
    val id: String,
    val traceId: String?,
    val tag: String?,
    val message: String?,
    val stackTrace: String,
    val breadcrumbs: List<String>,
    val extras: Map<String, String>,
    val timestamp: Long,
    val synced: Boolean = false,
)
