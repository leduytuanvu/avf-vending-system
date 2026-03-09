package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.*
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class DispenseProductUseCaseTest {

    private val transactionRepository: TransactionRepository = mock()
    private val productRepository: ProductRepository = mock()

    private val useCase = DispenseProductUseCase(
        transactionRepository = transactionRepository,
        productRepository = productRepository,
    )

    private val product = Product(
        id = "prod_1",
        name = "Cola",
        price = 15_000L,
        imageUrl = "",
        categoryId = "cat_1",
    )

    private val transaction = Transaction(
        id = "",
        productId = "prod_1",
        slotId = "slot_1",
        amount = 15_000L,
        paymentMethod = PaymentMethod.CASH,
        status = TransactionStatus.PENDING,
        createdAt = 0L,
        syncStatus = SyncStatus.PENDING,
    )

    @Test
    fun `invoke persists transaction as PAYMENT_SUCCESS before advancing to DISPENSING`() = runTest {
        val expectedId = "txn_1"
        whenever(productRepository.getProductById("prod_1")).thenReturn(product)
        whenever(transactionRepository.insertPending(any())).thenReturn(expectedId)

        val result = useCase(transaction)

        assertEquals(expectedId, result)
        // Transaction is inserted with PAYMENT_SUCCESS status
        verify(transactionRepository).insertPending(argThat { status == TransactionStatus.PAYMENT_SUCCESS })
        // Then advanced to DISPENSING
        verify(transactionRepository).updateTransactionStatus(expectedId, TransactionStatus.DISPENSING)
        verify(transactionRepository).updateDispenseStatus(expectedId, DispenseStatus.DISPENSING)
    }

    @Test
    fun `invoke does NOT decrement stock (only RecordDispenseResultUseCase does that)`() = runTest {
        whenever(productRepository.getProductById("prod_1")).thenReturn(product)
        whenever(transactionRepository.insertPending(any())).thenReturn("txn_1")

        useCase(transaction)

        // Stock must NOT be touched here — only after hardware confirms the drop
        verify(transactionRepository, never()).updateStatus(any(), any())
    }

    @Test
    fun `invoke throws ValidationError when product not found`() = runTest {
        whenever(productRepository.getProductById("prod_1")).thenReturn(null)

        var thrown: AppError.ValidationError? = null
        try {
            useCase(transaction)
        } catch (e: AppError.ValidationError) {
            thrown = e
        }
        assertNotNull(thrown)
        verify(transactionRepository, never()).insertPending(any())
    }
}
