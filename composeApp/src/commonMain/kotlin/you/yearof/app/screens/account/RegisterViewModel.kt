package you.yearof.app.screens.account

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import you.yearof.app.api.UserService
import you.yearof.app.notifications.SnackbarManager
import you.yearof.app.screens.account.AccountRouter
import you.yearof.app.ui.ServerSelectorController
import you.yearof.app.util.Preferences

data class RegisterUiState(
    val loading: Boolean = false,
)

@KoinViewModel
class RegisterViewModel(
    private val userService: UserService,
    @InjectedParam private val router: AccountRouter,
    preferences: Preferences,
    snackbarManager: SnackbarManager,
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

    val serverSelector = ServerSelectorController(viewModelScope, preferences, snackbarManager)

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
        router.onSuccess()
    }

    fun toLogin() = viewModelScope.launch { router.toOpposite() }

    fun onCancel() = viewModelScope.launch { router.onCancel() }
}
