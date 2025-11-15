package you.yearof.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import you.yearof.app.onboarding.CameraPermissions
import you.yearof.app.onboarding.OnboardingViewModel
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.rememberPermissionState
import you.yearof.app.screens.CaptureScreen

@Serializable
data object Initialization

@Serializable
data object Onboarding

@Serializable
sealed interface OnboardingRoute {
    @Serializable
    data object Camera : OnboardingRoute
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
fun App(onboardingViewModel: OnboardingViewModel = viewModel { OnboardingViewModel() }) {
    val cameraPermission = rememberPermissionState(Permission.Camera)
    val nav = rememberNavController()

    LaunchedEffect(cameraPermission.status) {
        onboardingViewModel.setCameraPermission(cameraPermission.status)
    }

    // TODO: add Modifier.safeContentPadding() somewhere

    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        NavHost(navController = nav, startDestination = Initialization) {
            composable<Initialization> { InitializationDecider(nav = nav, viewModel = onboardingViewModel) }

            navigation<Onboarding>(startDestination = OnboardingRoute.Camera) {
                composable<OnboardingRoute.Camera> {
                    val onboardingState by onboardingViewModel.uiState.collectAsState()
                    CameraPermissions(
                        status = onboardingState.camera,
                        onRequest = cameraPermission::request,
                        onContinue = { nav.toNextRoute(onboardingViewModel) },
                    )
                }
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
fun InitializationDecider(
    nav: NavController,
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (!viewModel.ready()) return@LaunchedEffect

        val next = viewModel.nextStep()

        if (next == null) {
            nav.navigate(Main) { popUpTo(Initialization) { inclusive = true } }
        } else {
            nav.navigate(Onboarding) { popUpTo(Initialization) { inclusive = true } }
            nav.navigate(next)
        }
    }

    // TODO: show loading/black screen
}

private fun NavController.toNextRoute(model: OnboardingViewModel) {
    when (val next = model.nextStep()) {
        null ->
            navigate(Main) {
                popUpTo(Onboarding) { inclusive = true }
                launchSingleTop = true
            }
        else ->
            navigate(next) {
                val currentId = currentDestination?.id
                if (currentId != null) popUpTo(currentId) { inclusive = true }
                launchSingleTop = true
            }
    }
}
