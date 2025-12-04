package you.yearof.app.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.resources.Res
import you.yearof.app.resources.camera
import you.yearof.app.resources.camera_solid
import you.yearof.app.resources.house
import you.yearof.app.resources.house_solid
import you.yearof.app.resources.user
import you.yearof.app.resources.user_solid
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
    val selectedIcon: DrawableResource,
) {
    data object Feed : BottomTab(
        route = FeedNav.LocalFeed,
        label = "Feed",
        icon = Res.drawable.house,
        selectedIcon = Res.drawable.house_solid,
    )

    data object Capture : BottomTab(
        route = CaptureNav.Capture,
        label = "Capture",
        icon = Res.drawable.camera,
        selectedIcon = Res.drawable.camera_solid,
    )

    data object Profile : BottomTab(
        route = ProfileNav.Profile,
        label = "Profile",
        icon = Res.drawable.user,
        selectedIcon = Res.drawable.user_solid,
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

    val selected =
        destination?.hierarchy?.firstNotNullOfOrNull { destination ->
            when {
                destination.hasRoute<FeedGraph>() -> BottomTab.Feed
                destination.hasRoute<CaptureGraph>() -> BottomTab.Capture
                destination.hasRoute<ProfileGraph>() -> BottomTab.Profile
                else -> null
            }
        }

    NavGraphGuard(graph = MainGraph::class, navController = navController) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            tabs.forEach { tab ->
                val isSelected = selected == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(tab.route) {
                                popUpTo<MainGraph> { saveState = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(if (isSelected) tab.selectedIcon else tab.icon),
                            contentDescription = tab.label,
                        )
                    },
                    label = { Text(tab.label) },
                )
            }
        }
    }
}
