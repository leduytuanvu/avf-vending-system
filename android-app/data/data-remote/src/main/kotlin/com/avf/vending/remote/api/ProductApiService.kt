package com.avf.vending.remote.api

import com.avf.vending.remote.dto.DeltaResponseDto
import com.avf.vending.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {
    @GET("products")
    suspend fun getProductsDelta(@Query("since") since: Long): DeltaResponseDto<ProductDto>
}
