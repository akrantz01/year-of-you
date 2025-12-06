package you.yearof.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController

@Composable
fun BindNavigationCoordinator(
    navController: NavHostController,
    navigationCoordinator: NavigationCoordinator,
) {
    LaunchedEffect(navigationCoordinator) {
        navigationCoordinator.commands.collect { command ->
            when (command) {
                is NavCommand.Go -> {
                    navController.navigate(command.route) {
                        launchSingleTop = command.singleTop
                    }
                }

                is NavCommand.ReplaceRoot -> {
                    navController.navigate(command.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                is NavCommand.PopTo -> navController.popBackStack(command.route, command.inclusive)

                is NavCommand.Raw -> {
                    navController.navigate(command.route) {
                        command.navOptions?.invoke(this)
                    }
                }

                NavCommand.Up -> navController.navigateUp()
            }
        }
    }
}
