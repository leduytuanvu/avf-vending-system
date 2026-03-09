package com.avf.vending.feature.idle

import com.avf.vending.domain.model.MachineStatus

data class IdleState(
    val machineStatus: MachineStatus? = null,
    val adminTapCount: Int = 0,
)

sealed class IdleIntent {
    object Tap : IdleIntent()
    object AdminTap : IdleIntent()
}

sealed class IdleEffect {
    object NavigateToStorefront : IdleEffect()
    object NavigateToAdmin : IdleEffect()
}
