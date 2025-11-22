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
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    suspend fun navigateTo(
        route: NavigationRoute,
        options: (NavOptionsBuilder.() -> Unit)? = null,
    ) {
        _navigationEvents.emit(NavigationEvent.NavigateTo(route, options))
    }

    suspend fun navigateUp() {
        _navigationEvents.emit(NavigationEvent.NavigateUp)
    }
}

sealed class NavigationEvent {
    data class NavigateTo(
        val route: NavigationRoute,
        val navOptions: (NavOptionsBuilder.() -> Unit)? = null,
    ) : NavigationEvent()

    data object NavigateUp : NavigationEvent()
}
