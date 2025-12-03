package you.yearof.app.screens.account

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.api.UserService
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.ProfileNav

data class LoginUiState(
    val loading: Boolean = false,
)

@KoinViewModel
class LoginViewModel(
    private val navigationCoordinator: NavigationCoordinator,
    private val userService: UserService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    val usernameState = TextFieldState(initialText = "")
    val passwordState = TextFieldState(initialText = "")

    val canLogin: Boolean
        get() = usernameState.text.isNotBlank() && passwordState.text.isNotEmpty()

    fun onLogin() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }

        try {
            userService.login(usernameState.text.toString(), passwordState.text.toString())
            navigationCoordinator.navigateTo(ProfileNav.Profile) {
                popUpTo(ProfileNav.Profile) { inclusive = true }
            }
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun toRegister() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.AccountRegister)
    }

    fun onCancel() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }
}
