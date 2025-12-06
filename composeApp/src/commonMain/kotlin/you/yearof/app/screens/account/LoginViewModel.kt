package you.yearof.app.screens.account

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import you.yearof.app.api.ApiException
import you.yearof.app.api.UnauthorizedException
import you.yearof.app.api.UserService
import you.yearof.app.notifications.SnackbarManager
import you.yearof.app.screens.account.AccountRouter
import you.yearof.app.ui.ServerSelectorController
import you.yearof.app.util.Log
import you.yearof.app.util.Preferences

data class LoginUiState(
    val loading: Boolean = false,
)

@KoinViewModel
class LoginViewModel(
    private val userService: UserService,
    @InjectedParam private val router: AccountRouter,
    preferences: Preferences,
    private val snackbarManager: SnackbarManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    val serverSelector = ServerSelectorController(viewModelScope, preferences, snackbarManager)

    val usernameState = TextFieldState(initialText = "")
    val passwordState = TextFieldState(initialText = "")

    val canLogin: Boolean
        get() = usernameState.text.isNotBlank() && passwordState.text.isNotEmpty()

    fun onLogin() =
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            try {
                userService.login(usernameState.text.toString(), passwordState.text.toString())
                router.onSuccess()
            } catch (_: UnauthorizedException) {
                snackbarManager.error("Invalid username or password")
            } catch (e: ApiException) {
                Log.error("LoginViewModel", "api exception: $e")
                snackbarManager.error("Unexpected server error, please try again later")
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }

    fun toRegister() = viewModelScope.launch { router.toOpposite() }

    fun onCancel() = viewModelScope.launch { router.onCancel() }
}
