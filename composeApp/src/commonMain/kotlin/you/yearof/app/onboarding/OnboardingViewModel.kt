package you.yearof.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.Initialization
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.PermissionRequirement
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.permissions.PermissionsCoordinator
import you.yearof.app.screens.Main

data class OnboardingUiState(
    val camera: PermissionStatus = PermissionStatus.Loading,
    val notifications: PermissionStatus = PermissionStatus.Loading,
)

@KoinViewModel
class OnboardingViewModel(
    private val navigationCoordinator: NavigationCoordinator,
) : ViewModel() {
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

    fun onContinueClicked() {
        viewModelScope.launch {
            when (val route = nextStep()) {
                null ->
                    navigationCoordinator.navigateTo(Main) {
                        popUpTo(Onboarding) { inclusive = true }
                        launchSingleTop = true
                    }
                else ->
                    navigationCoordinator.navigateTo(route) {
                        launchSingleTop = true
                    }
            }
        }
    }

    fun proceedFromInitialization() {
        viewModelScope.launch {
            val next = nextStep()

            if (next == null) {
                navigationCoordinator.navigateTo(Main) {
                    popUpTo(Initialization) { inclusive = true }
                }
            } else {
                navigationCoordinator.navigateTo(Onboarding) {
                    popUpTo(Initialization) { inclusive = true }
                }
                navigationCoordinator.navigateTo(next)
            }
        }
    }
}
