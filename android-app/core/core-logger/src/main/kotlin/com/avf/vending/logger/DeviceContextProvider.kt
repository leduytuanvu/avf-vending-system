package com.avf.vending.logger

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects static device + app context once and caches it.
 * Used by ErrorReporter to enrich every error report with:
 *   - deviceModel, androidSdk, appVersion
 * machineId is left empty here; callers add it via the extras parameter
 * when they have access to config/hardware context.
 */
@Singleton
class DeviceContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cachedContext: Map<String, String> by lazy {
        buildMap {
            put("deviceManufacturer", Build.MANUFACTURER)
            put("deviceModel", Build.MODEL)
            put("androidSdk", Build.VERSION.SDK_INT.toString())
            put("androidRelease", Build.VERSION.RELEASE)
            put("appVersion", resolveAppVersion())
            put("appPackage", context.packageName)
        }
    }

    fun collect(): Map<String, String> = cachedContext

    private fun resolveAppVersion(): String = runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")
}
