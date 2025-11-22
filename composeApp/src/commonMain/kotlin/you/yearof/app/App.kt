package you.yearof.app

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import you.yearof.app.navigation.BottomBar
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.navigation.NavigationEvent
import you.yearof.app.navigation.NavigationRoute
import you.yearof.app.navigation.sharedViewModel
import you.yearof.app.onboarding.CameraPermissionsScreen
import you.yearof.app.onboarding.InitializationScreen
import you.yearof.app.onboarding.NotificationPermissionsScreen
import you.yearof.app.onboarding.Onboarding
import you.yearof.app.onboarding.OnboardingRoute
import you.yearof.app.onboarding.OnboardingViewModel
import you.yearof.app.screens.CaptureGraph
import you.yearof.app.screens.CaptureNav
import you.yearof.app.screens.CapturePreviewScreen
import you.yearof.app.screens.CaptureScreen
import you.yearof.app.screens.FeedGraph
import you.yearof.app.screens.FeedNav
import you.yearof.app.screens.MainGraph
import you.yearof.app.screens.ProfileGraph
import you.yearof.app.screens.ProfileNav

@Serializable
data object Initialization : NavigationRoute

@Composable
fun App(modifier: Modifier = Modifier) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).crossfade(true).build()
    }

    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
        ) {
            AppNavigation()
        }
    }
}

@Composable
private fun AppNavigation(navigationCoordinator: NavigationCoordinator = koinInject()) {
    val navController = rememberNavController()

    // Centralized navigation handling
    LaunchedEffect(navigationCoordinator) {
        navigationCoordinator.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navController.navigate(event.route) {
                        event.navOptions?.invoke(this)
                    }
                }
                is NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Initialization,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable<Initialization> {
                InitializationScreen()
            }

            navigation<Onboarding>(startDestination = OnboardingRoute.Camera) {
                composable<OnboardingRoute.Camera> { backStackEntry ->
                    val viewModel: OnboardingViewModel = backStackEntry.sharedViewModel(navController)
                    CameraPermissionsScreen(viewModel = viewModel)
                }

                composable<OnboardingRoute.Notifications> { backStackEntry ->
                    val viewModel: OnboardingViewModel = backStackEntry.sharedViewModel(navController)
                    NotificationPermissionsScreen(viewModel = viewModel)
                }
            }

            navigation<MainGraph>(startDestination = CaptureGraph) {
                // TODO: switch to feed once implemented
                navigation<FeedGraph>(startDestination = FeedNav.Feed) {
                    composable<FeedNav.Feed> { }
                }

                navigation<CaptureGraph>(startDestination = CaptureNav.Capture) {
                    composable<CaptureNav.Capture> { CaptureScreen() }
                    composable<CaptureNav.CapturePreview> { backStackEntry ->
                        val preview = backStackEntry.toRoute<CaptureNav.CapturePreview>()
                        CapturePreviewScreen(capture = preview.toCompletedCapture())
                    }
                }

                navigation<ProfileGraph>(startDestination = ProfileNav.Profile) {
                    composable<ProfileNav.Profile> { TODO() }
                }
            }
        }
    }
}
