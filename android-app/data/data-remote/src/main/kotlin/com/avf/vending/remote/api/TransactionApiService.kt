package com.avf.vending.remote.api

import com.avf.vending.remote.dto.TransactionDto
import retrofit2.http.Body
import retrofit2.http.POST

interface TransactionApiService {
    @POST("transactions/batch")
    suspend fun uploadBatch(@Body transactions: List<TransactionDto>)
}
