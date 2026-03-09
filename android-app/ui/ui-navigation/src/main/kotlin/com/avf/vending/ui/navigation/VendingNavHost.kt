package com.avf.vending.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.avf.vending.feature.admin.AdminScreen
import com.avf.vending.feature.dispensing.DispensingScreen
import com.avf.vending.feature.idle.IdleScreen
import com.avf.vending.feature.payment.PaymentScreen
import com.avf.vending.feature.storefront.StorefrontScreen

@Composable
fun VendingNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Idle.route) {
        composable(Screen.Idle.route) {
            IdleScreen(
                onWakeUp = { navController.navigate(Screen.Storefront.route) },
                onAdminAccess = { navController.navigate(Screen.Admin.route) },
            )
        }

        composable(Screen.Storefront.route) {
            StorefrontScreen(
                onProductSelected = { slotId, productId, amount ->
                    navController.navigate(Screen.Payment(slotId, productId, amount).route)
                },
                onAdminAccess = { navController.navigate(Screen.Admin.route) },
            )
        }

        composable(
            route = Screen.Payment.ROUTE,
            arguments = listOf(
                navArgument("slotId") { type = NavType.StringType },
                navArgument("productId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val slotId = backStackEntry.arguments?.getString("slotId").orEmpty()
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            val amount = backStackEntry.arguments?.getLong("amount") ?: 0L
            PaymentScreen(
                slotId = slotId,
                productId = productId,
                amount = amount,
                onPaymentComplete = { transactionId, slotAddress ->
                    navController.navigate(Screen.Dispensing(transactionId, slotAddress).route) {
                        popUpTo(Screen.Storefront.route)
                    }
                },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Dispensing.ROUTE,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType },
                navArgument("slotAddress") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId").orEmpty()
            val slotAddress = backStackEntry.arguments?.getString("slotAddress").orEmpty()
            DispensingScreen(
                transactionId = transactionId,
                slotAddress = slotAddress,
                onDone = {
                    navController.navigate(Screen.Idle.route) {
                        popUpTo(Screen.Idle.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Admin.route) {
            AdminScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
