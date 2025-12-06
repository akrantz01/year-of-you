package you.yearof.app.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

/**
 * Centralized navigation coordinator.
 * All routes must implement NavigationRoute interface for type safety.
 */
@Single
class NavigationCoordinator {
    private val _commands = MutableSharedFlow<NavCommand>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val commands: SharedFlow<NavCommand> = _commands.asSharedFlow()

    // New, higher-level API (callers will migrate to these)
    fun go(route: NavigationRoute, singleTop: Boolean = true) {
        _commands.tryEmit(NavCommand.Go(route, singleTop))
    }

    fun replaceRoot(route: NavigationRoute) {
        _commands.tryEmit(NavCommand.ReplaceRoot(route))
    }

    fun popTo(route: NavigationRoute, inclusive: Boolean = false) {
        _commands.tryEmit(NavCommand.PopTo(route, inclusive))
    }

    fun navigateUp() {
        _commands.tryEmit(NavCommand.Up)
    }

    // Legacy entry points preserved during migration
    suspend fun navigateTo(
        route: NavigationRoute,
        options: (NavOptionsBuilder.() -> Unit)? = null,
    ) {
        _commands.emit(NavCommand.Raw(route, options))
    }
}

sealed interface NavCommand {
    data class Go(
        val route: NavigationRoute,
        val singleTop: Boolean = true,
    ) : NavCommand

    data class ReplaceRoot(
        val route: NavigationRoute,
    ) : NavCommand

    data class PopTo(
        val route: NavigationRoute,
        val inclusive: Boolean,
    ) : NavCommand

    data class Raw(
        val route: NavigationRoute,
        val navOptions: (NavOptionsBuilder.() -> Unit)? = null,
    ) : NavCommand

    data object Up : NavCommand
}
