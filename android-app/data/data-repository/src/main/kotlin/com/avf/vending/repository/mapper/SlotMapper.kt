package com.avf.vending.repository.mapper

import com.avf.vending.domain.model.Slot
import com.avf.vending.domain.model.SlotAddress
import com.avf.vending.local.database.entity.SlotEntity

object SlotMapper {
    fun SlotEntity.toDomain() = Slot(
        id = id,
        address = SlotAddress(row = row.first(), col = col),
        productId = productId,
        stock = stock,
        capacity = capacity,
    )

    fun Slot.toEntity() = SlotEntity(
        id = id,
        slotId = id,
        row = address.row.toString(),
        col = address.col,
        productId = productId,
        stock = stock,
        capacity = capacity,
    )
}
