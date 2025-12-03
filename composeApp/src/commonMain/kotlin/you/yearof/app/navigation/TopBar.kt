package you.yearof.app.navigation

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import you.yearof.app.screens.MainGraph

@Composable
fun TopBar(
    navController: NavHostController,
    scrollBehaviour: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier,
) {
    NavGraphGuard(graph = MainGraph::class, navController = navController) {
        TopAppBar(
            modifier = modifier,
            title = { Text("Year of You") },
            scrollBehavior = scrollBehaviour,
        )
    }
}
