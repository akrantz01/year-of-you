package you.yearof.app.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.rememberPermissionState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun InitializationScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    var takingTooLong by remember { mutableStateOf(false) }

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
    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        takingTooLong = true
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = takingTooLong) {
            CircularProgressIndicator()
        }
    }
}
