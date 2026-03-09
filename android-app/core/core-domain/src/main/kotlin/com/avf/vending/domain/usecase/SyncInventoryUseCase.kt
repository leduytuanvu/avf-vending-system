package com.avf.vending.domain.usecase

import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.SyncRepository
import javax.inject.Inject

class SyncInventoryUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(sinceTimestamp: Long) {
        val changed = productRepository.getChangedSince(sinceTimestamp)
        changed.forEach { product ->
            syncRepository.enqueue(entityType = "product", entityId = product.id, priority = 0)
        }
    }
}
