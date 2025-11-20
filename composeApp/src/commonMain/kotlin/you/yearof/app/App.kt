package you.yearof.app

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import you.yearof.app.camera.CameraPosition
import you.yearof.app.onboarding.CameraPermissions
import you.yearof.app.onboarding.NotificationPermissions
import you.yearof.app.onboarding.Onboarding
import you.yearof.app.onboarding.OnboardingRoute
import you.yearof.app.onboarding.OnboardingViewModel
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.rememberPermissionState
import you.yearof.app.screens.CaptureScreen
import you.yearof.app.screens.Main
import you.yearof.app.screens.Route

@Serializable
data object Initialization

@Composable
fun App(onboardingViewModel: OnboardingViewModel = viewModel { OnboardingViewModel() }) {
    val cameraPermission = rememberPermissionState(Permission.Camera)
    val notificationPermission = rememberPermissionState(Permission.Notification)

    val nav = rememberNavController()

    LaunchedEffect(cameraPermission.status) {
        onboardingViewModel.updatePermission(Permission.Camera, cameraPermission.status)
    }

    LaunchedEffect(notificationPermission.status) {
        onboardingViewModel.updatePermission(Permission.Notification, notificationPermission.status)
    }

    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
        ) {
            NavHost(navController = nav, startDestination = Initialization) {
                composable<Initialization> { InitializationDecider(nav = nav, viewModel = onboardingViewModel) }

                navigation<Onboarding>(startDestination = OnboardingRoute.Camera) {
                    composable<OnboardingRoute.Camera> {
                        val cameraStatus by onboardingViewModel.cameraStatus.collectAsState()
                        CameraPermissions(
                            status = cameraStatus,
                            onRequest = cameraPermission::request,
                            onContinue = { nav.toNextOnboardingRoute(onboardingViewModel) },
                        )
                    }

                    composable<OnboardingRoute.Notifications> {
                        val notificationStatus by onboardingViewModel.notificationStatus.collectAsState()
                        NotificationPermissions(
                            status = notificationStatus,
                            onRequest = notificationPermission::request,
                            onContinue = { nav.toNextOnboardingRoute(onboardingViewModel) },
                        )
                    }
                }

                navigation<Main>(startDestination = Route.Capture) {
                    // TODO: switch to feed once implemented
                    composable<Route.Feed> { TODO() }
                    composable<Route.Capture> {
                        CaptureScreen(
                            onCaptureComplete = { images ->
                                nav.navigate(
                                    Route.CapturePreview(
                                        frontPath = images[CameraPosition.Front]!!,
                                        backPath = images[CameraPosition.Back]!!,
                                    ),
                                )
                            },
                        )
                    }
                    composable<Route.CapturePreview> { TODO() }
                    composable<Route.Profile> { TODO() }
                }
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

private fun NavController.toNextOnboardingRoute(model: OnboardingViewModel) {
    when (val route = model.nextStep()) {
        null ->
            navigate(Main) {
                popUpTo(Onboarding) { inclusive = true }
                launchSingleTop = true
            }
        else ->
            navigate(route) {
                val currentId = currentDestination?.id
                if (currentId != null) popUpTo(currentId) { inclusive = true }
                launchSingleTop = true
            }
    }
}
