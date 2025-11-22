package you.yearof.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.rememberPermissionState

@Composable
fun InitializationScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    // Initialize permission states at the app level before deciding where to navigate
    val cameraPermission = rememberPermissionState(Permission.Camera)
    val notificationPermission = rememberPermissionState(Permission.Notification)

    val state by viewModel.uiState.collectAsState()

    // Update ViewModel with current permission states
    LaunchedEffect(cameraPermission.status) {
        viewModel.updatePermission(Permission.Camera, cameraPermission.status)
    }

    LaunchedEffect(notificationPermission.status) {
        viewModel.updatePermission(Permission.Notification, notificationPermission.status)
    }

    // Wait for permissions to load, then proceed
    LaunchedEffect(state) {
        if (!viewModel.ready()) return@LaunchedEffect
        viewModel.proceedFromInitialization()
    }

    // TODO: Show loading/splash screen instead of blank
}
