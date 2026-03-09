package com.avf.vending.domain.service

import com.avf.vending.domain.model.AppError
import com.avf.vending.domain.model.Product
import com.avf.vending.domain.model.ProductSlot
import com.avf.vending.domain.model.Slot
import com.avf.vending.domain.repository.ProductRepository
import com.avf.vending.domain.repository.SlotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Domain service for product and slot catalogue queries.
 *
 * Replaces five anemic single-method use cases:
 *   GetProductsUseCase, GetProductBySlotUseCase, GetSlotsUseCase,
 *   ValidateSlotUseCase — all of which were pure repository delegates.
 *
 * The key addition over the individual wrappers is [observeAvailableProducts]:
 * the slot × product join with stock filtering was previously scattered across
 * StorefrontViewModel as an ad-hoc combine() call. Moving it here makes it
 * testable, reusable across screens, and correctly owned by the domain layer.
 */
class CatalogService @Inject constructor(
    private val productRepository: ProductRepository,
    private val slotRepository: SlotRepository,
) {
    /** Reactive stream of every active [Product]. */
    fun observeActiveProducts(): Flow<List<Product>> =
        productRepository.observeActiveProducts()

    /** Reactive stream of every [Slot]. */
    fun observeSlots(): Flow<List<Slot>> =
        slotRepository.observeSlots()

    /**
     * Reactive stream of all [ProductSlot]s that a customer can actually purchase:
     * the slot must have a product assigned AND stock must be greater than zero.
     *
     * This is the canonical "what is on sale right now" query.  Previously this
     * join lived in StorefrontViewModel — placing it here means any screen (e.g.
     * an idle attract carousel, a second display) can observe the same live feed
     * without duplicating the filtering logic.
     */
    fun observeAvailableProducts(): Flow<List<ProductSlot>> =
        combine(slotRepository.observeSlots(), productRepository.observeActiveProducts()) { slots, products ->
            val productMap = products.associateBy { it.id }
            slots
                .filter { slot -> slot.productId != null && slot.stock > 0 }
                .mapNotNull { slot ->
                    productMap[slot.productId]?.let { product -> ProductSlot(slot = slot, product = product) }
                }
        }

    /** Returns a [Slot] by its ID, or null if not found. */
    suspend fun getSlotById(slotId: String): Slot? =
        slotRepository.getBySlotId(slotId)

    /**
     * Returns the product assigned to [slotId], or null if the slot is empty.
     * Prefer [validateSlot] when the calling context requires the product to exist.
     */
    suspend fun getProductBySlot(slotId: String): Product? =
        productRepository.getProductBySlotId(slotId)

    /** Returns a product by its ID, or null if not found. */
    suspend fun getProductDetail(productId: String): Product? =
        productRepository.getProductById(productId)

    /**
     * Validates that [slotId] exists, has stock, and has a product assigned.
     *
     * Unlike the old [ValidateSlotUseCase] which only checked product assignment,
     * this also guards against purchasing from an out-of-stock slot — a check that
     * matters when the customer's tap and the last-item dispense race each other.
     *
     * @throws AppError.ValidationError with a descriptive message on any failure.
     * @return The assigned [Product] on success.
     */
    suspend fun validateSlot(slotId: String): Product {
        val slot = slotRepository.getBySlotId(slotId)
            ?: throw AppError.ValidationError("Slot not found: $slotId")
        if (slot.stock <= 0)
            throw AppError.ValidationError("Slot '$slotId' is out of stock")
        return productRepository.getProductBySlotId(slotId)
            ?: throw AppError.ValidationError("No product assigned to slot: $slotId")
    }
}
