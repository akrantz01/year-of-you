package you.yearof.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.resources.Res
import you.yearof.app.resources.arrow_left
import you.yearof.app.screens.MainGraph

private const val SlideDuration = 300

@Composable
fun TopBar(
    navController: NavHostController,
    scrollBehaviour: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier,
) {
    NavGraphGuard(graph = MainGraph::class, navController = navController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val isBackButtonVisible =
            navBackStackEntry != null &&
                navController.previousBackStackEntry?.destination?.let { it !is NavGraph } == true

        val transition = updateTransition(targetState = isBackButtonVisible, label = "Back button")
        val iconWidth by transition.animateDp(
            transitionSpec = { tween(durationMillis = SlideDuration) },
            label = "Back button width",
        ) { visible ->
            if (visible) 48.dp else 0.dp
        }

        TopAppBar(
            modifier = modifier,
            scrollBehavior = scrollBehaviour,
            title = { Text("Year of You") },
            navigationIcon = {
                Box(
                    modifier =
                        Modifier
                            .width(iconWidth)
                            .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedVisibility(
                        visible = isBackButtonVisible,
                        enter = slideInHorizontally(animationSpec = tween(durationMillis = SlideDuration)),
                        exit = slideOutHorizontally(animationSpec = tween(durationMillis = SlideDuration)),
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(Res.drawable.arrow_left),
                                contentDescription = "Back arrow",
                            )
                        }
                    }
                }
            },
        )
    }
}
