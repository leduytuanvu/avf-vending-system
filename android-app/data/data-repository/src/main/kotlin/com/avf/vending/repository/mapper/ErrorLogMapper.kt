package com.avf.vending.repository.mapper

import com.avf.vending.domain.model.ErrorLog
import com.avf.vending.domain.model.EventLog
import com.avf.vending.local.database.entity.ErrorLogEntity
import com.avf.vending.local.database.entity.EventLogEntity

object ErrorLogMapper {

    fun ErrorLogEntity.toDomain() = ErrorLog(
        id = id,
        traceId = traceId,
        tag = tag,
        message = message,
        stackTrace = stackTrace,
        breadcrumbs = breadcrumbsJson.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() },
        extras = emptyMap(), // extras are write-only in current impl; parsed on backend
        timestamp = timestamp,
        synced = synced,
    )

    fun ErrorLog.toEntity() = ErrorLogEntity(
        id = id,
        traceId = traceId,
        tag = tag,
        message = message,
        stackTrace = stackTrace,
        breadcrumbsJson = breadcrumbs.joinToString(",", "[", "]") { "\"${it.replace("\"", "\\\"")}\"" },
        extrasJson = extras.entries.joinToString(",", "{", "}") { (k, v) -> "\"$k\":\"$v\"" },
        timestamp = timestamp,
        synced = synced,
    )

    fun EventLog.toEntity() = EventLogEntity(
        id = id,
        traceId = traceId,
        screen = screen,
        action = action,
        metadata = metadata,
        timestamp = timestamp,
        synced = false,
    )

    fun EventLogEntity.toDomain() = EventLog(
        id = id,
        traceId = traceId,
        screen = screen,
        action = action,
        metadata = metadata,
        timestamp = timestamp,
    )
}
