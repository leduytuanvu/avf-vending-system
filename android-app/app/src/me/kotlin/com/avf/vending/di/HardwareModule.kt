package com.avf.vending.di

import com.avf.vending.hardware.api.driver.BillValidatorDriver
import com.avf.vending.hardware.api.driver.VendingMachineDriver
import com.avf.vending.hardware.bill.ICTBillDriver
import com.avf.vending.hardware.me.MeDriver
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.impl.CashPaymentProcessor
import com.avf.vending.payment.impl.WalletPaymentProcessor
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
    abstract fun bindVendingDriver(impl: MeDriver): VendingMachineDriver

    @Binds
    @Singleton
    abstract fun bindBillDriver(impl: ICTBillDriver): BillValidatorDriver

    @Binds
    @IntoSet
    abstract fun bindCashProcessor(impl: CashPaymentProcessor): PaymentProcessor

    @Binds
    @IntoSet
    abstract fun bindWalletProcessor(impl: WalletPaymentProcessor): PaymentProcessor
}
