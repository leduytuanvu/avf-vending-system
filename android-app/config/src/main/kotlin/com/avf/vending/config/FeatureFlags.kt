package com.avf.vending.config

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlags @Inject constructor(
    private val remoteConfig: RemoteConfig,
) {
    suspend fun isWalletPaymentEnabled(): Boolean =
        remoteConfig.getBoolean("wallet_payment_enabled", default = false)

    suspend fun isAdminPanelEnabled(): Boolean =
        remoteConfig.getBoolean("admin_panel_enabled", default = true)

    suspend fun isTelemetryEnabled(): Boolean =
        remoteConfig.getBoolean("telemetry_enabled", default = false)
}
