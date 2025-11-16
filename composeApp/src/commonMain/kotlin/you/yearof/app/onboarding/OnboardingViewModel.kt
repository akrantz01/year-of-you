package you.yearof.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import you.yearof.app.OnboardingRoute
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.PermissionRequirement
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.permissions.PermissionsCoordinator

data class OnboardingUiState(
    val camera: PermissionStatus = PermissionStatus.Loading,
    val notifications: PermissionStatus = PermissionStatus.Loading,
)

class OnboardingViewModel : ViewModel() {
    private val coordinator =
        PermissionsCoordinator(
            scope = viewModelScope,
            requirements =
                listOf(
                    PermissionRequirement(Permission.Camera),
                    PermissionRequirement(Permission.Notification, optional = true),
                ),
        )

    val uiState: StateFlow<OnboardingUiState> =
        coordinator.snapshot
            .map { snapshot ->
                OnboardingUiState(
                    camera = snapshot.statuses[Permission.Camera] ?: PermissionStatus.Loading,
                    notifications = snapshot.statuses[Permission.Notification] ?: PermissionStatus.Loading,
                )
            }.stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingUiState())

    val cameraStatus: StateFlow<PermissionStatus> = checkNotNull(coordinator.statusOf(Permission.Camera))

    val notificationStatus: StateFlow<PermissionStatus> = checkNotNull(coordinator.statusOf(Permission.Notification))

    fun ready(): Boolean {
        val state = uiState.value
        return state.camera != PermissionStatus.Loading && state.notifications != PermissionStatus.Loading
    }

    fun nextStep(): OnboardingRoute? =
        when (coordinator.nextPermission()) {
            Permission.Camera -> OnboardingRoute.Camera
            Permission.Notification -> OnboardingRoute.Notifications
            else -> null
        }

    fun updatePermission(
        permission: Permission,
        status: PermissionStatus,
    ) {
        coordinator.update(permission, status)
    }
}
