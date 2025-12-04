package you.yearof.app.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import you.yearof.app.util.Log
import you.yearof.app.util.Preferences

sealed interface AuthenticationState {
    data object Loading : AuthenticationState
    data object Unauthenticated : AuthenticationState
    data class Authenticated(val id: UInt, val name: String, val username: String): AuthenticationState
}

fun AuthenticationState.isAuthenticated() = when (this) {
    is AuthenticationState.Authenticated -> true
    else -> false
}

@Single
class UserService(
    private val api: ApiService,
    private val preferences: Preferences,
) {
    private val _state = MutableStateFlow<AuthenticationState>(AuthenticationState.Loading)
    val state = _state.asStateFlow()

    suspend fun load() {
        val cached = preferences.user.firstOrNull()
        if (cached != null) {
            Log.info("UserService", "found cached user: id=${cached.id}")
            _state.update { AuthenticationState.Authenticated(cached.id, cached.name, cached.username) }
        }

        Log.info("UserService", "attempting to fetch user from API")
        refresh()
    }

    suspend fun register(displayName: String, username: String, password: String) {
        api.register(displayName, username, password)
        // TODO: this won't work correctly till registration is sorted out
    }

    suspend fun login(username: String, password: String) {
        api.login(username, password)
        refresh()
    }

    private suspend fun refresh() {
        val current = api.currentUser()
        if (current != null) {
            Log.info("UserService", "got user: id=${current.id}")
            _state.update { AuthenticationState.Authenticated(current.id, current.displayName, current.username) }
            preferences.setUser(current.id, current.username, current.displayName)
        } else {
            Log.info("UserService", "no user found")
            _state.update { AuthenticationState.Unauthenticated }
            preferences.clearUser()
        }
    }

    suspend fun logout() {
        api.logout()
        preferences.clearUser()
        _state.update { AuthenticationState.Unauthenticated }
    }
}
