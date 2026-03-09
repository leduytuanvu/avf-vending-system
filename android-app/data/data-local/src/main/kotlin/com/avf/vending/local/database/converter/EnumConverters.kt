package com.avf.vending.local.database.converter

import androidx.room.TypeConverter
import com.avf.vending.domain.model.DispenseStatus
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.model.SyncStatus
import com.avf.vending.domain.model.TransactionStatus

class EnumConverters {
    @TypeConverter fun paymentMethodToString(v: PaymentMethod): String = v.name
    @TypeConverter fun stringToPaymentMethod(v: String): PaymentMethod = PaymentMethod.valueOf(v)

    @TypeConverter fun paymentStatusToString(v: PaymentStatus): String = v.name
    @TypeConverter fun stringToPaymentStatus(v: String): PaymentStatus = PaymentStatus.valueOf(v)

    @TypeConverter fun transactionStatusToString(v: TransactionStatus): String = v.name
    @TypeConverter fun stringToTransactionStatus(v: String): TransactionStatus = TransactionStatus.valueOf(v)

    @TypeConverter fun syncStatusToInt(v: SyncStatus): Int = v.ordinal
    @TypeConverter fun intToSyncStatus(v: Int): SyncStatus = SyncStatus.entries[v]

    @TypeConverter fun dispenseStatusToString(v: DispenseStatus): String = v.name
    @TypeConverter fun stringToDispenseStatus(v: String): DispenseStatus = DispenseStatus.valueOf(v)
}
