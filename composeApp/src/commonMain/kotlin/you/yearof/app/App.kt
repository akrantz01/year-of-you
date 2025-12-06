package you.yearof.app

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okio.FileSystem
import org.koin.compose.koinInject
import you.yearof.app.api.HttpService
import you.yearof.app.api.UserService
import you.yearof.app.navigation.BottomBar
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.navigation.NavigationRoute
import you.yearof.app.navigation.BindNavigationCoordinator
import you.yearof.app.navigation.TopBar
import you.yearof.app.navigation.sharedViewModel
import you.yearof.app.notifications.BindSnackbarHost
import you.yearof.app.notifications.NotificationSnackbarHost
import you.yearof.app.notifications.SnackbarManager
import you.yearof.app.onboarding.CameraPermissionsScreen
import you.yearof.app.onboarding.InitializationScreen
import you.yearof.app.onboarding.AccountLoginScreen
import you.yearof.app.onboarding.AccountPromptScreen
import you.yearof.app.onboarding.AccountRegistrationScreen
import you.yearof.app.onboarding.NotificationPermissionsScreen
import you.yearof.app.onboarding.Onboarding
import you.yearof.app.onboarding.OnboardingRoute
import you.yearof.app.onboarding.OnboardingViewModel
import you.yearof.app.screens.CaptureGraph
import you.yearof.app.screens.CaptureNav
import you.yearof.app.screens.capture.CapturePreviewScreen
import you.yearof.app.screens.capture.CaptureScreen
import you.yearof.app.screens.FeedGraph
import you.yearof.app.screens.FeedNav
import you.yearof.app.screens.feed.LocalFeedScreen
import you.yearof.app.screens.MainGraph
import you.yearof.app.screens.ProfileGraph
import you.yearof.app.screens.ProfileNav
import you.yearof.app.screens.feed.SharedFeedScreen
import you.yearof.app.screens.profile.ProfileLoginScreen
import you.yearof.app.screens.profile.ProfileRegistrationScreen
import you.yearof.app.screens.profile.ProfileScreen
import you.yearof.app.screens.profile.AccountSettingsScreen

@Serializable
data object Initialization : NavigationRoute

@Composable
fun App(modifier: Modifier = Modifier) {
    val httpService: HttpService = koinInject()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .logger(DebugLogger()) // TODO: disable in production builds
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "images")
                    .maxSizeBytes(128L * 1024 * 1024) // 128 MB
                    .build()
            }
            .components {
                add(KtorNetworkFetcherFactory(httpService.client))
            }
            .build()
    }

    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            AppNavigation()
        }
    }
}

@Composable
private fun AppNavigation(
    navigationCoordinator: NavigationCoordinator = koinInject(),
    userService: UserService = koinInject(),
    snackbarManager: SnackbarManager = koinInject(),
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    BindNavigationCoordinator(navController = navController, navigationCoordinator = navigationCoordinator)

    DisposableEffect(userService) {
        val job = scope.launch {
            userService.load()
        }
        onDispose {
            job.cancel()
        }
    }

    BindSnackbarHost(
        snackbarManager = snackbarManager,
        hostState = snackbarHostState,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            TopBar(
                navController = navController,
                scrollBehaviour = scrollBehaviour,
            )
        },
        bottomBar = {
            val borderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
            BottomBar(
                modifier =
                    Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset.Zero,
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
                navController = navController,
            )
        },
        snackbarHost = {
            NotificationSnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Initialization,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
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

                composable<OnboardingRoute.AccountPrompt> { backStackEntry ->
                    val viewModel: OnboardingViewModel = backStackEntry.sharedViewModel(navController)
                    AccountPromptScreen(viewModel = viewModel)
                }

                composable<OnboardingRoute.AccountRegistration> { backStackEntry ->
                    val viewModel: OnboardingViewModel = backStackEntry.sharedViewModel(navController)
                    AccountRegistrationScreen(viewModel = viewModel)
                }

                composable<OnboardingRoute.AccountConfirmation> { TODO() }

                composable<OnboardingRoute.AccountLogin> { backStackEntry ->
                    val viewModel: OnboardingViewModel = backStackEntry.sharedViewModel(navController)
                    AccountLoginScreen(viewModel = viewModel)
                }
            }

            navigation<MainGraph>(startDestination = FeedGraph) {
                navigation<FeedGraph>(startDestination = FeedNav.LocalFeed) {
                    composable<FeedNav.LocalFeed> { LocalFeedScreen() }
                    composable<FeedNav.SharedFeed> { SharedFeedScreen() }
                }

                navigation<CaptureGraph>(startDestination = CaptureNav.Capture) {
                    composable<CaptureNav.Capture> { CaptureScreen() }
                    composable<CaptureNav.CapturePreview> { backStackEntry ->
                        val preview = backStackEntry.toRoute<CaptureNav.CapturePreview>()
                        CapturePreviewScreen(capture = preview.toCompletedCapture())
                    }
                }

                navigation<ProfileGraph>(startDestination = ProfileNav.Profile) {
                    composable<ProfileNav.Profile> { ProfileScreen() }
                    composable<ProfileNav.AccountLogin> { ProfileLoginScreen() }
                    composable<ProfileNav.AccountRegister> { ProfileRegistrationScreen() }
                    composable<ProfileNav.AccountSettings> { AccountSettingsScreen() }
                }
            }
        }
    }
}
