package you.yearof.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.permissions.rememberPermissionState
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
    // TODO: add Modifier.safeContentPadding() somewhere
    MaterialTheme {
        val nav = rememberNavController()

        NavHost(navController = nav, startDestination = Initialization) {
            composable<Initialization> { InitializationDecider(nav = nav) }

            navigation<Onboarding>(startDestination = OnboardingRoute.Camera) {
                composable<OnboardingRoute.Camera> { TODO() }
            }

            navigation<Main>(startDestination = Route.Capture) {
                // TODO: switch to feed once implemented
                composable<Route.Feed> { TODO() }
                composable<Route.Capture> { CaptureScreen() }
                composable<Route.Profile> { TODO() }
            }
        }
    }
}

@Composable
fun InitializationDecider(nav: NavController) {
    val cameraPermission = rememberPermissionState(Permission.Camera)

    LaunchedEffect(cameraPermission.status) {
        val ready = cameraPermission.status != PermissionStatus.Loading
        if (!ready) return@LaunchedEffect

        val next =
            when {
                cameraPermission.status != PermissionStatus.Granted -> OnboardingRoute.Camera
                else -> null
            }

        if (next == null) {
            nav.navigate(Main) { popUpTo(Initialization) { inclusive = true } }
        } else {
            nav.navigate(Onboarding) { popUpTo(Initialization) { inclusive = true } }
            nav.navigate(next)
        }
    }

    // TODO: show loading/black screen
}
