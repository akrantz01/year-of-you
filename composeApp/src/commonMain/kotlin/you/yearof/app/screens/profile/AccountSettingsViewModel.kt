package you.yearof.app.screens.profile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.api.ApiException
import you.yearof.app.api.AuthenticationState
import you.yearof.app.api.BadRequestException
import you.yearof.app.api.ConflictException
import you.yearof.app.api.UserService
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.notifications.SnackbarManager
import you.yearof.app.util.Log

data class AccountSettingsUiState(
    val displayNameSaving: Boolean = false,
    val usernameSaving: Boolean = false,
)

@KoinViewModel
class AccountSettingsViewModel(
    private val navigationCoordinator: NavigationCoordinator,
    private val userService: UserService,
    private val snackbarManager: SnackbarManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState = _uiState.asStateFlow()

    val displayNameState = TextFieldState()
    val usernameState = TextFieldState()

    init {
        viewModelScope.launch {
            userService.state.collect { current ->
                if (current is AuthenticationState.Authenticated) {
                    displayNameState.setTextAndPlaceCursorAtEnd(current.name)
                    usernameState.setTextAndPlaceCursorAtEnd(current.username)
                }
            }
        }
    }

    fun onDisplayNameSave() = viewModelScope.launch {
        _uiState.update { it.copy(displayNameSaving = true) }

        try {
            userService.update(displayName = displayNameState.text.toString())
            snackbarManager.success("Your display name has been saved!")
        } catch (e: BadRequestException) {
            snackbarManager.error("Invalid display name: ${e.message}")
        } catch (e: ApiException) {
            Log.error("AccountSettingsViewModel", "api exception: $e")
            snackbarManager.error("Unexpected server error, please try again later")
        } finally {
            _uiState.update { it.copy(displayNameSaving = false) }
        }
    }

    fun onUsernameSave() = viewModelScope.launch {
        _uiState.update { it.copy(usernameSaving = true) }

        try {
            userService.update(username = usernameState.text.toString())
            snackbarManager.success("Your username has been saved!")
        } catch (_: ConflictException) {
            snackbarManager.error("Username already exists")
        } catch (e: BadRequestException) {
            snackbarManager.error("Invalid username: ${e.message}")
        } catch (e: ApiException) {
            Log.error("AccountSettingsViewModel", "api exception: $e")
            snackbarManager.error("Unexpected server error, please try again later")
        } finally {
            _uiState.update { it.copy(usernameSaving = false) }
        }
    }

    fun back() = viewModelScope.launch { navigationCoordinator.navigateUp() }
}
