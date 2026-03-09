package com.avf.vending.repository.impl

import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.repository.PaymentRepository
import com.avf.vending.local.database.dao.PaymentDao
import com.avf.vending.repository.mapper.PaymentMapper.toDomain
import com.avf.vending.repository.mapper.PaymentMapper.toEntity
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
) : PaymentRepository {

    override suspend fun insert(payment: Payment): String {
        paymentDao.insert(payment.toEntity())
        return payment.id
    }

    override suspend fun getById(id: String): Payment? = paymentDao.getById(id)?.toDomain()

    override suspend fun updateStatus(id: String, status: PaymentStatus) {
        paymentDao.updateStatus(id, status.name)
    }

    override suspend fun getByTransactionId(transactionId: String): Payment? =
        paymentDao.getByTransactionId(transactionId)?.toDomain()
}
