package you.yearof.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.database.Capture
import you.yearof.app.database.CaptureDao
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.CaptureNav
import you.yearof.app.screens.FeedNav
import you.yearof.app.util.Paths

data class CapturePreviewUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
)

@KoinViewModel
class CapturePreviewViewModel(
    @Provided private val captures: CaptureDao,
    @Provided private val paths: Paths,
    private val navigationCoordinator: NavigationCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CapturePreviewUiState())
    val uiState: StateFlow<CapturePreviewUiState> = _uiState.asStateFlow()

    fun onSave(capture: CompletedCapture) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                doSave(capture)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save capture") }
            }
        }
    }

    private suspend fun doSave(capture: CompletedCapture) {
        val base = paths.inDocuments("captures").toPath().resolve(capture.timestamp.toEpochMilliseconds().toString())
        FileSystem.SYSTEM.createDirectories(base)

        val frontPath = base.resolve("front.jpeg")
        FileSystem.SYSTEM.atomicMove(source = capture.frontPath.toPath(), target = frontPath)

        val backPath = base.resolve("back.jpeg")
        FileSystem.SYSTEM.atomicMove(source = capture.backPath.toPath(), target = backPath)

        captures.insert(
            Capture(
                frontPath = frontPath.toString(),
                backPath = backPath.toString(),
                atMillis = capture.timestamp.toEpochMilliseconds(),
            ),
        )

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
