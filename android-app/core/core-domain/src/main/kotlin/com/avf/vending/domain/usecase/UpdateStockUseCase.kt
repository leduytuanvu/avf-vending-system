package com.avf.vending.domain.usecase

import com.avf.vending.domain.repository.SyncRepository
import javax.inject.Inject

class UpdateStockUseCase @Inject constructor(
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(slotId: String, delta: Int) {
        // Persist stock change locally, then enqueue sync
        syncRepository.enqueue(entityType = "slot_stock", entityId = slotId, priority = 1)
    }
}
