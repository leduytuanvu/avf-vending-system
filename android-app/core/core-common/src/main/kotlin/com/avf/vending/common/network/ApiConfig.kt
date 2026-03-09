package com.avf.vending.common.network

/**
 * Provides the API base URL for network requests.
 * Implemented by the app module using BuildConfig (per flavor).
 */
interface ApiConfig {
    val baseUrl: String
}
