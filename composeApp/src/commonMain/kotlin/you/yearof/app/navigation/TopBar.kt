package you.yearof.app.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.resources.Res
import you.yearof.app.resources.arrow_left
import you.yearof.app.screens.MainGraph

@Composable
fun TopBar(
    navController: NavHostController,
    scrollBehaviour: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier,
) {
    NavGraphGuard(graph = MainGraph::class, navController = navController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val isBackButtonVisible = navBackStackEntry != null && navController.previousBackStackEntry != null

        TopAppBar(
            modifier = modifier,
            scrollBehavior = scrollBehaviour,
            title = { Text("Year of You") },
            navigationIcon = {
                if (isBackButtonVisible) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = "Back arrow",
                        )
                    }
                }
            },
        )
    }
}
