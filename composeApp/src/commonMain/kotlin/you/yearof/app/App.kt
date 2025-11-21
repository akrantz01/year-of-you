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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import you.yearof.app.database.Capture
import you.yearof.app.database.rememberDatabase
import you.yearof.app.onboarding.CameraPermissions
import you.yearof.app.onboarding.NotificationPermissions
import you.yearof.app.onboarding.Onboarding
import you.yearof.app.onboarding.OnboardingRoute
import you.yearof.app.onboarding.OnboardingViewModel
import you.yearof.app.permissions.Permission
import you.yearof.app.permissions.rememberPermissionState
import you.yearof.app.screens.CapturePreview
import you.yearof.app.screens.CaptureScreen
import you.yearof.app.screens.Main
import you.yearof.app.screens.Route

@Serializable
data object Initialization

@Composable
fun App(onboardingViewModel: OnboardingViewModel = viewModel { OnboardingViewModel() }) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).crossfade(true).build()
    }

    val scope = rememberCoroutineScope()

    val cameraPermission = rememberPermissionState(Permission.Camera)
    val notificationPermission = rememberPermissionState(Permission.Notification)

    val db = rememberDatabase()
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
                            onCaptureComplete = { completed ->
                                nav.navigate(Route.CapturePreview.from(completed))
                            },
                        )
                    }
                    composable<Route.CapturePreview> { backStackEntry ->
                        val preview = backStackEntry.toRoute<Route.CapturePreview>()
                        CapturePreview(
                            capture = preview.toCompletedCapture(),
                            onSave = { capture ->
                                scope.launch {
                                    db.captures().insert(
                                        Capture(
                                            frontPath = capture.frontPath,
                                            backPath = capture.backPath,
                                            atMillis = capture.timestamp.toEpochMilliseconds(),
                                        ),
                                    )
                                    nav.navigate(Route.Feed) {
                                        popUpTo(Route.Capture) { inclusive = true }
                                    }
                                }
                            },
                            onCancel = { nav.popBackStack() },
                        )
                    }
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
