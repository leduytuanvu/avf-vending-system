package com.avf.vending.remote.api

import com.avf.vending.remote.dto.ErrorLogBatchDto
import com.avf.vending.remote.dto.EventLogBatchDto
import retrofit2.http.Body
import retrofit2.http.POST

interface TelemetryApiService {
    /** Upload a batch of error logs. Server must handle idempotency by error log id. */
    @POST("telemetry/errors")
    suspend fun sendErrors(@Body payload: ErrorLogBatchDto)

    /** Upload a batch of events (analytics / audit trail). Server must handle idempotency by event id. */
    @POST("telemetry/events/batch")
    suspend fun sendEvents(@Body payload: EventLogBatchDto)

    /** Generic structured event (analytics / audit trail). Single-event endpoint. */
    @POST("telemetry/events")
    suspend fun sendEvent(@Body payload: Map<String, String>)
}
