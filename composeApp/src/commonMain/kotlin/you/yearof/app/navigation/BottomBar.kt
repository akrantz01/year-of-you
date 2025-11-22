package you.yearof.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.camera_rotate
import app.composeapp.generated.resources.circle
import app.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.screens.CaptureGraph
import you.yearof.app.screens.CaptureNav
import you.yearof.app.screens.FeedGraph
import you.yearof.app.screens.FeedNav
import you.yearof.app.screens.MainGraph
import you.yearof.app.screens.ProfileGraph
import you.yearof.app.screens.ProfileNav

sealed class BottomTab(
    val route: NavigationRoute,
    val label: String,
    val icon: DrawableResource,
) {
    data object Feed : BottomTab(
        route = FeedNav.Feed,
        label = "Feed",
        icon = Res.drawable.circle,
    )

    data object Capture : BottomTab(
        route = CaptureNav.Capture,
        label = "Capture",
        icon = Res.drawable.camera_rotate,
    )

    data object Profile : BottomTab(
        route = ProfileNav.Profile,
        label = "Profile",
        icon = Res.drawable.compose_multiplatform,
    )
}

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { listOf(BottomTab.Feed, BottomTab.Capture, BottomTab.Profile) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val show = destination?.hierarchy?.any { it.hasRoute<MainGraph>() } ?: false
    val selected =
        destination?.hierarchy?.firstNotNullOfOrNull { destination ->
            when {
                destination.hasRoute<FeedGraph>() -> BottomTab.Feed
                destination.hasRoute<CaptureGraph>() -> BottomTab.Capture
                destination.hasRoute<ProfileGraph>() -> BottomTab.Profile
                else -> null
            }
        }

    if (show) {
        NavigationBar(modifier = modifier) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = selected == tab,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo<MainGraph> { saveState = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(tab.icon),
                            contentDescription = tab.label,
                        )
                    },
                    label = { Text(tab.label) },
                )
            }
        }
    }
}
