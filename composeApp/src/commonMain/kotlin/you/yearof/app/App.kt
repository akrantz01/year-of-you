package you.yearof.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import kotlinx.serialization.Serializable
import you.yearof.app.screens.CaptureScreen

@Serializable
data object Initialization

@Serializable
data object Onboarding

@Serializable
sealed interface OnboardingRoute {
    @Serializable
    data object Camera
}

@Serializable
data object Main

@Serializable
sealed interface Route {
    @Serializable
    data object Feed : Route
    @Serializable
    data object Capture : Route
    @Serializable
    data object Profile : Route
}

@Composable
fun App() {
    MaterialTheme {
        val nav = rememberNavController()

        NavHost(navController = nav, startDestination = Initialization) {
            composable<Initialization> {
                Testing()
            }

            navigation<Onboarding>(startDestination = OnboardingRoute.Camera) {
                composable<OnboardingRoute.Camera> { TODO() }
            }

            navigation<Main>(startDestination = Route.Capture) { // TODO: switch to feed once implemented
                composable<Route.Feed> { TODO() }
                composable<Route.Capture> { CaptureScreen() }
                composable<Route.Profile> { TODO() }
            }
        }
    }
}

@Composable
fun Testing() {
    val greeting = remember { Greeting().greet() }
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer).safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Compose: $greeting")
        EnsureCameraPermissions {
            CaptureScreen()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnsureCameraPermissions(content: @Composable () -> Unit) {
    val cameraPermission = rememberPermissionState(Permission.Camera)

    if (cameraPermission.status.isGranted) {
        content()
    } else {
        LaunchedEffect(Unit) {
            cameraPermission.launchPermissionRequest()
        }
    }
}
