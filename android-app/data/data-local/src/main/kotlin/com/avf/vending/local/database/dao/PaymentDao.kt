package com.avf.vending.local.database.dao

import androidx.room.*
import com.avf.vending.local.database.entity.PaymentEntity

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getByTransactionId(transactionId: String): PaymentEntity?

    @Query("UPDATE payments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String): Int
}