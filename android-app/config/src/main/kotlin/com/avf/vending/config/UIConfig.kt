package com.avf.vending.config

data class UIConfig(
    val language: String = "vi",
    val idleTimeoutMs: Long = 30_000L,
    val paymentTimeoutMs: Long = 180_000L,
    val attractBannerIntervalMs: Long = 5_000L,
    val brightness: Int = 80,               // 0–100
    val showCategoryBar: Boolean = true,
    val showSearchBar: Boolean = false,
    val gridColumns: Int = 4,
    val primaryColor: String = "#0A84FF",
)
