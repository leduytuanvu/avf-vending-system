package com.avf.vending.common.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()

    val isValid get() = this is Valid
}
