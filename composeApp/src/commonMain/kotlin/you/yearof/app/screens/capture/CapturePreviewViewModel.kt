package you.yearof.app.screens.capture

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.api.UserService
import you.yearof.app.api.isAuthenticated
import you.yearof.app.database.Capture
import you.yearof.app.database.CaptureDao
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.CaptureNav
import you.yearof.app.screens.FeedNav
import you.yearof.app.util.Paths

data class CapturePreviewUiState(
    val share: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

@KoinViewModel
class CapturePreviewViewModel(
    @Provided private val captures: CaptureDao,
    @Provided private val paths: Paths,
    private val navigationCoordinator: NavigationCoordinator,
    userService: UserService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CapturePreviewUiState())
    val uiState: StateFlow<CapturePreviewUiState> = _uiState.asStateFlow()

    val authenticated = userService.state
        .map { it.isAuthenticated() }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = false)

    val captionState = TextFieldState()

    fun onShareChange(value: Boolean) {
        _uiState.update { it.copy(share = value) }
    }

    fun onSave(capture: CompletedCapture) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            try {
                doSave(capture)
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message ?: "Failed to save capture") }
            }
        }
    }

    private suspend fun doSave(capture: CompletedCapture) {
        val state = uiState.value

        val base = Path(paths.inDocuments("captures"), capture.timestamp.toEpochMilliseconds().toString())
        SystemFileSystem.createDirectories(base)

        val frontPath = Path(base, "front.jpeg")
        SystemFileSystem.atomicMove(source = Path(capture.frontPath), destination = frontPath)

        val backPath = Path(base, "back.jpeg")
        SystemFileSystem.atomicMove(source = Path(capture.backPath), destination = backPath)

        captures.insert(
            Capture(
                frontPath = frontPath.toString(),
                backPath = backPath.toString(),
                at = capture.timestamp,
                swapped = capture.swapped,
                caption = captionState.text.toString(),
                shared = state.share,
            ),
        )

        // TODO: start capture upload

        navigationCoordinator.navigateTo(FeedNav.Feed) {
            popUpTo(CaptureNav.Capture) { inclusive = false }
        }
    }

    fun onCancel() {
        viewModelScope.launch {
            navigationCoordinator.navigateUp()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
