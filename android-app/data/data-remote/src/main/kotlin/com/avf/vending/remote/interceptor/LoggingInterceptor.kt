package com.avf.vending.remote.interceptor

import okhttp3.logging.HttpLoggingInterceptor

object LoggingInterceptor {
    fun create(level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { setLevel(level) }
}
