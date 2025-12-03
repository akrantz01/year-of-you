package you.yearof.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.reflect.KClass

@Composable
fun <T : NavigationRoute> NavGraphGuard(graph: KClass<T>, navController: NavHostController, content: @Composable () -> Unit) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val show = backStackEntry?.destination?.hierarchy?.any { it.hasRoute(graph) } ?: false

    if (show) content()
}
