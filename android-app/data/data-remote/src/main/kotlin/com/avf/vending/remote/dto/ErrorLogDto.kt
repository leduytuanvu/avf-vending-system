package com.avf.vending.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorLogDto(
    val id: String,
    val traceId: String?,
    val tag: String?,
    val message: String?,
    val stackTrace: String,
    val breadcrumbs: List<String>,
    val extras: Map<String, String>,
    val timestamp: Long,
)

@Serializable
data class ErrorLogBatchDto(
    val errors: List<ErrorLogDto>,
)
