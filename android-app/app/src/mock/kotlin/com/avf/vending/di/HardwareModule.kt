package com.avf.vending.di

import com.avf.vending.hardware.api.driver.BillValidatorDriver
import com.avf.vending.hardware.api.driver.VendingMachineDriver
import com.avf.vending.hardware.mock.MockBillDriver
import com.avf.vending.hardware.mock.MockVendingDriver
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.impl.MockCashProcessor
import com.avf.vending.payment.impl.MockWalletProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindVendingDriver(impl: MockVendingDriver): VendingMachineDriver

    @Binds
    @Singleton
    abstract fun bindBillDriver(impl: MockBillDriver): BillValidatorDriver

    @Binds
    @IntoSet
    abstract fun bindCashProcessor(impl: MockCashProcessor): PaymentProcessor

    @Binds
    @IntoSet
    abstract fun bindWalletProcessor(impl: MockWalletProcessor): PaymentProcessor
}
