package you.yearof.app.screens.account

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.api.UserService
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.ProfileNav

data class RegisterUiState(
    val loading: Boolean = false,
)

@KoinViewModel
class RegisterViewModel(
    private val userService: UserService,
    private val navigationCoordinator: NavigationCoordinator
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    val displayNameState = TextFieldState(initialText = "")
    val usernameState = TextFieldState(initialText = "")
    val passwordState = TextFieldState(initialText = "")
    val passwordConfirmationState = TextFieldState(initialText = "")

    val canRegister: Boolean
        get() = displayNameState.text.isNotBlank() && usernameState.text.isNotBlank()
            && passwordState.text.isNotEmpty() && passwordConfirmationState.text.isNotEmpty()
            && passwordsMatch

    val passwordsMatch: Boolean
        get() = passwordState.text == passwordConfirmationState.text

    fun onRegister() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }

        try {
            doRegister()
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
    }

    private suspend fun doRegister() {
        userService.register(
            displayName = displayNameState.text.toString(),
            username = usernameState.text.toString(),
            password = passwordState.text.toString(),
        )
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }

    fun toLogin() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.AccountLogin)
    }

    fun onCancel() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }
}
