package com.avf.vending.domain.model

sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class DatabaseError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class HardwareError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class PaymentError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class ValidationError(message: String) : AppError(message)
    class UnknownError(message: String, cause: Throwable? = null) : AppError(message, cause)
}
