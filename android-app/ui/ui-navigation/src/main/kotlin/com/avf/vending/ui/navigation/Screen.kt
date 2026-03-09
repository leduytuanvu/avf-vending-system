package com.avf.vending.ui.navigation

sealed class Screen(val route: String) {
    object Idle : Screen("idle")
    object Storefront : Screen("storefront")
    object Admin : Screen("admin")

    data class Payment(
        val slotId: String,
        val productId: String,
        val amount: Long,
    ) : Screen("payment/$slotId/$productId/$amount") {
        companion object {
            const val ROUTE = "payment/{slotId}/{productId}/{amount}"
        }
    }

    data class Dispensing(
        val transactionId: String,
        val slotAddress: String,
    ) : Screen("dispensing/$transactionId/$slotAddress") {
        companion object {
            const val ROUTE = "dispensing/{transactionId}/{slotAddress}"
        }
    }
}
