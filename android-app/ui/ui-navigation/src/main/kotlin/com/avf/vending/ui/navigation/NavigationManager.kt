package com.avf.vending.ui.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class NavCommand {
    data class Navigate(val screen: Screen) : NavCommand()
    object NavigateUp : NavCommand()
    data class PopTo(val route: String, val inclusive: Boolean = false) : NavCommand()
}

interface NavigationManager {
    val commands: Flow<NavCommand>
    fun navigate(screen: Screen)
    fun navigateUp()
    fun popTo(route: String, inclusive: Boolean = false)
}

@Singleton
class NavigationManagerImpl @Inject constructor() : NavigationManager {

    private val _commands = MutableSharedFlow<NavCommand>(extraBufferCapacity = 8)
    override val commands: Flow<NavCommand> = _commands.asSharedFlow()

    override fun navigate(screen: Screen) {
        _commands.tryEmit(NavCommand.Navigate(screen))
    }

    override fun navigateUp() {
        _commands.tryEmit(NavCommand.NavigateUp)
    }

    override fun popTo(route: String, inclusive: Boolean) {
        _commands.tryEmit(NavCommand.PopTo(route, inclusive))
    }
}
