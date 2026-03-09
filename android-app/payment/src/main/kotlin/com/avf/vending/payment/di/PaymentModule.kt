package com.avf.vending.payment.di

import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.impl.CashPaymentProcessor
import com.avf.vending.payment.impl.WalletPaymentProcessor
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// PaymentProcessor bindings are provided per-flavor via app/src/{flavor}/di/HardwareModule.kt
// This module is a placeholder for any payment-wide bindings.
@Module
@InstallIn(SingletonComponent::class)
object PaymentModule
