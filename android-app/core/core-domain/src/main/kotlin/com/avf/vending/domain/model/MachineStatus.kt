package com.avf.vending.domain.model

data class MachineStatus(
    val temperature: Float,
    val doorOpen: Boolean,
    val errors: List<String> = emptyList(),
    val isOnline: Boolean = true,
)
