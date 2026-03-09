package com.avf.vending.common.validation

interface Validator<T> {
    fun validate(value: T): ValidationResult
}
