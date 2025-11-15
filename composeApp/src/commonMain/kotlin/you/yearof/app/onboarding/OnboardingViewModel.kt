package you.yearof.app.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import you.yearof.app.OnboardingRoute
import you.yearof.app.permissions.PermissionStatus

data class OnboardingUiState(
    val camera: PermissionStatus = PermissionStatus.Loading,
)

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun setCameraPermission(status: PermissionStatus) {
        _uiState.update { current ->
            current.copy(camera = status)
        }
    }

    fun ready(): Boolean {
        val state = _uiState.value
        return state.camera != PermissionStatus.Loading
    }

    fun nextStep(): OnboardingRoute? {
        val state = _uiState.value
        return when {
            state.camera != PermissionStatus.Granted -> OnboardingRoute.Camera
            else -> null
        }
    }
}
