package you.yearof.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.database.AppDatabase
import you.yearof.app.database.Capture
import you.yearof.app.database.CaptureDao
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.navigation.NavigationCoordinator

data class CapturePreviewUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
)

@KoinViewModel
class CapturePreviewViewModel(
    @Provided private val captures: CaptureDao,
    private val navigationCoordinator: NavigationCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CapturePreviewUiState())
    val uiState: StateFlow<CapturePreviewUiState> = _uiState.asStateFlow()

    fun onSave(capture: CompletedCapture) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                captures.insert(
                    Capture(
                        frontPath = capture.frontPath,
                        backPath = capture.backPath,
                        atMillis = capture.timestamp.toEpochMilliseconds(),
                    ),
                )

                navigationCoordinator.navigateTo(Route.Feed) {
                    popUpTo(Route.Capture) { inclusive = true }
                }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save capture",
                    )
            }
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
