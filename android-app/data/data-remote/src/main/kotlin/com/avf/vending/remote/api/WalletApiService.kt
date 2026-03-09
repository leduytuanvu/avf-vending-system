package com.avf.vending.remote.api

import com.avf.vending.remote.dto.WalletQRResponseDto
import com.avf.vending.remote.dto.WalletStatusDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WalletApiService {
    @POST("wallet/qr")
    suspend fun createQR(@Query("amount") amount: Long): WalletQRResponseDto

    @GET("wallet/status/{sessionId}")
    suspend fun checkStatus(@Path("sessionId") sessionId: String): WalletStatusDto
}
