package you.yearof.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel

/**
 * Helper to get a shared ViewModel instance across a navigation graph.
 * The ViewModel will be scoped to the parent navigation graph, so all
 * screens within that graph will share the same instance.
 *
 * Example: Share OnboardingViewModel across all onboarding screens
 */
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavHostController): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel()
    val parentEntry =
        remember(navGraphRoute) {
            navController.getBackStackEntry(navGraphRoute)
        }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}
