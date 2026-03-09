package com.avf.vending.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {
    // NavController bindings are provided via Compose's rememberNavController
    // and passed down through the composable tree from VendingNavHost.
}
