package you.yearof.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.Initialization
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.PermissionRequirement
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.permissions.PermissionsCoordinator
import you.yearof.app.screens.MainGraph
import you.yearof.app.screens.account.AccountRouter
import you.yearof.app.util.Preferences

data class OnboardingUiState(
    val camera: PermissionStatus = PermissionStatus.Loading,
    val notifications: PermissionStatus = PermissionStatus.Loading,
    val accountOnboardingSeen: Boolean = false,
)

@KoinViewModel
class OnboardingViewModel(
    private val navigationCoordinator: NavigationCoordinator,
    private val preferences: Preferences,
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

    private val accountOnboardingSeen: StateFlow<Boolean> =
        preferences.accountOnboardingSeen.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    val uiState: StateFlow<OnboardingUiState> =
        combine(coordinator.snapshot, accountOnboardingSeen) { snapshot, seen ->
                OnboardingUiState(
                    camera = snapshot.statuses[Permission.Camera] ?: PermissionStatus.Loading,
                    notifications = snapshot.statuses[Permission.Notification] ?: PermissionStatus.Loading,
                    accountOnboardingSeen = seen,
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
            else ->
                when (accountOnboardingSeen.value) {
                    false -> OnboardingRoute.AccountPrompt
                    else -> null
                }
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
                    navigationCoordinator.navigateTo(MainGraph) {
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
                navigationCoordinator.navigateTo(MainGraph) {
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

    fun loginAccountRouter(): AccountRouter = object : AccountRouter {
        override suspend fun onSuccess() {
            completeAccountOnboarding()
        }

        override suspend fun toOpposite() {
            navigationCoordinator.navigateTo(OnboardingRoute.AccountRegistration) {
                launchSingleTop = true
            }
        }

        override suspend fun onCancel() = completeAccountOnboarding()
    }

    fun registrationAccountRouter(): AccountRouter = object : AccountRouter {
        override suspend fun onSuccess() {
            navigationCoordinator.navigateTo(OnboardingRoute.AccountConfirmation) {
                launchSingleTop = true
            }
        }

        override suspend fun toOpposite() {
            navigationCoordinator.navigateTo(OnboardingRoute.AccountLogin) {
                launchSingleTop = true
            }
        }

        override suspend fun onCancel() = completeAccountOnboarding()
    }

    fun onAccountPromptSkip() {
        viewModelScope.launch {
            completeAccountOnboarding()
        }
    }

    fun onAccountPromptStart() {
        viewModelScope.launch {
            navigationCoordinator.navigateTo(OnboardingRoute.AccountRegistration) {
                launchSingleTop = true
            }
        }
    }

    private suspend fun completeAccountOnboarding() {
        markAccountOnboardingSeen()
        navigationCoordinator.navigateTo(MainGraph) {
            popUpTo(Onboarding) { inclusive = true }
            launchSingleTop = true
        }
    }

    private suspend fun markAccountOnboardingSeen() {
        preferences.setAccountOnboardingSeen(true)
    }
}
